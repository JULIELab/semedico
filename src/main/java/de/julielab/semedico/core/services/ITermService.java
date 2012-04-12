package de.julielab.semedico.core.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.ITaxonomy;

public interface ITermService extends ITaxonomy, IStringTermService {

	public void readAllTerms() throws SQLException;

	public void readTermsInFacet(Facet facet) throws SQLException;

	// public IFacetTerm getTermWithInternalIdentifier(String id);
	public IFacetTerm readTermWithInternalIdentifier(String id)
			throws SQLException;

	public IFacetTerm readTermWithId(Integer id) throws SQLException;

	public Collection<IFacetTerm> getRegisteredTerms();

	public void insertTerm(IFacetTerm term) throws SQLException;

	public void insertTerm(IFacetTerm term, List<String> occurrences)
			throws SQLException;

	public void insertIndexOccurrencesForTerm(IFacetTerm term,
			Collection<String> indexOccurrences) throws SQLException;

	public void registerTerm(IFacetTerm term);

	// public boolean isTermRegistered(String id);
	// public boolean isTermUnkown(String id);

	public void setFacetService(IFacetService facetService);

	public IFacetService getFacetService();

	public boolean isTermViewable(String id);

	public List<IFacetTerm> getTermsForFacet(Facet facet);

	public Collection<String> readOccurrencesForTerm(IFacetTerm term)
			throws SQLException;

	public Collection<String> readIndexOccurrencesForTerm(IFacetTerm term)
			throws SQLException;

	public Collection<IFacetTerm> filterTermsNotInIndex(
			Collection<IFacetTerm> nodes);

	public String termIdForTerm(IFacetTerm term);

	public IFacetTerm createKeywordTerm(String mappedID, String originalValue);

	Integer[] facetIdForTerm(IFacetTerm term);

	public void addFacetIdToTerm(List<Integer> facetIds, String termId);

	/**
	 * @param facet
	 * @return
	 */
	public Collection<IFacetTerm> getFacetRoots(Facet facet);

}
