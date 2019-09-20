package de.julielab.semedico.core.search.results;

import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;

public abstract class SemedicoESSearchResult {
	protected long elapsedTime;
	protected String errorMessage;
	protected long numDocumentsFound;
	protected SemedicoESSearchCarrier searchCarrier;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean hasError() {
		return errorMessage != null;
	}

	public void setElapsedTime(long time) {
		elapsedTime = time;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	/**
	 * The search carrier that was used for the search process that yielded this
	 * result. May be null if there was no search carrier involved.
	 * 
	 * @return The {@link SemedicoESSearchCarrier} involved to created this result.
	 */
	public SemedicoESSearchCarrier getSearchCarrier() {
		return searchCarrier;
	}

	public void setSearchCarrier(SemedicoESSearchCarrier searchCarrier) {
		this.searchCarrier = searchCarrier;
	}

	/**
	 * Returns the number of Lucene/ElasticSearch documents found in the request
	 * from which this result is derived. Note that there are also aggregation
	 * results that actually might have a different number of items as their
	 * respective results.
	 * 
	 * @return The number of hit documents in the request leading to this result.
	 */
	public long getNumDocumentsFound() {
		return numDocumentsFound;
	}

	public void setNumDocumentsFound(long numDocumentsFound) {
		this.numDocumentsFound = numDocumentsFound;
	}
}
