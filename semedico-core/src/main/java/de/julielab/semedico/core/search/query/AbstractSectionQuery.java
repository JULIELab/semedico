package de.julielab.semedico.core.search.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;

/**
 * @deprecated All field-based queries will rather be restricted by index,
 * type and potentially a specific field value filter instead of creating
 * a class for each possible document part.
 */
public class AbstractSectionQuery extends DocumentSpanQuery {

    private Set<String> searchFieldFilter;

    public AbstractSectionQuery(ParseTree query, String index) {
        super(query, index, SearchScope.ABSTRACT_SECTIONS);
        this.resultSize = 10;
    }

    public AbstractSectionQuery(ParseTree query, String index, Set<String> searchFieldFilter) {
        this(query, index);
        this.searchFieldFilter = searchFieldFilter;
    }

    public AbstractSectionQuery(ParseTree query, String index, AggregationRequest... aggregationRequests) {
        this(query, index, Collections.emptySet());
        putAggregationRequest(aggregationRequests);
    }

    public AbstractSectionQuery(ParseTree query, String index, Collection<String> requestedFields) {
        this(query, index, Collections.emptySet());
        this.requestedFields = requestedFields;
    }

    public AbstractSectionQuery(ParseTree query, String index, Stream<String> requestedFields,
                                Stream<AggregationRequest> aggregationRequests) {
        this(query, index, requestedFields.collect(Collectors.toList()));
        aggregationRequests.forEach(this::putAggregationRequest);
    }

    public AbstractSectionQuery(ParseTree query, String index, String... requestedFields) {
        this(query, index);
        this.requestedFields = Arrays.asList(requestedFields);
    }


    @Override
    public Set<String> getSearchedFields() {
        return searchFieldFilter;
    }

    @Override
    public void enableHighlighting() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "AbstractSectionQuery [searchFieldFilter=" + searchFieldFilter + ", query=" + query + ", index=" + index
                + ", searchScopes=" + searchScopes + "]";
    }


}
