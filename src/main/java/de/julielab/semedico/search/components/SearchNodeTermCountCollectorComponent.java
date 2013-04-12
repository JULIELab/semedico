package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SearchNodeTermCountCollectorComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchNodeTermCountCollector {
	}

	private final ISearchComponent searchNodeTermCountsSubchain;

	public SearchNodeTermCountCollectorComponent(
			@SearchNodeTermCountsSubchain ISearchComponent searchNodeTermCountsSubchain) {
		this.searchNodeTermCountsSubchain = searchNodeTermCountsSubchain;
	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		if (null == searchCarrier.searchCmd
				|| null == searchCarrier.searchCmd.nodeCmd)
			throw new IllegalArgumentException("An instance of "
					+ SearchNodeSearchCommand.class.getName()
					+ " was expected but not passed.");
		SearchNodeSearchCommand nodeCmd = searchCarrier.searchCmd.nodeCmd;
		if (null == nodeCmd.searchNodes)
			throw new IllegalArgumentException(
					"A list of search nodes to get potential indirect links from is required. However, the list was null.");

		SearchCarrier continueSearchCarrier = new SearchCarrier();
		boolean terminate = false;
		for (int i = 0; i < nodeCmd.searchNodes.size(); ++i) {
			nodeCmd.nodeIndex = i;
			terminate = terminate
					|| searchNodeTermCountsSubchain.process(searchCarrier);
			// Since we do multiple runs, we must reset the search Carrier.
			// Otherwise, we have a lot of duplicates with facet parameters and
			// other information bleeding. Just save the received counts and
			// continue anew.
			continueSearchCarrier.searchResult = new SemedicoSearchResult();
			continueSearchCarrier.searchResult
					.addSearchNodeTermCounts(searchCarrier.searchResult.searchNodeTermCounts
							.get(0));
			SearchCarrier newSearchCarrier = new SearchCarrier();
			newSearchCarrier.searchCmd = new SemedicoSearchCommand();
			newSearchCarrier.searchCmd.nodeCmd = new SearchNodeSearchCommand();
			newSearchCarrier.searchCmd.nodeCmd.searchNodes = searchCarrier.searchCmd.nodeCmd.searchNodes;
		}
		return terminate;
	}

}
