package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.support.IncludeExclude;
//import org.elasticsearch.search.aggregations.BucketOrder;
//import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.max.MaxAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder.Field;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.HighlightCommand.HlField;
import de.julielab.scicopia.core.elasticsearch.legacy.IFacetField.FacetType;
import de.julielab.scicopia.core.elasticsearch.legacy.AggregationCommand.OrderCommand;

public class ElasticSearchComponent extends AbstractSearchComponent implements ISearchServerComponent {

	// The following highlighting-defaults are taken from
	// http://www.elasticsearch.org/guide/reference/api/search/highlighting/
	// Default size of highlighting fragments
	private static final int DEFAULT_FRAGSIZE = 100;
	private static final int DEFAULT_NUMBER_FRAGS = 5;

	private static final String DEFAULT_SCRIPT_LANG = "painless";
	private Logger log;
	private Client client;

	public ElasticSearchComponent(Logger log, ISearchClientProvider searchClientProvider) {
		this.log = log;
		client = searchClientProvider.getSearchClient().getClient();
	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		StopWatch w = new StopWatch();
		w.start();
		List<SearchServerCommand> serverCmds = searchCarrier.getSearchServerCommands();
		if (null == serverCmds)
			throw new IllegalArgumentException("A " + SearchServerCommand.class.getName()
					+ " is required for an ElasticSearch search, but none is present.");

		// It could be that the search component occurs multiple times in a
		// search chain. But then, the last response(s) should have been
		// consumed by now.
		searchCarrier.serverResponses.clear();

		// One "Semedico search" may result in multiple search server commands,
		// e.g. suggestions where for each facet suggestions are searched or for
		// B-terms where there are multiple search nodes.
		// We should just take care that the results are ordered in a parallel
		// way to the server commands, see at the end of the method.
		List<SearchRequestBuilder> searchRequestBuilders = new ArrayList<>(serverCmds.size());
		List<SearchRequestBuilder> suggestionBuilders = new ArrayList<>(serverCmds.size());
		log.debug("Number of search server commands: {}", serverCmds.size());
		for (int i = 0; i < serverCmds.size(); i++) {
			log.debug("Configuration ElasticSearch query for server command {}", i);

			SearchServerCommand serverCmd = serverCmds.get(i);

			if (null != serverCmd.query) {
				handleSearchRequest(searchRequestBuilders, serverCmd);
			}
			if (null != serverCmd.suggestionText) {
				handleSuggestionRequest(suggestionBuilders, serverCmd);
			}

		}

		// Send the query to the server
		if (!searchRequestBuilders.isEmpty()) {
			MultiSearchRequestBuilder multiSearch = client.prepareMultiSearch();
			for (SearchRequestBuilder srb : searchRequestBuilders)
				multiSearch.add(srb);
			MultiSearchResponse multiSearchResponse = multiSearch.execute().actionGet();
			Item[] responses = multiSearchResponse.getResponses();
			for (int i = 0; i < responses.length; i++) {
				Item item = responses[i];
				List<FacetCommand> facetCmds = serverCmds.get(i).facetCmds;
				SearchResponse response = item.getResponse();

				log.trace("Response from ElasticSearch: {}", response);

				ElasticSearchServerResponse serverRsp = new ElasticSearchServerResponse(log, response, facetCmds);
				searchCarrier.addSearchServerResponse(serverRsp);

				if (null == response) {
					serverRsp.setQueryError(QueryError.NO_RESPONSE);
				}
			}
		}
		if (!suggestionBuilders.isEmpty()) {
			for (SearchRequestBuilder suggestBuilder : suggestionBuilders) {
				SearchResponse suggestResponse = suggestBuilder.execute().actionGet();
				searchCarrier.addSearchServerResponse(new ElasticSearchServerResponse(suggestResponse));
			}
		}
		w.stop();
		log.debug("ElasticSearch process took {}ms ({}s)", w.getTime(), w.getTime() / 1000);

		return false;
	}

	protected void handleSuggestionRequest(List<SearchRequestBuilder> suggestBuilders, SearchServerCommand serverCmd) {
        SuggestBuilder suggestBuilder = new SuggestBuilder().addSuggestion("",
                SuggestBuilders.completionSuggestion(serverCmd.suggestionField).text(serverCmd.suggestionText));
        SearchRequestBuilder suggestionRequestBuilder = client.prepareSearch(serverCmd.index).suggest(suggestBuilder);

        suggestBuilders.add(suggestionRequestBuilder);
        if (log.isDebugEnabled())
            log.debug("Suggesting on index {}. Created search query \"{}\".", serverCmd.index,
            		suggestBuilder);
	}

	protected void handleSearchRequest(List<SearchRequestBuilder> searchRequestBuilders,
			SearchServerCommand serverCmd) {
		if (null == serverCmd.getFieldsToReturn()) {
			serverCmd.addField("*");
		}
		if (serverCmd.index == null) {
			throw new IllegalArgumentException("The search command does not define an index to search on.");
		}
		
		SearchRequestBuilder srb = client.prepareSearch(serverCmd.index);
		if (serverCmd.indexTypes != null && !serverCmd.indexTypes.isEmpty()) {
			srb.setTypes(serverCmd.indexTypes.toArray(new String[serverCmd.indexTypes.size()]));
		}
		
		srb.setFetchSource(serverCmd.fetchSource);

		QueryBuilder queryBuilder = serverCmd.query;
		srb.setQuery(queryBuilder);

		if (null != serverCmd.getFieldsToReturn()) {
	        for (String field : serverCmd.getFieldsToReturn()) {
	        	srb.addStoredField(field);
	        }
		}
		
		srb.setFrom(serverCmd.start);
		if (serverCmd.rows >= 0) {
			srb.setSize(serverCmd.rows);
		} else {
			srb.setSize(0);
		}
		if (null != serverCmd.aggregationCmds) {
			for (AggregationCommand aggCmd : serverCmd.aggregationCmds.values()) {
				log.debug("Adding top aggregation command {} to query.", aggCmd.name);
				AbstractAggregationBuilder aggregationBuilder = buildAggregation(aggCmd);
				srb.addAggregation(aggregationBuilder);
			}
		}

		log.debug("Number of facet commands: {}", serverCmd.facetCmds != null ? serverCmd.facetCmds.size() : 0);
		if (null != serverCmd.facetCmds) {
			for (FacetCommand fc : serverCmd.facetCmds) {
				String field = fc.getField();
				if (null == field) {
					throw new IllegalArgumentException("FacetCommand without fields to facet on occurred.");
				}
				TermsAggregationBuilder fb = configureFacets(fc, FacetType.count);
				srb.addAggregation(fb);
			}
		}
		
		if (null != serverCmd.hlCmds && !serverCmd.hlCmds.isEmpty()) {
            HighlightBuilder hb = new HighlightBuilder();
            srb.highlighter(hb);
            for (int j = 0; j < serverCmd.hlCmds.size(); j++) {
            	HighlightCommand hlc = serverCmd.hlCmds.get(j);
				for (HlField hlField : hlc.fields) {
					Field field = new Field(hlField.field);
					int fragsize = DEFAULT_FRAGSIZE;
					int fragnum = DEFAULT_NUMBER_FRAGS;
					if (hlField.type != null)
						field.highlighterType(hlField.type);
					if (!hlField.requirefieldmatch)
						field.requireFieldMatch(false);
					if (hlField.fragsize != Integer.MIN_VALUE)
						fragsize = hlField.fragsize;
					if (hlField.fragnum != Integer.MIN_VALUE)
						fragnum = hlField.fragnum;
					if (hlField.noMatchSize != Integer.MIN_VALUE)
						field.noMatchSize(hlField.noMatchSize);
					field.fragmentSize(fragsize);
					field.numOfFragments(fragnum);
					if (null != hlField.highlightQuery) {
						field.highlightQuery(hlField.highlightQuery);
					}
					if (null != hlField.pre) {
						field.preTags(hlField.pre);
					}
					if (null != hlField.post) {
						field.postTags(hlField.post);
					}
					hb.field(field);
				}
			}
		}
		
		if (null != serverCmd.sortCmds) {
			for (SortCommand sortCmd : serverCmd.sortCmds) {
				srb.addSort(sortCmd.field, sortCmd.order);
			}
		}

		searchRequestBuilders.add(srb);

		log.debug("Searching on index {}. Created search query \"{}\".", serverCmd.index, srb);
	}

	protected AbstractAggregationBuilder<?> buildAggregation(AggregationCommand aggCmd) {
		if (TermsAggregation.class.equals(aggCmd.getClass())) {
			TermsAggregation termsAgg = (TermsAggregation) aggCmd;
			TermsAggregationBuilder termsBuilder = AggregationBuilders.terms(termsAgg.name).field(termsAgg.field);
//			List<BucketOrder> compoundOrder = new ArrayList<>();
			List<Terms.Order> compoundOrder = new ArrayList<>();
			for (OrderCommand orderCmd : termsAgg.order) {
//				BucketOrder order = null;
				Terms.Order order = null;
				boolean ascending = false;
				if (null != orderCmd && null != orderCmd.sortOrder)
					ascending = orderCmd.sortOrder == OrderCommand.SortOrder.ASCENDING;
				if (null != orderCmd) {
					switch (orderCmd.referenceType) {
					case AGGREGATION_MULTIVALUE:
//						order = BucketOrder.aggregation(orderCmd.referenceName, orderCmd.metric.name(), ascending);
						order = Terms.Order.aggregation(orderCmd.referenceName, orderCmd.metric.name(), ascending);
						break;
					case AGGREGATION_SINGLE_VALUE:
//						order = BucketOrder.aggregation(orderCmd.referenceName, ascending);
						order = Terms.Order.aggregation(orderCmd.referenceName, ascending);
						break;
					case COUNT:
						order = Terms.Order.count(ascending);
						break;
					case TERM:
						order = Terms.Order.term(ascending);
						break;
					}
					if (null != order)
						compoundOrder.add(order);
				}
			}
			if (!compoundOrder.isEmpty()) {
//				termsBuilder.order(BucketOrder.compound(compoundOrder));
				termsBuilder.order(Terms.Order.compound(compoundOrder));
			}
			if (null != termsAgg.size) {
				termsBuilder.size(termsAgg.size);
			}

			// Add sub aggregations
			if (null != termsAgg.subaggregations) {
				for (AggregationCommand subAggCmd : termsAgg.subaggregations.values()) {
					termsBuilder.subAggregation(buildAggregation(subAggCmd));
				}
			}
			return termsBuilder;
		}
		if (MaxAggregation.class.equals(aggCmd.getClass())) {
			MaxAggregation maxAgg = (MaxAggregation) aggCmd;
			MaxAggregationBuilder maxBuilder = AggregationBuilders.max(maxAgg.name);
			if (null != maxAgg.field) {
				maxBuilder.field(maxAgg.field);
			}
			if (null != maxAgg.script) {
				maxBuilder.script(new Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, maxAgg.script, Collections.emptyMap()));
			}
			return maxBuilder;
		}
		if (SignificantTermsAggregation.class.equals(aggCmd.getClass())) {
			SignificantTermsAggregation sigAgg = (SignificantTermsAggregation) aggCmd;
			SignificantTermsAggregationBuilder esSigAgg = AggregationBuilders.significantTerms(sigAgg.name);
			esSigAgg.field(sigAgg.field);
			return esSigAgg;
		}
		log.error("Unhandled aggregation command class: {}", aggCmd.getClass());
		return null;
	}

//	private TermsAggregationBuilder configureFacets(FacetCommand fc, FacetType facetType) {
	private TermsAggregationBuilder configureFacets(FacetCommand fc, FacetType facetType) {
		String field = fc.getField();
//		TermsAggregationBuilder tb = AggregationBuilders.terms(fc.name + facetType).field(field)
		TermsAggregationBuilder tb = AggregationBuilders.terms(fc.name + facetType).field(field)
				.size(fc.limit >= 0 ? fc.limit : Integer.MAX_VALUE);
		if (null != fc.sort) {
//			BucketOrder order;
			Terms.Order order;
			switch (fc.sort) {
			case COUNT:
//				order = BucketOrder.count(false);
				order = Terms.Order.count(false);
				break;
			case TERM:
//				order = BucketOrder.key(true);
				order = Terms.Order.term(true);
				break;
			case REVERSE_COUNT:
//				order = BucketOrder.count(true);
				order = Terms.Order.count(true);
				break;
			case REVERSE_TERM:
//				order = BucketOrder.key(false);
				order = Terms.Order.term(false);
				break;
			default:
				throw new IllegalArgumentException("Unknown facet term sort order: " + fc.sort.name());
			}
			tb.order(order);
		}
		// Which terms to count
		if (null != fc.terms && !fc.terms.isEmpty()) {
			tb.includeExclude(new IncludeExclude(fc.terms.toArray(new String[] {}), new String[]{}));
		}
		return tb;
	}

}
