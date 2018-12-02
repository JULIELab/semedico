package de.julielab.semedico.core.concepts;

import java.util.Collection;
import java.util.List;

import de.julielab.semedico.core.facets.Facet;

public interface IConcept {

	ConceptType getConceptType();
	
	void addFacet(Facet facet);

	/**
	 * Returns the first facet that is <b>not</b> the keyword facet, if such a facet exists for this term.
	 * 
	 * @return
	 */
	Facet getFirstFacet();

	List<Facet> getFacets();

	String getId();

	String getPreferredName();

	boolean isContainedInFacet(Facet otherFacet);

	void setFacets(List<Facet> facets);

	void setPreferredName(String preferredName);

	void setId(String id);

	/**
	 * Name other than the preferred name that mean the same thing.
	 */
	List<String> getSynonyms();

	/**
	 * @return
	 */
	List<String> getDescriptions();

	void setDescription(List<String> description);

	/**
	 * <p>
	 * Returns a list of all defined or observed textual occurrences of this term. This includes the preferred name, its
	 * synonyms and writing variants of those.
	 * </p>
	 * 
	 * @return All textual occurrences of this term.
	 * @see #getPreferredName()
	 * @see #getSynonyms()
	 */
	List<String> getOccurrences();

	String getDescription();

	boolean isNonDatabaseConcept();

	void setNonDatabaseConcept(boolean isNonDatabaseTerm);

	String getDisplayName();
	
	/**
	 * A string that serves as qualification to the term to distinguish it from other terms that are very similar on
	 * first sight. This is currently used for genes with the same name. The qualifier is the species of the gene.
	 */
	String[] getQualifiers();

	boolean isCoreTerm();
}
