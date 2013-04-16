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

import java.util.List;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.components.ISearchComponent;
import de.julielab.semedico.search.components.ISearchComponent.ArticleChain;
import de.julielab.semedico.search.components.ISearchComponent.DocumentChain;
import de.julielab.semedico.search.components.ISearchComponent.DocumentPagingChain;
import de.julielab.semedico.search.components.ISearchComponent.FacetCountChain;
import de.julielab.semedico.search.components.ISearchComponent.IndirectLinkArticleListChain;
import de.julielab.semedico.search.components.ISearchComponent.IndirectLinksChain;
import de.julielab.semedico.search.components.ISearchComponent.SwitchSearchNodeChain;
import de.julielab.semedico.search.components.ISearchComponent.TermSelectChain;
import de.julielab.semedico.search.components.QueryAnalysisCommand;
import de.julielab.semedico.search.components.SearchCarrier;
import de.julielab.semedico.search.components.SearchNodeSearchCommand;
import de.julielab.semedico.search.components.SemedicoSearchCommand;
import de.julielab.semedico.search.components.SemedicoSearchResult;
import de.julielab.semedico.search.components.SolrSearchCommand;

/**
 * @author faessler
 * 
 */
public class SearchService implements ISearchService {

	private final ISearchComponent documentSearchChain;
	private final ISearchComponent facetCountChain;
	private final ISearchComponent termSelectChain;
	private final ISearchComponent switchSearchNodeChain;
	private final ISearchComponent indirectLinksChain;
	private final ISearchComponent highlightedArticleChain;
	private final ISearchComponent indirectLinkArticleChain;
	private final ISearchComponent documentPagingChain;

	public SearchService(@DocumentChain ISearchComponent documentSearchChain,
			@DocumentPagingChain ISearchComponent documentPagingChain,
			@TermSelectChain ISearchComponent termSelectChain,
			@FacetCountChain ISearchComponent facetCountChain,
			@ArticleChain ISearchComponent highlightedArticleChain,
			@SwitchSearchNodeChain ISearchComponent switchSearchNodeChain,
			@IndirectLinksChain ISearchComponent indirectLinksChain,
			@IndirectLinkArticleListChain ISearchComponent indirectLinkArticleChain) {
		this.documentSearchChain = documentSearchChain;
		this.documentPagingChain = documentPagingChain;
		this.termSelectChain = termSelectChain;
		this.facetCountChain = facetCountChain;
		this.highlightedArticleChain = highlightedArticleChain;
		this.switchSearchNodeChain = switchSearchNodeChain;
		this.indirectLinksChain = indirectLinksChain;
		this.indirectLinkArticleChain = indirectLinkArticleChain;

	}

	@Override
	public SemedicoSearchResult doArticleSearch(int documentId, String solrQuery) {
		SearchCarrier carrier = new SearchCarrier();
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.documentId = documentId;
		carrier.searchCmd = searchCmd;

		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.solrQuery = solrQuery;
		carrier.solrCmd = solrCmd;

		highlightedArticleChain.process(carrier);

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;
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

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = new SemedicoSearchResult();

		return searchResult;
	}

	@Override
	public SemedicoSearchResult doIndirectLinkArticleSearch(
			IFacetTerm selectedLinkTerm,
			List<Multimap<String, IFacetTerm>> searchNodes, int searchNodeIndex) {
		SearchCarrier carrier = new SearchCarrier();
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.semedicoQuery = searchNodes.get(searchNodeIndex);
		SearchNodeSearchCommand nodeCmd = new SearchNodeSearchCommand();
		nodeCmd.searchNodes = searchNodes;
		nodeCmd.nodeIndex = searchNodeIndex;
		nodeCmd.linkTerm = selectedLinkTerm;
		searchCmd.nodeCmd = nodeCmd;
		carrier.searchCmd = searchCmd;

		indirectLinkArticleChain.process(carrier);

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;

		return searchResult;

	}

	@Override
	public SemedicoSearchResult doIndirectLinksSearch(
			List<Multimap<String, IFacetTerm>> searchNodes) {
		SearchCarrier carrier = new SearchCarrier();
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		SearchNodeSearchCommand nodeCmd = new SearchNodeSearchCommand();
		nodeCmd.searchNodes = searchNodes;
		searchCmd.nodeCmd = nodeCmd;
		carrier.searchCmd = searchCmd;

		indirectLinksChain.process(carrier);

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;

		return searchResult;
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

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;
		return searchResult;
	}

	@Override
	public SemedicoSearchResult doDocumentPagingSearch(String solrQuery, int startPosition) {
		SearchCarrier carrier = new SearchCarrier();
		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.solrQuery = solrQuery;
		solrCmd.start = startPosition;
		carrier.solrCmd = solrCmd;
		
		documentPagingChain.process(carrier);
		
		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;
		return searchResult;
	}

	@Override
	public SemedicoSearchResult doRelatedArticleSearch(Integer relatedDocumentId) {
		SearchCarrier carrier = new SearchCarrier();
		SemedicoSearchCommand searchCmd = new SemedicoSearchCommand();
		searchCmd.documentId = relatedDocumentId;
		carrier.searchCmd = searchCmd;
		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.addField(IIndexInformationService.TITLE);
		solrCmd.addField(IIndexInformationService.ABSTRACT);
		solrCmd.addField(IIndexInformationService.PUBLICATION_TYPES);
		solrCmd.addField(IIndexInformationService.PUBMED_ID);
		solrCmd.addField(IIndexInformationService.TITLE);
		solrCmd.addField(IIndexInformationService.DATE);
		carrier.solrCmd = solrCmd;
		
		highlightedArticleChain.process(carrier);

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;
		return searchResult;
	}

	@Override
	public SemedicoSearchResult doSearchNodeSwitchSearch(String solrQuery) {
		SearchCarrier carrier = new SearchCarrier();
		SolrSearchCommand solrCmd = new SolrSearchCommand();
		solrCmd.solrQuery = solrQuery;
		carrier.solrCmd = solrCmd;
		
		switchSearchNodeChain.process(carrier);

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;

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

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = new SemedicoSearchResult();

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

		carrier.setElapsedTime();
		SemedicoSearchResult searchResult = carrier.searchResult;

		return searchResult;
	}

}
