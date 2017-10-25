package de.julielab.semedico.core.search.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.translation.SearchTask;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.Indexes.Sentences;

public class SentenceQuery extends DocumentSpanQuery {

	public SentenceQuery(ParseTree query) {
		super(query, SearchTask.SENTENCES);
		setRequestedFields(Arrays.asList(Sentences.text, Sentences.likelihood));
		setIndex(IIndexInformationService.Indexes.Sentences.name);
	}
	
	public SentenceQuery(ParseTree query, AggregationRequest... aggregationRequests) {
		this(query);
		setRequestedFields(Arrays.asList(Sentences.text, Sentences.likelihood));
		putAggregationRequest(aggregationRequests);
	}

	public SentenceQuery(ParseTree query, Collection<String> requestedFields) {
		this(query);
		this.requestedFields = requestedFields;
	}


	public SentenceQuery(ParseTree query, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		this(query);
		this.requestedFields = requestedFields.collect(Collectors.toList());
		aggregationRequests.forEach(this::putAggregationRequest);
	}

	public SentenceQuery(ParseTree query, String... requestedFields) {
		this(query);
		this.requestedFields = Arrays.asList(requestedFields);
	}


	public SentenceQuery() {
		super(SearchTask.SENTENCES);
		setRequestedFields(Arrays.asList(Sentences.text, Sentences.likelihood));
		setIndex(IIndexInformationService.Indexes.Sentences.name);
	}



	@Override
	public void enableHighlighting() {
		throw new NotImplementedException();
	}

}
