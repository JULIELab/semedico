package de.julielab.semedico.core.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.Facet;


public class FacetService implements IFacetService{

	private static Logger logger = LoggerFactory.getLogger(FacetService.class);
	private final String selectFacets = "select t1.name, t1.css_identifier, t1.facet_id, t1.type, t1.facet_order, t2.name as index "+
									    " from facet t1, index t2 where t1.default_index_id = t2.index_id and t1.hidden = 'false' order by facet_order ";

	private final String selectFacetsWithId = "select t1.name, t1.css_identifier, t1.facet_id, t1.type, t2.name as index "+
										" from facet t1, index t2 where t1.default_index_id = t2.index_id and facet_id = ";

	private final int KEYWORD_FACET_ID = 0;
	public final static Facet KEYWORD_FACET = new Facet("Keyword", "keywords");
	private Connection connection;
	private static List<Facet> facets;
	private static Map<Integer, Facet> facetsById;

	public FacetService(Connection connection) throws SQLException{
		this();
		this.connection = connection;
		
		getFacets();
	}
	
	public FacetService() {
		if( facetsById == null )
			facetsById = new HashMap<Integer, Facet>();
		if( facets == null )
			facets = new ArrayList<Facet>();
	}
	
	public List<Facet> getFacets() {
		if( facets != null && facets.size() > 0 )
			return facets;
		
		try {
			ResultSet rs = connection.createStatement().executeQuery(selectFacets);
			
			while( rs.next() ){
				Facet facet = createFacet(rs);
				facet.setId(rs.getInt("facet_id"));
			
				if( facet.getId() == KEYWORD_FACET_ID )
					facetsById.put(facet.getId(), KEYWORD_FACET);
				else{
					facets.add(facet);
					facetsById.put(facet.getId(), facet);
				}
				
				logger.info(facet + " loaded.");
			}

			Collections.sort(facets);
			for( int i = 0; i < facets.size(); i++ )
				facets.get(i).setIndex(i);
		} catch (SQLException e) {
			logger.error("SQL exception: ", e);
		}
			
		return facets;
	}

	public Facet readFacetWithId(Integer id) throws SQLException{

		ResultSet rs = connection.createStatement().executeQuery(selectFacetsWithId + id);
		Facet facet = null;
		while( rs.next() ){
			facet = createFacet(rs);
			facets.add(facet);
			facetsById.put(facet.getId(), facet);
		}
		
		return facet;
	}
	
	private Facet createFacet(ResultSet rs) throws SQLException{
		Facet facet = new Facet(rs.getString("name"), rs.getString("css_identifier"));
		facet.setId(rs.getInt("facet_id"));
		facet.setDefaultIndexName(rs.getString("index"));
		facet.setType(rs.getInt("type"));
		facet.setPosition(rs.getInt("facet_order"));
		//facet.setTerms(termService.getTermsForFacet(facet));
		//facet.setVisible(rs.getBoolean("visible"));
		return facet;
	}
	
	@Override
	public Facet getFacetWithName(String facetName) {
		if( facets == null || facets.size() == 0 )
			getFacets();
		
		for( Facet facet: facets )
			if( facet.getName().equals(facetName) )
				return facet;
		
		return null;
	}

	public Facet getFacetWithId(Integer id) {
		Facet facet = facetsById.get(id);
		if( facet == null ){
			try {
				facet = readFacetWithId(id);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
		return facet;
	}
	
	public Facet getFacetForIndex(String indexName){
		for( Facet facet: facetsById.values() )
			if( facet.getDefaultIndexName().equals(indexName) )
				return facet;
		
		return null;
	}
	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public List<Facet> getFacetsWithType(int type) throws SQLException {
		List<Facet> facets = getFacets();
		List<Facet> facetsWithType = new ArrayList<Facet>();
		
		for(Facet facet: facets )
			if( facet.getType() == type && !facetsWithType.contains(facet) )
				facetsWithType.add(facet);

		return facetsWithType;
	}

	public Facet getKeywordFacet() {
		return KEYWORD_FACET;
	}
	
}