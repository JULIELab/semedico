package de.julielab.semedico.core.docmod.base.broadcasting;

import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

import java.util.*;

public class QueryBroadcastResult {
    private List<ISemedicoQuery> queries;
    private Map<ISemedicoQuery, List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>>> resultCollectors;

    public Map<ISemedicoQuery, List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>>> getResultCollectors() {
        return resultCollectors;
    }

    public void addSearchResultCollector(ISemedicoQuery query, SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult> resultCollector) {
        if (resultCollectors == null)
            resultCollectors = new TreeMap<>(Comparator.comparingInt(System::identityHashCode));
        final List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>> collectorsForQuery = resultCollectors.compute(query, (q, l) -> {
            List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>> collectors = l;
            if (collectors == null)
                collectors = new ArrayList<>();
            return collectors;
        });
        collectorsForQuery.add(resultCollector);
    }

    public List<ISemedicoQuery> getQueries() {
        return queries;
    }

    public ISemedicoQuery getQuery(int index) {
        return queries.get(index);}

    public List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>> getResultCollectors(ISemedicoQuery query) {
        return resultCollectors.get(query);
    }

    public void addQuery(ISemedicoQuery queryClone) {
        if (queries == null)
            queries = new ArrayList<>();
        queries.add(queryClone);
    }
}
