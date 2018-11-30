/**
 * SearchCarrier.java
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
 * Creation date: 06.04.2013
 */

/**
 *
 */
package de.julielab.semedico.core.search.components.data;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import de.julielab.elastic.query.components.data.ElasticSearchCarrier;
import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.search.components.QueryAnalysisCommand;
import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.search.query.TranslatedQuery;
import de.julielab.semedico.core.search.searchresponse.IElasticServerResponse;
import de.julielab.semedico.core.search.services.SearchService.SearchOption;

/**
 * @author faessler
 */

public class SemedicoESSearchCarrier extends ElasticSearchCarrier<IElasticServerResponse>
        implements ISemedicoSearchCarrier<AbstractSemedicoElasticQuery, IElasticServerResponse> {

    public QueryAnalysisCommand queryAnalysisCmd;
    /**
     * Get rid of general-purpose objects and go for stronger typing to make
     * connections clear
     */
    @Deprecated
    public SemedicoSearchCommand searchCmd;
    private SearchState searchState;
    private AbstractUserInterfaceState uiState;
    private List<AbstractSemedicoElasticQuery> queries;
    private List<EnumSet<SearchOption>> searchOptions;
    private List<TranslatedQuery> translatedQueries;
    public SemedicoESSearchCarrier(String chainName) {
        super(chainName);
    }

    public SearchState getSearchState() {
        return searchState;
    }

    public void setSearchState(SearchState searchState) {
        this.searchState = searchState;
    }

    public AbstractUserInterfaceState getUiState() {
        return uiState;
    }

    public void setUiState(AbstractUserInterfaceState uiState) {
        this.uiState = uiState;
    }

    public List<EnumSet<SearchOption>> getSearchOptions() {
        return searchOptions;
    }

    public void setSearchOptions(List<EnumSet<SearchOption>> searchOptions) {
        this.searchOptions = searchOptions;
    }

    public EnumSet<SearchOption> getSearchOptions(int index) {
        return searchOptions.get(index);
    }

    public void addSearchOptions(EnumSet<SearchOption> searchOptions) {
        if (this.searchOptions == null)
            this.searchOptions = new ArrayList<>();
        this.searchOptions.add(searchOptions);
    }

    public void setElapsedTime() {
        // if (null != result)
        // result.setElapsedTime(sw.getTime());
        sw.stop();
    }

    public void addTranslatedQuery(TranslatedQuery translatedQuery) {
        if (translatedQueries == null)
            translatedQueries = new ArrayList<>();
        translatedQueries.add(translatedQuery);
    }

    public void addQuery(AbstractSemedicoElasticQuery query) {
        if (queries == null)
            queries = new ArrayList<>();
        queries.add(query);
    }

    @Override
    public List<AbstractSemedicoElasticQuery> getQueries() {
        return queries;
    }

    @Override
    public void setQueries(List<AbstractSemedicoElasticQuery> queries) {
        this.queries = queries;
    }

    @Override
    public AbstractSemedicoElasticQuery getQuery(int index) {
        return queries.get(index);
    }

    @Override
    public IElasticServerResponse getSearchResponse(int index) {
        return null;
    }


    public List<TranslatedQuery> getTranslatedQueries() {
        return translatedQueries;
    }

    public void setTranslatedQueries(List<TranslatedQuery> translatedQueries) {
        this.translatedQueries = translatedQueries;
    }

    public TranslatedQuery getTranslatedQuery(int index) {
        return translatedQueries.get(index);
    }
}
