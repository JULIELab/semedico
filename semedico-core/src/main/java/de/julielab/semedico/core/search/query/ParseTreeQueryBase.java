package de.julielab.semedico.core.search.query;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.search.ServerType;

/**
 * A basic query implementation using a {@link ParseTree} as query
 * representation and may be set an index and index types. Only the searchScopes of the
 * query must be given by extending classes.
 *
 * @author faessler
 */
public class ParseTreeQueryBase extends AbstractSemedicoElasticQuery {

    protected ParseTree query;

    public ParseTreeQueryBase(String index, Set<SearchScope> searchTask) {
        super(index, searchTask);
    }

    public ParseTreeQueryBase(ParseTree query, String index, Set<SearchScope> searchTask) {
        super(index, searchTask);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, Set<SearchScope> searchTask, AggregationRequest... aggregationRequests) {
        super(index, searchTask, aggregationRequests);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, Set<SearchScope> searchTask, Collection<String> requestedFields) {
        super(index, searchTask, requestedFields);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, Set<SearchScope> searchTask, Stream<String> requestedFields,
                              Stream<AggregationRequest> aggregationRequests) {
        super(index, searchTask, requestedFields, aggregationRequests);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, Set<SearchScope> searchTask, String... requestedFields) {
        super(index, searchTask, requestedFields);
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

    @Override
    public ServerType getServerType() {
        // At the moment, we only use parse tree queries for ElasticSearch. That might change, then the return type
        // must be made variable.
        return ServerType.ELASTIC_SEARCH;
    }
}
