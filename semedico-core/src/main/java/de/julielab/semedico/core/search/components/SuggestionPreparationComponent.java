package de.julielab.semedico.core.search.components;

import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.MaxAggregation;
import de.julielab.scicopia.core.elasticsearch.legacy.TermsAggregation;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

public class SuggestionPreparationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SuggestionPreparation {
		//
	}

	/**
	 * The top aggregation for the suggestions, grouping suggestions by facet ID.
	 */
	public static final String TOP_AGG = "suggestionsTopAggregation";
	/**
	 * The {@link TermsAggregation} specifying to group by facet ID.
	 */
	public static final String FACET_ID_TERMS_AGG = "facetIdTerms";
	/**
	 * The {@link TermsAggregation} specifying to group by term ID. This is only used to avoid multiple suggestions for the same term.
	 */
	public static final String TERM_ID_TERMS_AGG = "facetIdTerms";
//	/**
//	 * The {@link TopHitsAggregation}, retrieving the top scored suggestions per facet.
//	 */
	public static final String SUGGESTIONS_BY_FACET_AGG = "suggestionsByFacet";
	/**
	 * The {@link MaxAggregation}, used to sort the {@link #TERM_ID_TERMS_AGG} aggregation by the score of their top
	 * scored suggestion.
	 */
	public static final String MAX_TERM_SCORE_AGG = "maxTermSuggestionScore";
	/**
	 * The {@link MaxAggregation}, used to sort the {@link #FACET_ID_TERMS_AGG} aggregation by the score of their top
	 * scored suggestion.
	 */
	public static final String MAX_FACET_SCORE_AGG = "maxFacetSuggestionScore";

	private Logger log;
	private String suggestionIndexName;

	public SuggestionPreparationComponent(Logger log, @Symbol(SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME) String suggestionIndexName) {
		this.log = log;
		this.suggestionIndexName = suggestionIndexName;

	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		return configureForCompletionSuggestions(searchCarrier);
	}

	public boolean configureForCompletionSuggestions(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.getSearchCommand();
		String fragment = searchCmd.getSuggestionsCommand().fragment;
		List<Facet> facets = searchCmd.getSuggestionsCommand().facets;

		SearchServerCommand serverCmd = new SearchServerCommand();
		Multimap<String, String> facetCategories = HashMultimap.create();
		
		if (facets != null && !facets.isEmpty()) {
			for (Facet facet : facets) {
				facetCategories.put(ITermSuggestionService.Contexts.FACET_CONTEXT, facet.getId());
			}
			log.debug("Filtering suggestions for facets with IDs {}", facetCategories.get(ITermSuggestionService.Contexts.FACET_CONTEXT));
		}
		
		serverCmd.index = suggestionIndexName;
		serverCmd.rows = 10;
		serverCmd.suggestionText = fragment;
		if (null != facetCategories && !facetCategories.isEmpty()) {
			serverCmd.suggestionField = "suggestionTextContext";
		} else {
			serverCmd.suggestionField = SUGGESTION_TEXT;
		}
		serverCmd.suggestionCategories = facetCategories;

		// set a few settings we just don't need for suggestions
		serverCmd.setFieldsToReturn(null);
		semCarrier.addSearchServerCommand(serverCmd);
		return false;
	}

}
