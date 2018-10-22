package de.julielab.semedico.core.docmod.base.broadcasting;

import java.util.ArrayList;
import java.util.List;

public class FieldTermCollectorBroadcast implements IResultCollectorBroadcast {

    /**
     * The {@link de.julielab.semedico.core.search.results.collectors.FieldTermCollector} builds a map that
     * associates the aggregation request names with their result. It is not required to collect multiple results
     * with one field term collector, and thus with a field term collector broadcast, but it is possible and might
     * lead to briefer code in some situations.
     */
    private List<String> aggregationRequestsNames;

    public FieldTermCollectorBroadcast() {
        aggregationRequestsNames = new ArrayList<>();
    }

    public FieldTermCollectorBroadcast(List<String> aggregationRequestsNames) {

        this.aggregationRequestsNames = aggregationRequestsNames;
    }

    public List<String> getAggregationRequestsNames() {

        return aggregationRequestsNames;
    }

    public void setAggregationRequestsNames(List<String> aggregationRequestsNames) {
        this.aggregationRequestsNames = aggregationRequestsNames;
    }

    @Override
    public String getResultBaseName() {
        return "fieldtermscollector";
    }
}
