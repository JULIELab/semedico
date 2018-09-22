package de.julielab.semedico.core.search.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.Indices.Chunks;

/**
 * @deprecated All field-based queries will rather be restricted by index,
 * type and potentially a specific field value filter instead of creating
 * a class for each possible document part.
 */
public class ChunkQuery extends DocumentSpanQuery {

	public ChunkQuery(ParseTree query, String index) {
		super(query, index, SearchScope.CHUNKS);
		setRequestedFields(Collections.singletonList(Chunks.text));
		this.resultSize = 10;
	}
	
	public ChunkQuery(ParseTree query, String index, AggregationRequest... aggregationRequests) {
		this(query, index);
		putAggregationRequest(aggregationRequests);
	}

	public ChunkQuery(ParseTree query, String index, Collection<String> requestedFields) {
		this(query, index);
		this.requestedFields = requestedFields;
	}


	public ChunkQuery(ParseTree query, String index, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		this(query, index);
		this.requestedFields = requestedFields.collect(Collectors.toList());
		aggregationRequests.forEach(this::putAggregationRequest);
	}

	public ChunkQuery(ParseTree query, String index, String... requestedFields) {
		this(query, index);
		this.requestedFields = Arrays.asList(requestedFields);
	}


	@Override
	public void enableHighlighting() {
		throw new NotImplementedException();
	}

}
