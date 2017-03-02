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
package de.julielab.semedico.core.search.components;

import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.ABSTRACT;
import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.TITLE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.HighlightCommand;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.HighlightCommand.HlField;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.SemedicoSearchConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * @author faessler
 * 
 */
public class ArticleSearchPreparationComponent extends AbstractSearchComponent {

	private ITermService termService;
	private Logger log;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ArticleSearchPreparation {
		//
	}

	public ArticleSearchPreparationComponent(Logger log, ITermService termService) {
		this.log = log;
		this.termService = termService;
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
		String documentId = semCarrier.searchCmd.documentId;
		if (null == documentId || documentId.length() == 0)
			throw new IllegalArgumentException("The document ID of the article to load is required.");

		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();

		String docIdString = String.valueOf(documentId);
		TermQuery docIdQuery = new TermQuery();
		docIdQuery.term = docIdString;
		docIdQuery.field = "_id";
		if (null == serverCmd.query) {
			serverCmd.query = docIdQuery;
			log.debug("Fetching article with _id {} without highlighting because no query was given.", documentId);
		} else {
			log.debug("Fetching article with _id {} and highlighting.", documentId);
			// Highlighting only makes sense when we have a query to highlight
			// against.
			HighlightCommand hlc = new HighlightCommand();
			HlField hlField;
			// setting "fragnum" to zero causes the whole field string to be highlighted in elastic search
			hlField = hlc.addField(TITLE, SemedicoSearchConstants.HIGHLIGHT_SNIPPETS, 0);
			hlField.fragnum = 0;
			hlField.pre = "<span class=\"highlightFull\">";
			hlField.post = "</span>";
			hlField.requirefieldmatch = false;

			hlField = hlc.addField(ABSTRACT, SemedicoSearchConstants.HIGHLIGHT_SNIPPETS, 0);
			hlField.fragnum = 0;
			hlField.pre = "<span class=\"highlightFull\">";
			hlField.post = "</span>";
			hlField.requirefieldmatch = false;

			// event highlighting
			SearchServerQuery eventHlQuery = serverCmd.namedQueries.get("eventHl");
			if (null != eventHlQuery) {
				NestedQuery nestedQuery = (NestedQuery)eventHlQuery;
				if(null != nestedQuery.innerHits.highlight){
					HighlightCommand innerHlc = nestedQuery.innerHits.highlight;
					innerHlc.fields.get(0).pre = "<span class=\"highlightFull\">";
					innerHlc.fields.get(0).post = "</span>";
					// basically specifies the maximum number of highlights
					nestedQuery.innerHits.size = 20;
				}
				hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.EventFields.sentence, 1, 1000);
				hlField.highlightQuery = eventHlQuery;
			}

			// sentence highlighting
			SearchServerQuery sentenceHlQuery = serverCmd.namedQueries.get("sentenceHl");
			if (null != sentenceHlQuery) {
				NestedQuery nestedQuery = (NestedQuery)sentenceHlQuery;
				if(null != nestedQuery.innerHits.highlight){
					HighlightCommand innerHlc = nestedQuery.innerHits.highlight;
					innerHlc.fields.get(0).pre = "<span class=\"highlightFull\">";
					innerHlc.fields.get(0).post = "</span>";
					// basically specifies the maximum number of highlights
					nestedQuery.innerHits.size = 20;
				}
				hlField = hlc.addField(IIndexInformationService.GeneralIndexStructure.Nested.sentencestext, 1, 1000);
				hlField.highlightQuery = sentenceHlQuery;
			}

			serverCmd.addHighlightCmd(hlc);

			BoolClause idclause = new BoolClause();
			idclause.occur = Occur.FILTER;
			idclause.addQuery(docIdQuery);
			BoolClause hlClause = new BoolClause();
			hlClause.occur = Occur.SHOULD;
			hlClause.addQuery(serverCmd.query);
			BoolQuery boolQuery = new BoolQuery();
			boolQuery.addClause(idclause);
			boolQuery.addClause(hlClause);
			serverCmd.query = boolQuery;
		}
		serverCmd.rows = 1;
		serverCmd.index = IIndexInformationService.Indexes.documents;
		// solrCmd.addFilterQuery(idField + ":" + documentId);
		serverCmd.indexTypes = semCarrier.searchCmd.indexTypes;

		return false;
	}

}
