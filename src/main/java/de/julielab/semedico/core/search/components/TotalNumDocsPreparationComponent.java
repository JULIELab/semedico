package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;

public class TotalNumDocsPreparationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TotalNumDocsPreparation {
	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SearchServerCommand solrCmd = searchCarrier.getSingleSearchServerCommandOrCreate();

		// TODO deprecated! Replace by query_all; perhaps actually move this component to the  elastic.query project since it is quite general
//		solrCmd.serverQuery = "*:*";
		solrCmd.rows = 0;
		return false;
	}

}
