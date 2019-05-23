package de.julielab.semedico.core.search.components;

import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_ID;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_PREF_NAME;
import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.TERM_SYNONYMS;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;


import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerResponse;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

public class SuggestionProcessComponent extends AbstractSearchComponent {

	private Logger log;
	private IFacetService facetService;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SuggestionProcess {
		//
	}

	public SuggestionProcessComponent(Logger log, IFacetService facetService) {
		this.log = log;
		this.facetService = facetService;
	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		return processSuggestionCompletionResponse((SemedicoSearchCarrier)searchCarrier);
	}

	private boolean processSuggestionCompletionResponse(SemedicoSearchCarrier searchCarrier) {
		List<ISearchServerResponse> serverResponses = searchCarrier.serverResponses;

		if (null == serverResponses) {
			throw new IllegalStateException("This component requires search server responses, but non were found.");
		}
		ArrayList<FacetTermSuggestionStream> resultList = new ArrayList<>();

		for (int i = 0; i < serverResponses.size(); i++) {
			// The facet list determines the order of the command list (see
			// suggestion preparation component) which in turn determines the
			// order of the response list. Thus, facets and responses should be
			// parallel.
			ISearchServerResponse serverRsp = serverResponses.get(i);
			Facet facet = null;
			List<Facet> facets = searchCarrier.getSearchCommand().getSuggestionsCommand().facets;
			if (facets != null && !facets.isEmpty())
				facet = facets.get(i);

			if (serverRsp.getNumSuggestions() == 0) {
				log.debug("No suggestions returned");
				continue;
			}
			List<ISearchServerDocument> docs = serverRsp.getSuggestionResults();

			FacetTermSuggestionStream facetHit = new FacetTermSuggestionStream(facet);
			resultList.add(facetHit);

			Set<String> occurredTermIds = new HashSet<>();
			for (int j = 0; j < docs.size() && occurredTermIds.size() < 50; j++) {

				ISearchServerDocument doc = docs.get(j);

				Map<String, Object> source = doc.getFieldPayload();
				String termId = (String) source.get(TERM_ID);

				if (occurredTermIds.contains(termId))
					continue;
				occurredTermIds.add(termId);

				// The value returned first seems to be the one closer
				// to the search. Actually this is kind of a quick hack
				// because we wanted to index author names like 'Kim, A'
				// AND 'Kim A' so that you wouldn't have to type the
				// comma every time.
				String termName = doc.get("text");
				String preferredName = (String) source.get(TERM_PREF_NAME);
				List<String> termSynonyms = (List<String>) source.get(TERM_SYNONYMS);
				String facetName = null;
				String shortFacetName = null;
				List<Object> facetIds = (List<Object>) source.get(ITermSuggestionService.Fields.FACETS);
				if (null != facetIds && !facetIds.isEmpty()) {
					Facet conceptFacet = facetService.getFacetById(facetIds.get(0).toString());
					facetName = conceptFacet.getName();
					shortFacetName = conceptFacet.getShortName();
				}
				facetHit.addTermSuggestion(termId, termName, preferredName, termSynonyms,
						Collections.<String> emptyList(), facetName, shortFacetName, "", TokenType.CONCEPT);
			}
		}
		
		LegacySemedicoSearchResult result = new LegacySemedicoSearchResult(searchCarrier.getSearchCommand().getSemedicoQuery());
		result.suggestions = resultList;
		searchCarrier.setResult(result);
		
		return false;
	}
}
