/**
 * TextSearchPreparatorComponent.java
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

import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IUIService;

/**
 * @author faessler
 * 
 */
public class TextSearchPreparatorComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TextSearchPreparator {
	}

	private final ApplicationStateManager asm;

	public TextSearchPreparatorComponent(ApplicationStateManager asm,
			IUIService uiService) {
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
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		if (null == solrCmd)
			throw new IllegalArgumentException(
					"Non-null "
							+ SolrSearchCommand.class.getName()
							+ " object is expected which knows about the sort criterium to use and whether the review filter should be actived. However, no such object is present.");
		SemedicoSearchCommand searchCmd = searchCarrier.searchCmd;
		if (null == searchCmd || null == searchCmd.semedicoQuery)
			throw new IllegalArgumentException(
					"Non-null "
							+ SemedicoSearchCommand.class.getName()
							+ " instance with the Semedico query expected, but one of the two were null.");
		solrCmd.rows = SemedicoSearchConstants.MAX_DOCS_PER_PAGE;

		solrCmd.dohighlight = true;
		HighlightCommand hlc = new HighlightCommand();
		hlc.fields.add(TEXT);
		hlc.fields.add(TITLE);
		hlc.pre = "<b>";
		hlc.post = "</b>";
		hlc.snippets = Math.min(3, searchCmd.semedicoQuery.size());
		solrCmd.addHighlightCmd(hlc);

		SearchState searchState = asm.get(SearchState.class);
		solrCmd.sortCriterium = searchState.getSortCriterium();
		solrCmd.filterReviews = searchState.isReviewsFiltered();

		return false;
	}

}
