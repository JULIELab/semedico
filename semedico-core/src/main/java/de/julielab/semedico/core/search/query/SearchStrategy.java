package de.julielab.semedico.core.search.query;

/**
 * <p>Represents a query strategy.</p><p>The default is to search the user query in some fields. Other strategies
 * may use some specific field weighting, document classification or query expansion and other techniques to optimize the search result
 * to a specific information need, e.g. the search for precision medicine documents.</p>
 */
public interface SearchStrategy {
    String name();
}
