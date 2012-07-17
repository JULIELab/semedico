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
import de.julielab.semedico.query.IQueryTranslationService;
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
	
	@Inject
	private IQueryTranslationService queryTranslationService;
	
	@Inject
	private Logger logger;

	@Persist
	private List<Multimap<String, IFacetTerm>> searchNodes;
	
	@Property
	@Persist
	private List<LazyDisplayGroup<DocumentHit>> searchNodeDisplayGroups;
	
	@Property
	private IFacetTerm selectedBTerm;
	
	@Persist 
	private String[] bTermSolrQueries;
	

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
				labelStore.addStringLabel(cachedTermLabel, IFacetService.BTERMS_FACET);
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
		return searchNodeDisplayGroups.get(0);
	}
	
	public LazyDisplayGroup<DocumentHit> getDisplayGroup2() {
		return searchNodeDisplayGroups.get(1);
	}
	
	public String getSolrQueryString1() {
		return bTermSolrQueries[0];
	}
	
	public String getSolrQueryString2() {
		return bTermSolrQueries[1];
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
			return new LazyDisplayGroup<DocumentHit>(0, 0, 0, Collections.<DocumentHit>emptyList());
		}
		FacetedSearchResult searchResult = searchService.searchBTermSearchNode(searchNodes, selectedBTerm, searchNodeIndex);
		LazyDisplayGroup<DocumentHit> displayGroup = new LazyDisplayGroup<DocumentHit>(
				searchResult.getTotalHits(), MAX_DOCS_PER_PAGE, MAX_BATCHES,
				searchResult.getDocumentHits());
		return displayGroup;
	}

	public void setSearchNodes(List<Multimap<String, IFacetTerm>> searchNodes) {
		this.searchNodes = searchNodes;
		this.organiseBTerms();
		searchNodeDisplayGroups = new ArrayList<LazyDisplayGroup<DocumentHit>>();
		bTermSolrQueries = new String[searchNodes.size()];
		for (int i = 0; i < searchNodes.size(); i++) {
			searchNodeDisplayGroups.add(getBTermDocs(i));
		}
	}

	private void refreshDisplayGroups() {
		logger.debug("Refreshing B-Term document display groups.");
		for (int i = 0; i < searchNodes.size(); i++) {
			searchNodeDisplayGroups.set(i, getBTermDocs(i));
			bTermSolrQueries[i] = queryTranslationService.createQueryForBTermSearchNode(searchNodes, selectedBTerm, i);
		}
	}
	
	public Object onTermSelect() {
		refreshDisplayGroups();
		return this;
	}

}
