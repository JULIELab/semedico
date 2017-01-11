package de.julielab.semedico.core.search.components;

import static de.julielab.semedico.core.suggestions.ITermSuggestionService.Fields.SUGGESTION_TEXT;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationCommand.OrderCommand;
import de.julielab.elastic.query.components.data.aggregation.MaxAggregation;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;
import de.julielab.elastic.query.components.data.aggregation.TopHitsAggregation;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor;
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery.FieldValueFactor.Modifier;
import de.julielab.elastic.query.components.data.query.MultiMatchQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
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
	/**
	 * The {@link TopHitsAggregation}, retrieving the top scored suggestions per facet.
	 */
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

	private int maxTokenLength = 15;
	private Logger log;
	private String suggestionIndexName;

	public SuggestionPreparationComponent(Logger log, @Symbol(SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME) String suggestionIndexName) {
		this.log = log;
		this.suggestionIndexName = suggestionIndexName;

	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
//		return configureForNgramSearchSuggestions(searchCarrier);
		return configureForCompletionSuggestions(searchCarrier);
	}

	private boolean configureForNgramSearchSuggestions(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		String fragment = searchCmd.suggCmd.fragment;
		SearchServerCommand serverCmd = new SearchServerCommand();

		BoolClause facetQuery = null;
		if (searchCmd.suggCmd.facets != null && searchCmd.suggCmd.facets.size() > 0) {
			facetQuery = new BoolClause();
			facetQuery.occur = BoolClause.Occur.MUST;
			for (Facet facet : searchCmd.suggCmd.facets) {
				TermQuery termQuery = new TermQuery();
				termQuery.term = facet.getId();
				termQuery.field = ITermSuggestionService.Fields.FACETS;
				facetQuery.addQuery(termQuery);
			}
			// log.debug("Filtering suggestions for facets with IDs {}", facetIds);
		}

		serverCmd.index = IIndexInformationService.Indexes.suggestions;
		// We don't directly return the search results. We get them via the aggregation, see below.
		serverCmd.rows = 0;
		serverCmd.suggestionField = SUGGESTION_TEXT;

		MultiMatchQuery suggestionQuery = new MultiMatchQuery();
		suggestionQuery.query = fragment;
		suggestionQuery.addField(ITermSuggestionService.Fields.SUGGESTION_TEXT);
		suggestionQuery.addField(ITermSuggestionService.Fields.QUALIFIERS);
		suggestionQuery.type = MultiMatchQuery.Type.cross_fields;
		BoolQuery completeQuery = new BoolQuery();
		if (null != facetQuery)
			completeQuery.addClause(facetQuery);
		BoolClause suggClause = new BoolClause();
		suggClause.occur = BoolClause.Occur.MUST;
		suggClause.addQuery(suggestionQuery);
		completeQuery.addClause(suggClause);
		
		FieldValueFactor fieldValueFactor = new FunctionScoreQuery.FieldValueFactor();
		fieldValueFactor.field = ITermSuggestionService.Fields.LENGTH;
		fieldValueFactor.modifier = Modifier.RECIPROCAL;
		FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery();
		functionScoreQuery.fieldValueFactor = fieldValueFactor;
		functionScoreQuery.query = completeQuery;
		serverCmd.query = functionScoreQuery;

		// We want to group aggregations by facets. To do this, we first get the top facets in the suggestions and for
		// each facet, get the top scoring suggestions.
		// ------ Subaggregations of the facet ID terms aggregation
		MaxAggregation maxTermSuggestionScoreAgg = new MaxAggregation();
		maxTermSuggestionScoreAgg.name = MAX_TERM_SCORE_AGG;
		// This 'script' just says to use the document match score for the aggregation.
		maxTermSuggestionScoreAgg.script = "_score";
		
		TopHitsAggregation topSuggestionHitsAgg = new TopHitsAggregation();
		topSuggestionHitsAgg.name = SUGGESTIONS_BY_FACET_AGG;
		// TODO magic number
		topSuggestionHitsAgg.size = 1;
		topSuggestionHitsAgg.addIncludeField("*");
		
		TermsAggregation termIdAgg = new TermsAggregation();
		termIdAgg.field = ITermSuggestionService.Fields.TERM_ID;
		termIdAgg.size = 3;
		termIdAgg.name = TERM_ID_TERMS_AGG;
		OrderCommand termOrder = new TermsAggregation.OrderCommand();
		termOrder.referenceType = OrderCommand.ReferenceType.AGGREGATION_SINGLE_VALUE;
		termOrder.referenceName = MAX_TERM_SCORE_AGG;
		termOrder.sortOrder = OrderCommand.SortOrder.DESCENDING;
		termIdAgg.addOrder(termOrder);
		termIdAgg.addSubaggregation(maxTermSuggestionScoreAgg);
		termIdAgg.addSubaggregation(topSuggestionHitsAgg);
		// ------- End facet ID terms sub aggregations
		MaxAggregation maxFacetSuggestionScoreAgg = new MaxAggregation();
		maxFacetSuggestionScoreAgg.name = MAX_FACET_SCORE_AGG;
		// This 'script' just says to use the document match score for the aggregation.
		maxFacetSuggestionScoreAgg.script = "_score";
		
		TermsAggregation facetIdAgg = new TermsAggregation();
		facetIdAgg.name = TOP_AGG;
		facetIdAgg.addSubaggregation(maxFacetSuggestionScoreAgg);
		facetIdAgg.addSubaggregation(termIdAgg);
		facetIdAgg.field = ITermSuggestionService.Fields.FACETS;
		// TODO magic number
		facetIdAgg.size = 5;
		// Sort the facet ID buckets by the maximum score of their respective suggestions. This way, the facet that
		// contains the best hit will be ranked first.
		OrderCommand facetOrder = new TermsAggregation.OrderCommand();
		facetOrder.referenceType = OrderCommand.ReferenceType.AGGREGATION_SINGLE_VALUE;
		facetOrder.referenceName = MAX_FACET_SCORE_AGG;
		facetOrder.sortOrder = OrderCommand.SortOrder.DESCENDING;
		facetIdAgg.addOrder(facetOrder);
		serverCmd.addAggregationCommand(facetIdAgg);

//		ScoringCommand scoringCmd = new ScoringCommand();
//		scoringCmd.weightField = ITermSuggestionService.Fields.LENGTH;
//		scoringCmd.weightFieldStrategy = ScoringCommand.FieldValueFactorModifier.RECIPROCAL;
//		serverCmd.scoringCommand = scoringCmd;

		// set a few settings we just don't need for suggestions
		semCarrier.addSearchServerCommand(serverCmd);
		// }
		return false;
	}

	public boolean configureForCompletionSuggestions(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		String fragment = searchCmd.suggCmd.fragment;
		// String escapedFragment = escapeForLucene(searchCmd.suggCmd.fragment);
		// String sortField = searchServerComponent.getScoreFieldName();
		// for (Facet facet : searchCmd.suggCmd.facets) {
		SearchServerCommand serverCmd = new SearchServerCommand();
		// String fragmentQuery = String.format("+%s:%s ", SUGGESTION_TEXT, escapedFragment.toLowerCase());
//		StringBuilder facetQuery = null;
//		List<String> facetIds = null;
		Multimap<String, String> facetCategories = HashMultimap.create();
		if (searchCmd.suggCmd.facets != null && searchCmd.suggCmd.facets.size() > 0) {
//			facetIds = new ArrayList<>();
			// facetQuery = new StringBuilder();
			// facetQuery.append("+(");
			for (Facet facet : searchCmd.suggCmd.facets) {
				facetCategories.put(ITermSuggestionService.Context.FACET_CONTEXT, facet.getId());
//				facetIds.add(facet.getId());
				// facetQuery.append(ITermSuggestionService.Fields.FACETS);
				// facetQuery.append(":");
				// facetQuery.append(facet.getId());
				// facetQuery.append(" ");
			}
			log.debug("Filtering suggestions for facets with IDs {}", facetCategories.get(ITermSuggestionService.Context.FACET_CONTEXT));
			// facetQuery.append(")");
		}
		// serverCmd.serverQuery = String.format("%s %s", fragmentQuery,
		// facetQuery != null ? facetQuery.toString() : "");
		// serverCmd.addSortCommand(sortField, SortOrder.DESCENDING);
		// serverCmd.addSortCommand(ITermSuggestionService.Fields.SORTING, SortOrder.ASCENDING);
		serverCmd.index = suggestionIndexName;
		serverCmd.rows = 10;
		serverCmd.suggestionText = fragment;
		if (null != facetCategories && facetCategories.size() > 0)
			serverCmd.suggestionField = "suggestionTextContext";
		else
			serverCmd.suggestionField = SUGGESTION_TEXT;
//		serverCmd.suggestionFacets = facetIds;
		serverCmd.suggestionCategories = facetCategories;

		// set a few settings we just don't need for suggestions
		serverCmd.fieldsToReturn = null;
		semCarrier.addSearchServerCommand(serverCmd);
		// }
		return false;
	}
	
	/**
	 * The Lucene/Solr parser will throw an error when a ot-quoted term is searched which begins with a minus sign.
	 * Additionally, when a whitespace occurs, we want to search for the fragment as a phrase rather then searching for
	 * multiple strings.
	 * */
	public String escapeForLucene(String termFragment) {
		String word = termFragment;
		// remove already existing quotation marks (especially important
		// when a longer phrase is cut by maxTokenLength)
		word = word.replaceAll("\"", "");
		if (word.length() > maxTokenLength)
			word = word.substring(0, maxTokenLength);

		if (word.startsWith("-") || word.indexOf(' ') > -1) {
			word = "\"" + word + "\"";
		}

		return word;
	}

}
