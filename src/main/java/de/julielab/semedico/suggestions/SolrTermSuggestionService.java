/**
 * SolrTermSuggestionService.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 25.07.2011
 **/

/**
 * 
 */
package de.julielab.semedico.suggestions;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_SUGGESTIONS_CORE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SOLR_URL;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.FACETS;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.TERM_ID;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.TERM_SYNONYMS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;

/**
 * This class uses a Solr instance for creation and querying an index of term suggestion documents.
 * <p>
 * Each suggestion document has four fields:
 * <ul>
 * <li> <b>termId</b>: The ID used to identify the term for which the Solr document is a suggestion.
 * <li> <b>suggestionText</b>: The actual text which represents the term in this concrete suggestion.
 * <li> <b>facets</b>: An array (multiValued field) of facet IDs the term of this suggestion belongs to.
 * <li> <b>synonyms</b>: One string of comma separated synonyms of this term. The synonym given by suggestionText is excluded.
 * </ul>
 * For each synonym/writing variant of a term, a new suggestion document is created. The Solr schema
 * should use an EdgeNGram TokenFilter to create N-Gram terms by which the suggestions can be found.
 * </p>
 * <p>
 * The fields <b>suggestionText</b> and <b>synonyms</b> should be indexed and thus being searchable. The other fields
 * are only used for duplicate avoidance (<b>termId</b>) and displaying purposes (<b>synonyms</b>).
 * </p>
 * 
 * @author faessler
 * 
 */
public class SolrTermSuggestionService implements ITermSuggestionService {

	private final ITermService termService;
	private final ITermOccurrenceFilterService termOccurrenceFilterService;
	private final SolrServer solr;

	private int maxTokenLength = 15;
	private final Logger logger;

	public SolrTermSuggestionService(Logger logger,
			 ITermService termService,
			ITermOccurrenceFilterService termOccurrenceFilterService, @Symbol(SOLR_URL) String solrUrl, @Symbol(SOLR_SUGGESTIONS_CORE) String suggestionsCoreName)
			throws MalformedURLException {
		this.logger = logger;
		this.termService = termService;
		this.termOccurrenceFilterService = termOccurrenceFilterService;
		
		String suggestionsCoreUrl = solrUrl;
		suggestionsCoreUrl += solrUrl.endsWith("/") ? suggestionsCoreName : "/" + suggestionsCoreName;
		this.solr = new CommonsHttpSolrServer(suggestionsCoreUrl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.suggestions.ITermSuggestionService#getSuggestionsForFragment
	 * (java.lang.String, java.util.List)
	 */
	@Override
	public List<FacetTermSuggestionStream> getSuggestionsForFragment(
			String termFragment, List<Facet> facets) {
		HashMap<Facet, FacetTermSuggestionStream> facetHits = new HashMap<Facet, FacetTermSuggestionStream>();
		ArrayList<FacetTermSuggestionStream> resultList = new ArrayList<FacetTermSuggestionStream>();
		String termQuery = "";
		String word = termFragment;
		if (word.length() > maxTokenLength)
			word = word.substring(0, maxTokenLength);

		// The Lucene/Solr parser will throw an error when a not-quoted term is
		// searched which begins with a minus sign.
		// Additionally, when a whitespace occurs, we want to search for the
		// fragment as a phrase rather then searching for multiple
		// strings.
		if (word.startsWith("-") || word.indexOf(' ') > -1)
			word = "\"" + word + "\"";

		termQuery += String.format("+%s:%s ", SUGGESTION_TEXT, word.toLowerCase());

		try {
			for (Facet facet : facets) {

				String query = String.format("%s +%s:\"%s\"", termQuery, FACETS, facet.getId());
				SolrQuery solrQuery = new SolrQuery(query);
				SolrDocumentList solrDocs = solr.query(solrQuery).getResults();
				if (solrDocs.getNumFound() == 0)
					continue;

				FacetTermSuggestionStream facetHit = new FacetTermSuggestionStream(
						facet);
				facetHits.put(facet, facetHit);
				resultList.add(facetHit);

				Set<String> occurredTermIds = new HashSet<String>();

				for (int i = 0; i < solrDocs.size()
						&& occurredTermIds.size() < 50; i++) {

					SolrDocument doc = solrDocs.get(i);

					String termId = (String) doc.getFieldValue(TERM_ID);

					if (occurredTermIds.contains(termId))
						continue;
					occurredTermIds.add(termId);

					String termName = (String) doc
							.getFieldValue(SUGGESTION_TEXT);
					String termSynonyms = (String) doc
							.getFieldValue(TERM_SYNONYMS);

					facetHit.addTermSuggestion(termId, termName, termSynonyms);

				}
				occurredTermIds.clear();
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}

		return resultList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.suggestions.ITermSuggestionService#createSuggestionIndex
	 * (java.lang.String)
	 */
	@Override
	public void createSuggestionIndex(String solrUrl) throws IOException,
			SQLException {
		logger.info("Solr suggestion index creation started...");
		logger.info("WARNING: All documents in the existing index are deleted.");

		Collection<IFacetTerm> terms = termService
				.filterTermsNotInIndex(termService.getNodes());

		// This function is used in the for-loop to get the facet ID rather than
		// the output of Facet.toString().
		Function<Facet, Integer> facet2IdFunction = new Function<Facet, Integer>() {
			@Override
			public Integer apply(Facet facet) {
				return facet.getId();
			}
		};

		List<SolrInputDocument> solrDocs = new ArrayList<SolrInputDocument>(
				terms.size());
		for (IFacetTerm term : terms) {
			// Get the filtered contents of the field "occurrences" in this
			// term's
			// database entry
			Collection<String> suggestions = termOccurrenceFilterService
					.filterTermOccurrences(term,
							termService.readOccurrencesForTerm(term));
			for (final String suggestion : suggestions) {

				// This predicate is used below to select all synonym strings which
				// are not equal to the current suggestion itself.
				Predicate<String> synonymSelectPredicate = new Predicate<String>() {
					@Override
					public boolean apply(String synonym) {
						return !synonym.equals(suggestion);
					}
				};

				SolrInputDocument solrDoc = new SolrInputDocument();
				solrDoc.addField(TERM_ID, term.getId());
				solrDoc.addField(FACETS, Collections2.transform(
						term.getFacets(), facet2IdFunction));
				// TODO getShortDescription will be changed to return an array
				// of synonyms which then can easily be joined
				solrDoc.addField(
						TERM_SYNONYMS,
						StringUtils.join(Collections2.filter(
								Arrays.asList(term.getSynonyms().split(
										";")), synonymSelectPredicate), ", "));
				solrDoc.addField(SUGGESTION_TEXT, suggestion);
				solrDocs.add(solrDoc);
			}
		}

		try {
			logger.info("Removing old index documents...");
			solr.deleteByQuery("*:*");
			logger.info("Adding suggestion documents to the index...");
			solr.add(solrDocs);
			logger.info("Committing changes and optimizing suggestion index...");
			solr.commit();
			solr.optimize();
			logger.info("Creation of suggestion index completed.");
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

}
