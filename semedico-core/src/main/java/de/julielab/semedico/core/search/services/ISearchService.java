/**
 * ISearchService.java
 * <p>
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * <p>
 * Author: faessler
 * <p>
 * Current version: 1.0
 * Since version:   1.0
 * <p>
 * Creation date: 09.04.2013
 */

/**
 *
 */
package de.julielab.semedico.core.search.services;

import de.julielab.semedico.core.entities.documentmodules.QueryBroadcastRequest;
import de.julielab.semedico.core.entities.documentmodules.QueryTarget;
import de.julielab.semedico.core.search.broadcasting.IAggregationBroadcast;
import de.julielab.semedico.core.search.broadcasting.IResultCollectorBroadcast;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoResultCollection;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.search.results.SingleSearchResult;
import de.julielab.semedico.core.search.services.SearchService.SearchOption;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author faessler
 *
 */
public interface ISearchService {

    <C extends ISemedicoSearchCarrier<?, ?>, R extends SemedicoSearchResult> Future<SingleSearchResult<R>> search(
            ISemedicoQuery query,
            EnumSet<SearchOption> searchOptions,
            SearchResultCollector<C, R> collector
    );

    SemedicoResultCollection search(
            ISemedicoQuery query,
            EnumSet<SearchOption> searchOptions,
             SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>,
                    ? super SemedicoSearchResult>... collectors
    );

    SemedicoResultCollection search(
            List<ISemedicoQuery> queries,
            List<EnumSet<SearchOption>> searchOptionList,
            List<List<SearchResultCollector<? super ISemedicoSearchCarrier<?, ?>,
                    ? super SemedicoSearchResult>>> collectorLists);

    List<ISemedicoQuery> broadcastQuery(ISemedicoQuery query,
                                        EnumSet<SearchOption> searchOptions,
                                        List<QueryTarget> queryTargets,
                                        List<IAggregationBroadcast> aggregationBroadcasts,
                                        List<IResultCollectorBroadcast> resultCollectorBroadcasts);
}
