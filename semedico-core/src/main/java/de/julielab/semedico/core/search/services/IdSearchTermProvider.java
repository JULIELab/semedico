package de.julielab.semedico.core.search.services;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;

/**
 * The default search term provider. Returns the ID of the provided concept as search term, regardless of the facet.
 * 
 * @author faessler
 * 
 */
public class IdSearchTermProvider implements ISearchTermProvider {

	@Override
	public String getSearchTerm(IConcept concept, Facet facet) {
		return concept.getId();
	}

	@Override
	public String getSearchTerm(String termId) {
		return termId;
	}

}
