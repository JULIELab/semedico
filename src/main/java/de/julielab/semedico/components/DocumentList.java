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

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.util.LazyDisplayGroup;

/**
 * @author faessler
 *
 */
public class DocumentList {
	@Parameter
	@Property
	private LazyDisplayGroup<DocumentHit> displayGroup;
	
	@Parameter
	@Property
	private String emptyMessage;
	
	@Parameter
	private String solrQueryString;
	
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
	private IFacetedSearchService searchService;

	public void onActionFromPagerLink(int page) throws IOException {
		displayGroup.setCurrentBatchIndex(page);
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(solrQueryString, startPosition, maxNumberHighlights);
		displayGroup.setDisplayedObjects(documentHits);
	}

	public void onActionFromPreviousBatchLink() throws IOException {
		displayGroup.displayPreviousBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(solrQueryString, startPosition, maxNumberHighlights);
		displayGroup.setDisplayedObjects(documentHits);
	}

	public void onActionFromNextBatchLink() throws IOException {
		displayGroup.displayNextBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService
				.constructDocumentPage(solrQueryString, startPosition, maxNumberHighlights);
		displayGroup.setDisplayedObjects(documentHits);
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
}

