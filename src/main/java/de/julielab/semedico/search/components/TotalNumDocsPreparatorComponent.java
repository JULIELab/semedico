package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TotalNumDocsPreparatorComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TotalNumDocsPreparator {
	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		SolrSearchCommand solrCmd = new SolrSearchCommand();
		searchCarrier.solrCmd = solrCmd;

		solrCmd.solrQuery = "*:*";
		solrCmd.rows = 0;
		return false;
	}

}
