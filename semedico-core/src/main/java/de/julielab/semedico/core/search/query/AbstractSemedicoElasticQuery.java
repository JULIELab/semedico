package de.julielab.semedico.core.search.query;

import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.search.ServerType;
import org.apache.commons.collections4.map.Flat3Map;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractSemedicoElasticQuery<Q> implements IElasticQuery<Q> {

    protected String index;
    protected SearchStrategy searchStrategy;
    protected int resultSize;
    protected List<SemedicoIndexField> searchedFields;
    protected List<String> requestedFields;
    protected Map<String, AggregationRequest> aggregationRequests;
    protected HighlightCommand hlCmd;
    protected ResultType resultType;

    public AbstractSemedicoElasticQuery(String index, SearchStrategy searchStrategy) {
        this.index = index;
        this.searchStrategy = searchStrategy;
        searchedFields = Collections.emptyList();
        requestedFields = Collections.emptyList();
        aggregationRequests = Collections.emptyMap();
        resultType = ResultType.UNSPECIFIED;

    }

    public AbstractSemedicoElasticQuery(String index, SearchStrategy searchStrategy, List<SemedicoIndexField> searchedFields) {
        this(index, searchStrategy);
        this.searchedFields = searchedFields;
    }

    public AbstractSemedicoElasticQuery(String index, SearchStrategy searchStrategy, List<SemedicoIndexField> searchedFields, List<String> requestedFields) {
        this(index, searchStrategy, searchedFields);
        this.requestedFields = requestedFields;
    }

    public AbstractSemedicoElasticQuery(String index, SearchStrategy searchStrategy, SemedicoIndexField... searchedFields) {
        this(index, searchStrategy, Arrays.asList(searchedFields));
    }

    public AbstractSemedicoElasticQuery(String index, SearchStrategy searchStrategy, AggregationRequest... aggregationRequests) {
        this(index, searchStrategy);
        putAggregationRequest(aggregationRequests);
    }

    public AbstractSemedicoElasticQuery(String index, SearchStrategy searchStrategy, Stream<SemedicoIndexField> searchedFields, Stream<AggregationRequest> aggregationRequests) {
        this(index, searchStrategy, searchedFields.collect(Collectors.toList()));
        aggregationRequests.forEach(this::putAggregationRequest);
    }

    @Override
    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    public HighlightCommand getHlCmd() {
        return hlCmd;
    }

    public void setHlCmd(HighlightCommand hlCmd) {
        this.hlCmd = hlCmd;
    }

    @Override
    public AbstractSemedicoElasticQuery clone() throws CloneNotSupportedException {
        AbstractSemedicoElasticQuery clone = (AbstractSemedicoElasticQuery) super.clone();
        clone.searchedFields = searchedFields.stream().collect(Collectors.toList());

        clone.requestedFields = requestedFields.stream().collect(Collectors.toList());
        clone.aggregationRequests = new HashMap<>(aggregationRequests.size());
        for (String key : aggregationRequests.keySet()) {
            clone.aggregationRequests.put(key, aggregationRequests.get(key).clone());
        }
        return clone;
    }

    @Override
    public List<SemedicoIndexField> getSearchedFields() {
        return searchedFields;
    }

    public void setSearchedFields(List<SemedicoIndexField> searchedFields) {
        this.searchedFields = searchedFields;
    }

    @Override
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * The maximum number of results that are actually fetched from the search
     * server. There might be more hits but only the top-resultSize hits are
     * loaded into Semedico.
     *
     * @return The number of results fetched from the search server.
     */
    public int getResultSize() {
        return resultSize;
    }

    /**
     * Sets the maximum number of results that are actually fetched from the
     * search server. There might be more hits but only the top-resultSize hits
     * are loaded into Semedico.
     *
     * @param resultSize The number of results fetched from the search server.
     */
    public void setResultSize(int resultSize) {
        this.resultSize = resultSize;
    }

    @Override
    public List<String> getRequestedFields() {
        return requestedFields;
    }

    public void setRequestedFields(List<String> requestedStoredFields) {
        this.requestedFields = requestedStoredFields;
    }

    public void addRequestedFields(String... storedFields) {
        if (requestedFields.isEmpty())
            this.requestedFields = new ArrayList<>();
        for (int i = 0; i < storedFields.length; i++) {
            String storedField = storedFields[i];
            this.requestedFields.add(storedField);
        }
    }

    public void putAggregationRequest(AggregationRequest... requests) {
        if (aggregationRequests.isEmpty() && requests.length <= 3)
            aggregationRequests = new Flat3Map<>();
        else if (aggregationRequests.isEmpty())
            aggregationRequests = new HashMap<>(requests.length);
        for (int i = 0; i < requests.length; i++) {
            AggregationRequest request = requests[i];
            aggregationRequests.put(request.name, request);
        }
    }

    @Override
    public Map<String, AggregationRequest> getAggregationRequests() {
        return aggregationRequests;
    }

    @Override
    public void setSearchStrategy(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.ELASTIC_SEARCH;
    }
}
