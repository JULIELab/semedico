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

import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.FACETS;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.SORTING;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.TERM_ID;
import static de.julielab.semedico.suggestions.ITermSuggestionService.Fields.TERM_SYNONYMS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;

/**
 * This class uses a Solr instance for creation and querying an index of term
 * suggestion documents.
 * <p>
 * Each suggestion document has four fields:
 * <ul>
 * <li><b>termId</b>: The ID used to identify the term for which the Solr
 * document is a suggestion.
 * <li><b>suggestionText</b>: The actual text which represents the term in this
 * concrete suggestion.
 * <li><b>facets</b>: An array (multiValued field) of facet IDs the term of this
 * suggestion belongs to.
 * <li><b>synonyms</b>: One string of comma separated synonyms of this term. The
 * synonym given by suggestionText is excluded.
 * </ul>
 * For each synonym/writing variant of a term, a new suggestion document is
 * created. The Solr schema should use an EdgeNGram TokenFilter to create N-Gram
 * terms by which the suggestions can be found.
 * </p>
 * <p>
 * The fields <b>suggestionText</b> and <b>synonyms</b> should be indexed and
 * thus being searchable. The other fields are only used for duplicate avoidance
 * (<b>termId</b>) and displaying purposes (<b>synonyms</b>).
 * </p>
 * 
 * @author faessler
 * 
 */
public class SolrTermSuggestionService implements ITermSuggestionService {

	// private final static Comparator<SolrDocument>
	// solrSuggestionByNameComparator = new Comparator<SolrDocument>() {
	// @Override
	// public int compare(SolrDocument arg0, SolrDocument arg1) {
	// String value0 = (String) arg0.getFieldValue(SUGGESTION_TEXT);
	// String value1 = (String) arg1.getFieldValue(SUGGESTION_TEXT);
	// return value0.toLowerCase().compareTo((value1).toLowerCase());
	// }
	// };

	private final ITermService termService;
	private final ITermOccurrenceFilterService termOccurrenceFilterService;
	private final HttpSolrServer suggSolr;

	private int maxTokenLength = 15;
	private final Logger logger;

	private final SolrServer searchSolr;

	private final IFacetService facetService;
	private RuleBasedCollator collator;

	public SolrTermSuggestionService(Logger logger, ITermService termService,
			ITermOccurrenceFilterService termOccurrenceFilterService,
			@InjectService("SolrSuggester") SolrServer suggSolr,
			@InjectService("SolrSearcher") SolrServer searchSolr,
			IFacetService facetService,
			IRuleBasedCollatorWrapper ruleBasedCollatorWrapper)
			throws MalformedURLException {
		this.logger = logger;
		this.termService = termService;
		this.termOccurrenceFilterService = termOccurrenceFilterService;
		this.suggSolr = (HttpSolrServer) suggSolr;
		this.searchSolr = searchSolr;
		this.facetService = facetService;
		this.collator = ruleBasedCollatorWrapper.getCollator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.suggestions.ITermSuggestionService#
	 * getSuggestionsForFragment (java.lang.String, java.util.List)
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

		// Search for suggestions for terms...
		termQuery += String.format("+%s:%s ", SUGGESTION_TEXT,
				word.toLowerCase());

		// The following comparator is responsible for the final sorting of the
		// retrieved suggestions. It is most important for author names and
		// might even not be used for other terms.
		// The comparator's main motivation is to rank such suggestions high
		// which have a character-wise exact match to the user input. Since
		// author names are collated, e.g. "Sühnel" and "Suehnel" are considered
		// the same in the Solr index, there may be side effects: Authors named
		// "Sue", for example, could be sorted behind "Sühnel" even though the
		// user types "sue". Simply ordering "ü" behind "ue" won't suffice since
		// when the user types "sü", "Sühnel" SHOULD be placed before "Sue".
		// This comparator delivers the desired behavior.
		final String fWord = word;
		Comparator<SolrDocument> comparator = new Comparator<SolrDocument>() {

			private Collator generalCollator = getGeneralCollator();

			@Override
			public int compare(SolrDocument arg0, SolrDocument arg1) {
				String value0 = (String) arg0.getFieldValues(SUGGESTION_TEXT)
						.iterator().next();
				String value1 = (String) arg1.getFieldValues(SUGGESTION_TEXT)
						.iterator().next();
				// Since this is a character-wise comparison, it actually might
				// happen that we have suggestions which do not start with the
				// exact user input. Example: User input "sue", Suggestion
				// "Sühnel".
				int startsWith1 = value1.toLowerCase().startsWith(fWord) ? 1
						: 0;
				int startsWith0 = value0.toLowerCase().startsWith(fWord) ? 1
						: 0;
				int charDifference = startsWith1 - startsWith0;
				// When one suggestion is a perfect match to the user input and
				// the other is not,
				// prefer it in the ranking.
				if (charDifference != 0)
					return charDifference;
				// Otherwise, just return normal sorting by a GENERAL collator,
				return generalCollator.compare(value0, value1);
			}

			private Collator getGeneralCollator() {
				Collator generalCollator = Collator.getInstance();
				return generalCollator;
			}

		};

		try {
			for (FacetGroup<Facet> facetGroup : facetService.getFacetGroups()) {
				for (Facet facet : facetGroup) {
					// if (!facets.contains(facet))
					// continue;
					// ...which belong to the current facet.
					String query = String.format("%s +%s:\"%s\"", termQuery,
							FACETS, facet.getId());
					// Now, first get the most relevant suggestions...
					SolrQuery solrQuery = new SolrQuery(query);
					solrQuery.set("sort", "score desc, " + SORTING + " asc");
					SolrDocumentList solrDocs = suggSolr.query(solrQuery)
							.getResults();
					if (solrDocs.getNumFound() == 0)
						continue;

					// Collections.sort(solrDocs,
					// solrSuggestionByNameComparator);
					Collections.sort(solrDocs, comparator);

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

						// The value returned first seems to be the one closer
						// to the search. Actually this is kind of a quick hack
						// because we wanted to index author names like 'Kim, A'
						// AND 'Kim A' so that you wouldn't have to type the
						// comma every time.
						String termName = (String) doc
								.getFieldValues(SUGGESTION_TEXT).iterator()
								.next();
						String termSynonyms = (String) doc
								.getFieldValue(TERM_SYNONYMS);

						facetHit.addTermSuggestion(termId, termName,
								termSynonyms);

					}
					occurredTermIds.clear();
				}
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
		try {
			logger.info("Removing old index documents...");
			suggSolr.deleteByQuery("*:*");

			addSuggestionsForAuthors();
			// addSuggestionsForIndexFieldValues(IndexFieldNames.FACET_JOURNALS);
			// addSuggestionsForIndexFieldValues(IndexFieldNames.FACET_YEARS);
			// addSuggestionsForDatabaseTerms();

			logger.info("Committing changes and optimizing suggestion index...");
			suggSolr.commit();
			suggSolr.optimize();
			logger.info("Creation of suggestion index complete.");
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @throws IOException
	 * @throws SolrServerException
	 * 
	 */
	private void addSuggestionsForAuthors() throws SolrServerException,
			IOException {
		final Iterator<byte[][]> canonicalAuthorNames = termService
				.getCanonicalAuthorNames();
		final Facet authorFacet = facetService.getAuthorFacet();
		logger.info("Creating suggestions for authors...");

		Iterator<SolrInputDocument> it = new Iterator<SolrInputDocument>() {

			private Stack<String> waitingStringTerms = new Stack<String>();

			@Override
			public boolean hasNext() {
				return canonicalAuthorNames.hasNext();
			}

			@Override
			public SolrInputDocument next() {
				String name;
				if (waitingStringTerms.size() == 0) {
					byte[][] can = canonicalAuthorNames.next();
					name = new String(can[0], Charset.forName("UTF-8"));
				} else
					name = waitingStringTerms.pop();
				String stringTermId = termService.checkStringTermId(name,
						authorFacet);
				SolrInputDocument solrDoc = new SolrInputDocument();
				solrDoc.addField(TERM_ID, stringTermId);
				solrDoc.addField(FACETS,
						Lists.newArrayList(authorFacet.getId()));
				solrDoc.addField(SUGGESTION_TEXT, name);
				if (name.contains(",")) {
					solrDoc.addField(SUGGESTION_TEXT, name.replaceAll(",", ""));
					String reverseName = name.substring(name.indexOf(",") + 1,
							name.length()).trim();
					reverseName += " "
							+ name.substring(0, name.indexOf(",")).trim();
					waitingStringTerms.push(reverseName);
				}
				solrDoc.addField(SORTING, name);
				return solrDoc;
			}

			@Override
			public void remove() {
				throw new NotImplementedException();
			}
		};

		suggSolr.add(it);
	}

	/**
	 * @param fieldName
	 * @return
	 */
	private void addSuggestionsForIndexFieldValues(String fieldName) {
		logger.info(
				"Adding suggestions for string terms in Medline index field {}.",
				fieldName);
		final Facet facet = facetService.getFacetByIndexFieldName(fieldName);

		if (facet == null)
			throw new IllegalArgumentException("No facet for field name '"
					+ fieldName + "' found.");

		// TODO Use Term component!
		SolrQuery q = new SolrQuery("*:*");
		q.setFacet(true);
		q.set("facet.field", fieldName);
		q.setFacetLimit(-1);
		try {
			logger.info("Retrieving string terms from field '" + fieldName
					+ "'.");
			FacetField facetField = searchSolr.query(q)
					.getFacetField(fieldName);
			logger.info("Got {} string terms for suggestion indexing.",
					facetField.getValueCount());
			final Iterator<Count> results = facetField.getValues().iterator();
			Iterator<SolrInputDocument> it = new Iterator<SolrInputDocument>() {

				private Stack<String> waitingStringTerms = new Stack<String>();

				@Override
				public boolean hasNext() {
					return results.hasNext();
				}

				@Override
				public SolrInputDocument next() {
					String name = null;
					if (waitingStringTerms.size() == 0) {
						Count strTerm = results.next();
						name = strTerm.getName();
					} else
						name = waitingStringTerms.pop();
					String stringTermId = termService.checkStringTermId(name,
							facet);
					SolrInputDocument solrDoc = new SolrInputDocument();
					solrDoc.addField(TERM_ID, stringTermId);
					solrDoc.addField(FACETS, Lists.newArrayList(facet.getId()));
					solrDoc.addField(SUGGESTION_TEXT, name);
					// TODO ---- Quick & dirty: Author names reverse, author
					// names without comma
					if (name.contains(",")) {
						solrDoc.addField(SUGGESTION_TEXT,
								name.replaceAll(",", ""));
						String reverseName = name.substring(
								name.indexOf(",") + 1, name.length()).trim();
						reverseName += " "
								+ name.substring(0, name.indexOf(",")).trim();
						// solrDoc.addField(SUGGESTION_TEXT, reverseName);
						waitingStringTerms.push(reverseName);
					}
					solrDoc.addField(SORTING, name);

					// ---- End of quick & dirty
					return solrDoc;
				}

				@Override
				public void remove() {
					throw new NotImplementedException();
				}

			};
			suggSolr.add(it);
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param terms
	 * @return
	 * @throws SQLException
	 */
	protected void addSuggestionsForDatabaseTerms() throws SQLException {

		logger.info("Adding suggestions for terms in the database.");

		// This function is used in the for-loop to get the facet ID rather than
		// the output of Facet.toString().
		final Function<Facet, Integer> facet2IdFunction = new Function<Facet, Integer>() {
			@Override
			public Integer apply(Facet facet) {
				return facet.getId();
			}
		};

		final Iterator<IFacetTerm> termIt = termService.filterTermsNotInIndex(
				termService.getNodes()).iterator();

		Iterator<SolrInputDocument> solrDocIt = new Iterator<SolrInputDocument>() {

			private Stack<String> currentSuggestions = new Stack<String>();
			private IFacetTerm currentTerm = null;

			@Override
			public boolean hasNext() {
				return termIt.hasNext();
			}

			@Override
			public SolrInputDocument next() {

				try {
					if (currentSuggestions.size() == 0) {
						currentTerm = termIt.next();
						// Get the filtered contents of the field "occurrences"
						// in this
						// term's
						// database entry
						currentSuggestions.addAll(termOccurrenceFilterService
								.filterTermOccurrences(currentTerm, termService
										.readOccurrencesForTerm(currentTerm)));

						if (currentSuggestions.size() == 0)
							currentSuggestions.push(currentTerm.getName());
					}

					final String suggestion = currentSuggestions.pop();
					// This predicate is used below to select all synonym
					// strings
					// which
					// are not equal to the current suggestion itself.
					Predicate<String> synonymSelectPredicate = new Predicate<String>() {
						@Override
						public boolean apply(String synonym) {
							return !synonym.equals(suggestion);
						}
					};

					SolrInputDocument solrDoc = new SolrInputDocument();
					solrDoc.addField(TERM_ID, currentTerm.getId());
					solrDoc.addField(FACETS, Collections2.transform(
							currentTerm.getFacets(), facet2IdFunction));
					// TODO getShortDescription will be changed to return an
					// array
					// of synonyms which then can easily be joined
					if (currentTerm.getSynonyms() == null)
						// Just for avoiding the NPE which would be thrown
						// below.
						currentTerm.setShortDescription("");
					solrDoc.addField(TERM_SYNONYMS, StringUtils.join(
							Collections2.filter(Arrays.asList(currentTerm
									.getSynonyms().split(";")),
									synonymSelectPredicate), ", "));
					solrDoc.addField(SUGGESTION_TEXT, suggestion);

					return solrDoc;
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public void remove() {
				throw new NotImplementedException();
			}

		};
		try {
			suggSolr.add(solrDocIt);
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
