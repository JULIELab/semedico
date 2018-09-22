package de.julielab.semedico.core.search.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.Indices.Documents;

/**
 * @deprecated All field-based queries will rather be restricted by index,
 * type and potentially a specific field value filter instead of creating
 * a class for each possible document part.
 */
public class DocumentTitleQuery extends DocumentSpanQuery {

	public DocumentTitleQuery(ParseTree query, String index) {
		super(query, index, SearchScope.DOC_TITLES);
		setRequestedFields(Arrays.asList(Documents.titletext, Documents.titlelikelihood));
		this.resultSize = 10;
	}
	
	public DocumentTitleQuery(ParseTree query, String index, AggregationRequest... aggregationRequests) {
		this(query, index);
		putAggregationRequest(aggregationRequests);
	}

	public DocumentTitleQuery(ParseTree query, String index, Collection<String> requestedFields) {
		this(query, index);
		this.requestedFields = requestedFields;
	}


	public DocumentTitleQuery(ParseTree query, String index, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		this(query, index);
		this.requestedFields = requestedFields.collect(Collectors.toList());
		aggregationRequests.forEach(this::putAggregationRequest);
	}

	public DocumentTitleQuery(ParseTree query, String index, String... requestedFields) {
		this(query, index);
		this.requestedFields = Arrays.asList(requestedFields);
	}


	@Override
	public void enableHighlighting() {
		throw new NotImplementedException();
	}

}
