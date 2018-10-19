package de.julielab.semedico.docmods.base.entities;

/**
 * This interface exists to collect single highlight strings (e.g. for the highlighted document title) and
 * lists of highlights (e.g. the document snippets containing query terms) under a single umbrella. This is used
 * in {@link ISerpItem} to return highlights and lists of highlights by the same method {@link ISerpItem#getHighlight(ISerpItem.SerpHighlightType)}.
 */
public interface ISerpHighlight {
}
