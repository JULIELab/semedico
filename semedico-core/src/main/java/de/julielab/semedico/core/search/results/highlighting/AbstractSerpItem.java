package de.julielab.semedico.core.search.results.highlighting;

import de.julielab.semedico.core.entities.docmods.DocModInfo;

/**
 * An interface for SEarch Result Page items. It contains the methods required to display summaries of search hits
 * on the search result page. This class is an extension point for document modules that should create a
 * SERP item the is appropriate for their document structure.
 */
public abstract class AbstractSerpItem {

    private String documentId;
    private DocModInfo docModInfo;

    public AbstractSerpItem(String documentId, DocModInfo docModInfo) {
        this.documentId = documentId;
        this.docModInfo = docModInfo;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * All SERP items need to implement a method to return highlights for their title. This may also be
     * the document title without any highlights if no query terms match the title. Should not return
     * <tt>null</tt>. If there is no title, return a placeholder instead.
     *
     * @return The highlighted document title.
     */
    public abstract Highlight getTitleHighlight();

    /**
     * Al SERP items need to implement a method to return their authors, possibly with hit highlights.
     * This method may return <tt>null</tt>.
     *
     * @return The authors of the document.
     */
    public abstract SerpHighlightList<AuthorHighlight> getAuthorHighlight();

    /**
     * All SERP items need to implement a method to return highlights of their text contents.
     * If there were no its within the main text, this method should return a small snippet of the text.
     * This method should not return <tt>null</tt>. If there is no document text, return a placeholder
     * instead.
     *
     * @return Text hit highlights.
     */
    public abstract SerpHighlightList getTextHighlight();

    /**
     * <p>
     * Returns the respective highlight of the field / type / document part indicated by <tt>highlightType</tt>.
     * The highlight types are document module specific. Each document module should check that the given parameter
     * value is valid and return an informative error message otherwise.</p>
     * <p>
     * The return value is either {@link Highlight} or {@link SerpHighlightList}. Each document module must
     * take care to return the value that it is using in its own SERP item Tapestry template.</p>
     *
     * @param highlightType The kind of highlight - title hits, document hits, author hits - to retrieve.
     * @return The requested highlight or <tt>null</tt>
     */
    public abstract ISerpHighlight getHighlight(String highlightType);

    /**
     * @return The information object of the document module this hits stems from.
     */
    DocModInfo getDocModInfo() {
        return docModInfo;
    }

    public void setDocModInfo(DocModInfo docModInfo) {
        this.docModInfo = docModInfo;
    }


}
