package de.julielab.semedico.core.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchComponent.TermDocumentFrequencyChain;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.ITermDocumentFrequencyService;
import de.julielab.semedico.core.util.TripleStream;

public class TermDocumentFrequencyService implements ITermDocumentFrequencyService {

	/**
	 * This is (hopefully - check in constructor) a synchronized map. That means that single class (kind of 'atomic
	 * operations') of the map a thread safe (e.g. map.get()). HOWEVER when executing a series of operations on the map
	 * we have to put the series in a synchronized block anyway because in between the single operations the map could
	 * be changed by another thread.
	 */
	private Map<String, Long> frequencies;
	private Logger log;
	private ISearchComponent documentFrequencyChain;
	private CountDownLatch updateLatch;
	private CountDownLatch initializationLatch;

	public TermDocumentFrequencyService(Logger log, @TermDocumentFrequencyChain ISearchComponent documentFrequencyChain) {
		this.log = log;
		this.documentFrequencyChain = documentFrequencyChain;
		this.frequencies = Collections.synchronizedMap(new HashMap<String, Long>());
		this.updateLatch = new CountDownLatch(0);
		this.initializationLatch = new CountDownLatch(1);
	}

	@Override
	public void updateTermDocumentFrequencies() {
		// We synchronize the count check and the creation of the new latch so that nothing can
		// interfere in between the operations. The first thread observing a 0-count will create a new latch and
		// perform the update, all other threads will observe a 1-count and return.
		synchronized (this) {
			if (updateLatch.getCount() > 0)
				return;
			updateLatch = new CountDownLatch(1);
		}
		StopWatch w = new StopWatch();
		w.start();
		log.info("Updating term document frequencies.");

		SearchCarrier searchCarrier = new SearchCarrier("TermDocumentFrequencyChain");
		documentFrequencyChain.process(searchCarrier);
		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) ((SemedicoSearchCarrier)searchCarrier).result;
		TripleStream<String, Long, Long> termDocumentFrequencies = searchResult.termDocumentFrequencies;
		synchronized (frequencies) {
			while (termDocumentFrequencies.incrementTuple()) {
				String termId = termDocumentFrequencies.getLeft();
				// Originally document frequencies should have been to the right and in the middle the facet counts.
				// But we don't do this anymore. We continue using the triple stream due to the merging
				// capabilities.
				Long documentFrequency = termDocumentFrequencies.getMiddle();
				frequencies.put(termId, documentFrequency);
			}
		}

		w.stop();
		log.info("Done updating term document frequencies. Took {}s ( {}m).", (w.getTime() / 1000),
				(w.getTime() / 1000) / 60);
		updateLatch.countDown();
		synchronized (initializationLatch) {
			if (null != initializationLatch && initializationLatch.getCount() > 0)
				initializationLatch.countDown();
		}
	}

	@Override
	public long getDocumentFrequencyForTerm(String termId) {
		// If we don't know any counts, first get them.
		if (!isInitialized())
			updateTermDocumentFrequencies();
		// The above call could have caused the update of document frequencies or it ran in an already running process
		// and did return immediately. For the second case we will wait until the existing process has finished.
		awaitInitialization();
		Long frequency = frequencies.get(termId);
		if (null == frequency) {
			log.warn("Document frequency for term with ID {} is unknown.", termId);
			return 0;
		}
		return frequency;
	}

	@Override
	public void run() {
		updateTermDocumentFrequencies();
	}

	@Override
	public boolean isInitialized() {
		return !frequencies.isEmpty();
	}

	@Override
	public void awaitInitialization() {
		if (null != initializationLatch) {
			try {
				initializationLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
