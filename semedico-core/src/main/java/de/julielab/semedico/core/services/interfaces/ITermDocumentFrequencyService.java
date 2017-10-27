package de.julielab.semedico.core.services.interfaces;

public interface ITermDocumentFrequencyService extends Runnable {
	/**
	 * Causes the service to re-run the document frequency counting and update its internal cache. Does return
	 * immediately if the update process is already running.
	 */
	void updateTermDocumentFrequencies();

	/**
	 * Returns the document frequency of the term with ID <tt>termId</tt>. There is no difference made between fields.
	 * If a term should occur in differing numbers in different fields, it is undetermined which value is returned.
	 * <p>
	 * If the service is not yet initialized, this call will cause the initialization and wait until it has finished.
	 * </p>
	 * 
	 * @param termId
	 * @return
	 */
	long getDocumentFrequencyForTerm(String termId);

	/**
	 * 
	 * @return Whether document frequencies have been received at least once since the service is up.
	 */
	boolean isInitialized();

	/**
	 * Blocks until the service did at least once retrieve document frequencies. After this first time, this call will
	 * return immediately.
	 */
	void awaitInitialization();
}
