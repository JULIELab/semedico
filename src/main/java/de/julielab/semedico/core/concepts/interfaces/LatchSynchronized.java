package de.julielab.semedico.core.concepts.interfaces;

import java.util.concurrent.CountDownLatch;

public interface LatchSynchronized {
	public void setSynchronizeLatch(CountDownLatch latch);
}
