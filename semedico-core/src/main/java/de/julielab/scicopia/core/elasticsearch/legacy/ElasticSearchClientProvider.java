package de.julielab.scicopia.core.elasticsearch.legacy;

import static de.julielab.scicopia.core.elasticsearch.legacy.ElasticQuerySymbolConstants.ES_CLUSTER_NAME;
import static de.julielab.scicopia.core.elasticsearch.legacy.ElasticQuerySymbolConstants.ES_HOST;
import static de.julielab.scicopia.core.elasticsearch.legacy.ElasticQuerySymbolConstants.ES_PORT;

import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClient;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClientProvider;
import de.julielab.scicopia.core.elasticsearch.legacy.ElasticSearchClient;

public class ElasticSearchClientProvider implements ISearchClientProvider {

	private final Logger log;
	private ElasticSearchClient elasticSearchServer;

	public ElasticSearchClientProvider(Logger log, LoggerSource loggerSource,
			@Symbol(ES_CLUSTER_NAME) String clusterName, @Symbol(ES_HOST) String host, @Symbol(ES_PORT) int port) {
		this.log = log;
		String[] hosts = host.split(",");
		elasticSearchServer = new ElasticSearchClient(
				loggerSource.getLogger(ElasticSearchClient.class), clusterName, hosts, port);
	}

	@Override
	public ISearchClient getSearchClient() {
		return elasticSearchServer;
	}

	@PostInjection
	public void startupService(RegistryShutdownHub shutdownHub) {
		shutdownHub.addRegistryShutdownListener(new Runnable() {
			public void run() {
				log.info("Shutting down elastic search clients.");
				elasticSearchServer.shutdown();
			}
		});
	}

}
