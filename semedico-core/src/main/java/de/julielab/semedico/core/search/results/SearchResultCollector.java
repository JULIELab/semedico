package de.julielab.semedico.core.search.results;

import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;

public abstract class SearchResultCollector<C extends ISemedicoSearchCarrier<?, ?>, R extends SemedicoESSearchResult> {
	private Object name;
	private R result;

	public SearchResultCollector(Object name) {
		this.name = name;

	}

	public Object getName() {
		return name;
	}

	public void setResult(R result) {
		this.result = result;
	}

	public R getResult() {
		return result;
	}

	public abstract R collectResult(C carrier, int responseIndex);
}
