package de.julielab.semedico.core.search.results;

public class SingleSearchResult<R> {
	private R result;

	public SingleSearchResult(R result) {
		this.result = result;
	}

	public R getResult() {
		return result;
	}
}
