package de.julielab.semedico.core.util;


public interface TermCountCursor {
    boolean forwardCursor();

    String getName();

    Number getFacetCount(String type);

    long size();

    boolean isValid();

    void reset();
}