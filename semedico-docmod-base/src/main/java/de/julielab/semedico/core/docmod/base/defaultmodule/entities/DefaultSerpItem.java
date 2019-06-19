package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.entities.docmods.DocModInfo;

import java.util.HashMap;
import java.util.Map;

public class DefaultSerpItem extends AbstractSerpItem {

    private Map<String, ISerpHighlight> highlights;

    public DefaultSerpItem(String docId, DocModInfo docModInfo) {
        super(docId, docModInfo);
        highlights = new HashMap<>();
    }

    @Override
    public Highlight getTitleHighlight() {
        return (Highlight) getHighlight(DefaultDocumentModule.FIELD_TITLE);
    }

    @Override
    public SerpHighlightList<AuthorHighlight> getAuthorHighlight() {
        return (SerpHighlightList<AuthorHighlight>) getHighlight(DefaultDocumentModule.FIELD_AUTHORS);
    }

    @Override
    public SerpHighlightList getTextHighlight() {
        return (SerpHighlightList) getHighlight(DefaultDocumentModule.FIELD_TEXT);
    }

    /**
     * The {@link DefaultSerpItem} has the highlight types {@link DefaultDocumentModule#FIELD_AUTHORS}, {@link DefaultDocumentModule#FIELD_TEXT} and {@link DefaultDocumentModule#FIELD_TEXT}.
     *
     * @param highlightType The kind of highlight - title hits, document hits, author hits - to retrieve.
     * @return The requested highlights or <tt>null</tt>.
     */
    @Override
    public ISerpHighlight getHighlight(String highlightType) {
        return highlights.get(highlightType);
    }


    public void addHighlight(String highlightField, String highlight, float score) {
        highlights.put(highlightField, new Highlight(highlight, highlightField, score));
    }

    public void addHighlight(String highlightField, ISerpHighlight highlight) {
        highlights.put(highlightField, highlight);
    }
}
