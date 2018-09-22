package de.julielab.semedico.core.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.map.Flat3Map;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.search.services.SearchService.SearchOption;

public abstract class AbstractSemedicoElasticQuery implements IElasticQuery {

	protected String index;
	protected Collection<String> indexTypes;
	protected Set<SearchScope> searchScopes;
	protected SearchOption searchMode;
	protected int resultSize;
	protected Collection<String> requestedFields;
	protected Set<SearchOption> searchOptions;
	protected Map<String, AggregationRequest> aggregationRequests;

	public AbstractSemedicoElasticQuery(String index, Set<SearchScope> searchTask) {
		this.index = index;
		this.searchScopes = searchTask;
	}

	public AbstractSemedicoElasticQuery(String index, Set<SearchScope> searchTask, Collection<String> requestedFields) {
		this(index, searchTask);
		this.requestedFields = requestedFields;
	}

	public AbstractSemedicoElasticQuery(String index, Set<SearchScope> searchTask, String... requestedFields) {
		this(index, searchTask);
		this.requestedFields = Arrays.asList(requestedFields);
	}

	public AbstractSemedicoElasticQuery(String index, Set<SearchScope> searchTask, AggregationRequest... aggregationRequests) {
		this(index, searchTask);
		putAggregationRequest(aggregationRequests);
	}
	
	public AbstractSemedicoElasticQuery(String index, Set<SearchScope> searchTask, Stream<String> requestedFields, Stream<AggregationRequest> aggregationRequests) {
		this(index, searchTask);
		this.requestedFields = requestedFields.collect(Collectors.toList());
		aggregationRequests.forEach(this::putAggregationRequest);
	}

	@Override
	public Set<String> getSearchedFields() {
		return Collections.emptySet();
	}

	@Override
	public Collection<String> getIndexTypes() {
		return indexTypes;
	}

	@Override
	public void setIndexTypes(Collection<String> indexTypes) {
		this.indexTypes = indexTypes;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public String getIndex() {
		return index;
	}

	@Override
	public Set<SearchScope> getScopes() {
		return searchScopes;
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
	 * @param resultSize
	 *            The number of results fetched from the search server.
	 */
	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}

	public void setSearchOptions(Set<SearchOption> searchOptions) {
		this.searchOptions = searchOptions;
	}

	public Set<SearchOption> getSearchOptions() {
		return searchOptions;
	}

	@Override
	public Collection<String> getRequestedFields() {
		return requestedFields;
	}

	public void setRequestedFields(Collection<String> storedFields) {
		this.requestedFields = storedFields;
	}

	public void addRequestedFields(String... storedFields) {
		if (this.requestedFields == null)
			this.requestedFields = new ArrayList<>();
		for (int i = 0; i < storedFields.length; i++) {
			String storedField = storedFields[i];
			this.requestedFields.add(storedField);
		}
	}

	public void putAggregationRequest(AggregationRequest... requests) {
		if (null == aggregationRequests && requests.length <= 3)
			aggregationRequests = new Flat3Map<>();
		else if (null == aggregationRequests)
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

}
