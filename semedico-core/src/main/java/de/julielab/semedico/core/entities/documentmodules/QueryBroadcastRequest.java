package de.julielab.semedico.core.entities.documentmodules;

import java.util.ArrayList;
import java.util.List;

public class QueryBroadcastRequest {
    private List<QueryTarget> queryTargets;

    public List<QueryTarget> getQueryTargets() {
        return queryTargets;
    }

    public void setQueryTargets(List<QueryTarget> queryTargets) {
        this.queryTargets = queryTargets;
    }
}
