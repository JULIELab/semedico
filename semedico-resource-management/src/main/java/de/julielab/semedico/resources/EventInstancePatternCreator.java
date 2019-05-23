package de.julielab.semedico.resources;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;

public class EventInstancePatternCreator implements IEventInstancePatternCreator{

	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public EventInstancePatternCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
	}
	
	@Override
	public void writeEventTermPatterns(File outputFile) {
		if (outputFile.exists()) {
			log.info("Output file {} already exists and is deleted.", outputFile.getAbsolutePath());
			outputFile.delete();
		}
		String cypherQuery = String.format("MATCH (f:%s) return f", FacetLabels.General.EVENTS);
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("query", cypherQuery);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
				JsonSerializer.toJson(queryMap));
		try {
			String string = EntityUtils.toString(response);
			JSONObject responseObject = new JSONObject(string);
			JSONArray facetObjects = responseObject.getJSONArray("data");
			for (int i = 0; i < facetObjects.length(); i++) {
				JSONObject facet = facetObjects.getJSONArray(i).getJSONObject(0).getJSONObject("data");
				String id = facet.getString(FacetConstants.PROP_ID);
				String inducingTerm = facet.getString(FacetConstants.PROP_INDUCING_TERM);
				// likelihood-arg1-eventtype-anythingornothing
				String pattern = "jrex:" + inducingTerm + "-.*-.*(-[a-z]+)?";
				String mapping = pattern + "=" + id;
				FileUtils.write(outputFile, mapping + "\n", "UTF-8", true);
				log.info("Writing event term instance pattern \"{}\".", pattern);
			}
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}

}
