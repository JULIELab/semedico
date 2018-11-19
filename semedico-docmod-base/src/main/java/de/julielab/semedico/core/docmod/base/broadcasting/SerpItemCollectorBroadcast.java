package de.julielab.semedico.core.docmod.base.broadcasting;

/**
 * <p>This small class indicates that SERP (search result page) items should be collected from the query result
 * sent in the broadcast. This is just a signal for the document modules to return their SERP item search result
 * collectors.</p>
 */
public class SerpItemCollectorBroadcast implements IResultCollectorBroadcast {
    @Override
    public String getResultBaseName() {
        return "serpitems";
    }
}
