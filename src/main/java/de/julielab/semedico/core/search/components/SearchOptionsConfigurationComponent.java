package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.EnumSet;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.SearchService.SearchOption;

/**
 * Simple component that deactivates aggregations, stored field returning and
 * highlighting based on the given search options for each search server
 * request.
 * 
 * @author faessler
 *
 */
public class SearchOptionsConfigurationComponent extends AbstractSearchComponent {

	public SearchOptionsConfigurationComponent(Logger log) {
		super(log);
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchOptionsConfiguration {
		//
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier carrier = (SemedicoSearchCarrier) searchCarrier;
		if (carrier.searchOptions != null && !carrier.searchOptions.isEmpty()) {
			for (int i = 0; i < carrier.serverRequests.size(); ++i) {
				SearchServerRequest serverCmd = carrier.serverRequests.get(i);
				EnumSet<SearchOption> options;
				// Check for "either exactly one option set for all queries" or
				// "one set for each query"
				if (carrier.searchOptions.size() == 1) {
					options = carrier.searchOptions.get(0);
				} else if (carrier.searchOptions.size() == carrier.serverRequests.size()) {
					options = carrier.searchOptions.get(i);
				} else {
					throw new IllegalStateException("There are " + carrier.serverRequests.size()
							+ " search server commands and " + carrier.searchOptions.size()
							+ " search option sets. Either specify no sets, a single set that will be used for all searches or one set for each search.");
				}
				for (SearchOption option : options) {
					switch (option) {
					case HIT_COUNT:
						serverCmd.aggregationCmds = Collections.emptyMap();
						serverCmd.fieldsToReturn = Collections.emptyList();
						serverCmd.hlCmds = Collections.emptyList();
						break;
					case NO_AGGREGATIONS:
						serverCmd.aggregationCmds = Collections.emptyMap();
						break;
					case NO_FIELDS:
						serverCmd.fieldsToReturn = Collections.emptyList();
						break;
					case NO_HIGHLIGHTING:
						serverCmd.hlCmds = Collections.emptyList();
						break;
					case RETURN_SERVER_QUERY:
						break;
					default:
						break;
					}
				}
			}
		}
		return false;
	}

}
