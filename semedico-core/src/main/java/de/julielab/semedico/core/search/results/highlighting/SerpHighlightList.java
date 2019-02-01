package de.julielab.semedico.core.search.results.highlighting;


import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is used for fields where we expect multiple highlight items that should be looped over. The most
 * prominent example being highlighted document text snippets.
 */
public class SerpHighlightList<T extends Highlight> extends ArrayList<T> implements ISerpHighlight {
    /**
     * To be used when a highlight was requested for a field or document part for which no highlight exists
     * or that is not (yet) supported by the document module queried for this highlight.
     */
    public static final SerpHighlightList EMPTY_HIGHLIGHT_LIST = new SerpHighlightList();

    public SerpHighlightList(int initialCapacity) {
        super(initialCapacity);
    }

    public SerpHighlightList() {
    }

    public SerpHighlightList(Collection<? extends T> c) {
        super(c);
    }
}
