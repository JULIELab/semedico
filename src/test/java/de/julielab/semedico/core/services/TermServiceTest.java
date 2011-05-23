package de.julielab.semedico.core.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dbunit.Assertion;
import org.dbunit.DBTestCase;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;

import com.google.common.collect.Lists;

import de.julielab.db.test.PostgreSQLDataTypeFactory;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.TermService;

public class TermServiceTest extends DBTestCase {

	private final String INIT_TESTDATA_FILE_PATH = "src/test/resources/terms.xml";
	private final String TARGET_TESTDATA_FILE_PATH = "src/test/resources/terms_target.xml";
	private ITermService termService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		termService = new TermService(new FacetService(getConnection().getConnection()), getConnection().getConnection());
		
	}
	protected void tearDown() throws Exception {
		DatabaseOperation.DELETE_ALL.execute(getConnection(), new XmlDataSet(new BufferedReader(new FileReader(TARGET_TESTDATA_FILE_PATH))));
    	String setval = "select setval('term_term_id_seq', 1, false)";
		getConnection().getConnection().createStatement().execute(setval);
		
		super.tearDown();		
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		getConnection();
		return new XmlDataSet(new BufferedReader(new FileReader(INIT_TESTDATA_FILE_PATH)));
	}
	
	@Override
	protected DatabaseOperation getTearDownOperation() throws Exception {
		return DatabaseOperation.DELETE_ALL;
	}
	
	// TODO commented out for compatibility issues, better solve the problem
//	protected IDatabaseConnection getConnection(){		
//		IDatabaseConnection connection;
//		try {
//			connection = super.getConnection();
//			if( !connection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY).getClass().equals(PostgreSQLDataTypeFactory.class) )
//				connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgreSQLDataTypeFactory(connection.getConnection()) );
//			
//			return connection;
//
//		} catch (Exception e) {
//			throw new IllegalStateException(e);
//		}		
//	}
	
	public void testInsertTerm() throws Exception{
		
		FacetTerm term = new FacetTerm();
		term.setFacet(new Facet(1));
		term.setLabel("term 1");
		term.setInternalIdentifier("term1");
		term.setIndexNames(Lists.newArrayList("index 1"));
		term.setDescription("description");
		term.setShortDescription("short description");
		term.setKwicQuery("kwic query");
		
		termService.insertTerm(term);

		term = new FacetTerm();
		term.setFacet(new Facet(1));
		term.setLabel("term 2");
		term.setInternalIdentifier("term2");
		term.setIndexNames(Lists.newArrayList("index 2"));
		term.setDescription("description2");
		term.setShortDescription("short description2");
		term.setKwicQuery("kwic query2");
		
		List<String> suggestions = new ArrayList<String>();
		suggestions.add("suggestion1");
		suggestions.add("suggestion2");
		termService.insertTerm(term, suggestions);
		
		IDataSet result = getConnection().createDataSet();
		ITable resultTermTable = result.getTable("term_view");
		IDataSet expected = new XmlDataSet(new BufferedReader(new FileReader(TARGET_TESTDATA_FILE_PATH)));
		ITable expectedTermTable = expected.getTable("term_view");

		Assertion.assertEquals(expectedTermTable, resultTermTable);		
	}
	
	public void testGetSuggestionsForTerm() throws Exception{

		FacetTerm term = new FacetTerm(2);		
		Collection<String> suggestions = termService.readOccurrencesForTerm(term);
		assertEquals(2, suggestions.size());

		Iterator<String> iterator =  suggestions.iterator();
		assertEquals("suggestion 1", iterator.next());
		assertEquals("suggestion 2", iterator.next());		
	}
	
}
