package de.julielab.semedico.core.util;

import java.util.concurrent.CountDownLatch;

public class LatchSynchronizer {

	private CountDownLatch synchronizeLatch;

	public void setSynchronizeLatch(CountDownLatch latch) {
		this.synchronizeLatch = latch;
	}

	public void synchronize() {
		// This check saves us the synchronized block as soon as the
		// synchronized object is ready for use. Synchronization always comes
		// with a performance penalty.
		if (null == synchronizeLatch)
			return;
		synchronized (this) {
			if (null != synchronizeLatch) {
				try {
					synchronizeLatch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronizeLatch = null;
			}
		}
	}
}
