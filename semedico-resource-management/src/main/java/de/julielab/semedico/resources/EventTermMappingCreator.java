package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EventTermMappingCreator implements IEventTermMappingCreator {

	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public EventTermMappingCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
	}

	@Override
	public void writeEventTermMapping(File outputFile) {
		if (outputFile.exists()) {
			log.info("Output file {} already exists and is deleted.", outputFile.getAbsolutePath());
			outputFile.delete();
		}
		String cypherQuery = "MATCH (t:EVENT_TERM) return t";
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("query", cypherQuery);
		// if (null != parameters)
		// queryMap.put("params", parameters);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
				JsonSerializer.toJson(queryMap));
		try {
			String string = EntityUtils.toString(response);
			JSONObject responseObject = new JSONObject(string);
			JSONArray termObjects = responseObject.getJSONArray("data");
			if (termObjects.length() == 0) {
				log.info("There are no event term mappings.");
			}
			for (int i = 0; i < termObjects.length(); i++) {
				JSONObject term = termObjects.getJSONArray(i).getJSONObject(0).getJSONObject("data");
				String specificType = term.getString(ConceptConstants.PROP_SPECIFIC_EVENT_TYPE);
				String id = term.getString(ConceptConstants.PROP_ID);
				String mapping = specificType + "\t" + id;
				FileUtils.write(outputFile, mapping + "\n", "UTF-8", true);
				log.info("Writing mapping \"{}\".", mapping);
			}
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}

}
