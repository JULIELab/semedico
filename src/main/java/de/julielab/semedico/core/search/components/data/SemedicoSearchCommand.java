/**
 * QueryAnalysisCommand.java
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
package de.julielab.semedico.core.search.components.data;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.julielab.elastic.query.components.data.FieldTermsCommand;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.query.translation.SearchTask;

/**
 * @author faessler
 * 
 * @deprecated Use subclasses of {@link ISemedicoQuery} instead
 */
@Deprecated
public class SemedicoSearchCommand {
	/**
	 * Semedico representation of a user query. Used for each actual search (as opposed to single document requests, for
	 * example).
	 */
	public ParseTree semedicoQuery;
	/**
	 * The {@link SearchTask} to perform with his query. The task influences the queries being used.
	 */
	public SearchTask task;
	public String index;
	/**
	 * Type indexes and index types that should be searched for this search.
	 */
	public List<String> indexTypes;
	/**
	 * A single document to be retrieved.
	 */
	public String documentId;
	/**
	 * The facets to count terms for, respecting taxonomic facets in that only the currently visible terms are counted.
	 */
	public List<UIFacet> facetsToCount;
	/**
	 * A search command for determination of indirect links between search nodes, i.e. the documents resulting from
	 * multiple search queries.
	 */
	public SearchNodeSearchCommand nodeCmd;
	/**
	 * The maximum number of highlighting-snippets in text search
	 */
	public int hlsnippets;
	/**
	 * Facets for which we want to retrieve <it>all</it> term IDs found in the respective facet source fields in the
	 * search index. In other words, "get the IDs of all terms in these facets that are actually in the search index" .
	 * Used for filtering purposes for term suggestions.
	 * 
	 * @deprecated It was a bit over-engineered to create a component of its own and this input field. The component
	 *             just creates FacetCommands which can also be done in the SearchService with less
	 *             configuration-overhead. Do it this way, instead.
	 */
	@Deprecated
	public List<Facet> facetsToGetAllIndexTerms;

	/**
	 * Used to get back the terms in an index field. Allows for some configuration on ordering and the result size.
	 */
	public FieldTermsCommand fieldTermsCmd;

	public SuggestionsSearchCommand suggCmd;

	/**
	 * The set of index fields that will be searched upon. When left empty, the default will be applied which is
	 * determined depending on the terms forming the query. This setting can be used to overwrite this default
	 */
	public Collection<String> searchFieldFilter = Collections.emptySet();
	/**
	 * Specifies a limit of documents to return for this search. This does influence the maximum number of found
	 * documents.
	 */
	public int limit;
	/**
	 * The number of document hits to return. This can be set to 0 for performance reasons when the actual document hits
	 * are not important.
	 */
	public int docSize;

	public SemedicoSearchCommand() {
		documentId = null;
		hlsnippets = Integer.MIN_VALUE;
		docSize = Integer.MIN_VALUE;
	}

	public void addFacetToCount(UIFacet uiFacet) {
		if (null == facetsToCount)
			// We would rather use a plain list but this results in API
			// incompatibility.
			facetsToCount = new FacetGroup<UIFacet>("facetsToCount", -1);
		facetsToCount.add(uiFacet);
	}

}
