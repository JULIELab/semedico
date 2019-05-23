package de.julielab.semedico.core;

public class TermLabels {

	/**
	 * Labels to represent a meta information about a term, e.g. whether it has already been processed for suggestions
	 * or the query dictionary.
	 * 
	 * @author faessler
	 * 
	 */
	public enum GeneralLabel implements Labels {
		TERM, PENDING_FOR_SUGGESTIONS, PENDING_FOR_QUERY_DICTIONARY, NO_SUGGESTIONS, DO_NOT_USE_FOR_QUERY_DICTIONARY, HOLLOW, EVENT_TERM,
		/**
		 * Label for terms that represent one or more terms as being mapped to each other. Terms for which no mapping is
		 * defined are labeled themselves with this label. This is done by the aggregation algorithm in the Neo4j server
		 * TermManager plugin (that calls the TermAggregateBuilder which is also contained in the plugin).
		 */
		MAPPING_AGGREGATE, AGGREGATE
	}

}
