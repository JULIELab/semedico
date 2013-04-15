/**
 * ResultListCreatorComponent.java
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
package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.util.LazyDisplayGroup;

/**
 * @author faessler
 * 
 */
public class ResultListCreationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ResultListCreation {
	}

	private final IDocumentService documentService;

	public ResultListCreationComponent(IDocumentService documentService) {
		this.documentService = documentService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		QueryResponse solrResponse = searchCarrier.solrResponse;
		if (null == solrResponse)
			throw new IllegalArgumentException(
					"The solr response must not be null, but it is.");
		SemedicoSearchResult searchResult = searchCarrier.searchResult;
		if (null == searchResult) {
			searchResult = new SemedicoSearchResult();
			searchCarrier.searchResult = searchResult;
		}

		List<DocumentHit> documentHits = Lists.newArrayList();

		SolrDocumentList solrDocs = solrResponse.getResults();
		for (SolrDocument solrDoc : solrDocs) {
			// Is it possible to highlight corresponding to the user input and
			// return fragments for each term hit instead of returning multiple
			// snippets for the same term?
			DocumentHit documentHit = documentService.getHitListDocument(
					solrDoc, solrResponse.getHighlighting());
			documentHits.add(documentHit);
		}

		LazyDisplayGroup<DocumentHit> displayGroup = new LazyDisplayGroup<DocumentHit>(
				(int) solrResponse.getResults().getNumFound(),
				SemedicoSearchConstants.MAX_DOCS_PER_PAGE,
				SemedicoSearchConstants.MAX_BATCHES, documentHits);
		searchResult.documentHits = displayGroup;

		return false;
	}

}
