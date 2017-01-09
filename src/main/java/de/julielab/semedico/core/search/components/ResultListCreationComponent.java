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
package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.util.LazyDisplayGroup;

/**
 * @author faessler
 * 
 */
public class ResultListCreationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ResultListCreation {
		//
	}

	private final IDocumentService documentService;
	private Logger log;

	public ResultListCreationComponent(Logger log, IDocumentService documentService) {
		this.log = log;
		this.documentService = documentService;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		ISearchServerResponse serverResponse = semCarrier.getSingleSearchServerResponse();
		if (null == serverResponse)
			throw new IllegalArgumentException("The search server response must not be null, but it is.");
		SemedicoSearchResult searchResult = semCarrier.searchResult;
		if (null == searchResult) {
			searchResult = new SemedicoSearchResult(semCarrier.searchCmd.semedicoQuery);
			semCarrier.searchResult = searchResult;
		}

		List<HighlightedSemedicoDocument> documentHits = Lists.newArrayList();

		List<ISearchServerDocument> solrDocs = serverResponse.getDocumentResults();
		log.debug("Retrieved {} documents for display, {} documents hits overall.", solrDocs.size(),
				serverResponse.getNumFound());
		searchResult.totalNumDocs = serverResponse.getNumFound();
		for (ISearchServerDocument solrDoc : solrDocs) {
			// Is it possible to highlight corresponding to the user input and
			// return fragments for each term hit instead of returning multiple
			// snippets for the same term?
			HighlightedSemedicoDocument documentHit = documentService.getHitListDocument(solrDoc, serverResponse.getHighlighting());
			documentHits.add(documentHit);
		}

		LazyDisplayGroup<HighlightedSemedicoDocument> displayGroup = new LazyDisplayGroup<HighlightedSemedicoDocument>(
				(int) serverResponse.getNumFound(), SemedicoSearchConstants.MAX_DOCS_PER_PAGE,
				SemedicoSearchConstants.MAX_BATCHES, documentHits);
		searchResult.documentHits = displayGroup;

		return false;
	}

}
