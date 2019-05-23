package de.julielab.semedico.core.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.ImportMapping;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;

public class Neo4jImportService implements ITermDatabaseImportService {

	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	public Neo4jImportService(@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	@Override
	public String getDBHost() {
		return neo4jEndpoint;
	}

	@Override
	public String importTerms(ImportConceptAndFacet termsAndFacet) {
		String restRequest = termsAndFacet.toNeo4jRestRequest();
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + ConceptManager.TERM_MANAGER_ENDPOINT
				+ ConceptManager.INSERT_TERMS, restRequest);
		try {
			return EntityUtils.toString(response);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String importMappings(List<ImportMapping> mappings) {
		Map<String, Object> requestData = new HashMap<>();
		requestData.put(ConceptManager.KEY_MAPPINGS, JsonSerializer.toJson(mappings));
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + ConceptManager.TERM_MANAGER_ENDPOINT
				+ ConceptManager.INSERT_MAPPINGS, JsonSerializer.toJson(requestData));
		try {
			return EntityUtils.toString(response);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
