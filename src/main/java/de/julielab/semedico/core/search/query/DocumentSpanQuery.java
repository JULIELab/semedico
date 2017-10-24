package de.julielab.semedico.core.search.query;

import java.util.Collection;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.translation.SearchTask;

public abstract class DocumentSpanQuery extends ParseTreeQueryBase {

	public DocumentSpanQuery(ParseTree query, SearchTask searchTask) {
		super(query, searchTask);
	}

	public DocumentSpanQuery(ParseTree query, SearchTask searchTask, AggregationRequest... aggregationRequests) {
		super(query, searchTask, aggregationRequests);
	}

	public DocumentSpanQuery(ParseTree query, SearchTask searchTask, Collection<String> requestedFields) {
		super(query, searchTask, requestedFields);
	}

	public DocumentSpanQuery(ParseTree query, SearchTask searchTask, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		super(query, searchTask, requestedFields, aggregationRequests);
	}

	public DocumentSpanQuery(ParseTree query, SearchTask searchTask, String... requestedFields) {
		super(query, searchTask, requestedFields);
	}

	public DocumentSpanQuery(SearchTask searchTask) {
		super(searchTask);
	}

	/**
	 * This method enables the default highlighting of the respective query. For
	 * example, the sentence query activates the sentence text highlighting, the
	 * relation query activates argument, type and epistemic modality hints
	 * highlighting etc.
	 */
	public abstract void enableHighlighting();

}
