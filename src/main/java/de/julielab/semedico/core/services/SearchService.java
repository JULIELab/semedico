/**
 * SearchService.java
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
 * Creation date: 09.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.services;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.components.ISearchComponent;
import de.julielab.semedico.search.components.ISearchComponent.DocumentChain;
import de.julielab.semedico.search.components.ISearchComponent.FacetCountChain;
import de.julielab.semedico.search.components.ISearchComponent.SwitchSearchNodeChain;
import de.julielab.semedico.search.components.ISearchComponent.TermSelectChain;
import de.julielab.semedico.search.components.QueryAnalysisCommand;
import de.julielab.semedico.search.components.SearchCarrier;
import de.julielab.semedico.search.components.SemedicoSearchCommand;
import de.julielab.semedico.search.components.SemedicoSearchResult;
import de.julielab.semedico.search.components.SolrSearchCommand;
import de.julielab.util.LazyDisplayGroup;

/**
 * @author faessler
 * 
 */
public class SearchService implements ISearchService {

	private final ISearchComponent documentSearchChain;
	private final ISearchComponent facetCountChain;
	private final ISearchComponent termSelectChain;
	private final ISearchComponent switchSearchNodeChain;

	public SearchService(@DocumentChain ISearchComponent documentSearchChain,
			@TermSelectChain ISearchComponent termSelectChain,
			@FacetCountChain ISearchComponent facetCountChain, @SwitchSearchNodeChain ISearchComponent switchSearchNodeChain) {
		this.documentSearchChain = documentSearchChain;
		this.termSelectChain = termSelectChain;
		this.facetCountChain = facetCountChain;
		this.switchSearchNodeChain = switchSearchNodeChain;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.interfaces.ISearchService#doNewSearch
	 * (java.lang.String, java.lang.String, int)
	 */
	@Override
	public SemedicoSearchResult doNewDocumentSearch(String userQuery,
			String termId, Integer facetId) {
		SearchCarrier carrier = new SearchCarrier();
		QueryAnalysisCommand queryCmd = new QueryAnalysisCommand();
		queryCmd.userQuery = userQuery;
		queryCmd.selectedTermId = termId;
		if (facetId != null)
			queryCmd.facetIdForSelectedTerm = facetId;
		carrier.queryCmd = queryCmd;

		documentSearchChain.process(carrier);

		long elapsedTime = carrier.sw.getTime();
		SemedicoSearchResult searchResult = carrier.searchResult;
		searchResult.elapsedTime = elapsedTime;
		return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doTermSelectSearch(com.google.common.collect.Multimap)
	 */
	@Override
	public SemedicoSearchResult doTermSelectSearch(
			Multimap<String, IFacetTerm> semedicoQuery, String userQuery) {
		SearchCarrier carrier = new SearchCarrier();
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.semedicoQuery = semedicoQuery;
		carrier.searchCmd = searchCmd;

		QueryAnalysisCommand queryCmd = new QueryAnalysisCommand();
		queryCmd.userQuery = userQuery;
		carrier.queryCmd = queryCmd;

		termSelectChain.process(carrier);

		SemedicoSearchResult searchResult = carrier.searchResult;
		searchResult.elapsedTime = carrier.sw.getTime();

		return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doTabSelectSearch()
	 */
	@Override
	public SemedicoSearchResult doTabSelectSearch(String solrQuery) {
		SearchCarrier carrier = new SearchCarrier();
		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.solrQuery = solrQuery;
		carrier.solrCmd = solrCmd;

		facetCountChain.process(carrier);
		
		SemedicoSearchResult searchResult = new SemedicoSearchResult();
		searchResult.elapsedTime = carrier.sw.getTime();

		return searchResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.ISearchService#
	 * doFacetNavigationSearch(de.julielab.semedico.core.UIFacet)
	 */
	@Override
	public SemedicoSearchResult doFacetNavigationSearch(UIFacet uiFacet,
			String solrQuery) {
		SearchCarrier carrier = new SearchCarrier();
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.addFacetToCount(uiFacet);
		carrier.searchCmd = searchCmd;

		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.solrQuery = solrQuery;
		carrier.solrCmd = solrCmd;
		
		facetCountChain.process(carrier);
		
		SemedicoSearchResult searchResult = new SemedicoSearchResult();
		searchResult.elapsedTime = carrier.sw.getTime();
		
		return searchResult;
	}

	@Override
	public SemedicoSearchResult doSearchNodeSwitchSearch(String solrQuery, Multimap<String, IFacetTerm> semedicoQuery) {
		SearchCarrier carrier = new SearchCarrier();
		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.solrQuery = solrQuery;
		carrier.solrCmd = solrCmd;
		
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.semedicoQuery = semedicoQuery;
		carrier.searchCmd = searchCmd;
		
		switchSearchNodeChain.process(carrier);
		
		SemedicoSearchResult searchResult = carrier.searchResult;
		searchResult.elapsedTime = carrier.sw.getTime();
		
		return searchResult;
	}

}
