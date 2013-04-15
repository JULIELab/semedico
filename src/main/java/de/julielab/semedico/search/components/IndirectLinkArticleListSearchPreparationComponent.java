package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.semedico.core.SortCriterium;

public class IndirectLinkArticleListSearchPreparationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IndirectLinkArticleListSearchPreparation {
	}
	
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		solrCmd.filterReviews = false;
		solrCmd.sortCriterium = SortCriterium.RELEVANCE;
		return false;
	}

}
