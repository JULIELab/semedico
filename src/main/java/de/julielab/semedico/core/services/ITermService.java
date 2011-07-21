package de.julielab.semedico.core.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchy;

public interface ITermService extends IMultiHierarchy<FacetTerm> {

	
	public void readAllTerms() throws SQLException;
	public void readTermsInFacet(Facet facet) throws SQLException;
	
	public FacetTerm getTermWithInternalIdentifier(String id);
	public FacetTerm readTermWithInternalIdentifier(String id) throws SQLException;
	public FacetTerm readTermWithId(Integer id) throws SQLException;
	
	public Collection<FacetTerm> getRegisteredTerms();
	
	public void insertTerm(FacetTerm term) throws SQLException;
	public void insertTerm(FacetTerm term, List<String> occurrences) throws SQLException;
	public void insertIndexOccurrencesForTerm(FacetTerm term, Collection<String> indexOccurrences) throws SQLException;
	
	public void registerTerm(FacetTerm term);
//	public boolean isTermRegistered(String id);
//	public boolean isTermUnkown(String id);
	
	public void setFacetService(IFacetService facetService);
	public IFacetService getFacetService();

	public boolean isTermViewable(String id);
	
	public List<FacetTerm> getTermsForFacet(Facet facet);
	public Collection<String> readOccurrencesForTerm(FacetTerm term) throws SQLException;
	public Collection<String> readIndexOccurrencesForTerm(FacetTerm term) throws SQLException;	
	
	public boolean termOccuredInDocumentIndex(FacetTerm term) throws IOException;
	public Collection<FacetTerm> filterTermsNotInIndex(Collection<FacetTerm> nodes);
	public String termIdForTerm(FacetTerm term);
	public FacetTerm createKeywordTerm(String mappedID, String originalValue);
	Integer[] facetIdForTerm(FacetTerm term);
	public void addFacetIdToTerm(List<Integer> facetIds, String termId);
}
