package de.julielab.semedico.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.datarepresentation.ConceptCoordinates;
import de.julielab.neo4j.plugins.datarepresentation.ImportConcept;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacetGroup;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;

import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.TermLabels;

import de.julielab.semedico.core.services.Neo4jService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;

/**
 * This class serves to define event terms in the sense of events like those that are extracted by JReX or described by
 * the BioNLP SharedTask challenges. It creates or updates terms in the database and creates a dedicated facet just for
 * those terms, the "Event" facet.
 * 
 * @author faessler
 * 
 */
public class EventTermDefiner implements IEventTermDefiner {

	private ITermDatabaseImportService termImportService;
	private Logger log;
	private String neo4jEndpoint;
	private IHttpClientService httpClientService;

	public EventTermDefiner(Logger log, ITermDatabaseImportService termImportService, @Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.termImportService = termImportService;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;
	}

	@Override
	public void defineEventTerms(String eventTermDefinition) throws IOException {
		
		
		List<ImportConcept> terms = new ArrayList<>();
		LineIterator lineIt = FileUtils.lineIterator(new File(eventTermDefinition));
		String line = null;
		while (lineIt.hasNext()) {
			try {
				line = lineIt.nextLine();
				String[] split = line.split("\\t");
				String specificType = split[0];
				String termSourceId = split[1];
				String eventArgumentNumbers = split[2];
				String synonymCSV = split[3];
				String writingVariantsCSV = split[4];
				String[] eventArgumentTypeStrings = eventArgumentNumbers.split(",");
				String[] synonyms = !StringUtils.isBlank(synonymCSV) ? synonymCSV.split(",") : null;
				String[] writingVariants =
						!StringUtils.isBlank(writingVariantsCSV) ? writingVariantsCSV.split(",") : null;

				String source = null;
				if(termSourceId.contains("http://purl.obolibrary.org/obo/GO"))
					source = "GO";
				if (termSourceId.contains("http://www.bootstrep.eu/ontology/GRO"))
					source = "GRO";
						
				EventImportTerm term = new EventImportTerm(new ConceptCoordinates(termSourceId, source, true));
				term.prefName = specificType;
				term.coordinates.source = source;
				term.addGeneralLabel(ConceptManager.TermLabel.EVENT_TERM.name());
				term.specificEventType = specificType;
				term.eventValence = new ArrayList<>();
				for (int i = 0; i < eventArgumentTypeStrings.length; i++) {
					String eventArgumentNumberString = eventArgumentTypeStrings[i];
					term.eventValence.add(Integer.parseInt(eventArgumentNumberString));
				}
				if (null != synonyms)
					term.synonyms = Arrays.asList(synonyms);
				if (null != writingVariants)
					term.writingVariants = Arrays.asList(writingVariants);
				// TODO This should be some kind of constant or identifier to a "Source Node" in the database having
				// all
				// information about the actual source so it could be displayed in Semedico.
//				term.source = "BioNLP 2009 Shared Task Event Types";
				terms.add(term);

				log.info("Adding event term definition \"{}\" to term with source ID "
						+ "\"{}\".", specificType, termSourceId);
				
				
			} catch (ArrayIndexOutOfBoundsException e) {
				log.error("Format error. Exception: ", e);
				log.error("Line was: {}", line);
			}
		}

		List<String> facetGeneralLabels = Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
				FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString(),
				FacetLabels.General.EVENTS.toString(), FacetLabels.General.USE_FOR_BTERMS.toString());
		ImportFacet eventFacet =
				new ImportFacet("Events", "events", FacetConstants.SRC_TYPE_HIERARCHICAL, Lists.newArrayList(
						IIndexInformationService.GeneralIndexStructure.text, IIndexInformationService.GeneralIndexStructure.title), null, 0, facetGeneralLabels,
						new ImportFacetGroup("BioMed"));
		//eventFacet.sourceName = IIndexInformationService.FACET_EVENTS;
		//eventFacet.addGeneralLabel(FacetLabels.General.USE_FOR_BTERMS.name());
		eventFacet.addUniqueLabel(FacetLabels.General.EVENTS.toString());

		log.info("Importing facet term definitions into database.");
		ImportConceptAndFacet importTermAndFacet = new ImportConceptAndFacet(terms, eventFacet);
		termImportService.importTerms(importTermAndFacet);

		log.info("Setting all event types to have children so they can be drilled-down in Semedico.");
		String cypherQuery = String.format(
				"MATCH (f:%s)-->(x) " + "WITH x.id as tid,f.id as fid merge (t:%s {id:tid}) "
						+ "on match set t.%s= "
						+ "CASE t.%s "
						+ "WHEN null "
						+ "THEN [fid] "
						+ "ELSE t.%s + [fid] END", FacetLabels.General.EVENTS,
				TermLabels.GeneralLabel.TERM, ConceptConstants.PROP_CHILDREN_IN_FACETS,
				ConceptConstants.PROP_CHILDREN_IN_FACETS, ConceptConstants.PROP_CHILDREN_IN_FACETS);
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("query", cypherQuery);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + "/" + Neo4jService.CYPHER_ENDPOINT,
				JsonSerializer.toJson(queryMap));
		
		EntityUtils.consume(response);

		log.info("Done.");
	}
}
