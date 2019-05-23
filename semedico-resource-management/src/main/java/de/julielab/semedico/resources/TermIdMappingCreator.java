package de.julielab.semedico.resources;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.slf4j.Logger;

import de.julielab.neo4j.plugins.Export;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.Utils;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;

public class TermIdMappingCreator implements ITermIdMappingCreator {
	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	private Logger log;

	public TermIdMappingCreator(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	@Override
	public void writeIdMapping(String outputFile, String idProperty, String... labels) {
		if (null == labels)
			throw new IllegalArgumentException(
					"You have to provide at least one term label to create the ID mapping for.");
		log.info("Writing the mapping from property \"" + idProperty + "\" to database term IDs for labels "
				+ Arrays.toString(labels) + " to file \"" + outputFile + "\".");
		JSONArray labelsArray = new JSONArray((Object[]) labels);
		Map<String, Object> parameters = new HashMap<>();
		if (null != idProperty)
			parameters.put(Export.PARAM_ID_PROPERTY, idProperty);
		if (null != labelsArray)
			parameters.put(Export.PARAM_LABELS, labelsArray.toString());
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + Export.EXPORT_ENDPOINT
				+ Export.TERM_ID_MAPPING, JsonSerializer.toJson(parameters));
		try {
			JSONArray jsonArray = new JSONArray(EntityUtils.toString(response));
			log.info("Retrieved {} bytes of ID mapping file data.", jsonArray.length());
			byte[] bytes = new byte[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				bytes[i] = (byte) jsonArray.getInt(i);
			}
			Utils.writeByteJsonArrayToStringFile(jsonArray, outputFile, true);
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}

	}

}
