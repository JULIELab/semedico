package de.julielab.semedico.core.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.List;

import org.dbunit.DBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.IFacetService;

public class FacetServiceTest extends DBTestCase {

	public void testGetFacets() throws SQLException, Exception {
		IFacetService facetService = new FacetService();

		facetService.setConnection(getConnection().getConnection());

		
		List<Facet> facets = facetService.getFacets();
		
		assertEquals(2, facets.size());
		Facet facet1 = facets.get(0);
		assertEquals((Integer)1, facet1.getId());
		assertEquals("Gene Expression and its Regulation", facet1.getName());
		assertEquals("geneExpression", facet1.getCssId());
		assertEquals("index 1", facet1.getDefaultIndexName());
//		assertEquals(1, facet1.getTerms().size());
//		assertEquals("term 1", facet1.getTerms().get(0).getLabel());
		assertEquals(0, facet1.getType());
		
		Facet facet2 = facets.get(1);
		assertEquals((Integer)2, facet2.getId());
		assertEquals("Authors", facet2.getName());
		assertEquals("authors", facet2.getCssId());
		assertEquals("index 2", facet2.getDefaultIndexName());		
//		assertEquals(1, facet2.getTerms().size());
//		assertEquals("term 2", facet2.getTerms().get(0).getLabel());
		assertEquals(1, facet2.getType());
		
		facets = facetService.getFacets();
		assertEquals(facet1, facets.get(0));
		assertEquals(facet2, facets.get(1));
	}


	@Override
	protected IDataSet getDataSet() throws Exception {
		return new XmlDataSet(new BufferedReader(new FileReader("resources/facets.xml")));
	}

	@Override
	protected DatabaseOperation getTearDownOperation() throws Exception {
		return DatabaseOperation.DELETE_ALL;
	}

}
