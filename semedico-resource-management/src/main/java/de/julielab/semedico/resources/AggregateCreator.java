package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AggregateCreator implements IAggregateCreator {

	private Logger log;
	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	public AggregateCreator(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	@Override
	public void createAggregates(Set<String> allowedMappingTypes, String termLabel, String aggregatedTermsLabel) {
		log.info("Creating aggregates with label \"{}\" for the following mapping types: {}", aggregatedTermsLabel,
				allowedMappingTypes);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(ConceptManager.KEY_AGGREGATED_LABEL, aggregatedTermsLabel);
		parameters.put(ConceptManager.KEY_ALLOWED_MAPPING_TYPES, JsonSerializer.toJson(allowedMappingTypes));
		if (!StringUtils.isBlank(termLabel))
			parameters.put(ConceptManager.KEY_LABEL, termLabel);
		httpClientService.sendPostRequest(neo4jEndpoint + "/"
				+ ConceptManager.TERM_MANAGER_ENDPOINT
				+ ConceptManager.BUILD_AGGREGATES_BY_MAPPINGS, JsonSerializer.toJson(parameters));
	}
}
