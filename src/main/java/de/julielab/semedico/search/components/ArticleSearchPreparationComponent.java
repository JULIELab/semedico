/**
 * ArticleSearchPreparatorComponent.java
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

import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.TEXT;
import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.TITLE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * @author faessler
 * 
 */
public class ArticleSearchPreparationComponent implements ISearchComponent {

	private final ApplicationStateManager asm;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ArticleSearchPreparation {
	}

	public ArticleSearchPreparationComponent(ApplicationStateManager asm) {
		this.asm = asm;

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
		if (searchCarrier.searchCmd.documentId < 0)
			throw new IllegalArgumentException(
					"The document ID of the article to load is required.");

		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		if (null == solrCmd) {
			solrCmd = new SolrSearchCommand();
			searchCarrier.solrCmd = solrCmd;
		}

		if (StringUtils.isEmpty(solrCmd.solrQuery)) {
			solrCmd.solrQuery = "*:*";
		}
		solrCmd.addFilterQuery(IIndexInformationService.PUBMED_ID + ":"
				+ searchCarrier.searchCmd.documentId);
		solrCmd.dohighlight = true;
		HighlightCommand hlc = new HighlightCommand();
		hlc.fields.add(TEXT);
		hlc.fields.add(TITLE);
		hlc.fragsize = 50000;
		hlc.pre = "<span class=\"highlightFull\">";
		hlc.post = "</span>";
		solrCmd.addHighlightCmd(hlc);

		return false;
	}

}
