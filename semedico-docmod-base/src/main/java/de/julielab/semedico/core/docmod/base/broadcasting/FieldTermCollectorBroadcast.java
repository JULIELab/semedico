package de.julielab.semedico.core.docmod.base.broadcasting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldTermCollectorBroadcast implements IResultCollectorBroadcast {

    public static final String FIELDTERMSCOLLECTOR_NAME = "fieldtermscollector";
    /**
     * The {@link de.julielab.semedico.core.search.results.collectors.FieldTermCollector} builds a map that
     * associates the aggregation request names with their result. It is not required to collect multiple results
     * with one field term collector, and thus with a field term collector broadcast, but it is possible and might
     * lead to briefer code in some situations.
     */
    private List<String> aggregationRequestBaseNames;

    public FieldTermCollectorBroadcast() {
        aggregationRequestBaseNames = new ArrayList<>();
    }

    public FieldTermCollectorBroadcast(List<String> aggregationRequestBaseNames) {

        this.aggregationRequestBaseNames = aggregationRequestBaseNames;
    }

    public FieldTermCollectorBroadcast(String... aggregationRequestBaseNames) {
        this(Arrays.asList(aggregationRequestBaseNames));
    }

    public List<String> getAggregationRequestBaseNames() {

        return aggregationRequestBaseNames;
    }

    public void setAggregationRequestBaseNames(List<String> aggregationRequestBaseNames) {
        this.aggregationRequestBaseNames = aggregationRequestBaseNames;
    }

    @Override
    public String getResultBaseName() {
        return FIELDTERMSCOLLECTOR_NAME;
    }
}
