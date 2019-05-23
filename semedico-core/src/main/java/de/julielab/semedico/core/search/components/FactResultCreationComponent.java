package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;

public class FactResultCreationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FactResultCreation {
		//
	}
	
	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		// TODO Auto-generated method stub
		return false;
	}

}
