package de.julielab.semedico.core.docmod.base.entities;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used for fields where we expect multiple highlight items that should be looped over. The most
 * prominent example being highlighted document text snippets.
 */
public class SerpHighlightList extends ArrayList<Highlight> implements ISerpHighlight {
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

    public SerpHighlightList(@NotNull Collection<? extends Highlight> c) {
        super(c);
    }
}
