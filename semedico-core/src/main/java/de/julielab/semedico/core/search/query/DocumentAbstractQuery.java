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
public class DocumentAbstractQuery extends DocumentSpanQuery {

	public DocumentAbstractQuery(ParseTree query) {
		super(query, Documents.name, SearchScope.DOC_ABSTRACTS);
		setRequestedFields(Arrays.asList(Documents.abstracttext));
		this.resultSize = 10;
	}
	
	public DocumentAbstractQuery(ParseTree query, AggregationRequest... aggregationRequests) {
		this(query);
		putAggregationRequest(aggregationRequests);
	}

	public DocumentAbstractQuery(ParseTree query, Collection<String> requestedFields) {
		this(query);
		this.requestedFields = requestedFields;
	}


	public DocumentAbstractQuery(ParseTree query, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		this(query);
		this.requestedFields = requestedFields.collect(Collectors.toList());
		aggregationRequests.forEach(this::putAggregationRequest);
	}

	public DocumentAbstractQuery(ParseTree query, String... requestedFields) {
		this(query);
		this.requestedFields = Arrays.asList(requestedFields);
	}


	@Override
	public void enableHighlighting() {
		throw new NotImplementedException();
	}

}
