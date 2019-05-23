/**
 * SearchState.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 15.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.services.SortCriterium;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.ParseTree;

/**
 * @author faessler
 * 
 */

public class SearchState {
	/**
	 * The most important information about session state: These are the query
	 * terms which determine the currently retrieved documents in the first
	 * place.
	 * It is a list in order to store multiple queries that can then be analyzed
	 * for B-term computation. The index of the query currently active forthe
	 * user is given by currentSearchNodeIndex.
	 * private List<Multimap<String, IFacetTerm>> queryTerms;
	 */
	
	private List<ParseTree> queryTerms = new ArrayList<>();

	// The translated queries from a Multimap<String, IFacetTerm> in queryTerms.
	/**
	 * @deprecated should be done via semedicoQuery.toLuceneSearchString()
	 *             (ParseTree);
	 */
	@Deprecated
	private List<String> elasticQueryStrings;
	
	// Since a term can occur in multiple facets, this map stores the
	// information from which facet a particular term had been chosen by the
	// user.
	// Also a list parallel to queryTerms.
	private List<Map<IConcept, Facet>> queryTermFacetMap;
	
	// When doing a multi-node-search, each search node is done by performing a
	// normal Semedico search. This index indicates which search node is
	// currently active.
	private int activeSearchNodeIndex;

	// Determines how the found documents are to be ordered for display.
	private SortCriterium sortCriterium;

	// Set to true during the rendering phase after a query was put into the
	// input field and a search had been triggered (opposed to searches by
	// clicking on terms).
	private boolean newSearch;

	// The string that has been passed by the user, be it a selected suggestion
	// or free text input.
	private String userQueryString;

	private Logger logger = LoggerFactory.getLogger(SearchState.class);

	public SearchState() {
		super();
		this.elasticQueryStrings = new ArrayList<>();
		this.queryTermFacetMap = new ArrayList<>();
		this.sortCriterium = SortCriterium.RELEVANCE;
		this.newSearch = true;
		this.activeSearchNodeIndex = -1;
		this.createNewSearchNode();
	}

	/**
	 * @return the sortCriterium
	 */
	public SortCriterium getSortCriterium() {
		return sortCriterium;
	}

	/**
	 * @param sortCriterium the sortCriterium to set
	 */
	public void setSortCriterium(SortCriterium sortCriterium) {
		if (null != logger) {
			logger.debug("Setting sort criterium to {}", sortCriterium);
		}
		this.sortCriterium = sortCriterium;
	}
	
	/**
	 * @return the newSearch
	 */
	public boolean isNewSearch() {
		return newSearch;
	}

	/**
	 * @param newSearch
	 *            the newSearch to set
	 */
	public void setNewSearch(boolean newSearch) {
		this.newSearch = newSearch;
	}

	/**
	 * @return The parse tree.
	 */
	public ParseTree getSemedicoQuery() {
		return queryTerms.get(activeSearchNodeIndex);
	}

	public List<ParseTree> getSearchNodes() {
		return queryTerms;
	}

	public ParseTree getSearchNode(int index) {
		return queryTerms.get(index);
	}

	/**
	 * @param parseTree
	 *            The parse tree to set.
	 */
	public void setDisambiguatedQuery(ParseTree parseTree) {
		logger.debug("Setting current query to {}", parseTree);
		this.queryTerms.set(activeSearchNodeIndex, parseTree);
	}

	/**
	 * This method starts a new search node for a multi-node-search.
	 * 
	 */
	public void createNewSearchNode() {
		this.queryTerms.add(null);
		this.queryTermFacetMap.add(new HashMap<IConcept, Facet>());
		this.elasticQueryStrings.add(null);
		this.activeSearchNodeIndex++;
	}

	/**
	 * @return the rawQuery
	 */
	public String getUserQueryString() {
		return userQueryString;
	}

	/**
	 * @param userQueryString
	 */
	public void setUserQueryString(String userQueryString) {
		this.userQueryString = userQueryString;
	}

	/**
	 * 
	 * @return
	 * @deprecated Should be done via semedicoQuery.toLuceneQueryString()
	 *             (ParseTree)
	 */
	@Deprecated
	public String getSolrQueryString() {
		return elasticQueryStrings.get(activeSearchNodeIndex);
	}

	public String getSolrQuery(int index) {
		return elasticQueryStrings.get(index);
	}

	public int getActiveSearchNodeIndex() {
		return activeSearchNodeIndex;
	}

	public void setActiveSearchNodeIndex(int index) {
		if (index >= queryTerms.size()) {
			throw new IllegalArgumentException(
					"Cannot set the active search node index to "
					+ index + ": Only "	+ queryTerms.size()	+ " search nodes exist.");
		}
		activeSearchNodeIndex = index;
	}

	/**
	 * 
	 */
	public void clear() {
		queryTerms.clear();
		queryTermFacetMap.clear();
		elasticQueryStrings.clear();
		activeSearchNodeIndex = -1;
		userQueryString = null;
		createNewSearchNode();
		sortCriterium = SortCriterium.RELEVANCE;
	}

}
