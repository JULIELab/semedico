package de.julielab.semedico.core.search.query;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.SearchScope;

public abstract class DocumentSpanQuery extends ParseTreeQueryBase {

	public DocumentSpanQuery(String index, Set<SearchScope> searchTask) {
		super(index, searchTask);
	}
	
	public DocumentSpanQuery(ParseTree query, String index, Set<SearchScope> searchTask) {
		super(query, index, searchTask);
	}

	public DocumentSpanQuery(ParseTree query, String index, Set<SearchScope> searchTask,
			AggregationRequest... aggregationRequests) {
		super(query, index, searchTask, aggregationRequests);
	}

	public DocumentSpanQuery(ParseTree query, String index, Set<SearchScope> searchTask, Collection<String> requestedFields) {
		super(query, index, searchTask, requestedFields);
	}

	public DocumentSpanQuery(ParseTree query, String index, Set<SearchScope> searchTask, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		super(query, index, searchTask, requestedFields, aggregationRequests);
	}

	public DocumentSpanQuery(ParseTree query, String index, Set<SearchScope> searchTask, String... requestedFields) {
		super(query, index, searchTask, requestedFields);
	}
	
	public DocumentSpanQuery(ParseTree query, String index, SearchScope searchScope) {
		this(query, index, EnumSet.of(searchScope));
	}

	/**
	 * This method enables the default highlighting of the respective query. For
	 * example, the sentence query activates the sentence text highlighting, the
	 * relation query activates argument, type and epistemic modality hints
	 * highlighting etc.
	 */
	public abstract void enableHighlighting();

}
