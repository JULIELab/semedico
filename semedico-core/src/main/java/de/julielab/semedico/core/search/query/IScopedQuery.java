package de.julielab.semedico.core.search.query;

import de.julielab.semedico.core.search.SearchScope;

import java.util.Set;

/**
 * This interface is not usable for queries directly and is thus package-private. It should only be used for extension
 * for other query interfaces that support scoping. Scoping means to focus on some part of a document, e.g. sentences
 * or paragraphs, but also complex structures like relations. A scoped query may define one or multiple such scopes,
 * pre-defined in {@link SearchScope}. Then, the defined scopes are searched.
 */
interface IScopedQuery {
    Set<SearchScope> getScopes();
}
