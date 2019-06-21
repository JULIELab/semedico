package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AggregateDeleter implements IAggregateDeleter {

	private Logger log;
	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	public AggregateDeleter(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	@Override
	public void deleteAggregates(String aggregatedTermsLabel) {
		log.info("Deleting aggregates with label \"{}\".", aggregatedTermsLabel);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(ConceptManager.KEY_AGGREGATED_LABEL, aggregatedTermsLabel);
		httpClientService.sendPostRequest(neo4jEndpoint + "/" + ConceptManager.TERM_MANAGER_ENDPOINT
				+ ConceptManager.DELETE_AGGREGATES, JsonSerializer.toJson(parameters));
	}

}
