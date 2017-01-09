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
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.HighlightedSemedicoDocument.AuthorHighlight;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.util.LazyDisplayGroup;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.pages.Article;
import de.julielab.semedico.services.IStatefulSearchService;

/**
 * @author faessler
 *
 */
public class DocumentList
{
	@Parameter(required=true)
	private ParseTree query;
	
	@Parameter(required=true)
	private AbstractUserInterfaceState uiState;
	
	@Parameter
	@Property
	private LazyDisplayGroup<HighlightedSemedicoDocument> displayGroup;
	
	@Parameter
	@Property
	private String emptyMessage;
	
	@Parameter
	private int maxNumberHighlights;

	@Property
	private HighlightedSemedicoDocument hitItem;

	@Property
	private int hitIndex;

	@Property
	private int authorIndex;

	@Property
	private int pagerItem;

	@Deprecated
	@Property
	private String kwicItem;
	
	@Property
	private HighlightedSemedicoDocument.Highlight hlItem;

	@Property
	private Author authorItem;
	
	@Inject
	@Property
	private Request request;
	
	@Inject
	private IStatefulSearchService searchService;
	
	@Inject
	private ComponentResources componentResources;
	
	@InjectPage
	private Article article;
	
	public void onActionFromPagerLink(int page) throws IOException
	{
		try
		{
			displayGroup.setCurrentBatchIndex(page);
			int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
			SemedicoSearchResult searchResult = searchService.doDocumentPagingSearch(query, startPosition).get();	// führt Suche aus!
			displayGroup.setDisplayedObjects(searchResult.documentHits.getDisplayedObjects());
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}

	public void onActionFromPreviousBatchLink() throws IOException
	{
		try
		{
			displayGroup.displayPreviousBatch();
			int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
			SemedicoSearchResult searchResult = searchService.doDocumentPagingSearch(query, startPosition).get();	// führt Suche aus!
			displayGroup.setDisplayedObjects(searchResult.documentHits.getDisplayedObjects());
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}

	public void onActionFromNextBatchLink() throws IOException
	{
		try
		{
			displayGroup.displayNextBatch();
			int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
			SemedicoSearchResult searchResult = searchService.doDocumentPagingSearch(query, startPosition).get();
			displayGroup.setDisplayedObjects(searchResult.documentHits.getDisplayedObjects());
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isCurrentPage()
	{
		return pagerItem == displayGroup.getCurrentBatchIndex();
	}

	public String getCurrentHitClass()
	{
		return hitIndex % 2 == 0 ? "evenHit" : "oddHit";
	}

	public String getCurrentArticleTypeClass()
	{
		if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_ABSTRACT)
		{
			return "hitIconAbstract";
		}
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_TITLE)
		{
			return "hitIconTitle";
		}
		else if (hitItem.getDocument().getType() == SemedicoDocument.TYPE_FULL_TEXT)
		{
			return "hitIconFull";
		}
		else
		{
			return null;
		}
	}

	public int getIndexOfFirstArticle()
	{
		return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	}

	public boolean isNotLastAuthor()
	{
		return authorIndex < hitItem.getDocument().getAuthors().size() - 1;
	}
	
	public String getAuthorList()
	{
		List<AuthorHighlight> authors = hitItem.getAuthorHighlights();
		return StringUtils.join(authors, ", ");
	}

	Article onViewArticle(String docId, String indexType)
	{
		article.set(docId, indexType, query, uiState);
		return article;
	}
}