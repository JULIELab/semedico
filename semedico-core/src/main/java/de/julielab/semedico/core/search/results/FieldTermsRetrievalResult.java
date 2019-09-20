package de.julielab.semedico.core.search.results;

import de.julielab.elastic.query.components.data.FieldTermItem;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FieldTermsRetrievalResult extends SemedicoESSearchResult {

	private Map<String, Stream<FieldTermItem>> fieldTerms;

	public FieldTermsRetrievalResult() {
		this.fieldTerms = new HashMap<>();
	}
	
	public FieldTermsRetrievalResult(Map<String, Stream<FieldTermItem>> map) {
		this.fieldTerms = map;
	}

	public Stream<FieldTermItem> getFieldTerms(String aggregationName) {
		return fieldTerms.get(aggregationName);
	}
	
	public Map<String, Stream<FieldTermItem>> getFieldTerms() {
		return fieldTerms;
	}

	public void put(String requestName, Stream<FieldTermItem> fieldTermStream) {
		fieldTerms.put(requestName, fieldTermStream);
	}
	
}
