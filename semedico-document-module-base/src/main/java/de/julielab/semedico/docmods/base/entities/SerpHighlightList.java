package de.julielab.semedico.docmods.base.entities;

import java.util.Collections;
import java.util.List;

/**
 * This class is used for fields where we expect multiple highlight items that should be looped over. The most
 * prominent example being highlighted document text snippets.
 */
public class SerpHighlightList implements ISerpHighlight {
    /**
     * To be used when a highlight was requested for a field or document part for which no highlight exists
     * or that is not (yet) supported by the document module queried for this highlight.
     */
    public static final SerpHighlightList EMPTY_HIGHLIGHT_LIST = new SerpHighlightList(Collections.emptyList());
    private List<SerpHighlight> highlights;

    public SerpHighlightList(List<SerpHighlight> highlights) {
        this.highlights = highlights;
    }

    public List<SerpHighlight> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<SerpHighlight> highlights) {
        this.highlights = highlights;
    }
}
