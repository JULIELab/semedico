package de.julielab.semedico.core.search.results;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.FieldTermItem;

public class FieldTermsRetrievalResult extends SemedicoSearchResult {

	private Map<String, Stream<FieldTermItem>> fieldTerms;

	public FieldTermsRetrievalResult() {
		this.fieldTerms = new HashMap<>();
	}
	
	public FieldTermsRetrievalResult(Map<String, Stream<FieldTermItem>> map) {
		this.fieldTerms = map;
	}

	public Stream<FieldTermItem> getFieldTerms(String name) {
		return fieldTerms.get(name);
	}
	
	public Map<String, Stream<FieldTermItem>> getFieldTerms() {
		return fieldTerms;
	}

	public void put(String requestName, Stream<FieldTermItem> fieldTermStream) {
		fieldTerms.put(requestName, fieldTermStream);
	}
	
}
