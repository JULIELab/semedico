/**
 * SemedicoSearchResult.java
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
 * Creation date: 08.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.search.components.data;

import java.util.ArrayList;
import java.util.List;

import de.julielab.elastic.query.components.ISearchComponent.FacetIndexTermsChain;
import de.julielab.elastic.query.components.ISearchComponent.FieldTermsChain;
import de.julielab.elastic.query.components.data.FieldTermItem;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.FacetIndexTermsProcessComponent;
import de.julielab.semedico.core.search.components.FacetIndexTermsRetrievalComponent;
import de.julielab.semedico.core.util.LazyDisplayGroup;
import de.julielab.semedico.core.util.TripleStream;

/**
 * @author faessler
 * 
 */
public class LegacySemedicoSearchResult extends SemedicoSearchResult {
	public LazyDisplayGroup<HighlightedSemedicoDocument> documentHits;
	public List<TripleStream<String, Long, Long>> searchNodeTermCounts;
	public long totalNumDocs;
	public List<Label> indirectLinkLabels;
	public TripleStream<String, Long, Long> termDocumentFrequencies;
	/**
	 * The query for which this result is the response. This is currently only used by the ResultList to retrieve the
	 * original query that to the current result set.
	 */
	public ParseTree query;
	/**
	 * The single document (or <tt>null</tt>) resulting from an article search.
	 */
	public HighlightedSemedicoDocument semedicoDoc;
	/**
	 *  * Result from {@link FacetIndexTermsChain} and {@link FieldTermsChain}. This mechanics is older than the
	 * {@link #fieldTerms} related mechanics. For new search processes, prefer {@link #fieldTerms} over
	 * {@link #facetIndexTerms}.
	 * 
	 * @see FacetIndexTermsRetrievalComponent
	 * @see FacetIndexTermsProcessComponent
	 */
	public List<String> facetIndexTerms;
	/**
	 * The result of search chains that retrieve terms from a field in the index. This result includes not only the term
	 * itself but also some measures like its count or additional measures like the maximum score of documents a term
	 * occurs in. Used in {@link FieldTermsChain}, for example.
	 */
	public List<FieldTermItem> fieldTerms;
	public ArrayList<FacetTermSuggestionStream> suggestions;
	public String errorMessage;

	public LegacySemedicoSearchResult(ParseTree semedicoQuery) {
		this.query = semedicoQuery;
	}

	public void addSearchNodeTermCounts(TripleStream<String, Long, Long> termCounts) {
		if (null == searchNodeTermCounts)
			searchNodeTermCounts = new ArrayList<>();
		searchNodeTermCounts.add(termCounts);
	}
}
