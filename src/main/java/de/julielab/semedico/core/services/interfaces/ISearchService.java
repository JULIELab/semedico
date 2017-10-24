/**
 * ISearchService.java
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
 * Creation date: 09.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.core.services.interfaces;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;

import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.SingleSearchResult;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoResultCollection;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.services.SearchService.SearchOption;

/**
 * @author faessler
 * 
 */
public interface ISearchService
{

	<R extends SemedicoSearchResult> Future<SingleSearchResult<R>> search(ISemedicoQuery query, EnumSet<SearchOption> searchOptions,
			SearchResultCollector<R> collector);

	Future<SemedicoResultCollection> search(ISemedicoQuery query, EnumSet<SearchOption> searchOptions,
			@SuppressWarnings("unchecked") SearchResultCollector<? extends SemedicoSearchResult>... collectors);

	Future<SemedicoResultCollection> search(List<ISemedicoQuery> queries, List<EnumSet<SearchOption>> searchOptionList,
			@SuppressWarnings("unchecked") List<SearchResultCollector<? extends SemedicoSearchResult>>... collectorLists);
}
