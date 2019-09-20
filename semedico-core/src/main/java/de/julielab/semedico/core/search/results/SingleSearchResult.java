package de.julielab.semedico.core.search.results;

public class SingleSearchResult<R extends SemedicoESSearchResult> extends SemedicoESSearchResult {
	private R result;

	public SingleSearchResult(R result) {
		this.result = result;
		this.numDocumentsFound = result.numDocumentsFound;
	}

	public R getResult() {
		return result;
	}
}
