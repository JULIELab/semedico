package de.julielab.semedico.docmods.base.entities;

import java.util.List;

/**
 * An interface for SEarch Result Page items. It contains the methods required to display summaries of search hits
 * on the search result page. This class is an extension point for document modules that should create a
 * SERP item the is appropriate for their document structure.
 */
public interface ISerpItem {

    String getDocumentId();

    /**
     * Returns the respective highlight of the field / type / document part indicated by <tt>highlightType</tt>.
     * The return value is either {@link SerpHighlight} or {@link SerpHighlightList}. Each document module must
     * take care to return the value that it is using in its own SERP item Tapestry template.
     * @param highlightType The kind of highlight - title hits, document hits, author hits - to retrieve.
     * @return The requested highlight or <tt>null</tt>
     */
    ISerpHighlight getHighlight(SerpHighlightType highlightType);

    /**
     *
     * @return The information object of the document module this hits stems from.
     */
    DocumentModuleInfo getDocModInfo();

    enum SerpHighlightType {
        TITLE, AUTHORS
    }
}
