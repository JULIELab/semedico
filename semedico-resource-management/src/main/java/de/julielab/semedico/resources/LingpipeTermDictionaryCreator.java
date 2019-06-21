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
import org.slf4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LingpipeTermDictionaryCreator implements ILingpipeTermDictionaryCreator {

	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public LingpipeTermDictionaryCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
	}

	@Override
	public void writeLingpipeDictionary(String outputFilePath, String label) {
		writeLingpipeDictionary(outputFilePath, label, null, null);
	}

	@Override
	public void writeLingpipeDictionary(String outputFilePath, String label, String[] exclusionLabels, String[] properties) {
		log.info("Writing Lingpipe dictionary file for terms with label {} but not with any label in {} to {}, mapping the term names to the properties {}.",
				new Object[] { label, Arrays.toString(exclusionLabels), outputFilePath, Arrays.toString(properties) });
		Map<String, Object> parameters = new HashMap<>();
		if (!StringUtils.isBlank(label))
			parameters.put(Export.PARAM_LABEL, label);
		if (null != exclusionLabels && exclusionLabels.length > 0 && !StringUtils.isBlank(exclusionLabels[0]))
			parameters.put(Export.PARAM_EXCLUSION_LABEL, JsonSerializer.toJson(exclusionLabels));
		if (null != properties)
			parameters.put(Export.PARAM_ID_PROPERTY, properties);
		
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + Export.EXPORT_ENDPOINT
				+ Export.LINGPIPE_DICT, JsonSerializer.toJson(parameters));
		try {
//			JSONArray jsonArray = new JSONArray(EntityUtils.toString(response));
			String encoded = EntityUtils.toString(response);
			byte[] decoded = DatatypeConverter.parseBase64Binary(encoded);
			log.info("Retrieved {} bytes of Lingpipe dictionary file data (gzipped).", decoded.length);
			Utils.writeByteArrayToStringFile(decoded, outputFilePath, true);
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}

	}

}
