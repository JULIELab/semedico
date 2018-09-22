package de.julielab.semedico.core.search.services;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;

public interface ISearchTermProvider {
	/**
	 * Returns a search server (i.e. Lucene) term that can be used for the concrete search. Here, term means the string
	 * form of an index term, not a Semedico semantic unit.
	 * 
	 * @param concept
	 * @param facet
	 * @return
	 */
	String getSearchTerm(IConcept concept, Facet facet);

	/**
	 * Returns a search server (i.e. Lucene) term that can  be used for the concrete search.
	 * @param termId
	 * @return
	 */
	String getSearchTerm(String termId);
}
