package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.ConceptManager.MorphoLabel;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.constants.semedico.MorphoConstants;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TermDatabaseIndexCreator implements ITermDatabaseIndexCreator {

	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public TermDatabaseIndexCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
	}

	@Override
	public void createdIndexes() {
		try {
			Map<String, Object> requestMap = new HashMap<>();
			String cypher;
			HttpEntity response;

			log.info("Creating index for {} on property {}.", TermLabels.GeneralLabel.MAPPING_AGGREGATE,
					ConceptConstants.PROP_ID);
			cypher = "CREATE INDEX ON :" + TermLabels.GeneralLabel.MAPPING_AGGREGATE + "(" + ConceptConstants.PROP_ID
					+ ")";
			requestMap.put("query", cypher);
			log.trace("Cypher request map: {}", requestMap);
			response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
					JsonSerializer.toJson(requestMap));
			EntityUtils.consume(response);

			log.info("Creating index for {} on property {}.", TermLabels.GeneralLabel.TERM, ConceptConstants.PROP_ID);
			cypher = "CREATE INDEX ON :" + TermLabels.GeneralLabel.TERM + "(" + ConceptConstants.PROP_ID + ")";
			requestMap.put("query", cypher);
			log.trace("Cypher request map: {}", requestMap);
			response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
					JsonSerializer.toJson(requestMap));
			EntityUtils.consume(response);

			log.info("Creating index for {} on property {}.", FacetLabels.General.FACET, FacetConstants.PROP_ID);
			cypher = "CREATE INDEX ON :" + FacetLabels.General.FACET + "(" + FacetConstants.PROP_ID + ")";
			requestMap.put("query", cypher);
			log.trace("Cypher request map: {}", requestMap);
			response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
					JsonSerializer.toJson(requestMap));
			EntityUtils.consume(response);
			
			log.info("Creating index for {} on property {}.", MorphoLabel.WRITING_VARIANT, MorphoConstants.PROP_ID);
			cypher = "CREATE INDEX ON :" + MorphoLabel.WRITING_VARIANT + "(" + MorphoConstants.PROP_ID + ")";
			requestMap.put("query", cypher);
			log.trace("Cypher request map: {}", requestMap);
			response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
					JsonSerializer.toJson(requestMap));
			EntityUtils.consume(response);
			
			log.info("Creating index for {} on property {}.", MorphoLabel.ACRONYM, MorphoConstants.PROP_ID);
			cypher = "CREATE INDEX ON :" + MorphoLabel.ACRONYM + "(" + MorphoConstants.PROP_ID + ")";
			requestMap.put("query", cypher);
			log.trace("Cypher request map: {}", requestMap);
			response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
					JsonSerializer.toJson(requestMap));
			EntityUtils.consume(response);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

	}

}
