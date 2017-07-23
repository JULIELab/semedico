package de.julielab.semedico.core.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.datarepresentation.ConceptInsertionResponse;
import de.julielab.neo4j.plugins.datarepresentation.ImportConcept;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacetGroup;
import de.julielab.neo4j.plugins.datarepresentation.ImportMapping;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.core.facets.FacetGroupLabels;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.facets.FacetLabels.Unique;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;

public class Neo4jImportService implements ITermDatabaseImportService {

	private IHttpClientService httpClientService;
	private String neo4jEndpoint;
	private ITermDatabaseService termDatabaseService;
	private Logger log;
	private Gson gson;

	public Neo4jImportService(@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint, Logger log,
			ITermDatabaseService termDatabaseService, INeo4jHttpClientService httpClientService) {
		this.neo4jEndpoint = neo4jEndpoint;
		this.log = log;
		this.termDatabaseService = termDatabaseService;
		this.httpClientService = httpClientService;
		this.gson = new Gson();
	}

	@Override
	public String getDBHost() {
		return neo4jEndpoint;
	}

	@Override
	public List<ConceptInsertionResponse> importTerms(ImportConceptAndFacet termsAndFacet) {
		// int numFacets = termDatabaseService.getNumFacets();
		// if (0 == numFacets) {
		// log.info("Database does not contain any facets. Default bibliographic
		// facets are created first to keep their IDs stable (required for
		// LuCas).");
		// createDefaultFacets();
		// }

		int batchSize = 1000;

		int numConceptsImported = 0;

		List<ImportConcept> terms = termsAndFacet.terms;
		int numBatches = (int) Math.ceil((double) terms.size() / batchSize);

		List<ConceptInsertionResponse> responses = new ArrayList<>(numBatches);
		for (int batchNum = 0; batchNum < numBatches; batchNum++) {
			log.debug("Batch {} / {}", batchNum, numBatches);
			List<ImportConcept> batch = terms.subList(batchNum * batchSize,
					Math.min((batchNum + 1) * batchSize, terms.size()));
			ImportConceptAndFacet importConceptAndFacet = new ImportConceptAndFacet(batch, termsAndFacet.facet);
			importConceptAndFacet.importOptions = termsAndFacet.importOptions;
			log.debug("Performing the database import of {} concepts.", batch.size());
			ConceptInsertionResponse response = importConceptBatch(importConceptAndFacet);
			if (response != null)
				numConceptsImported += batch.size();
			responses.add(response);
			// it is not required that a facet is given, we might just have some
			// concepts with additional property information to be merged into
			// already existing concepts in the database
			if (termsAndFacet.facet != null) {
				if (termsAndFacet.facet.id == null)
					log.info("Server assigned facet ID {} for facet {}", response.facetId, termsAndFacet.facet.name);
				termsAndFacet.facet.id = response.facetId;
			}
		}
		log.info("{} of {} concepts have been successfully imported into the database.", numConceptsImported,
				termsAndFacet.terms.size());
		return responses;
	}

	private ConceptInsertionResponse importConceptBatch(ImportConceptAndFacet conceptsAndFacet) {
		HttpEntity response = httpClientService.sendPostRequest(
				neo4jEndpoint + "/" + ConceptManager.TERM_MANAGER_ENDPOINT + ConceptManager.INSERT_TERMS,
				conceptsAndFacet.toNeo4jRestRequest());
		try {

			return gson.fromJson(EntityUtils.toString(response), ConceptInsertionResponse.class);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * In the current version of Semedico, we don't use bibliographic facets
	 */
	@Deprecated
	@Override
	public void createDefaultFacets() {
		List<ImportConceptAndFacet> defaultFacets = getDefaultFacetsForImport();
		for (ImportConceptAndFacet facet : defaultFacets) {
			HttpEntity response = httpClientService.sendPostRequest(
					neo4jEndpoint + "/" + Neo4jService.TERM_MANAGER_ENDPOINT + ConceptManager.INSERT_TERMS,
					facet.toNeo4jRestRequest());
			try {
				EntityUtils.consume(response);
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private List<ImportConceptAndFacet> getDefaultFacetsForImport() {
		ImportFacetGroup facetGroupBibliography = new ImportFacetGroup("Bibliome", 2,
				Lists.newArrayList(FacetGroupLabels.General.SHOW_FOR_SEARCH.toString()));
		// Note that for these facets we set the source name explicitly since it
		// does not follow the general rule
		// "FacetConstants.FACET_FIELD_PREFIX + NodeIDPrefixConstants.FACET +
		// <number>". We could make the following
		// facets
		// compliant to this system, but then the LuCas mapping file would look
		// even less understandable...
		List<ImportConceptAndFacet> extraFacets = new ArrayList<>();
		ImportFacet authorsFacet = new ImportFacet("Authors", "authors", FacetConstants.SRC_TYPE_STRINGS,
				Lists.newArrayList(IIndexInformationService.AUTHORS),
				Lists.newArrayList(IIndexInformationService.FACET_AUTHORS), 0,
				Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
						FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString()),
				facetGroupBibliography);
		authorsFacet.sourceName = IIndexInformationService.FACET_AUTHORS;
		authorsFacet.addUniqueLabel(Unique.AUTHORS.name());
		ImportConceptAndFacet authors = new ImportConceptAndFacet(authorsFacet);
		extraFacets.add(authors);

		ImportFacet lastAuthorsFacet = new ImportFacet("Last Authors", "lastAuthors", FacetConstants.SRC_TYPE_STRINGS,
				Lists.newArrayList(IIndexInformationService.LAST_AUTHORS),
				Lists.newArrayList(IIndexInformationService.FACET_LAST_AUTHORS), 1,
				Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
						FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString()),
				facetGroupBibliography);
		lastAuthorsFacet.sourceName = IIndexInformationService.FACET_LAST_AUTHORS;
		lastAuthorsFacet.addUniqueLabel(Unique.LAST_AUTHORS.name());
		ImportConceptAndFacet lastAuthors = new ImportConceptAndFacet(lastAuthorsFacet);
		extraFacets.add(lastAuthors);

		ImportFacet firstAuthorsFacet = new ImportFacet("First Authors", "firstAuthors",
				FacetConstants.SRC_TYPE_STRINGS, Lists.newArrayList(IIndexInformationService.FIRST_AUTHORS),
				Lists.newArrayList(IIndexInformationService.FACET_FIRST_AUTHORS), 2,
				Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
						FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString()),
				facetGroupBibliography);
		firstAuthorsFacet.sourceName = IIndexInformationService.FACET_FIRST_AUTHORS;
		firstAuthorsFacet.addUniqueLabel(Unique.FIRST_AUTHORS.name());
		ImportConceptAndFacet firstAuthors = new ImportConceptAndFacet(firstAuthorsFacet);
		extraFacets.add(firstAuthors);

		ImportFacet journalsFacet = new ImportFacet("Journals", "journals", FacetConstants.SRC_TYPE_STRINGS,
				Lists.newArrayList(IIndexInformationService.JOURNAL),
				Lists.newArrayList(IIndexInformationService.FACET_JOURNALS), 3,
				Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
						FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString()),
				facetGroupBibliography);
		journalsFacet.sourceName = IIndexInformationService.FACET_JOURNALS;
		journalsFacet.addUniqueLabel(Unique.JOURNALS.name());
		ImportConceptAndFacet journals = new ImportConceptAndFacet(journalsFacet);
		extraFacets.add(journals);

		ImportFacet yearsFacet = new ImportFacet("Years", "years", FacetConstants.SRC_TYPE_STRINGS,
				Lists.newArrayList(IIndexInformationService.YEAR),
				Lists.newArrayList(IIndexInformationService.FACET_YEARS), 4,
				Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
						FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString()),
				facetGroupBibliography);
		yearsFacet.sourceName = IIndexInformationService.FACET_YEARS;
		yearsFacet.addUniqueLabel(Unique.YEARS.name());
		ImportConceptAndFacet years = new ImportConceptAndFacet(yearsFacet);
		extraFacets.add(years);

		// B-Term facet

		ImportFacetGroup facetGroupBterms = new ImportFacetGroup("B-Terms", 0,
				Lists.newArrayList(FacetGroupLabels.General.SHOW_FOR_BTERMS.name()));
		ImportFacet bTermsFacet = new ImportFacet("B-Terms", "bterms", FacetConstants.SRC_TYPE_FACET_AGGREGATION, null,
				null, 0, null, facetGroupBterms);
		// For aggregate facets, their source is only the base name of all facet
		// fields, suffixed by the respective
		// facet IDs.
		bTermsFacet.addUniqueLabel(FacetLabels.Unique.BTERMS.name());
		bTermsFacet.sourceName = IIndexInformationService.BTERMS;
		bTermsFacet.aggregationLabels = Lists.newArrayList(FacetLabels.General.USE_FOR_BTERMS.name());
		bTermsFacet.aggregationFields = Lists.newArrayList(IIndexInformationService.FACET_EVENTS);
		ImportConceptAndFacet bterms = new ImportConceptAndFacet(bTermsFacet);
		extraFacets.add(bterms);

		return extraFacets;
	}

	@Override
	public String importMappings(List<ImportMapping> mappings) {
		Map<String, Object> requestData = new HashMap<>();
		requestData.put(ConceptManager.KEY_MAPPINGS, JsonSerializer.toJson(mappings));
		HttpEntity response = httpClientService.sendPostRequest(
				neo4jEndpoint + "/" + ConceptManager.TERM_MANAGER_ENDPOINT + ConceptManager.INSERT_MAPPINGS,
				JsonSerializer.toJson(requestData));
		try {
			return EntityUtils.toString(response);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
