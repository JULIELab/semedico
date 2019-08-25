package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
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
 * @deprecated We don't use {@link ParseTree} any more
 */
public class ParseTreeQueryBase extends AbstractSemedicoElasticQuery<ParseTree> {

    protected ParseTree query;

    /**
     * This is the constructor to create a template query that is used for broadcasting.
     * @param query
     */
    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy) {
        this(query,searchStrategy, "");
    }

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

    public ParseTreeQueryBase(String index, SearchStrategy searchStrategy) {
        super(index, searchStrategy);
    }

    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy, String index) {
        super(index, searchStrategy);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy, String index, AggregationRequest... aggregationRequests) {
        super(index, searchStrategy, aggregationRequests);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy, String index, List<SemedicoIndexField> searchedFields, List<String> requestedFields) {
        super(index, searchStrategy, searchedFields, requestedFields);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy, String index, List<SemedicoIndexField> searchedFields) {
        super(index, searchStrategy, searchedFields);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy, String index, Stream<SemedicoIndexField> searchedFields,
                              Stream<AggregationRequest> aggregationRequests) {
        super(index, searchStrategy, searchedFields, aggregationRequests);
        this.query = query;
    }

    public ParseTreeQueryBase(ParseTree query, SearchStrategy searchStrategy, String index, SemedicoIndexField... searchedFields) {
        super(index, searchStrategy, searchedFields);
        this.query = query;
    }


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
