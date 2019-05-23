package de.julielab.scicopia.core.elasticsearch.legacy;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClient;

@FunctionalInterface
public interface ISearchClientProvider {
	
	ISearchClient getSearchClient();
}
