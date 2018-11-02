package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import de.julielab.semedico.core.docmod.base.entities.*;

import java.util.HashMap;
import java.util.Map;

public class DefaultSerpItem implements ISerpItem {

    private DocModInfo docModInfo;
    private String docId;
    private Map<String, ISerpHighlight> highlights;

    public DefaultSerpItem(DocModInfo docModInfo, String docId) {
        this.docModInfo = docModInfo;
        this.docId = docId;
        highlights = new HashMap<>();
    }

    @Override
    public String getDocumentId() {
        return docId;
    }

    @Override
    public ISerpHighlight getHighlight(String highlightType) {
        return null;
    }

    @Override
    public DocModInfo getDocModInfo() {
        return docModInfo;
    }

    public void addHighlight(String highlightField, String highlight, float score) {
        highlights.put(highlightField, new Highlight(highlight, highlightField, score));
    }
}
