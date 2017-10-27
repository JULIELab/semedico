package de.julielab.semedico.core.search.components.data;

import java.util.List;

import de.julielab.semedico.core.facets.Facet;

public class SuggestionsSearchCommand {
	public String fragment;
	public List<Facet> facets;
}
