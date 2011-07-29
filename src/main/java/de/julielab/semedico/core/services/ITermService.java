package de.julielab.semedico.core.services;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchy;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchyNode;

public interface ITermService extends IMultiHierarchy {

	
	public void readAllTerms() throws SQLException;
	public void readTermsInFacet(Facet facet) throws SQLException;
	
	public IMultiHierarchyNode getTermWithInternalIdentifier(String id);
	public IMultiHierarchyNode readTermWithInternalIdentifier(String id) throws SQLException;
	public IMultiHierarchyNode readTermWithId(Integer id) throws SQLException;
	
	public Collection<IMultiHierarchyNode> getRegisteredTerms();
	
	public void insertTerm(IMultiHierarchyNode term) throws SQLException;
	public void insertTerm(IMultiHierarchyNode term, List<String> occurrences) throws SQLException;
	public void insertIndexOccurrencesForTerm(IMultiHierarchyNode term, Collection<String> indexOccurrences) throws SQLException;
	
	public void registerTerm(IMultiHierarchyNode term);
//	public boolean isTermRegistered(String id);
//	public boolean isTermUnkown(String id);
	
	public void setFacetService(IFacetService facetService);
	public IFacetService getFacetService();

	public boolean isTermViewable(String id);
	
	public List<IMultiHierarchyNode> getTermsForFacet(Facet facet);
	public Collection<String> readOccurrencesForTerm(IMultiHierarchyNode term) throws SQLException;
	public Collection<String> readIndexOccurrencesForTerm(IMultiHierarchyNode term) throws SQLException;	
	
	public boolean termOccuredInDocumentIndex(IMultiHierarchyNode term) throws IOException;
	public Collection<IMultiHierarchyNode> filterTermsNotInIndex(Collection<IMultiHierarchyNode> nodes);
	public String termIdForTerm(IMultiHierarchyNode term);
	public IMultiHierarchyNode createKeywordTerm(String mappedID, String originalValue);
	Integer[] facetIdForTerm(IMultiHierarchyNode term);
	public void addFacetIdToTerm(List<Integer> facetIds, String termId);
	/**
	 * @param facet
	 * @return 
	 */
	public Collection<IMultiHierarchyNode> getFacetRoots(Facet facet);
}
