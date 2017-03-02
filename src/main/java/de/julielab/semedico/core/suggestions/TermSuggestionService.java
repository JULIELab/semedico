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
package de.julielab.semedico.core.suggestions;

import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.FACETS;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.SORTING;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_ID;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_PREF_NAME;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_SYNONYMS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import de.julielab.elastic.query.services.IIndexingService;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.services.query.QueryTokenizerImpl;

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
public class TermSuggestionService implements ITermSuggestionService {

	private final static int BATCH_SIZE_SOLR_IMPORT = 1000000;

	public final static String suggestionItemType = "items";

	private final ITermService termService;
	private final ITermOccurrenceFilterService termOccurrenceFilterService;

	private final Logger log;

	private final IFacetService facetService;

	private ISearchService searchService;

	private IIndexingService indexingService;

	private Boolean activated;

	private Boolean filterIndexTerms;

	private ILexerService lexerService;

	private String suggestionIndexName;

	public TermSuggestionService(Logger logger, ITermService termService,
			ITermOccurrenceFilterService termOccurrenceFilterService, IFacetService facetService,
			ISearchService searchService, IIndexingService indexingService, ILexerService lexerService,
			@Symbol(SemedicoSymbolConstants.SUGGESTIONS_ACTIVATED) Boolean activated,
			@Symbol(SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME) String suggestionIndex,
			@Symbol(SemedicoSymbolConstants.SUGGESTIONS_FILTER_INDEX_TERMS) Boolean filterIndexTerms)
			throws MalformedURLException {
		this.log = logger;
		this.termService = termService;
		this.termOccurrenceFilterService = termOccurrenceFilterService;
		this.searchService = searchService;
		this.indexingService = indexingService;
		this.facetService = facetService;
		this.lexerService = lexerService;
		this.activated = activated;
		this.suggestionIndexName = suggestionIndex;
		this.filterIndexTerms = filterIndexTerms;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.suggestions.ITermSuggestionService#
	 * getSuggestionsForFragment (java.lang.String, java.util.List)
	 */
	@Override
	public List<FacetTermSuggestionStream> getSuggestionsForFragment(String userInput, List<Facet> facets) {
		String termFragment = userInput.trim();
		try {
			List<FacetTermSuggestionStream> suggestions = new ArrayList<>();
			if (activated) {
				Collection<? extends FacetTermSuggestionStream> defaultSuggestions = getDefaultSuggestions(termFragment,
						facets);
				LegacySemedicoSearchResult suggestionResult = (LegacySemedicoSearchResult) searchService.doSuggestionSearch(termFragment, facets).get();
				// did we actually get a response, i.e. is the server
				// accessible?
				if (null != suggestionResult && null != suggestionResult.suggestions) {
					if (!suggestionResult.suggestions.isEmpty()) {
						suggestions.addAll(suggestionResult.suggestions);
					} else {
						if (termFragment.contains(" ")) {
							// successively remove leading tokens in an attempt
							// to give suggestions for the most recent typed
							// fragment
							int beginIndex = 1;
							String[] split = termFragment.split("\\s+");
							while (suggestionResult.suggestions.isEmpty() && beginIndex < split.length) {
								StringBuilder suffixBuilder = new StringBuilder();
								for (int i = beginIndex; i < split.length; ++i) {
									suffixBuilder.append(split[i]);
									if (i < split.length - 1)
										suffixBuilder.append(" ");
								}
								String suffix = suffixBuilder.toString();
								suggestionResult = ((LegacySemedicoSearchResult)searchService.doSuggestionSearch(suffix, facets).get());
								for (FacetTermSuggestionStream stream : suggestionResult.suggestions)
									stream.setBegin(userInput.length() - suffix.length());
								++beginIndex;
							}
							suggestions.addAll(suggestionResult.suggestions);
						}
					}
				}
				suggestions.addAll(defaultSuggestions);
				// this is currently the easy way to say
				// "boolean operators - our only default suggestions - are
				// stopwords anyway and cannot be searched for as keywords".
				// Might get more complicated when we have more default
				// suggestions.
				if (defaultSuggestions.isEmpty())
					suggestions.add(getKeywordSuggestion(termFragment));
			} else {
				log.debug("Suggestions are deactivated.");
			}

			return suggestions;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	private FacetTermSuggestionStream getKeywordSuggestion(String termFragment) {
		FacetTermSuggestionStream keywordSuggestion = new FacetTermSuggestionStream(Facet.KEYWORD_FACET);
		keywordSuggestion.addTermSuggestion(termFragment, termFragment, termFragment, null, null,
				Facet.KEYWORD_FACET.getName(), null, QueryTokenizerImpl.ALPHANUM, TokenType.KEYWORD);
		return keywordSuggestion;
	}

	private Collection<? extends FacetTermSuggestionStream> getDefaultSuggestions(String termFragment,
			List<Facet> facets) {
		List<FacetTermSuggestionStream> defaultSuggestions = new ArrayList<>();
		boolean filterFacets = null != facets && !facets.isEmpty();
		// Check one-word core suggestions like boolean operators
		if (!termFragment.contains(" ")) {
			String lowerCaseFragment = termFragment.toLowerCase();
			String name = null;
			int type = 0;
			if ("or".startsWith(lowerCaseFragment)) {
				name = NodeType.OR.name();
				type = QueryTokenizerImpl.OR_OPERATOR;
			}
			if ("and".startsWith(lowerCaseFragment)) {
				name = NodeType.AND.name();
				type = QueryTokenizerImpl.AND_OPERATOR;
			}
			if ("not".startsWith(lowerCaseFragment)) {
				name = NodeType.NOT.name();
				type = QueryTokenizerImpl.NOT_OPERATOR;
			}
			if ("(".equals(lowerCaseFragment)) {
				name = "(";
				type = QueryTokenizerImpl.LEFT_PARENTHESIS;
			}
			if (")".equals(lowerCaseFragment)) {
				name = ")";
				type = QueryTokenizerImpl.RIGHT_PARENTHESIS;
			}

			if (null != name && (!filterFacets || facets.contains(Facet.BOOLEAN_OPERATORS_FACET))) {
				FacetTermSuggestionStream booleanSuggestion = new FacetTermSuggestionStream(
						Facet.BOOLEAN_OPERATORS_FACET);
				booleanSuggestion.addTermSuggestion(name, name, name, null, null,
						Facet.BOOLEAN_OPERATORS_FACET.getName(), null, type, ITokenInputService.TokenType.LEXER);
				defaultSuggestions.add(booleanSuggestion);
			}
		}
		return defaultSuggestions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.suggestions.ITermSuggestionService#
	 * createSuggestionIndex (java.lang.String)
	 */
	@Override
	public void createSuggestionIndex() {
		log.info("Clearing suggestion index...");
//		indexingService.clearIndex(IIndexInformationService.Indexes.suggestions);
//		indexingService.clearIndex(IIndexInformationService.Indexes.suggestionsCompletion);
		indexingService.clearIndex(suggestionIndexName);

		log.info("Suggestion index creation started...");

		// addSuggestionsForAuthors();
		// addSuggestionsForIndexFieldValues(facetService
		// .getFacetByLabel(FacetLabels.Unique.JOURNALS));
		// addSuggestionsForIndexFieldValues(facetService
		// .getFacetByLabel(FacetLabels.Unique.YEARS));
		addSuggestionsForDatabaseTermsCompletionStrategy();
		// addSuggestionsForDatabaseTermsSearchStrategy();

		log.info("Committing changes and optimizing suggestion index...");
		indexingService.commit(suggestionIndexName);
//		indexingService.commit(IIndexInformationService.Indexes.suggestionsCompletion);
		log.info("Creation of suggestion index complete.");
	}

	/**
	 * @throws IOException
	 * @throws SolrServerException
	 * 
	 */
	private void addSuggestionsForAuthors() {
		final Iterator<byte[][]> canonicalAuthorNames = termService.getCanonicalAuthorNames();
		final Facet authorFacet = facetService.getAuthorFacet();
		log.info("Creating suggestions for authors...");

		List<byte[][]> batch = new ArrayList<byte[][]>(BATCH_SIZE_SOLR_IMPORT);
		while (canonicalAuthorNames.hasNext()) {
			for (int i = 0; i < BATCH_SIZE_SOLR_IMPORT && canonicalAuthorNames.hasNext(); i++) {
				batch.add(canonicalAuthorNames.next());
			}

			MemoryOneIterator it = new MemoryOneIterator(batch.iterator(), authorFacet);

			while (it.hasNext()) {
				try {
					// while (it.hasNext()) {
					// SolrInputDocument next = it.next();
					// suggSolr.add(next);
					// }
					// suggSolr.add(it);
					indexingService.indexDocuments(IIndexInformationService.Indexes.suggestions, suggestionItemType,
							it);
					log.info("Authors batch checkpoint ({} author name suggestions added)", BATCH_SIZE_SOLR_IMPORT);
				} catch (RuntimeException e) {
					log.error("Last: " + it.getLast());
					throw e;
				}
			}
			batch.clear();
		}

	}

	/**
	 * This was originally the only method to insert suggestions into the
	 * suggestion index. But when some errors occurred, it was not possible to
	 * see which terms caused errors. As was clear the problem was with author
	 * names, they got their own Iterator, the MemoryOneIterator. This iterator
	 * remembers the last value to be printed out in the case of an error.<br/>
	 * This should be unified, of course.
	 * 
	 * @param facet
	 * @return
	 */
	private void addSuggestionsForIndexFieldValues(final Facet facet) {
		log.info("Adding suggestions for string terms in Medline field source for facet {}.", facet);

		final boolean isAuthorFacet = facet.isAnyAuthorFacet();

		List<String> termsInFacetField = null;
		try {
			termsInFacetField = ((LegacySemedicoSearchResult)searchService.doRetrieveFacetIndexTerms(Lists.newArrayList(facet))
					.get()).facetIndexTerms;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		log.info("Got {} string terms for suggestion indexing.", termsInFacetField.size());
		final Iterator<String> results = termsInFacetField.iterator();
		Iterator<Map<String, Object>> it = new Iterator<Map<String, Object>>() {

			private Stack<String> waitingStringTerms = new Stack<String>();

			@Override
			public boolean hasNext() {
				return results.hasNext() && 0 == waitingStringTerms.size();
			}

			@Override
			public Map<String, Object> next() {
				String name = null;
				if (waitingStringTerms.size() == 0) {
					name = results.next();
				} else
					name = waitingStringTerms.pop();
				String stringTermId = termService.checkStringTermId(name, facet);
				Map<String, Object> solrDoc = new HashMap<String, Object>();
				solrDoc.put(TERM_ID, stringTermId);
				solrDoc.put(FACETS, Lists.newArrayList(facet.getId()));
				solrDoc.put(SUGGESTION_TEXT, name);
				// Authors should also be found <firstname> <lastname> wise
				// TODO This method shouldn't be used for authors anyway..?!
				if (isAuthorFacet) {
					solrDoc.put(SUGGESTION_TEXT, name.replaceAll(",", ""));
					String reverseName = name.substring(name.indexOf(",") + 1, name.length()).trim();
					reverseName += " " + name.substring(0, name.indexOf(",")).trim();
					waitingStringTerms.push(reverseName);
				}
				solrDoc.put(SORTING, name);

				// ---- End of quick & dirty
				return solrDoc;
			}

			@Override
			public void remove() {
				throw new NotImplementedException();
			}

		};
		indexingService.indexDocuments(IIndexInformationService.Indexes.suggestions, suggestionItemType, it);
	}

	/**
	 * @param terms
	 * @return
	 * @throws SQLException
	 */
	protected void addSuggestionsForDatabaseTermsCompletionStrategy() {
		log.warn(
				"All terms in the database are pushed into the PENDING_FOR_SUGGESTIONS set; this must be removed as soon as there is a kind of delta update mechanism for suggestion writing variants.");
		long pushedTerms = termService.pushAllTermsIntoSuggestionQueue();
		log.info("Pushed {} terms into suggestion queue.", pushedTerms);

		final boolean indexAll = !filterIndexTerms;
		log.info("Adding suggestions for terms in the database.");

		// This function is used in the for-loop to get the facet ID rather than
		// the output of Facet.toString().
		final Function<Facet, String> facet2IdFunction = new Function<Facet, String>() {
			@Override
			public String apply(Facet facet) {
				return facet.getId();
			}
		};

		final Iterator<IConcept> termIt = termService.getTermsInSuggestionQueue();

		if (!termIt.hasNext()) {
			log.info("There are no terms pending in the suggestion queue.");
			return;
		}

		try {
			List<String> termIdsInIndexList = Collections.emptyList();
			final Set<String> termIdsInIndex = new HashSet<>();
			if (!indexAll) {
				log.info(
						"Retrieving index terms for the {} suggestion-facets to filter terms to be indexed for suggestions.",
						facetService.getSuggestionFacets().size());
				termIdsInIndexList = ((LegacySemedicoSearchResult)searchService.doRetrieveFacetIndexTerms(facetService.getSuggestionFacets())
						.get()).facetIndexTerms;
				termIdsInIndex.addAll(termIdsInIndexList);
				log.info("Retrieved {} terms in suggestion facet fields in the index.", termIdsInIndex.size());
			}

			if (!indexAll && termIdsInIndex.size() == 0) {
				log.info("No terms found in the index, no suggestions will be created.");
				return;
			}

			Iterator<Map<String, Object>> solrDocIt = new Iterator<Map<String, Object>>() {

				private Stack<String> currentSuggestions = new Stack<String>();
				private IConcept currentTerm;

				@Override
				public boolean hasNext() {
					if (currentSuggestions.size() > 0)
						return true;
					do {
						IConcept nextTerm = null;
						if (null == nextTerm && termIt.hasNext())
							nextTerm = termIt.next();
						if (null == nextTerm)
							return false;

						boolean termOccuresInIndex = indexAll;
						// An aggregate counts as "occurs in index" if any of
						// its elements does.
						// if (!indexAll && nextTerm.isAggregate()) {
						// AggregateTerm aggregate = (AggregateTerm) nextTerm;
						// List<Concept> elements = aggregate.getElements();
						// if (elements.isEmpty())
						// throw new IllegalStateException(
						// "The aggregate with ID " + aggregate.getId() + " does
						// not have any elements.");
						// for (int i = 0; i < elements.size(); i++) {
						// Concept term = elements.get(i);
						// if (termIdsInIndex.contains(term.getId()))
						// termOccuresInIndex = true;
						// }
						// } else {
						if (!indexAll)
							termOccuresInIndex = termIdsInIndex.contains(nextTerm.getId());
						// }

						// nextTerm = termIt.next();
						if (termOccuresInIndex) {
							currentTerm = nextTerm;
							// It is possible that a valid term does not produce
							// valid suggestions because they are all filtered
							// out.
							// In this case, the loop continues to the next term
							// because there are still no suggestions.
							Collection<String> filteredTermOccurrences = termOccurrenceFilterService
									.filterTermOccurrences(currentTerm, currentTerm.getOccurrences());
							currentSuggestions.addAll(filteredTermOccurrences);
						}
					} while (currentSuggestions.size() == 0 && termIt.hasNext());
					return currentSuggestions.size() > 0;
				}

				@Override
				public Map<String, Object> next() {
					if (!hasNext())
						return null;

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

					Map<String, Object> docMap = new HashMap<>();
					// We always want to offer the preferred name as well as its
					// synonyms, except when one of those is the actual
					// suggestion
					// text. The synonymSelectPredicate filters out the
					// suggestion
					// text. In case the current suggestion is just a writing
					// variant, all names (others than writing variants) will be
					// shown.
					List<String> allNames = new ArrayList<>(currentTerm.getSynonyms().size() + 1);
					allNames.addAll(currentTerm.getSynonyms());
					allNames.add(currentTerm.getDisplayName());
					if (currentTerm.getDisplayName() != currentTerm.getPreferredName())
						allNames.add(currentTerm.getPreferredName());

					Map<String, Object> suggestionMap = new HashMap<>();
					Map<String, Object> suggestionContextMap = new HashMap<>();
					suggestionMap.put(ITermSuggestionService.SUGGESTION_TEXT.INPUT, Lists.newArrayList(suggestion));
					suggestionMap.put(ITermSuggestionService.SUGGESTION_TEXT.WEIGHT, 100 / suggestion.length());
					suggestionContextMap.put(ITermSuggestionService.SUGGESTION_TEXT.INPUT,
							Lists.newArrayList(suggestion));
					suggestionContextMap.put(ITermSuggestionService.SUGGESTION_TEXT.WEIGHT, 100 / suggestion.length());

					Map<String, Object> payload = new HashMap<>();
					// payload.put(TERM_SYNONYMS,
					// StringUtils.join(Collections2.filter(allNames,
					// synonymSelectPredicate), ", "));
					List<String> synonyms = new ArrayList<>(Collections2.filter(allNames, synonymSelectPredicate));
					Collections.sort(synonyms);
					payload.put(TERM_SYNONYMS, synonyms);
					payload.put(TERM_ID, currentTerm.getId());
					payload.put(TERM_PREF_NAME, currentTerm.getPreferredName());
					payload.put(FACETS, Collections2.transform(currentTerm.getFacets(), facet2IdFunction));
					suggestionMap.put("payload", payload);
					suggestionContextMap.put("payload", payload);

					Map<String, Object> context = new HashMap<>();
					context.put("facetContext", Collections2.transform(currentTerm.getFacets(), facet2IdFunction));
					// don't put it in the non-context map!
					suggestionContextMap.put("context", context);

					docMap.put(SUGGESTION_TEXT, suggestionMap);
					docMap.put("suggestionTextContext", suggestionContextMap);
					docMap.put(FACETS, Collections2.transform(currentTerm.getFacets(), facet2IdFunction));
					docMap.put("uid", currentTerm.getId() + "_" + suggestion);

					return docMap;
				}

				@Override
				public void remove() {
					throw new NotImplementedException();
				}

			};
			indexingService.indexDocuments(suggestionIndexName, suggestionItemType,
					solrDocIt);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param terms
	 * @return
	 * @throws SQLException
	 */
	protected void addSuggestionsForDatabaseTermsSearchStrategy() {
		log.warn(
				"All terms in the database are pushed into the PENDING_FOR_SUGGESTIONS set; this must be removed as soon as there is a kind of delta update mechanism for suggestion writing variants.");
		long pushedTerms = termService.pushAllTermsIntoSuggestionQueue();
		log.info("Pushed {} terms into suggestion queue.", pushedTerms);

		final boolean indexAll = !filterIndexTerms;
		if (indexAll)
			log.info("No filtering for index terms is performed.");
		log.info("Adding suggestions for terms in the database.");

		// This function is used in the for-loop to get the facet ID rather than
		// the output of Facet.toString().
		final Function<Facet, String> facet2IdFunction = new Function<Facet, String>() {
			@Override
			public String apply(Facet facet) {
				return facet.getId();
			}
		};

		final Iterator<IConcept> termIt = termService.getTermsInSuggestionQueue();

		if (!termIt.hasNext()) {
			log.info("There are no terms pending in the suggestion queue.");
			return;
		}

		try {
			List<String> termIdsInIndexList = Collections.emptyList();
			final Set<String> termIdsInIndex = new HashSet<>();
			if (!indexAll) {
				log.info(
						"Retrieving index terms for the {} suggestion-facets to filter terms to be indexed for suggestions.",
						facetService.getSuggestionFacets().size());
				termIdsInIndexList = ((LegacySemedicoSearchResult)searchService.doRetrieveFacetIndexTerms(facetService.getSuggestionFacets())
						.get()).facetIndexTerms;
				termIdsInIndex.addAll(termIdsInIndexList);
				log.info("Retrieved {} terms in suggestion facet fields in the index.", termIdsInIndex.size());
			}

			if (!indexAll && termIdsInIndex.size() == 0) {
				log.info("No terms found in the index, no suggestions will be created.");
				return;
			}

			Iterator<Map<String, Object>> solrDocIt = new Iterator<Map<String, Object>>() {

				private Stack<String> currentSuggestions = new Stack<String>();
				private IConcept currentTerm;

				@Override
				public boolean hasNext() {
					if (currentSuggestions.size() > 0)
						return true;
					do {
						IConcept nextTerm = null;
						while ((null == nextTerm || nextTerm.getFacets().isEmpty()) && termIt.hasNext())
							nextTerm = termIt.next();
						if (null == nextTerm)
							return false;

						boolean termOccuresInIndex = indexAll;
						// An aggregate counts as "occurs in index" if any of
						// its elements does.
						// DEPRECATED: We now directly store the aggregates in
						// the index because we currently don't use facets that
						// should closely resemble the original resource but
						// just a flat list of concepts where we want to avoid
						// duplicates
						// if (!indexAll && nextTerm.isAggregate()) {
						// AggregateTerm aggregate = (AggregateTerm) nextTerm;
						// List<Concept> elements = aggregate.getElements();
						// if (elements.isEmpty())
						// throw new IllegalStateException(
						// "The aggregate with ID " + aggregate.getId() + " does
						// not have any elements.");
						// for (int i = 0; i < elements.size(); i++) {
						// Concept term = elements.get(i);
						// if (termIdsInIndex.contains(term.getId()))
						// termOccuresInIndex = true;
						// }
						// } else {
						if (!indexAll)
							termOccuresInIndex = termIdsInIndex.contains(nextTerm.getId());
						// }

						if (termOccuresInIndex) {
							currentTerm = nextTerm;
							// It is possible that a valid term does not produce
							// valid suggestions because they are all filtered
							// out.
							// In this case, the loop continues to the next term
							// because there are still no suggestions.
							List<String> termOccurrences = new ArrayList<>();
							termOccurrences.add(currentTerm.getPreferredName());
							termOccurrences.addAll(currentTerm.getOccurrences());
							Collection<String> filteredTermOccurrences = termOccurrenceFilterService
									.filterTermOccurrences(currentTerm, termOccurrences);
							currentSuggestions.addAll(filteredTermOccurrences);
						}
					} while (currentSuggestions.size() == 0 && termIt.hasNext());
					return currentSuggestions.size() > 0;
				}

				@Override
				public Map<String, Object> next() {
					if (!hasNext())
						return null;

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

					Map<String, Object> docMap = new HashMap<>();
					docMap.put(TERM_ID, currentTerm.getId());
					docMap.put(FACETS, Collections2.transform(currentTerm.getFacets(), facet2IdFunction));
					// We always want to offer the preferred name as well as its
					// synonyms, except when one of those is the actual
					// suggestion
					// text. The synonymSelectPredicate filters out the
					// suggestion
					// text. In case the current suggestion is just a writing
					// variant, all names (others than writing variants) will be
					// shown.
					List<String> allNames = new ArrayList<>(currentTerm.getSynonyms().size());
					allNames.add(currentTerm.getPreferredName());
					allNames.addAll(currentTerm.getSynonyms());
					allNames = termOccurrenceFilterService.filterTermOccurrences(currentTerm, allNames);
					Collections.sort(allNames);
					// we rather need some kind of "qualifier" we can use to
					// display with every synonym
					// allNames.add(currentTerm.getDisplayName());
					// if (currentTerm.getDisplayName() !=
					// currentTerm.getPreferredName())
					// allNames.add(currentTerm.getPreferredName());
					docMap.put(TERM_SYNONYMS,
							StringUtils.join(Collections2.filter(allNames, synonymSelectPredicate), ", "));
					docMap.put(SUGGESTION_TEXT, suggestion);
					if (null != currentTerm.getQualifiers() && currentTerm.getQualifiers().length != 0) {
						docMap.put("qualifiers", currentTerm.getQualifiers());
					}
					docMap.put("length", suggestion.length());
					int tokentype = QueryTokenizerImpl.ALPHANUM;
					// TODO can most probably go away
					// switch (currentTerm.getEventType()) {
					// case BINARY:
					// tokentype = QueryTokenizerImpl.BINARY_EVENT;
					// break;
					// case BOTH:
					// tokentype = QueryTokenizerImpl.UNARY_OR_BINARY_EVENT;
					// break;
					// case UNARY:
					// tokentype = QueryTokenizerImpl.UNARY_EVENT;
					// break;
					// case NONE:
					// // no event
					// }
					docMap.put(ITokenInputService.LEXER_TYPE, tokentype);
					docMap.put("_id",
							(currentTerm.isAggregate() ? "aggregate_" : "") + currentTerm.getId() + "_" + suggestion);

					return docMap;
				}

				@Override
				public void remove() {
					throw new NotImplementedException();
				}

			};
			indexingService.indexDocuments(IIndexInformationService.Indexes.suggestions, suggestionItemType, solrDocIt);
			indexingService.commit(IIndexInformationService.Indexes.suggestions);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	private class MemoryOneIterator implements Iterator<Map<String, Object>> {

		private final Iterator<byte[][]> source;
		private final Facet facet;
		private boolean isAuthorFacet;
		private Map<String, Object> last;

		public MemoryOneIterator(Iterator<byte[][]> source, Facet facet) {
			this.source = source;
			this.facet = facet;
			this.isAuthorFacet = facet.isAnyAuthorFacet();
		}

		private Stack<String> waitingStringTerms = new Stack<String>();

		@Override
		public boolean hasNext() {
			return source.hasNext();
		}

		@Override
		public Map<String, Object> next() {
			String name;
			if (waitingStringTerms.size() == 0) {
				byte[][] can = source.next();
				name = new String(can[0], Charset.forName("UTF-8"));
			} else
				name = waitingStringTerms.pop();
			String stringTermId = termService.checkStringTermId(name, facet);
			Map<String, Object> indexingDoc = new HashMap<String, Object>();
			// SolrInputDocument solrDoc = new SolrInputDocument();
			last = indexingDoc;
			// last = solrDoc;
			indexingDoc.put(TERM_ID, stringTermId);
			indexingDoc.put(FACETS, Lists.newArrayList(facet.getId()));
			indexingDoc.put(SUGGESTION_TEXT, name);
			// Authors should also be found <firstname> <lastname> wise
			if (isAuthorFacet && name.contains(",")) {
				int cIndex = name.indexOf(",");
				String reverseName = name.substring(cIndex + 1, name.length()).trim();
				reverseName += " " + name.substring(0, cIndex).trim();
				waitingStringTerms.push(reverseName);
			}
			indexingDoc.put(SORTING, name);
			return indexingDoc;
		}

		@Override
		public void remove() {
			throw new NotImplementedException();
		}

		public Map<String, Object> getLast() {
			return last;
		}
	}

}
