package de.julielab.semedico.core.search.components.data;

import de.julielab.semedico.core.facets.Facet;

import java.util.List;

public class SuggestionsSearchCommand {
	public String fragment;
	public List<Facet> facets;
}
