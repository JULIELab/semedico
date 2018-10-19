package de.julielab.semedico.docmods.base.entities;

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
