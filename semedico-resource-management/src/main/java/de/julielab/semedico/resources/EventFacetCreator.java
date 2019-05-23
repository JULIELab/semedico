package de.julielab.semedico.resources;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacetGroup;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.facets.FacetGroupLabels;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;

/**
 * Creates facets in the database that are 'induced' by event terms, e.g.
 * positive regulation, phosphorylation etc. Those event facets have been used
 * in the rejected NAR2015 submission. Since facets have been deemed only of
 * secondary importance, the event facet solution was never quite right anyway
 * and - most importantly - the new event integration approach does not create
 * single event terms anymore but reprents events as complex structures in the
 * ElasticSearch index, for the NAR2016 submission the event facets are omitted.
 * 
 * @author faessler
 * 
 */
public class EventFacetCreator implements IEventFacetCreator {

	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;
	private ITermDatabaseImportService termImportService;

	public EventFacetCreator(Logger log,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService, ITermDatabaseImportService termImportService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
		this.termImportService = termImportService;
	}

	@Override
	public void createEventFacets() {
		ImportFacetGroup eventsFacetGroup = new ImportFacetGroup("Events", 1,
				Arrays.asList(FacetGroupLabels.General.SHOW_FOR_SEARCH.name()));

		log.info("Creating event facets an induced by already existing event terms.");
		String cypherQuery = "MATCH (t:EVENT_TERM) return t";
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("query", cypherQuery);
		// if (null != parameters)
		// queryMap.put("params", parameters);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/"
				+ Neo4jService.CYPHER_ENDPOINT, JsonSerializer.toJson(queryMap));
		try {
			String string = EntityUtils.toString(response);
			JSONObject responseObject = new JSONObject(string);
			JSONArray termObjects = responseObject.getJSONArray("data");
			for (int i = 0; i < termObjects.length(); i++) {
				JSONObject term = termObjects.getJSONArray(i).getJSONObject(0)
						.getJSONObject("data");
				String specificType = term.getString(ConceptConstants.PROP_SPECIFIC_EVENT_TYPE);
				String id = term.getString(ConceptConstants.PROP_ID);

				log.info("Creating event facet for event {}.", specificType);
				String[] eventFacetNameParts = specificType.split("_");
				for (int j = 0; j < eventFacetNameParts.length; j++) {
					String part = eventFacetNameParts[j];
					eventFacetNameParts[j] = StringUtils.capitalize(part);
				}
				String eventFacetName = StringUtils.join(eventFacetNameParts, " ");
				ImportFacet eventFacet = new ImportFacet(eventFacetName, "events_" + i,
						FacetConstants.SRC_TYPE_FLAT, Lists.newArrayList(
								IIndexInformationService.GeneralIndexStructure.text, IIndexInformationService.GeneralIndexStructure.title),
						null, 0, null, eventsFacetGroup);
				eventFacet.incucingTerm = id;
				eventFacet.addGeneralLabel(FacetLabels.General.USE_FOR_BTERMS.name());
				eventFacet.addGeneralLabel(FacetLabels.General.EVENTS.toString());
				ImportConceptAndFacet importTermAndFacet = new ImportConceptAndFacet(eventFacet);
				termImportService.importTerms(importTermAndFacet);
			}
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}

}
