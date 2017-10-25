package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;

/**
 * Simple component that applies Semedico-specific configuration. The original
 * single task, for example, was to add the index prefix found in the
 * configuration to all index names of all queries.
 * 
 * @author faessler
 *
 */
public class SemedicoConfigurationApplicationComponent extends AbstractSearchComponent {

	private String indexPrefix;

	public SemedicoConfigurationApplicationComponent(Logger log,
			@Symbol(SemedicoSymbolConstants.INDEX_PREFIX) String indexPrefix) {
		super(log);
		this.indexPrefix = indexPrefix;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SemedicoConfigurationApplication {
		//
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		if (indexPrefix.isEmpty())
			return false;
		SemedicoSearchCarrier carrier = (SemedicoSearchCarrier) searchCarrier;
		for (SearchServerRequest request : carrier.serverRequests)
			request.index = indexPrefix + request.index;
		return false;
	}

}
