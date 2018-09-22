package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.slf4j.Logger;

import com.google.common.cache.CacheLoader;

import de.julielab.semedico.core.concepts.interfaces.LatchSynchronized;

public abstract class AsyncCacheLoader<K, V> extends CacheLoader<K, V> {

	/**
	 * The maximum capacity of one batch. This is motivated by the usage of Neo4j by
	 * some caches. Neo4j uses Lucene as an index technology and Lucene has a
	 * maximum of 1024 boolean clauses by default. Since some cache loaders make
	 * long OR disjunctions to find their elements, this is a restricting factor.
	 */
	public static int BATCH_CAPACITY = 1024;
	/**
	 * The length of time (in milliseconds) that a new loadingWorker waits to
	 * accumulate values to load before beginning to load them.
	 */
	public static final long INIT_SLEEP_TIME = 5;

	private LinkedBlockingQueue<K> queue;
	// Must be a concurrent HashMap because getPendingProxies is called from
	// within the worker thread.
	private ConcurrentHashMap<K, V> pendingProxies;
	private ConcurrentHashMap<K, CountDownLatch> synchronizeLatches;
	private LoadingWorkerReference loadingWorkerReference;

	/**
	 * Returns an object that always holds a reference to the currently used
	 * loadingWorker thread that actually loads cache entries. This can be used to
	 * synchronize on the loading thread, if required.
	 * 
	 * @return An object to get the current loading thread from.
	 */
	public LoadingWorkerReference getLoadingWorkerReference() {
		return loadingWorkerReference;
	}

	protected Logger log;

	public AsyncCacheLoader(Logger log) {
		this.log = log;
		this.queue = new LinkedBlockingQueue<>();
		this.pendingProxies = new ConcurrentHashMap<>(BATCH_CAPACITY, 0.75f, 2);
		this.synchronizeLatches = new ConcurrentHashMap<>();
		this.loadingWorkerReference = new LoadingWorkerReference();
	}

	private void addToQueue(K key) {
		synchronized (queue) {
			queue.add(key);
		}
	}

	protected V getPendingProxy(K key) {
		return pendingProxies.remove(key);
	}

	abstract V getValueProxy(K key);

	@Override
	public V load(K key) {

		V proxy = pendingProxies.get(key);
		if (null == proxy) {
			proxy = getSynchronizedValueProxy(key);
			addToQueue(key);
			pendingProxies.put(key, proxy);
			if (null != loadingWorkerReference.loadingWorker && loadingWorkerReference.loadingWorker.isAlive()) {
				synchronized (queue) {
					queue.notifyAll();
				}
			} else {
				log.debug("Loading worker is not present and now created.");
				loadingWorkerReference.loadingWorker = new AsyncLoadingWorker();
				loadingWorkerReference.loadingWorker.start();
				log.debug("Loading worker has been started.");
			}
		}
		return proxy;
	}

	private V getSynchronizedValueProxy(K key) {
		// We synchronize our values via a CountDownLatch. Each value has its
		// own latch that is being count down - unlocking it - in the load
		// method.
		V proxy = getValueProxy(key);
		CountDownLatch latch = new CountDownLatch(1);
		try {
			((LatchSynchronized) proxy).setSynchronizeLatch(latch);
		} catch (ClassCastException e) {
			e.printStackTrace();
			log.error(
					"Error when synchronizing on proxy {}: Does not implement LatchSynchronized. Proxies need to implement that interface.",
					proxy);
		}
		synchronizeLatches.put(key, latch);
		return proxy;
	}

	abstract void loadAsyncBatch(ArrayList<K> batchList);

	/**
	 * A tiny wrapper class for the loadingWorker thread to allow other objects to
	 * always have access to the current thread for synchronization purposes. This
	 * is mainly used for JUnit tests which must explicitly wait for the thread
	 * because the JVM is terminated otherwise as soon as the test method has
	 * finished.
	 * 
	 * @author faessler
	 * 
	 */
	public class LoadingWorkerReference {
		AsyncLoadingWorker loadingWorker;

		private LoadingWorkerReference() {
		}

		/**
		 * Interrupts the worker - telling him to stop - and then wait until the
		 * worker's finished.
		 */
		public void interruptAndJoin() {
			if (null == loadingWorker)
				return;
			// return;
			try {
				loadingWorker.interrupt();
				loadingWorker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Wait for the loading worker to have finished the current queue. After this,
		 * the worker will wait until notified that new items have arrived in the queue.
		 */
		public void awaitCurrentBatchLoaded() {
			try {
				loadingWorker.getWaitIndicatorLatch().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Wait for the loading worker to die. This only happens when an un-handled
		 * exception in thrown in the worker's run method or the worker is interrupted.
		 */
		public void joinWorkerThread() {
			try {
				loadingWorker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	class AsyncLoadingWorker extends Thread {

		private CountDownLatch waitIndicatorLatch = new CountDownLatch(1);

		public CountDownLatch getWaitIndicatorLatch() {
			return waitIndicatorLatch;
		}

		@Override
		public void run() {
			try {
				// For the first run, measure with sleep time.
				long time = System.currentTimeMillis();
				Thread.sleep(INIT_SLEEP_TIME);
				while (!isInterrupted()) {
					try {
						ArrayList<K> batchList = new ArrayList<>();
						synchronized (queue) {
							queue.drainTo(batchList, BATCH_CAPACITY);
						}
						log.debug("Loading {} values as a batch. {} elements left in queue.", batchList.size(),
								queue.size());

						try {
							loadAsyncBatch(batchList);
						} finally {
							// Unlock the latches on the values that are
							// now completely loaded. Even if there was an
							// error when loading: We rather want an error than
							// the application just hanging forever.
							log.debug("Unlocking synchronization latches.");
							for (K key : batchList) {
								CountDownLatch latch = synchronizeLatches.remove(key);
								latch.countDown();
							}
						}

						log.debug("Loaded {} terms in {} ms", batchList.size(), System.currentTimeMillis() - time);
						// From now on, we just continue until the
						// queue is empty, no waiting times.
						time = System.currentTimeMillis();
						synchronized (queue) {
							if (queue.isEmpty()) {
								waitIndicatorLatch.countDown();
								try {
									log.debug("Queue is empty, waiting for new requests.");
									queue.wait();
									waitIndicatorLatch = new CountDownLatch(1);
									log.debug("Loading worker notified, waiting for {} ms for further loading items.",
											INIT_SLEEP_TIME);
									Thread.sleep(INIT_SLEEP_TIME);
								} catch (InterruptedException e) {
									interrupt();
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					} finally {
						waitIndicatorLatch.countDown();
					}
				}
			} catch (InterruptedException e) {
				log.error("Interrupted while initial sleep, nothing has been loaded.");
				waitIndicatorLatch.countDown();
			}
		}

	}

	@PostInjection
	public void startupService(RegistryShutdownHub shutdownHub) {
		shutdownHub.addRegistryShutdownListener(new Runnable() {
			public void run() {
				log.debug("Shutting down async cache loader.");
				loadingWorkerReference.interruptAndJoin();
			}
		});
	}
}
