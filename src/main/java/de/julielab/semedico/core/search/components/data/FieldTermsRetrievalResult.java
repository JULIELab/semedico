package de.julielab.semedico.core.search.components.data;

import java.util.stream.Stream;

import de.julielab.elastic.query.components.data.FieldTermItem;

public class FieldTermsRetrievalResult extends SemedicoSearchResult {

	private Stream<FieldTermItem> fieldTerms;

	public FieldTermsRetrievalResult(Stream<FieldTermItem> fieldTerms) {
		this.fieldTerms = fieldTerms;
	}

	public Stream<FieldTermItem> getFieldTerms() {
		return fieldTerms;
	}
	
}
