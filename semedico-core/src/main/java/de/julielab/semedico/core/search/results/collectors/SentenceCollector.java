package de.julielab.semedico.core.search.results.collectors;

import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SentenceSearchResult;

public class SentenceCollector extends SearchResultCollector<SemedicoESSearchCarrier, SentenceSearchResult> {

	public SentenceCollector(String name) {
		super(name);
	}

	@Override
	public SentenceSearchResult collectResult(SemedicoESSearchCarrier carrier, int responseIndex) {
		// TODO Auto-generated method stub
		return null;
	}

}
