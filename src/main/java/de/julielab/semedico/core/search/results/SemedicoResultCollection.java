package de.julielab.semedico.core.search.results;

import java.util.Map;

public class SemedicoResultCollection extends SemedicoSearchResult {
	private Map<Object, SemedicoSearchResult> collectionMap;

	public SemedicoResultCollection(Map<Object, SemedicoSearchResult> collectionMap) {
		this.collectionMap = collectionMap;

	}

	public void put(Object object, SemedicoSearchResult result) {
		collectionMap.put(object, result);
	}

	public SemedicoSearchResult getResult(Object name) {
		return collectionMap.get(name);
	}
}
