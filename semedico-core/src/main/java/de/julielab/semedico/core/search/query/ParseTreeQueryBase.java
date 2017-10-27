package de.julielab.semedico.core.search.query;

import java.util.Collection;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.translation.SearchTask;

/**
 * A basic query implementation using a {@link ParseTree} as query
 * representation and may be set an index and index types. Only the task of the
 * query must be given by extending classes.
 * 
 * @author faessler
 *
 */
public abstract class ParseTreeQueryBase extends AbstractSemedicoQuery {

	protected ParseTree query;

	public ParseTreeQueryBase(SearchTask searchTask) {
		this(null, searchTask);
	}

	public ParseTreeQueryBase(ParseTree query, SearchTask searchTask) {
		super(searchTask);
		this.query = query;
	}

	public ParseTreeQueryBase(ParseTree query, SearchTask searchTask, AggregationRequest... aggregationRequests) {
		super(searchTask, aggregationRequests);
		this.query = query;
	}

	public ParseTreeQueryBase(ParseTree query, SearchTask searchTask, Collection<String> requestedFields) {
		super(searchTask, requestedFields);
		this.query = query;
	}

	public ParseTreeQueryBase(ParseTree query, SearchTask searchTask, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		super(searchTask, requestedFields, aggregationRequests);
		this.query = query;
	}

	public ParseTreeQueryBase(ParseTree query, SearchTask searchTask, String... requestedFields) {
		super(searchTask, requestedFields);
		this.query = query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ParseTree getQuery() {
		return query;
	}


	public void setQuery(ParseTree query) {
		assert query != null;
		this.query = query;

	}
	
}
