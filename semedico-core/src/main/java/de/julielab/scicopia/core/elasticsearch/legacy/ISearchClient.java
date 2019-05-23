package de.julielab.scicopia.core.elasticsearch.legacy;

import org.elasticsearch.client.Client;

public interface ISearchClient {
	void shutdown();
	Client getClient();
}
