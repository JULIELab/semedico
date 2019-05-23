package IElementsAggregateIdMappingWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.neo4j.plugins.Export;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.Utils;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.resources.IElementsAggregateIdMappingWriter;

public class ElementsAggregateIdMappingWriter implements IElementsAggregateIdMappingWriter {

	private Logger log;
	private String neo4jEndpoint;
	private INeo4jHttpClientService httpClientService;

	public ElementsAggregateIdMappingWriter(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
	}
	
	@Override
	public void writeMapping(String aggregateLabel, File outputFile) {
		log.info("Writing element to aggregate ID mapping file for label \"{}\" to file \"{}\".", aggregateLabel, outputFile);
		Map<String, Object> parameters = new HashMap<>();
		if (!StringUtils.isBlank(aggregateLabel))
			parameters.put(Export.PARAM_LABEL, JsonSerializer.toJson(Collections.singletonList(aggregateLabel)));
		
		log.debug("Used parameters: {}", parameters);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + Export.EXPORT_ENDPOINT
				+ Export.ELEMENT_TO_AGGREGATE_ID_MAPPING, JsonSerializer.toJson(parameters));
		try {
			String encoded = EntityUtils.toString(response);
			byte[] decoded = DatatypeConverter.parseBase64Binary(encoded);
			log.info("Retrieved {} bytes of element-aggregate ID mapping file data (gzipped).", decoded.length);
			Utils.writeByteArrayToStringFile(decoded, outputFile.getAbsolutePath(), true);
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
		
	}

}
