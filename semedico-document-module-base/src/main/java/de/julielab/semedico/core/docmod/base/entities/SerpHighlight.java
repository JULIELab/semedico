package de.julielab.semedico.core.docmod.base.entities;

/**
 * A simple highlight, an HTML string with embedded tags for the actual highlighting. This class is used
 * for singular-cardinality highlights like for the document title. It can, however, also be used when multiple
 * highlights exist but can be returned in a single string.
 */
public class SerpHighlight implements ISerpHighlight {
    /**
     * To be used when a highlight was requested for a field or document part for which no highlight exists
     * or that is not (yet) supported by the document module queried for this highlight.
     */
    public static final SerpHighlight EMPTY_HIGHLIGHT = new SerpHighlight("");
    private String highlight;

    public SerpHighlight(String highlight) {
        this.highlight = highlight;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
}
