package de.julielab.semedico.resources;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;

public class FacetRootTermNumberSetter implements IFacetRootTermNumberSetter {

	private Logger log;
	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	public FacetRootTermNumberSetter(Logger log, INeo4jHttpClientService httpClientService,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint) {
		this.log = log;
		this.httpClientService = httpClientService;
		this.neo4jEndpoint = neo4jEndpoint;

	}

	@Override
	public void setFacetRootTermNumbers() {
		log.info("Setting the number of root terms on all facets.");
		String cypherQuery = "MATCH (f:FACET)-[r:HAS_ROOT_TERM]->() WITH f,count(r) as numRoots MERGE (x:FACET {id:f.id}) ON MATCH set x.numRoots = numRoots RETURN x";
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("query", cypherQuery);
		@SuppressWarnings("unused")
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
				JsonSerializer.toJson(queryMap));
	}

}
