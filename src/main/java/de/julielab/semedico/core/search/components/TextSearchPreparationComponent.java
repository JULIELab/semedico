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

import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.TITLE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.HighlightCommand.HlField;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.SortCommand.SortOrder;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.query.DocumentQuery;
import de.julielab.semedico.core.search.components.data.DocumentSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * @author faessler
 * 
 */
public class TextSearchPreparationComponent extends AbstractSearchComponent {

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
		@SuppressWarnings("unchecked")
		SemedicoSearchCarrier<DocumentQuery, DocumentSearchResult> semCarrier = (SemedicoSearchCarrier<DocumentQuery, DocumentSearchResult>) searchCarrier;
		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommand();
		SearchState searchState = semCarrier.searchState;
		if (null == serverCmd)
			throw new IllegalArgumentException("Non-null " + SearchServerCommand.class.getName()
					+ " object is expected which knows about the sort criterium to use and whether the review filter should be active. However, no such object is present.");
		if (null == searchState)
			throw new IllegalArgumentException(
					"The search state is null. However, it is required to get the user specified search details.");
		serverCmd.rows = semCarrier.query.getResultSize();
		serverCmd.fieldsToReturn = Arrays.asList(IIndexInformationService.DATE, IIndexInformationService.pmcid,
				IIndexInformationService.PUBMED_ID, IIndexInformationService.TITLE, IIndexInformationService.AUTHORS,
				IIndexInformationService.GeneralIndexStructure.affiliation, IIndexInformationService.ABSTRACT,
				IIndexInformationService.GeneralIndexStructure.journaltitle,
				IIndexInformationService.GeneralIndexStructure.journalvolume,
				IIndexInformationService.GeneralIndexStructure.journalissue,
				IIndexInformationService.GeneralIndexStructure.journalpages);
		{
			HlField hlField;
			HighlightCommand hlc = new HighlightCommand();
			hlField = hlc.addField(TITLE, SemedicoSearchConstants.HIGHLIGHT_SNIPPETS, 1000);
			hlField.pre = "<b>";
			hlField.post = "</b>";

			// the following fields are not searched themselves but by custom
			// _all fields (docmeta and mesh). Thus, "requirefieldmatch" must be
			// set to false in order to still get highlightings for the
			// individual fields (which we need to explain why a document was
			// hit)
			hlField = hlc.addField(IIndexInformationService.AUTHORS);
			hlField.requirefieldmatch = false;
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.affiliation);
			hlField.requirefieldmatch = false;
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.keywords);
			hlField.requirefieldmatch = false;
			hlField.type = "plain";
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.journaltitle);
			hlField.requirefieldmatch = false;
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.journalissue);
			hlField.requirefieldmatch = false;
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.journalvolume);
			hlField.requirefieldmatch = false;
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.meshminor);
			hlField.requirefieldmatch = false;
			hlField.type = "plain";
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.meshmajor);
			hlField.requirefieldmatch = false;
			hlField.type = "plain";
			hlField.pre = "<b>";
			hlField.post = "</b>";
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.substances);
			hlField.requirefieldmatch = false;
			hlField.type = "plain";
			hlField.pre = "<b>";
			hlField.post = "</b>";

			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.abstracttext);
			hlField.requirefieldmatch = false;
			hlField.noMatchSize = 200;
			hlField.pre = "<b>";
			hlField.post = "</b>";

			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.alltext);
			hlField.requirefieldmatch = true;
			hlField.fragnum = 3;
			hlField.pre = "<b>";
			hlField.post = "</b>";

			// event highlighting
			SearchServerQuery eventHlQuery = serverCmd.namedQueries.get("eventHl");
			if (null != eventHlQuery) {
				NestedQuery nestedQuery = (NestedQuery) eventHlQuery;
				if (null != nestedQuery.innerHits.highlight) {
					HighlightCommand innerHlc = nestedQuery.innerHits.highlight;
					innerHlc.fields.get(0).pre = "<b>";
					innerHlc.fields.get(0).post = "</b>";
					innerHlc.fields.get(0).fragsize = 200;
					// basically specifies the maximum number of highlights
					nestedQuery.innerHits.size = 4;
				}
				hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.EventFields.sentence, 1, 200);
				hlField.pre = "<b>";
				hlField.post = "</b>";
				hlField.highlightQuery = eventHlQuery;
			}

			// sentence highlighting
			SearchServerQuery sentenceHlQuery = serverCmd.namedQueries.get("sentenceHl");
			if (null != sentenceHlQuery) {
				NestedQuery nestedQuery = (NestedQuery) sentenceHlQuery;
				if (null != nestedQuery.innerHits.highlight) {
					HighlightCommand innerHlc = nestedQuery.innerHits.highlight;
					innerHlc.fields.get(0).pre = "<b>";
					innerHlc.fields.get(0).post = "</b>";
					innerHlc.fields.get(0).fragsize = 200;
					// basically specifies the maximum number of highlights
					nestedQuery.innerHits.size = 4;
				}
				hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.Nested.sentencestext, 1, 200);
				hlField.pre = "<b>";
				hlField.post = "</b>";
				hlField.highlightQuery = sentenceHlQuery;
			}

			// section highlighting
			SearchServerQuery sectionHlQuery = serverCmd.namedQueries.get("sectionHl");
			if (null != sectionHlQuery) {
				NestedQuery nestedQuery = (NestedQuery) sectionHlQuery;
				if (null != nestedQuery.innerHits.highlight) {
					HighlightCommand innerHlc = nestedQuery.innerHits.highlight;
					innerHlc.fields.get(0).pre = "<b>";
					innerHlc.fields.get(0).post = "</b>";
					innerHlc.fields.get(0).fragsize = 200;
					// basically specifies the maximum number of highlights
					nestedQuery.innerHits.size = 4;
				}
				hlField = hlc.addField(IIndexInformationService.PmcIndexStructure.Nested.sectionstext, 1, 200);
				hlField.pre = "<b>";
				hlField.post = "</b>";
				hlField.highlightQuery = sectionHlQuery;
			}

			serverCmd.addHighlightCmd(hlc);
		}

		switch (searchState.getSortCriterium()) {
		case DATE:
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure.date, SortOrder.DESCENDING);
			break;
		case DATE_AND_RELEVANCE:
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure.date, SortOrder.DESCENDING);
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure._score, SortOrder.DESCENDING);
			break;
		case RELEVANCE:
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure._score, SortOrder.DESCENDING);
		}

		serverCmd.filterReviews = searchState.isReviewsFiltered();

		return false;
	}
}
