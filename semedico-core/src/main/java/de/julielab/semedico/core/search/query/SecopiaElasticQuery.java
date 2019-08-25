package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.SecopiaParse;

import java.util.List;
import java.util.stream.Stream;

public class SecopiaElasticQuery extends AbstractSemedicoElasticQuery<SecopiaParse> {

    private final  SecopiaParse parse;

    public SecopiaElasticQuery(SecopiaParse parse, String index, SearchStrategy searchStrategy) {
        super(index, searchStrategy);
        this.parse = parse;
    }

    public SecopiaElasticQuery(SecopiaParse parse, String index, SearchStrategy searchStrategy, List<SemedicoIndexField> searchedFields) {
        super(index, searchStrategy, searchedFields);
        this.parse = parse;
    }

    public SecopiaElasticQuery(SecopiaParse parse, String index, SearchStrategy searchStrategy, List<SemedicoIndexField> searchedFields, List<String> requestedFields) {
        super(index, searchStrategy, searchedFields, requestedFields);
        this.parse = parse;
    }

    public SecopiaElasticQuery(SecopiaParse parse, String index, SearchStrategy searchStrategy, SemedicoIndexField... searchedFields) {
        super(index, searchStrategy, searchedFields);
        this.parse = parse;
    }

    public SecopiaElasticQuery(SecopiaParse parse, String index, SearchStrategy searchStrategy, AggregationRequest... aggregationRequests) {
        super(index, searchStrategy, aggregationRequests);
        this.parse = parse;
    }

    public SecopiaElasticQuery(SecopiaParse parse, String index, SearchStrategy searchStrategy, Stream<SemedicoIndexField> searchedFields, Stream<AggregationRequest> aggregationRequests) {
        super(index, searchStrategy, searchedFields, aggregationRequests);
        this.parse = parse;
    }

    @Override
    public SecopiaParse getQuery() {
        return parse;
    }


}
