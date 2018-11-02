package de.julielab.semedico.core.docmod.base.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private List<Highlight> highlights;

    public SerpHighlightList(List<Highlight> highlights) {
        this.highlights = highlights;
    }

    public void addHighlight(Highlight highlight) {
        if (highlights == null)
            highlights = new ArrayList<>();
        highlights.add(highlight);
    }

    public List<Highlight> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<Highlight> highlights) {
        this.highlights = highlights;
    }
}
