package de.julielab.semedico.core.search;

import de.julielab.semedico.core.search.services.SearchService;

/**
 * The supported index technologies. Used by {@link de.julielab.semedico.core.search.query.ISemedicoQuery} and
 * {@link SearchService} to determine the correct search chain. Search chains
 * are defined in {@link de.julielab.semedico.core.search.services.SemedicoSearchModule}.
 */
public enum ServerType {
    ELASTIC_SEARCH, TOPIC_MODEL
}
