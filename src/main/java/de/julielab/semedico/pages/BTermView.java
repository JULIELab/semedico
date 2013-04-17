/**
 * BTermView.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 03.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.exceptions.EmptySearchComplementException;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.components.SemedicoSearchResult;
import de.julielab.util.LazyDisplayGroup;

/**
 * @author faessler
 * 
 */
public class BTermView {

	@InjectPage
	private Index index;

	@Inject
	private ISearchService searchService;

	@Property
	@SessionState
	private BTermUserInterfaceState uiState;

	@SessionState(create = false)
	private SearchState searchState;

	@Inject
	private ComponentResources componentResources;

	@Inject
	private Logger logger;

	@Persist
	private List<Multimap<String, IFacetTerm>> searchNodes;

	@Property
	@Persist
	private List<LazyDisplayGroup<DocumentHit>> searchNodeDisplayGroups;

	@Property
	@Persist
	private IFacetTerm selectedBTerm;

	/**
	 * <p>
	 * Event handler which is executed before beginning page rendering.
	 * </p>
	 * <p>
	 * The main page will check whether there is a search whose search results
	 * could be displayed. If not, the user is redirected to the Index page.
	 * </p>
	 * 
	 * @return The Index page if there is no search to display. Otherwise, null
	 *         will be returned to signal the page rendering.
	 * @see http://tapestry.apache.org/page-navigation.html
	 */
	public Object onActivate() {
		if (searchState == null || searchState.getSearchNodes().size() < 2)
			return index;
		return null;
	}

	void organiseBTerms() throws EmptySearchComplementException {
		logger.debug("Passed search nodes: " + searchState);

		searchService.doIndirectLinksSearch(searchNodes);

	}

	public LazyDisplayGroup<DocumentHit> getDisplayGroup1() {
		return searchNodeDisplayGroups.get(0);
	}

	public LazyDisplayGroup<DocumentHit> getDisplayGroup2() {
		return searchNodeDisplayGroups.get(1);
	}

	public int getMaxNumberHighlights1() {
		return searchNodes.get(0).size();
	}

	public int getMaxNumberHighlights2() {
		return searchNodes.get(1).size();
	}

	private LazyDisplayGroup<DocumentHit> getBTermDocs(int searchNodeIndex) {
		if (selectedBTerm == null) {
			logger.debug("No B-Term selected, returning empty display group.");
			return new LazyDisplayGroup<DocumentHit>(0, 0, 0,
					Collections.<DocumentHit> emptyList());
		}
		SemedicoSearchResult searchResult = searchService
				.doIndirectLinkArticleSearch(selectedBTerm, searchNodes,
						searchNodeIndex);
		return searchResult.documentHits;
	}

	public void setSearchNodes(List<Multimap<String, IFacetTerm>> searchNodes)
			throws EmptySearchComplementException {
		this.searchNodes = searchNodes;
		uiState.reset();
		this.organiseBTerms();
		searchNodeDisplayGroups = new ArrayList<LazyDisplayGroup<DocumentHit>>();
		for (int i = 0; i < searchNodes.size(); i++) {
			searchNodeDisplayGroups.add(getBTermDocs(i));
		}
	}

	private void refreshDisplayGroups() {
		logger.trace("Refreshing B-Term document display groups.");
		for (int i = 0; i < searchNodes.size(); i++) {
			searchNodeDisplayGroups.set(i, getBTermDocs(i));
		}
	}

	public Object onTermSelect() {
		refreshDisplayGroups();
		return this;
	}

	/**
	 * 
	 */
	public void reset() {
		componentResources.discardPersistentFieldChanges();
	}

}
