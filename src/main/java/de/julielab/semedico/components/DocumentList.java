/**
 * DocumentList.java
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
 * Creation date: 16.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.components;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.search.components.SemedicoSearchResult;
import de.julielab.util.LazyDisplayGroup;

/**
 * @author faessler
 *
 */
public class DocumentList {
	@SessionState
	private SearchState searchState;
	
	@Parameter
	@Property
	private LazyDisplayGroup<DocumentHit> displayGroup;
	
	@Parameter
	@Property
	private String emptyMessage;
	
//	@Parameter
//	@Property
//	private String solrQueryString;
	
	@Parameter
	@Property
	private int searchNodeIndex;
	
	@Parameter
	@Property
	private boolean btermQuery;
	
	@Parameter
	private int maxNumberHighlights;

	@Property
	private DocumentHit hitItem;

	@Property
	private int hitIndex;

	@Property
	private int authorIndex;

	@Property
	private int pagerItem;

	@Property
	private String kwicItem;

	@Property
	private Author authorItem;
	
	@Inject
	private ISearchService searchService;
	
	public void onActionFromPagerLink(int page) throws IOException {
		String solrQueryString = searchState.getSolrQuery(searchNodeIndex);
		displayGroup.setCurrentBatchIndex(page);
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		SemedicoSearchResult searchResult = searchService.doDocumentPagingSearch(solrQueryString, startPosition);
		displayGroup.setDisplayedObjects(searchResult.documentHits.getDisplayedObjects());
	}

	public void onActionFromPreviousBatchLink() throws IOException {
		String solrQueryString = searchState.getSolrQuery(searchNodeIndex);
		displayGroup.displayPreviousBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		SemedicoSearchResult searchResult = searchService.doDocumentPagingSearch(solrQueryString, startPosition);
		displayGroup.setDisplayedObjects(searchResult.documentHits.getDisplayedObjects());
	}

	public void onActionFromNextBatchLink() throws IOException {
		String solrQueryString = searchState.getSolrQuery(searchNodeIndex);
		displayGroup.displayNextBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		SemedicoSearchResult searchResult = searchService.doDocumentPagingSearch(solrQueryString, startPosition);
		displayGroup.setDisplayedObjects(searchResult.documentHits.getDisplayedObjects());
	}
	
	public boolean isCurrentPage() {
		return pagerItem == displayGroup.getCurrentBatchIndex();
	}

	public String getCurrentHitClass() {
		return hitIndex % 2 == 0 ? "evenHit" : "oddHit";
	}

	public String getCurrentArticleTypeClass() {
		if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_ABSTRACT)
			return "hitIconAbstract";
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_TITLE)
			return "hitIconTitle";
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_FULL_TEXT)
			return "hitIconFull";
		else
			return null;
	}

	public int getIndexOfFirstArticle() {
		return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	}

	public boolean isNotLastAuthor() {
		return authorIndex < hitItem.getDocument().getAuthors().size() - 1;
	}
	
	public String getAuthorList() {
		List<Author> authors = hitItem.getDocument().getAuthors();
		return StringUtils.join(authors, ", ");
	}
}

