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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import org.apache.tapestry5.ioc.annotations.Symbol;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.HighlightCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.HighlightCommand.HlField;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import de.julielab.scicopia.core.elasticsearch.legacy.NestedQuery;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.GeneralIndexStructure;

/**
 * @author faessler
 * 
 */
public class TextSearchPreparationComponent extends AbstractSearchComponent {

	private int maxDocs;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface TextSearchPreparation {
		//
	}

	public TextSearchPreparationComponent(@Symbol(SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS) int maxDocs) {
		this.maxDocs = maxDocs;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.search.components.ISearchComponent#process(de.
	 * julielab .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommand();
		SearchState searchState = semCarrier.getSearchState();
		if (null == serverCmd) {
			throw new IllegalArgumentException("Non-null " + SearchServerCommand.class.getName()
					+ " object is expected which knows about the sort criterium to use and whether the review filter should be active. However, no such object is present.");
		}
		if (null == searchState) {
			throw new IllegalArgumentException(
					"The search state is null. However, it is required to get the user specified search details.");
		}
		if (serverCmd.rows == Integer.MIN_VALUE) {
			serverCmd.rows = maxDocs;
		}
		serverCmd.setFieldsToReturn(Arrays.asList(IIndexInformationService.GeneralIndexStructure.date, IIndexInformationService.GeneralIndexStructure.pmcid,
				IIndexInformationService.GeneralIndexStructure.pmid, IIndexInformationService.GeneralIndexStructure.title, IIndexInformationService.GeneralIndexStructure.authors,
				IIndexInformationService.GeneralIndexStructure.affiliation, IIndexInformationService.GeneralIndexStructure.abstracttext,
				IIndexInformationService.GeneralIndexStructure.journaltitle,
				IIndexInformationService.GeneralIndexStructure.journalvolume,
				IIndexInformationService.GeneralIndexStructure.journalissue,
				IIndexInformationService.GeneralIndexStructure.journalpages));
		{
			HlField hlField;
			HighlightCommand hlc = new HighlightCommand();
			hlField = hlc.addField(GeneralIndexStructure.title, SemedicoSearchConstants.HIGHLIGHT_SNIPPETS, 1000);
			hlField.pre = "<b>";
			hlField.post = "</b>";

			// the following fields are not searched themselves but by custom
			// _all fields (docmeta and mesh). Thus, "requirefieldmatch" must be
			// set to false in order to still get highlightings for the
			// individual fields (which we need to explain why a document was
			// hit)
			hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.authors);
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

			// sentence highlighting
			QueryBuilder sentenceHlQuery = serverCmd.namedQueries.get("sentenceHl");
//			SearchServerQuery sentenceHlQuery = null; 
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
			QueryBuilder sectionHlQuery = serverCmd.namedQueries.get("sectionHl");
//			SearchServerQuery sectionHlQuery = null;
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
				hlField = hlc.addField(IIndexInformationService.PmcIndexStructure.Nested.SECTIONSTEXT, 1, 200);
				hlField.pre = "<b>";
				hlField.post = "</b>";
				hlField.highlightQuery = sectionHlQuery;
			}

			serverCmd.addHighlightCmd(hlc);
		}

		switch (searchState.getSortCriterium()) {
		case DATE:
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure.date, SortOrder.DESC);
			break;
		case DATE_AND_RELEVANCE:
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure.date, SortOrder.DESC);
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure._score, SortOrder.DESC);
			break;
		case RELEVANCE:
			serverCmd.addSortCommand(IIndexInformationService.GeneralIndexStructure._score, SortOrder.DESC);
		}

		serverCmd.index = semCarrier.getSearchCommand().getIndex();

		return false;
	}
}
