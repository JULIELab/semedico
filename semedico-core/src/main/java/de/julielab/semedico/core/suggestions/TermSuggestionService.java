/**
 * TermSuggestionService.java
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
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_ID;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_PREF_NAME;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_SYNONYMS;

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

import de.julielab.scicopia.core.elasticsearch.legacy.IIndexingService;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

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

	private final static String suggestionItemType = "items";

	private final ITermService termService;
	private final ITermOccurrenceFilterService termOccurrenceFilterService;

	private final Logger log;

	private final IFacetService facetService;

	private ISearchService searchService;

	private IIndexingService indexingService;

	private Boolean activated;

	private Boolean filterIndexTerms;

	private String suggestionIndexName;

	public TermSuggestionService(Logger logger, ITermService termService,
			ITermOccurrenceFilterService termOccurrenceFilterService, IFacetService facetService,
			ISearchService searchService, IIndexingService indexingService,
			@Symbol(SemedicoSymbolConstants.SUGGESTIONS_ACTIVATED) Boolean activated,
			@Symbol(SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME) String suggestionIndex,
			@Symbol(SemedicoSymbolConstants.SUGGESTIONS_FILTER_INDEX_TERMS) Boolean filterIndexTerms)
		{
		this.log = logger;
		this.termService = termService;
		this.termOccurrenceFilterService = termOccurrenceFilterService;
		this.searchService = searchService;
		this.indexingService = indexingService;
		this.facetService = facetService;
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
				Facet.KEYWORD_FACET.getName(), null, "ALPHANUM", TokenType.KEYWORD);
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
			String type = "";
			if ("or".startsWith(lowerCaseFragment)) {
				name = NodeType.OR.name();
				type = "OR";
			}
			if ("and".startsWith(lowerCaseFragment)) {
				name = NodeType.AND.name();
				type = "AND";
			}
			if ("not".startsWith(lowerCaseFragment)) {
				name = NodeType.NOT.name();
				type = "NOT";
			}
			if ("(".equals(lowerCaseFragment)) {
				name = "(";
				type = "LPAR";
			}
			if (")".equals(lowerCaseFragment)) {
				name = ")";
				type = "RPAR";
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

		indexingService.clearIndex(suggestionIndexName);

		log.info("Suggestion index creation started...");

		addSuggestionsForDatabaseTermsCompletionStrategy();

		log.info("Committing changes and optimizing suggestion index...");
		indexingService.commit(suggestionIndexName);
		log.info("Creation of suggestion index complete.");
	}

	/**
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

			if (!indexAll && termIdsInIndex.isEmpty()) {
				log.info("No terms found in the index, no suggestions will be created.");
				return;
			}

			Iterator<Map<String, Object>> solrDocIt = new Iterator<Map<String, Object>>() {

				private Stack<String> currentSuggestions = new Stack<>();
				private IConcept currentTerm;

				@Override
				public boolean hasNext() {
					if (!currentSuggestions.isEmpty()) {
						return true;
					}
					do {
						IConcept nextTerm = null;
						if (termIt.hasNext())
							nextTerm = termIt.next();
						if (null == nextTerm)
							return false;

						boolean termOccuresInIndex = indexAll;
						// An aggregate counts as "occurs in index" if any of
						// its elements does.
						if (!indexAll) {
							termOccuresInIndex = termIdsInIndex.contains(nextTerm.getId());
						}
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
					} while (currentSuggestions.isEmpty() && termIt.hasNext());
					return !currentSuggestions.isEmpty();
				}

				@Override
				public Map<String, Object> next() {
					if (!hasNext())
						return null;

					final String suggestion = currentSuggestions.pop();
					// This predicate is used below to select all synonym
					// strings which are not equal to the current suggestion itself.
					Predicate<String> synonymSelectPredicate = new Predicate<String>() {
						@Override
						public boolean apply(String synonym) {
							return !synonym.equals(suggestion);
						}
					};

					Map<String, Object> docMap = new HashMap<>();
					// We always want to offer the preferred name as well as its
					// synonyms, except when one of those is the actual suggestion
					// text. The synonymSelectPredicate filters out the suggestion
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

					List<String> synonyms = new ArrayList<>(Collections2.filter(allNames, synonymSelectPredicate));
					Collections.sort(synonyms);
					docMap.put(TERM_SYNONYMS, synonyms);
					docMap.put(TERM_ID, currentTerm.getId());
					docMap.put(TERM_PREF_NAME, currentTerm.getPreferredName());
					docMap.put(FACETS, Collections2.transform(currentTerm.getFacets(), facet2IdFunction));

					Map<String, Object> context = new HashMap<>();
					context.put("facetContext", Collections2.transform(currentTerm.getFacets(), facet2IdFunction));
					// don't put it in the non-context map!
					suggestionContextMap.put("contexts", context);

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
			List<String> termIdsInIndexList;
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

			if (!indexAll && termIdsInIndex.isEmpty()) {
				log.info("No terms found in the index, no suggestions will be created.");
				return;
			}

			Iterator<Map<String, Object>> solrDocIt = new Iterator<Map<String, Object>>() {

				private Stack<String> currentSuggestions = new Stack<>();
				private IConcept currentTerm;

				@Override
				public boolean hasNext() {
					if (!currentSuggestions.isEmpty()) {
						return true;
					}
					do {
						IConcept nextTerm = null;
						while ((null == nextTerm || nextTerm.getFacets().isEmpty()) && termIt.hasNext()) {
							nextTerm = termIt.next();
						}
						if (null == nextTerm) {
							return false;
						}
						boolean termOccuresInIndex = indexAll;

						if (!indexAll) {
							termOccuresInIndex = termIdsInIndex.contains(nextTerm.getId());
						}

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
					} while (currentSuggestions.isEmpty() && termIt.hasNext());
					return !currentSuggestions.isEmpty();
				}

				@Override
				public Map<String, Object> next() {
					if (!hasNext())
						return null;

					final String suggestion = currentSuggestions.pop();
					// This predicate is used below to select all synonym
					// strings which
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
					// synonyms, except when one of those is the actual suggestion
					// text. The synonymSelectPredicate filters out the suggestion
					// text. In case the current suggestion is just a writing
					// variant, all names (others than writing variants) will be
					// shown.
					List<String> allNames = new ArrayList<>(currentTerm.getSynonyms().size());
					allNames.add(currentTerm.getPreferredName());
					allNames.addAll(currentTerm.getSynonyms());
					allNames = termOccurrenceFilterService.filterTermOccurrences(currentTerm, allNames);
					Collections.sort(allNames);

					docMap.put(TERM_SYNONYMS,
							StringUtils.join(Collections2.filter(allNames, synonymSelectPredicate), ", "));
					docMap.put(SUGGESTION_TEXT, suggestion);
					if (null != currentTerm.getQualifiers() && currentTerm.getQualifiers().length != 0) {
						docMap.put("qualifiers", currentTerm.getQualifiers());
					}
					docMap.put("length", suggestion.length());
					String tokentype = "ALPHANUM";

					docMap.put(ITokenInputService.LEXER_TYPE, tokentype);
					docMap.put("_id", currentTerm.getId() + "_" + suggestion);

					return docMap;
				}

				@Override
				public void remove() {
					throw new NotImplementedException();
				}

			};
			indexingService.indexDocuments(IIndexInformationService.Indexes.SUGGESTIONS, suggestionItemType, solrDocIt);
			indexingService.commit(IIndexInformationService.Indexes.SUGGESTIONS);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

}
