package de.julielab.semedico.core.search.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;

import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.translation.SearchTask;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.Indexes.Sentences;

public class SentenceQuery extends DocumentSpanQuery {

	public SentenceQuery(ParseTree query) {
		super(query, SearchTask.SENTENCES);
		setRequestedFields(Arrays.asList(Sentences.text, Sentences.likelihood));
	}
	
	public SentenceQuery(ParseTree query, AggregationRequest... aggregationRequests) {
		super(query, SearchTask.SENTENCES, aggregationRequests);
		setRequestedFields(Arrays.asList(Sentences.text, Sentences.likelihood));
	}

	public SentenceQuery(ParseTree query, Collection<String> requestedFields) {
		super(query, SearchTask.SENTENCES, requestedFields);
	}


	public SentenceQuery(ParseTree query, Stream<String> requestedFields,
			Stream<AggregationRequest> aggregationRequests) {
		super(query, SearchTask.SENTENCES, requestedFields, aggregationRequests);
	}

	public SentenceQuery(ParseTree query, String... requestedFields) {
		super(query, SearchTask.SENTENCES, requestedFields);
	}


	public SentenceQuery() {
		super(SearchTask.SENTENCES);
		setRequestedFields(Arrays.asList(Sentences.text, Sentences.likelihood));
	}



	@Override
	public void enableHighlighting() {
		throw new NotImplementedException();
	}

}
