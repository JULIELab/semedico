package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

public class AggregatePropertyCreator implements IAggregatePropertyCreator {
	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public AggregatePropertyCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	@Override
	public void createAggregateProperties() {
		log.info("Creating aggregates properties by copying the properties of their element terms.");
		httpClientService.sendPostRequest(neo4jEndpoint + "/" + ConceptManager.TERM_MANAGER_ENDPOINT
				+ ConceptManager.COPY_AGGREGATE_PROPERTIES);

	}

}
