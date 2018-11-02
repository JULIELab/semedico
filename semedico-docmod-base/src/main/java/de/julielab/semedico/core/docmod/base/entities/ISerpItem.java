package de.julielab.semedico.core.docmod.base.entities;

/**
 * An interface for SEarch Result Page items. It contains the methods required to display summaries of search hits
 * on the search result page. This class is an extension point for document modules that should create a
 * SERP item the is appropriate for their document structure.
 */
public interface ISerpItem {

    String getDocumentId();

    /**
     * <p>
     * Returns the respective highlight of the field / type / document part indicated by <tt>highlightType</tt>.
     * The highlight types are document module specific. Each document module should check that the given parameter
     * value is valid and return an informative error message otherwise.</p>
     * <p>
     * The return value is either {@link SerpHighlight} or {@link SerpHighlightList}. Each document module must
     * take care to return the value that it is using in its own SERP item Tapestry template.</p>
     *
     * @param highlightType The kind of highlight - title hits, document hits, author hits - to retrieve.
     * @return The requested highlight or <tt>null</tt>
     */
    ISerpHighlight getHighlight(String highlightType);

    /**
     * @return The information object of the document module this hits stems from.
     */
    DocModInfo getDocModInfo();


}
