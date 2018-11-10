package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.ServerType;

import java.util.List;
import java.util.stream.Stream;

/**
 * A basic query implementation using a {@link ParseTree} as query
 * representation and may be set an index and index types. Only the searchScopes of the
 * query must be given by extending classes.
 *
 * @author faessler
 */
public class ParseTreeQueryBase extends AbstractSemedicoElasticQuery {

    protected ParseTree query;

    /**
     * Note that the ParseTree is only shallow copied.
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public ParseTreeQueryBase clone() throws CloneNotSupportedException {
        // We refrain from cloning the ParseTree here because it is not implemented and not necessary for
        // the broadcasting which was the original usecase for queries being clonable
        return (ParseTreeQueryBase) super.clone();
    }

    public ParseTreeQueryBase(String index) {
        super(index);
    }

    public ParseTreeQueryBase(ParseTree query, String index) {
        super(index);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, AggregationRequest... aggregationRequests) {
        super(index, aggregationRequests);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, List<String> requestedFields) {
        super(index, requestedFields);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, Stream<String> requestedFields,
                              Stream<AggregationRequest> aggregationRequests) {
        super(index, requestedFields, aggregationRequests);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, String index, String... requestedFields) {
        super(index, requestedFields);
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
