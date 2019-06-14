package de.julielab.semedico.core.search.components;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Doesn't currently have any searchScopes. Meant to apply general configurational
 * aspects, such as adaptions to the index name or anything that doesn't need to
 * be reflected in the rest of the code.
 * 
 * @author faessler
 *
 */
public class SemedicoConfigurationApplicationComponent extends AbstractSearchComponent<SearchCarrier<? extends ISearchServerResponse>> {

	public SemedicoConfigurationApplicationComponent(Logger log) {
		super(log);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SemedicoConfigurationApplication {
		//
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		return false;
	}

}
