package de.julielab.semedico.core.search.results;

import java.util.Map;

public class SemedicoResultCollection extends SemedicoSearchResult {
    private Map<Object, SemedicoSearchResult> collectionMap;

    public SemedicoResultCollection(Map<Object, SemedicoSearchResult> collectionMap) {
        this.collectionMap = collectionMap;

    }

    /**
     * Adds <code>result</code> to the collection under its <code>name</code>.
     *
     * @param name   The name object.
     * @param result The search result.
     */
    public void put(Object name, SemedicoSearchResult result) {
        collectionMap.put(name, result);
    }

    /**
     * Returns the result with the name <code>name</code>. Note that <code>name</code> is of type <code>Object</code> and
     * thus accepts all parameters, even though they might not match any actual type in this collection.
     *
     * @param name The name of the result to get.
     * @return The result with name <code>name</code> or null.
     */
    public SemedicoSearchResult getResult(Object name) {
        return collectionMap.get(name);
    }
}
