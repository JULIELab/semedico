package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.EnumSet;

import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.semedico.core.search.services.SearchService.SearchOption;
import de.julielab.semedico.core.services.SemedicoCoreModule;

/**
 * Simple component that deactivates aggregations, stored field returning and
 * highlighting based on the given search options for each search server
 * request.
 * 
 * @author faessler
 *
 */
public class SearchOptionsConfigurationComponent extends AbstractSearchComponent<SemedicoESSearchCarrier> {

	public SearchOptionsConfigurationComponent(Logger log) {
		super(log);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchOptionsConfiguration {
		//
	}

	@Override
	protected boolean processSearch(SemedicoESSearchCarrier carrier) {
		if (carrier.getSearchOptions() != null && !carrier.getSearchOptions().isEmpty()) {
			for (int i = 0; i < carrier.getServerRequests().size(); ++i) {
				SearchServerRequest serverCmd = carrier.getServerRequests().get(i);
				EnumSet<SearchOption> options;
				// Check for "either exactly one option set for all queries" or
				// "one set for each query"
				if (carrier.getSearchOptions().size() == 1) {
					options = carrier.getSearchOptions(0);
				} else if (carrier.getSearchOptions().size() == carrier.getServerRequests().size()) {
					options = carrier.getSearchOptions(i);
				} else {
					throw new IllegalStateException("There are " + carrier.getServerRequests().size()
							+ " search server commands and " + carrier.getSearchOptions().size()
							+ " search option sets. Either specify no sets, a single set that will be used for all searches or one set for each search.");
				}
				for (SearchOption option : options) {
					switch (option) {
					case HIT_COUNT:
						SemedicoCoreModule.searchTraceLog.info("Deactivating due to search option {}: {}, {} and {}.",
								new Object[] { option, "aggregations", "field retrieval", "highlighting" });
						serverCmd.aggregationRequests = Collections.emptyMap();
						serverCmd.fieldsToReturn = Collections.emptyList();
						serverCmd.hlCmds = Collections.emptyList();
						break;
					case NO_AGGREGATIONS:
						SemedicoCoreModule.searchTraceLog.info("Deactivating due to search option {}: {}.",
								new Object[] { option, "aggregations" });
						serverCmd.aggregationRequests = Collections.emptyMap();
						break;
					case NO_FIELDS:
						SemedicoCoreModule.searchTraceLog.info("Deactivating due to search option {}: {}.",
								new Object[] { option, "field retrieval" });
						serverCmd.fieldsToReturn = Collections.emptyList();
						break;
					case NO_HIGHLIGHTING:
						SemedicoCoreModule.searchTraceLog.info("Deactivating due to search option {}: {}.",
								new Object[] { option, "highlighting" });
						serverCmd.hlCmds = Collections.emptyList();
						break;
					case NO_HITS:
						SemedicoCoreModule.searchTraceLog.info("Deactivating due to search option {}: {}.",
								new Object[] { option, "document hits" });
						serverCmd.rows = 0;
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
