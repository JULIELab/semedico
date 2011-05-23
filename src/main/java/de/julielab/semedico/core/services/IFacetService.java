package de.julielab.semedico.core.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import de.julielab.semedico.core.Facet;

public interface IFacetService {

	public void setConnection(Connection connection);
	public Connection getConnection();
	
	public List<Facet> getFacets() throws SQLException;
	public Facet getFacetWithId(Integer id);

	public List<Facet> getFacetsWithType(int type) throws SQLException;
	
	public Facet getFacetForIndex(String indexName);
	public Facet getKeywordFacet();
	public Facet getFacetWithName(String facetName) throws SQLException;
}
