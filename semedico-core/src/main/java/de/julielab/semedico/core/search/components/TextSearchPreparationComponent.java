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
import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Supplier;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.HighlightCommand.HlField;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.elastic.query.components.data.SortCommand.SortOrder;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.DocumentQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.DocumentSearchResult;
import de.julielab.semedico.core.services.SearchService.SearchOption;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

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
		SemedicoSearchCarrier<DocumentQuery, DocumentSearchResult> semCarrier = castCarrier(searchCarrier);
		Supplier<SearchServerRequest> s1 = () -> semCarrier.getSingleSearchServerRequest();
		Supplier<SearchState> s2 = () -> semCarrier.searchState;
		Supplier<ISemedicoQuery> s3 = () -> semCarrier.query;
		
		checkNotNull(s1, "Search Server Command", s2, "Search State", s3, "Search Query");
		stopIfError();

		SearchServerRequest serverCmd = s1.get();
		EnumSet<SearchOption> options = s3.get().getSearchOptions();
		serverCmd.rows = options.contains(SearchOption.HIT_COUNT) || options.contains(SearchOption.NO_FIELDS)? 0 : semCarrier.query.getResultSize();
		serverCmd.fieldsToReturn = options.contains(SearchOption.HIT_COUNT)|| options.contains(SearchOption.NO_FIELDS) ? Collections.emptyList() : Arrays.asList(IIndexInformationService.DATE, IIndexInformationService.pmcid,
				IIndexInformationService.PUBMED_ID, IIndexInformationService.TITLE, IIndexInformationService.AUTHORS,
				IIndexInformationService.GeneralIndexStructure.affiliation, IIndexInformationService.ABSTRACT,
				IIndexInformationService.GeneralIndexStructure.journaltitle,
				IIndexInformationService.GeneralIndexStructure.journalvolume,
				IIndexInformationService.GeneralIndexStructure.journalissue,
				IIndexInformationService.GeneralIndexStructure.journalpages);
		if (!options.contains(SearchOption.HIT_COUNT)|| options.contains(SearchOption.NO_HIGHLIGHTING)){
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

			serverCmd.addHighlightCmd(hlc);
		}

		SearchState searchState = s2.get();
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
