package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.Export;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.Utils;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TermIdToFacetIdMapCreator implements ITermIdToFacetIdMapCreator {

	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public TermIdToFacetIdMapCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	public void writeMapping(String outputFilePath, String label) throws IOException {
		log.info("Creating termID to facetID mapping for terms with label {}.", label);
		Map<String, Object> requestMap = new HashMap<>();
		if (!StringUtils.isBlank(label))
			requestMap.put(Export.PARAM_LABEL, label);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + Export.EXPORT_ENDPOINT
				+ Export.TERM_TO_FACET, JsonSerializer.toJson(requestMap));
		try {
			JSONArray jsonArray = new JSONArray(EntityUtils.toString(response));
			log.info("Retrieved {} bytes of term to facet ID mapping file data.", jsonArray.length());
			Utils.writeByteJsonArrayToStringFile(jsonArray, outputFilePath, true);
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}
}
