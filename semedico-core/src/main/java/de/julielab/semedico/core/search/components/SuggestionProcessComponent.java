package de.julielab.semedico.core.search.components;

import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_ID;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_PREF_NAME;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_SYNONYMS;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.searchresponse.IElasticServerResponse;
import org.slf4j.Logger;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.StringSearch;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationResult;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.ITopHitsAggregationResult;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

public class SuggestionProcessComponent extends AbstractSearchComponent {

	private IFacetService facetService;
	private IRuleBasedCollatorWrapper collatorWrapper;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SuggestionProcess {
		//
	}

	public SuggestionProcessComponent(Logger log, IFacetService facetService,
			IRuleBasedCollatorWrapper collatorWrapper) {
		super(log);
		this.facetService = facetService;
		this.collatorWrapper = collatorWrapper;
	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		// return processSuggestionNgramSearchResponse(searchCarrier);
		return processSuggestionCompletionResponse((SemedicoESSearchCarrier)searchCarrier);
	}

	private boolean processSuggestionNgramSearchResponse(SemedicoESSearchCarrier searchCarrier) {
		List<IElasticServerResponse> serverResponses = searchCarrier.getSearchResponses();

		if (null == serverResponses)
			throw new IllegalStateException("This component requires search server responses, but non were found.");

		ArrayList<FacetTermSuggestionStream> resultList = new ArrayList<FacetTermSuggestionStream>();
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
		final String fWord = searchCarrier.searchCmd.suggCmd.fragment;
		Comparator<ISearchServerDocument> comparator = new Comparator<ISearchServerDocument>() {

			private Collator generalCollator = getGeneralCollator();

			@Override
			public int compare(ISearchServerDocument arg0, ISearchServerDocument arg1) {
				// String value0 = (String)
				// arg0.getFieldValues(SUGGESTION_TEXT).iterator().next();
				// String value1 = (String)
				// arg1.getFieldValues(SUGGESTION_TEXT).iterator().next();
				String value0 = (String) arg0.getFieldValue(SUGGESTION_TEXT).get();
				String value1 = (String) arg1.getFieldValue(SUGGESTION_TEXT).get();
				// Since this is a character-wise comparison, it actually might
				// happen that we have suggestions which do not start with the
				// exact user input. Example: User input "sue", suggestion
				// "Sühnel".
				int startsWith1 = value1.toLowerCase().startsWith(fWord) ? 1 : 0;
				int startsWith0 = value0.toLowerCase().startsWith(fWord) ? 1 : 0;
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

		StringCharacterIterator suggestionTextIt = new StringCharacterIterator("dummy");
		StringSearch search = new StringSearch("dummy", suggestionTextIt, collatorWrapper.getCollator());

		for (int i = 0; i < serverResponses.size(); i++) {
			// The facet list determines the order of the command list (see
			// suggestion preparation component) which in turn determines the
			// order of the response list. Thus, facets and responses should be
			// parallel.
			IElasticServerResponse serverRsp = serverResponses.get(i);
			SearchServerRequest serverCmd = searchCarrier.getServerRequest(i);
			AggregationRequest suggestionsAggCmd = serverCmd.aggregationRequests
					.get(SuggestionPreparationComponent.TOP_AGG);

			ITermsAggregationResult suggestionAggResult = (ITermsAggregationResult) serverRsp
					.getAggregationResult(suggestionsAggCmd);
			if (null == suggestionAggResult || null == suggestionAggResult.getAggregationUnits()) {
				log.debug("No suggestions returned for server response nr. {}", i);
				continue;
			}

			for (ITermsAggregationUnit unit : suggestionAggResult.getAggregationUnits()) {
				String facetId = (String) unit.getTerm();
				Facet facet = facetService.getFacetById(facetId);

				if (null == facet) {
					log.debug(
							"Suggested facet with ID {} was not found, possibly because it is inactive. Skipping respective suggestions.",
							facetId);
					continue;
				}

				FacetTermSuggestionStream stream = new FacetTermSuggestionStream(facet);
				resultList.add(stream);
				ITermsAggregationResult termIdAggResult = (ITermsAggregationResult) unit
						.getSubaggregationResult(SuggestionPreparationComponent.TERM_ID_TERMS_AGG);
				for (ITermsAggregationUnit termIdUnit : termIdAggResult.getAggregationUnits()) {

					ITopHitsAggregationResult topSuggestions = (ITopHitsAggregationResult) termIdUnit
							.getSubaggregationResult(SuggestionPreparationComponent.SUGGESTIONS_BY_FACET_AGG);
					List<ISearchServerDocument> suggestionsForFacet = topSuggestions.getTopHits();
					// Collections.sort(suggestionsForFacet, comparator);

					// For duplicate filtering.
					Set<String> occurredTermIds = new HashSet<String>();
					for (int j = 0; j < suggestionsForFacet.size() && occurredTermIds.size() < 50; j++) {

						ISearchServerDocument doc = suggestionsForFacet.get(j);

						String termId = (String) doc.get(TERM_ID).get();

						// if (occurredTermIds.contains(termId))
						// continue;
						occurredTermIds.add(termId);

						// The value returned first seems to be the one closer
						// to the search. Actually this is kind of a quick hack
						// because we wanted to index author names like 'Kim, A'
						// AND 'Kim A' so that you wouldn't have to type the
						// comma every time.
						Collection<Object> qualifiers = doc.getFieldValues(ITermSuggestionService.Fields.QUALIFIERS).get();
						// Set<String> matchedQualifiers = new HashSet<>();
						// if (null != qualifiers) {
						// String[] suggFragSplit =
						// searchCarrier.searchCmd.suggCmd.fragment.split(" +");
						// for (int k = 0; k < suggFragSplit.length; k++) {
						// String suggestionToken = suggFragSplit[k];
						// search.setPattern(suggestionToken);
						// for (Object o : qualifiers) {
						// String qualifier = (String) o;
						// search.reset();
						// suggestionTextIt.setText(qualifier);
						// search.setTarget(suggestionTextIt);
						// if (search.first() != StringSearch.DONE &&
						// !matchedQualifiers.contains(qualifier))
						// matchedQualifiers.add(qualifier);
						// }
						// }
						// }

						String termName = (String) doc.getFieldValue(SUGGESTION_TEXT).get();
						// for (String qualifier : matchedQualifiers)
						// termName += ", " + qualifier;
						@SuppressWarnings("unchecked")
						List<String> termSynonyms =  (List<String>) doc.getFieldValue(TERM_SYNONYMS).get();
						Integer lexerType = (Integer) doc.getFieldValue(ITokenInputService.LEXER_TYPE).get();

						List<String> qualifierSet = new ArrayList<>();
						if (null != qualifiers) {
							for (Object qualifier : qualifiers)
								qualifierSet.add((String) qualifier);
						}
						// facetHit.addTermSuggestion(termId, termName,
						// termSynonyms, matchedQualifiers);

						stream.addTermSuggestion(termId, termName, "", termSynonyms, qualifierSet, "", null, lexerType,
								TokenType.CONCEPT);
					}
				}
			}
		}
		// TODO adapt
//		searchCarrier.result = new LegacySemedicoSearchResult(searchCarrier.searchCmd.semedicoQuery);
//		((LegacySemedicoSearchResult)searchCarrier.result).suggestions = resultList;
		return false;
	}

	private boolean processSuggestionCompletionResponse(SemedicoESSearchCarrier searchCarrier) {
		List<IElasticServerResponse> serverResponses = searchCarrier.getSearchResponses();

		if (null == serverResponses)
			throw new IllegalStateException("This component requires search server responses, but non were found.");

		ArrayList<FacetTermSuggestionStream> resultList = new ArrayList<FacetTermSuggestionStream>();
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
//		final String fWord = searchCarrier.searchCmd.suggCmd.fragment;
		// since we currently don't have suggestions for authors and the
		// comparator may, when multiple suggestions for the same term have been
		// retrieved, prefer the longer variant, we just leave it out
//		Comparator<ISearchServerDocument> comparator = new Comparator<ISearchServerDocument>() {
//
//			private Collator generalCollator = getGeneralCollator();
//
//			@Override
//			public int compare(ISearchServerDocument arg0, ISearchServerDocument arg1) {
//				String value0 = (String) arg0.getFieldValue(SUGGESTION_TEXT);
//				String value1 = (String) arg1.getFieldValue(SUGGESTION_TEXT);
//				// Since this is a character-wise comparison, it actually might
//				// happen that we have suggestions which do not start with the
//				// exact user input. Example: User input "sue", suggestion
//				// "Sühnel".
//				int startsWith1 = value1.toLowerCase().startsWith(fWord) ? 1 : 0;
//				int startsWith0 = value0.toLowerCase().startsWith(fWord) ? 1 : 0;
//				int charDifference = startsWith1 - startsWith0;
//				// When one suggestion is a perfect match to the user input and
//				// the other is not,
//				// prefer it in the ranking.
//				if (charDifference != 0)
//					return charDifference;
//				// Otherwise, just return normal sorting by a GENERAL collator,
//				return generalCollator.compare(value0, value1);
//			}
//
//			private Collator getGeneralCollator() {
//				Collator generalCollator = Collator.getInstance();
//				return generalCollator;
//			}
//
//		};

		for (int i = 0; i < serverResponses.size(); i++) {
			// The facet list determines the order of the command list (see
			// suggestion preparation component) which in turn determines the
			// order of the response list. Thus, facets and responses should be
			// parallel.
			IElasticServerResponse serverRsp = serverResponses.get(i);
			Facet facet = null;
			if (searchCarrier.searchCmd.suggCmd.facets != null && searchCarrier.searchCmd.suggCmd.facets.size() > 0)
				facet = searchCarrier.searchCmd.suggCmd.facets.get(i);

			if (serverRsp.getNumSuggestions() == 0) {
				log.debug("No suggestions returned");
				continue;
			}
			List<ISearchServerDocument> docs = serverRsp.getSuggestionResults();
			// Collections.sort(docs, comparator);

			FacetTermSuggestionStream facetHit = new FacetTermSuggestionStream(facet);
			resultList.add(facetHit);

			Set<String> occurredTermIds = new HashSet<String>();
			for (int j = 0; j < docs.size() && occurredTermIds.size() < 50; j++) {

				ISearchServerDocument doc = docs.get(j);

				String termId = (String) doc.getFieldPayload(TERM_ID).get();

				if (occurredTermIds.contains(termId))
					continue;
				occurredTermIds.add(termId);

				// The value returned first seems to be the one closer
				// to the search. Actually this is kind of a quick hack
				// because we wanted to index author names like 'Kim, A'
				// AND 'Kim A' so that you wouldn't have to type the
				// comma every time.
				String termName = (String) doc.get("text").get();
				String preferredName = (String) doc.getFieldPayload(TERM_PREF_NAME).get();
				@SuppressWarnings("unchecked")
				List<String> termSynonyms = (List<String>) doc.getFieldPayload(TERM_SYNONYMS).get();
				String facetName = null;
				String shortFacetName = null;
				List<Object> facetIds = (List<Object>) doc.getFieldPayload(ITermSuggestionService.Fields.FACETS).get();
				if (null != facetIds && !facetIds.isEmpty()) {
					Facet conceptFacet = facetService.getFacetById(facetIds.get(0).toString());
					facetName = conceptFacet.getName();
					shortFacetName = conceptFacet.getShortName();
				}
				facetHit.addTermSuggestion(termId, termName, preferredName, termSynonyms,
						Collections.<String> emptyList(), facetName, shortFacetName, 0, TokenType.CONCEPT);
			}
		}
		// TODO adapt
//		searchCarrier.result = new LegacySemedicoSearchResult(searchCarrier.searchCmd.semedicoQuery);
//		((LegacySemedicoSearchResult)searchCarrier.result).suggestions = resultList;
		return false;
	}
}
