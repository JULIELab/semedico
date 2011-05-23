package de.julielab.semedico.core.services;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.lucene.IIndexReaderWrapper;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;

public class TermService implements ITermService {

	private static final String selectTermsWithId = "select * from term where internal_identifier = ?";
	private static final String selectTerms = "select term_id, parent_id, facet_id, value, internal_identifier, "+
											     "kwic_query, index_names, short_description, "+
											     "description from term where hidden = 'false'";
	private static final String selectTermsInFacet = "select term_id, parent_id, facet_id, value, internal_identifier, "+
    													"kwic_query, index_names, short_description, "+
    													"description from term where hidden = 'false' AND facet_id=";

	private static final String selectTermWithId = "select term_id, parent_id, facet_id, value, internal_identifier, "+
														"kwic_query, index_names, short_description, "+
														"description from term where hidden = 'false' AND term_id=";

	private static final String insertTerm = "insert into term(term_id, parent_id, facet_id, value, internal_identifier, occurrences, kwic_query, index_names, short_description, description, hidden) "+
											                "values(?,?,?,?,?,?,?,?,?,?,?)";
	private static final String selectOccurrences = "select occurrences from term where term_id = ?";
	private static final String selectIndexOccurrences = "select index_occurrences from term where term_id = ?";
	private static final String selectTermWithInternalIdentifier = "select term_id from term where internal_identifier = ?";
	private static final String updateTermIndexOccurrences = "update term set index_occurrences = ? where term_id= ?";
	
	private static final Logger logger = Logger.getLogger(TermService.class);
	
	private Connection connection;
	
	private static Map<String, FacetTerm> termsById;
	private static Map<Facet, List<FacetTerm>> termsByFacet;
	private IFacetService facetService;
	private static HashSet<String> knownTermIdentifier;
	private IIndexReaderWrapper documentIndexReader;
	
	public TermService(IFacetService facetService, Connection connection) throws Exception{
		init(facetService, connection);
	}
	
	
	private void init(IFacetService facetService, Connection connection) throws Exception{
		this.connection = connection;
		this.facetService = facetService;
		
		if( termsById == null )
			termsById = new HashMap<String, FacetTerm>();
		if( knownTermIdentifier == null )
			knownTermIdentifier = new HashSet<String>();
		if( termsByFacet == null ){
			termsByFacet = new HashMap<Facet, List<FacetTerm>>();
			for( Facet facet: facetService.getFacets() )
				termsByFacet.put(facet, new ArrayList<FacetTerm>());
		}		
	}
	
	public FacetTerm createTerm(ResultSet rs) throws SQLException {
		FacetTerm term = new FacetTerm(rs.getInt("term_id"));
		Integer facetId = rs.getInt("facet_id");
		Facet facet = getFacetService().getFacetWithId(facetId);
		term.setFacet(facet);


		Array array = rs.getArray("index_names");
		if( array != null ){
			Collection<String> indexNames = new ArrayList<String>();
			String[] indexNamesArray = (String[]) array.getArray();
			for( String indexName: indexNamesArray )
				indexNames.add(indexName);
			term.setIndexNames(indexNames);
		}
		else
			term.setIndexNames(Collections.EMPTY_LIST);
		
		term.setLabel(rs.getString("value"));
		term.setInternalIdentifier(rs.getString("internal_identifier"));
		String description = rs.getString("description"); 
		if( description != null ){
			description = description.trim();
			description = description.equals("") ? null : description.replace("'", "&apos;");
			if( description != null && description.endsWith(",") )
				description = description.substring(0, description.length() - 1);
			
			term.setDescription(description);
		}
		
		term.setShortDescription(rs.getString("short_description"));
		term.setKwicQuery(rs.getString("kwic_query"));
	
		return term;
	}

	public ResultSet selectTermWithInternalIdentifier(String identifier)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(selectTermsWithId);
		statement.setString(1, identifier);
		return statement.executeQuery();
	}

	
	public final void readAllTerms() throws SQLException{
		readTermsWithSelectString(selectTerms);
	}
		
	public final void readTermsInFacet(Facet facet) throws SQLException{
		readTermsWithSelectString(selectTermsInFacet + facet.getId());
	}
	
	protected void readTermsWithSelectString(String select) throws SQLException{
		logger.info("reading terms..");
		long time = System.currentTimeMillis();
		ResultSet rs = connection.createStatement().executeQuery(select);
		Map<Integer, FacetTerm> termsByTermID = new HashMap<Integer, FacetTerm>();
		Map<Integer, List<FacetTerm>> termsByParentID = new HashMap<Integer, List<FacetTerm>>();
		int count = 0;
		while( rs.next() ){
			FacetTerm term = null; 
			try {
				term = createTerm(rs);
				
				registerTerm(term);
				
				termsByTermID.put(term.getId(), term);			
				Integer parentID = rs.getInt("parent_id");
				if( parentID != null && parentID != 0 ){
					List<FacetTerm> children = termsByParentID.get(parentID);
					if( children == null ){
						children = new ArrayList<FacetTerm>();
						termsByParentID.put(parentID, children);
					}
					children.add(term);
				}
			} catch (Exception e) {
				IllegalStateException newException = new IllegalStateException(e + " occured at term " + term);
				newException.initCause(e);
				throw newException;
			}
			
			count ++;
		}		

		rs.close();		
		for( Integer parentID: termsByParentID.keySet() ){
			FacetTerm parent = termsByTermID.get(parentID);
			if( parent == null ){
				// hack?
				parent = readTermWithId(parentID);
				if( parent == null )
					logger.warn("Parent term " + parentID + " doesn't exist!");
			}
			if( parent != null ){
				List<FacetTerm> childs = termsByParentID.get(parentID);

				parent.getSubTerms().addAll(childs);
				for( FacetTerm child : childs )
					child.setParent(parent);
				if( childs != null )
					logger.debug("term " + parent + " has "+childs.size()+ " child terms");
			}
		}
		for( FacetTerm term: termsByTermID.values() ){
			
			try {
			//	term.setKwicQuery(queryTranslationService.createKwicQueryForTerm(term));
			} catch (Exception e) {
				IllegalStateException newException = new IllegalStateException(e + " occured at term " + term);
				newException.initCause(e);
				throw newException;
			}
		}
			
		time = System.currentTimeMillis() - time;
		logger.info("("+count+") .. takes " + (time/1000) + " s");					
	}
	
	@Override
	public void insertTerm(FacetTerm term, List<String> occurrences) throws SQLException{
		ResultSet keyRS = connection.createStatement().executeQuery("select nextval('term_term_id_seq')");
		keyRS.next();
		
		Integer termId = keyRS.getInt(1);
		keyRS.close();
		term.setId(termId);
		PreparedStatement statement = connection.prepareStatement(insertTerm);
		statement.setInt(1, termId);
		if( term.getParent() != null )
			statement.setInt(2, term.getParent().getId());
		else
			statement.setNull(2, Types.NULL);
		
		statement.setInt(3, term.getFacet().getId());
		statement.setString(4, term.getLabel());
		statement.setString(5, term.getInternalIdentifier());
		if( occurrences != null ){
			Object[] occurrencesStrings = new String[occurrences.size()];
			for( int i = 0; i < occurrences.size(); i++ )
				occurrencesStrings[i] = occurrences.get(i);
			statement.setArray(6, connection.createArrayOf("varchar", occurrencesStrings));
		}
		else
			statement.setNull(6, Types.NULL);

		statement.setString(7, term.getKwicQuery());

		Collection<String> indexNames = term.getIndexNames();
		if( indexNames != null ){
			Object[] indexNamesStrings = new String[indexNames.size()];
			Iterator<String> indexNamesIterator = indexNames.iterator();
			for( int i = 0; i < indexNames.size(); i++ )
				indexNamesStrings[i] = indexNamesIterator.next();
			statement.setArray(8, connection.createArrayOf("varchar", indexNamesStrings));
		}
		else
			statement.setNull(8, Types.NULL);

		statement.setString(9, term.getShortDescription());
		statement.setString(10, term.getDescription());
		statement.setBoolean(11, false);	
		
		statement.execute();
		statement.close();
	}

	@Override
	public void insertTerm(FacetTerm term) throws SQLException {
		insertTerm(term, null);
	}

	@Override
	public void insertIndexOccurrencesForTerm(FacetTerm term, Collection<String> indexOccurrences) throws SQLException {
		if( indexOccurrences.size() == 0 )
			return;
		
		PreparedStatement statement = connection.prepareStatement(updateTermIndexOccurrences);
		statement.setInt(2, term.getId());
		
		List<String> indexOccurrencesList = new ArrayList<String>(indexOccurrences);
		if( indexOccurrences != null ){
			Object[] occurrencesStrings = new String[indexOccurrences.size()];
			for( int i = 0; i < indexOccurrencesList.size(); i++  )
				occurrencesStrings[i] = indexOccurrencesList.get(i);
			
			statement.setArray(1, connection.createArrayOf("varchar", occurrencesStrings));
		}

		statement.execute();
		statement.close();		
	}


	public Collection<FacetTerm> getRegisteredTerms(){
		return termsById.values();
	}
	
	// TODO write test
	public final FacetTerm readTermWithInternalIdentifier(String id) throws SQLException{
		ResultSet rs = selectTermWithInternalIdentifier(id);
		FacetTerm term = null;
			while( rs.next() ){
			term = createTerm(rs);
			registerTerm(term);
		}
		rs.close();
		return term;
	}

	// TODO write test
	public final FacetTerm readTermWithId(Integer id) throws SQLException{
		ResultSet rs = connection.createStatement().executeQuery(selectTermWithId+id);
		FacetTerm term = null;
			while( rs.next() ){
			term = createTerm(rs);
			registerTerm(term);
		}
		rs.close();
		return term;
	}
	
	public final void registerTerm(FacetTerm term) {
		
		termsById.put(term.getInternalIdentifier(), term);
		
		if( term.getFacet() != null && term.getFacet() != FacetService.KEYWORD_FACET ){
			term.setFacetIndex(termsByFacet.get(term.getFacet()).size());		
			termsByFacet.get(term.getFacet()).add(term);
		}
		
		if( !knownTermIdentifier.contains(term.getInternalIdentifier()) )
			knownTermIdentifier.add(term.getInternalIdentifier());
	}

	@Override
	public Collection<String> readOccurrencesForTerm(FacetTerm term) throws SQLException{
		PreparedStatement statement = connection.prepareStatement(selectOccurrences);
		statement.setInt(1, term.getId());
		
		ResultSet resultSet = statement.executeQuery();
		Collection<String> suggestions = new ArrayList<String>();
		
		while( resultSet.next() ){
			Array array = resultSet.getArray(1);
			if( array == null )
				break;
			
			String[] suggestionsArray = (String[]) array.getArray();
			for( String suggestion: suggestionsArray )
				suggestions.add(suggestion);
		}
		
		return suggestions;
	}

	@Override
	public Collection<String> readIndexOccurrencesForTerm(FacetTerm term)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(selectIndexOccurrences);
		statement.setInt(1, term.getId());
		
		ResultSet resultSet = statement.executeQuery();
		Collection<String> suggestions = new ArrayList<String>();
		
		while( resultSet.next() ){
			Array array = resultSet.getArray(1);
			if( array == null )
				break;
			
			String[] suggestionsArray = (String[]) array.getArray();
			for( String suggestion: suggestionsArray )
				suggestions.add(suggestion);
		}
		
		return suggestions;
	}


	public List<FacetTerm> getTermsForFacet(Facet facet) {
		if( facet == FacetService.KEYWORD_FACET ){
			List<FacetTerm> terms = new ArrayList<FacetTerm>();
			for( FacetTerm term : termsById.values() )
				if( term.getFacet().equals(FacetService.KEYWORD_FACET) )
					terms.add(term);
			return terms;
					
		}
		return termsByFacet.get(facet);
	}
	

	public FacetTerm getTermWithInternalIdentifier(String id) {
		FacetTerm term = termsById.get(id);
		if (term == null)
			// TODO slf4j parameter logging.
			logger.warn("FacetTerm with internal_identifier \"" + id + "\" is unknown.");
		return term;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;		
	}

	public boolean isTermRegistered(String id) {
		return termsById.containsKey(id);
	}

	public boolean isTermViewable(String id) {
		FacetTerm term = getTermWithInternalIdentifier(id);
		return term != null && term.getLabel() != null;
	}
	
	public boolean isTermUnkown(String id) {		
		return !knownTermIdentifier.contains(id);
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public IIndexReaderWrapper getDocumentIndexReader() {
		return documentIndexReader;
	}

	public void setDocumentIndexReader(IIndexReaderWrapper documentIndexReader) {
		this.documentIndexReader = documentIndexReader;
	}
	
	 //TODO write test
	@Override
	public boolean termOccuredInDocumentIndex(FacetTerm term) throws IOException {
		for( String fieldName: term.getIndexNames() ){
			org.apache.lucene.index.Term indexTerm = new org.apache.lucene.index.Term(fieldName, term.getInternalIdentifier());
			if( documentIndexReader.getIndexReader().docFreq(indexTerm) > 0 )
				return true;
		}
		return false;
	}

	//TODO write test
	@Override
	public int termIdForTerm(FacetTerm term) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(selectTermWithInternalIdentifier);
		statement.setString(1, term.getInternalIdentifier());
		
		ResultSet resultSet = statement.executeQuery();
		int termId = -1;
		if( resultSet.next() )
			termId = resultSet.getInt(1);
		return termId;
	}

	@Override
	public FacetTerm createKeywordTerm(String value, String label) {
		FacetTerm keywordTerm = new FacetTerm(-1);
		keywordTerm.setInternalIdentifier(value);
		keywordTerm.setLabel(label);
		keywordTerm.setFacet(FacetService.KEYWORD_FACET);
		keywordTerm.setIndexNames(Lists.newArrayList(IndexFieldNames.SEARCHABLE_FIELDS));
		return keywordTerm;
	}
}
