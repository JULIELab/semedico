package de.julielab.semedico.core.docmod.base.broadcasting;

import de.julielab.java.utilities.prerequisites.PrerequisiteChecker;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoESSearchResult;

import java.util.*;

public class QueryBroadcastResult {
    private List<ISemedicoQuery> queries;
    private Map<ISemedicoQuery, List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoESSearchResult>>> resultCollectors;

    public Map<ISemedicoQuery, List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoESSearchResult>>> getResultCollectors() {
        return resultCollectors;
    }

    public void addSearchResultCollector(ISemedicoQuery query, SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoESSearchResult> resultCollector) {
        if (resultCollectors == null)
            resultCollectors = new TreeMap<>(Comparator.comparingInt(System::identityHashCode));
        final List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoESSearchResult>> collectorsForQuery = resultCollectors.compute(query, (q, l) -> {
            List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoESSearchResult>> collectors = l;
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

    public List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoESSearchResult>> getResultCollectors(ISemedicoQuery query) {
        PrerequisiteChecker.checkThat().notEmpty(resultCollectors.get(query)).withNames("Result collectors for query " + query).execute();
        return resultCollectors.get(query);
    }

    public void addQuery(ISemedicoQuery queryClone) {
        if (queries == null)
            queries = new ArrayList<>();
        queries.add(queryClone);
    }
}
