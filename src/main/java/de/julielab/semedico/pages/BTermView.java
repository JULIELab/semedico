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

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.StringLabel;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;
import de.julielab.semedico.util.LazyDisplayGroup;

/**
 * @author faessler
 * 
 */
public class BTermView {

	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;

	@SessionState
	@Property
	private BTermUserInterfaceState uiState;

	@SessionState
	private SearchState searchState;

	@Inject
	private IBTermService bTermService;

	@Inject
	private ITermService termService;

	@Inject
	private IFacetService facetService;

	@Inject
	private ILabelCacheService labelCacheService;

	@Inject
	private IFacetedSearchService searchService;

	@Persist
	private List<Multimap<String, IFacetTerm>> searchNodes;
	
	@Inject
	private Logger logger;

	void organiseBTerms() {
		logger.debug("Passed search nodes: " + searchState);
		List<Label> bTermLabelList = bTermService
				.determineBTermLabelList(searchNodes);
		LabelStore labelStore = uiState.getLabelStore();

		// TODO inefficient!! we create labels for labels...
		for (Label l : bTermLabelList) {
			if (termService.hasNode(l.getName())) {
				TermLabel cachedTermLabel = labelCacheService
						.getCachedTermLabel(l.getName());
				cachedTermLabel.setCount(l.getCount());
				labelStore.addTermLabel(cachedTermLabel);
			} else {
				StringLabel cachedStringLabel = labelCacheService
						.getCachedStringLabel(l.getName());
				cachedStringLabel.setCount(l.getCount());
				labelStore.addStringLabel(cachedStringLabel,
						IFacetService.BTERMS_FACET);
			}
		}
		for (FacetConfiguration configuration : uiState
				.getFacetConfigurations().values())
			labelStore.sortLabelsIntoFacet(configuration);
	}

	public LazyDisplayGroup<DocumentHit> getDisplayGroup1() {
		return getBTermDocs(0);
	}
	
	public LazyDisplayGroup<DocumentHit> getDisplayGroup2() {
		return getBTermDocs(1);
	}
	
	private LazyDisplayGroup<DocumentHit> getBTermDocs(int searchNodeIndex) {
		Label selectedTerm = searchState.getSelectedTerm();
		if (selectedTerm == null)
			return new LazyDisplayGroup<DocumentHit>(0, 0, 0, Collections.<DocumentHit>emptyList());
		// Make a copy of the original search nodes so we can add the BTerm as a temporary restriction.
		logger.debug("All search nodes: {}", searchState);
		Multimap<String, IFacetTerm> queryTerms = HashMultimap.create(searchNodes
				.get(searchNodeIndex));
		IFacetTerm term = null;
		if (selectedTerm instanceof TermLabel) {
			term = ((TermLabel) selectedTerm).getTerm();
			queryTerms.put(term.getId(), term);
		} else {
			term = termService.getTermObjectForStringTerm(
					selectedTerm.getName(),
					facetService.getFacetById(IFacetService.BTERMS_FACET));
			queryTerms.put(term.getName(), term);
		}
		logger.debug("{}th search node with BTerm to display related documents: {}", searchNodeIndex, queryTerms);
		FacetedSearchResult searchResult = searchService.search(queryTerms);
		LazyDisplayGroup<DocumentHit> displayGroup = new LazyDisplayGroup<DocumentHit>(
				searchResult.getTotalHits(), MAX_DOCS_PER_PAGE, MAX_BATCHES,
				searchResult.getDocumentHits());
		return displayGroup;
	}

	public void setSearchNodes(List<Multimap<String, IFacetTerm>> searchNodes) {
		this.searchNodes = searchNodes;
		this.organiseBTerms();
	}

}
