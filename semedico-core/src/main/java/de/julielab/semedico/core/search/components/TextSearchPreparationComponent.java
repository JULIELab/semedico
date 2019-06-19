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
package de.julielab.semedico.core.search.components;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.SearchCarrier;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author faessler
 * 
 */
public class TextSearchPreparationComponent extends AbstractSearchComponent {

	public TextSearchPreparationComponent(Logger log) {
		super(log);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TextSearchPreparation {
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
//		SemedicoESSearchCarrier semCarrier = castCarrier(searchCarrier);
//		Supplier<SearchServerRequest> s1 = () -> semCarrier.getSingleSearchServerRequest();
//		Supplier<SearchState> s2 = () -> semCarrier.searchState;
//		// TODO adapt
//		Supplier<ISemedicoQuery> s3 = () -> null;//semCarrier.query;
//		
//		checkNotNull(s1, "Search Server Command", s2, "Search State", s3, "Search Query");
//		stopIfError();
//
//		SearchServerRequest serverCmd = s1.get();
//		Set<SearchOption> options = s3.get().getSearchOptions();
//		serverCmd.rows = options.contains(SearchOption.HIT_COUNT) || options.contains(SearchOption.NO_FIELDS)? 0 : semCarrier.query.getResultSize();
//		serverCmd.fieldsToReturn = options.contains(SearchOption.HIT_COUNT)|| options.contains(SearchOption.NO_FIELDS) ? Collections.emptyList() : Arrays.asList(IIndexInformationService.Indices.Documents.DATE, IIndexInformationService.Indices.Documents.pmcid,
//				IIndexInformationService.Indices.Documents.PUBMED_ID, IIndexInformationService.Indices.Documents.TITLE, IIndexInformationService.Indices.Documents.AUTHORS,
//				IIndexInformationService.Indices.Documents.affiliation, IIndexInformationService.Indices.Documents.ABSTRACT,
//				IIndexInformationService.Indices.Documents.journaltitle,
//				IIndexInformationService.Indices.Documents.journalvolume,
//				IIndexInformationService.Indices.Documents.journalissue,
//				IIndexInformationService.Indices.Documents.journalpages);
//		if (!options.contains(SearchOption.HIT_COUNT)|| options.contains(SearchOption.NO_HIGHLIGHTING)){
//			HlField hlField;
//			HighlightCommand hlc = new HighlightCommand();
//			hlField = hlc.addField(TITLE, SemedicoSearchConstants.HIGHLIGHT_SNIPPETS, 1000);
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//
//			// the following fields are not searched themselves but by custom
//			// _all fields (docmeta and mesh). Thus, "requirefieldmatch" must be
//			// set to false in order to still get highlightings for the
//			// individual fields (which we need to explain why a document was
//			// hit)
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.AUTHORS);
//			hlField.requirefieldmatch = false;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.affiliation);
//			hlField.requirefieldmatch = false;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.keywords);
//			hlField.requirefieldmatch = false;
//			hlField.type = "plain";
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.journaltitle);
//			hlField.requirefieldmatch = false;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.journalissue);
//			hlField.requirefieldmatch = false;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.journalvolume);
//			hlField.requirefieldmatch = false;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.meshminor);
//			hlField.requirefieldmatch = false;
//			hlField.type = "plain";
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.meshmajor);
//			hlField.requirefieldmatch = false;
//			hlField.type = "plain";
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.substances);
//			hlField.requirefieldmatch = false;
//			hlField.type = "plain";
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.abstracttext);
//			hlField.requirefieldmatch = false;
//			hlField.noMatchSize = 200;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//
//			hlField = hlc.addField(IIndexInformationService.Indices.Documents.alltext);
//			hlField.requirefieldmatch = true;
//			hlField.fragnum = 3;
//			hlField.pre = "<b>";
//			hlField.post = "</b>";
//
//			serverCmd.addHighlightCmd(hlc);
//		}
//
//		SearchState searchState = s2.get();
//		switch (searchState.getSortCriterium()) {
//		case DATE:
//			serverCmd.addSortCommand(IIndexInformationService.Indices.Documents.date, SortOrder.DESCENDING);
//			break;
//		case DATE_AND_RELEVANCE:
//			serverCmd.addSortCommand(IIndexInformationService.Indices.Documents.date, SortOrder.DESCENDING);
//			serverCmd.addSortCommand("_score", SortOrder.DESCENDING);
//			break;
//		case RELEVANCE:
//			serverCmd.addSortCommand(IIndexInformationService.Indices.Documents._score, SortOrder.DESCENDING);
//		}
//
//		serverCmd.filterReviews = searchState.isReviewsFiltered();

		return false;
	}
}
