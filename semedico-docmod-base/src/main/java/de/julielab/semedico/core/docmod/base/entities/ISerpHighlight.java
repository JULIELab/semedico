package de.julielab.semedico.core.docmod.base.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This interface exists to collect single highlight strings (e.g. for the highlighted document title) and
 * lists of highlights (e.g. the document snippets containing query terms) under a single umbrella. This is used
 * in {@link ISerpItem} to return highlights and lists of highlights by the same method {@link ISerpItem#getHighlight(String)}.
 */
public interface ISerpHighlight {

    default Highlight single() {
        if (getClass().equals(Highlight.class))
            return (Highlight) this;
        else if (getClass().equals(SerpHighlightList.class)) {
            SerpHighlightList list = (SerpHighlightList) this;
            if (!list.isEmpty())
                return list.get(0);
            return Highlight.EMPTY_HIGHLIGHT;
        }
        throw new IllegalStateException("This is an " + getClass().getName() + " highlight and not applicable to the called method.");
    }

    default SerpHighlightList list() {
        if (getClass().equals(SerpHighlightList.class))
            return (SerpHighlightList) this;
        else if (getClass().equals(Highlight.class)) {
            return new SerpHighlightList(Arrays.asList((Highlight) this));
        }
        throw new IllegalStateException("This is an " + getClass().getName() + " highlight and not applicable to the called method.");
    }

    default AuthorHighlight authors() {
        return (AuthorHighlight) this;
    }
}
