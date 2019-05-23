/**
 * SemedicoSearchCommand.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.search.components.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.julielab.scicopia.core.elasticsearch.legacy.FieldTermsCommand;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.translation.SearchTask;

/**
 * @author faessler
 * 
 */
public class SemedicoSearchCommand {
	/**
	 * Semedico representation of a user query. Used for each actual search (as opposed to single document requests, for
	 * example).
	 */
	private ParseTree semedicoQuery;
	/**
	 * The {@link SearchTask} to perform with his query. The task influences the queries being used.
	 */
	private SearchTask task;
	private String index;
	/**
	 * Type indexes and index types that should be searched for this search.
	 */
	private List<String> indexTypes;
	/**
	 * A single document to be retrieved.
	 */
	private String documentId;
	/**
	 * The facets to count terms for, respecting taxonomic facets in that only the currently visible terms are counted.
	 */
	private List<UIFacet> facetsToCount;

	/**
	 * Facets for which we want to retrieve <it>all</it> term IDs found in the respective facet source fields in the
	 * search index. In other words, "get the IDs of all terms in these facets that are actually in the search index" .
	 * Used for filtering purposes for term suggestions.
	 * 
	 * @deprecated It was a bit over-engineered to create a component of its own and this input field. The component
	 *             just creates FacetCommands which can also be done in the SearchService with less
	 *             configuration-overhead. Do it this way, instead.
	 */
	@Deprecated
	private List<Facet> facetsToGetAllIndexTerms;

	/**
	 * Used to get back the terms in an index field. Allows for some configuration on ordering and the result size.
	 */
	private FieldTermsCommand fieldTermsCommand;

	private SuggestionsSearchCommand suggCmd;

	/**
	 * The set of index fields that will be searched upon. When left empty, the default will be applied which is
	 * determined depending on the terms forming the query. This setting can be used to overwrite this default
	 */
	private Collection<String> searchFieldFilter = Collections.emptySet();

	public SemedicoSearchCommand() {
		documentId = null;
	}

	public void addFacetToCount(UIFacet uiFacet) {
		if (null == facetsToCount)
			// We would rather use a plain list but this results in API
			// incompatibility.
			facetsToCount = new FacetGroup<>("facetsToCount", -1);
		facetsToCount.add(uiFacet);
	}
	
	public Collection<String> getSearchFieldFilter() {
		return searchFieldFilter;
	}

	public void setSearchFieldFilter(Collection<String> searchFieldFilter) {
		this.searchFieldFilter = searchFieldFilter;
	}
	
	public String getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	public List<String> getIndexTypes() {
		return indexTypes;
	}

	public void setIndexTypes(List<String> indexTypes) {
		this.indexTypes = indexTypes;
	}
	
	public FieldTermsCommand getFieldTermsCommand() {
		return fieldTermsCommand;
	}
	
	public void setFieldTermsCommand(FieldTermsCommand ftc) {
		fieldTermsCommand = ftc;
	}
	
	public String getIndex() {
		return index;
	}
	
	public void setIndex(String index) {
		this.index = index;
	}
	
	public SearchTask getTask() {
		return task;
	}
	
	public void setTask(SearchTask task) {
		this.task = task;
	}
	
	public ParseTree getSemedicoQuery() {
		return semedicoQuery;
	}
	
	public void setSemedicoQuery(ParseTree semedicoQuery) {
		this.semedicoQuery = semedicoQuery;
	}
	
	public List<UIFacet> getFacetsToCount() {
		return facetsToCount;
	}
	
	public List<Facet> getFacetsToGetAllIndexTerms() {
		return facetsToGetAllIndexTerms;
	}
	
	public void setFacetsToGetAllIndexTerms(List<Facet> facets) {
		facetsToGetAllIndexTerms = facets;
	}
	
	public SuggestionsSearchCommand getSuggestionsCommand() {
		return suggCmd;
	}
	
	public void setSuggestionsCommand(SuggestionsSearchCommand suggestionsCommand) {
		suggCmd = suggestionsCommand;
	}
	
}
