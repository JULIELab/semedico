package de.julielab.semedico.core.services;

import java.io.IOException;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.db.IDBConnectionService;
import de.julielab.lucene.IIndexReaderWrapper;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.MultiHierarchy;

public class TermService extends MultiHierarchy implements ITermService {

	private static final String selectTermsWithId = "select * from term where internal_identifier = ?";
	private static final String selectTerms = "select term_id, parent_id, facet_id, value, internal_identifier, "
			+ "kwic_query, index_names, short_description, "
			+ "description from term where hidden = 'false'";
	private static final String selectTermsInFacet = "select term_id, parent_id, facet_id, value, internal_identifier, "
			+ "kwic_query, index_names, short_description, "
			+ "description from term where hidden = 'false' AND facet_id=";

	private static final String selectTermWithId = "select term_id, parent_id, facet_id, value, internal_identifier, "
			+ "kwic_query, index_names, short_description, "
			+ "description from term where hidden = 'false' AND term_id=";

	private static final String insertTerm = "insert into term(term_id, parent_id, facet_id, value, internal_identifier, occurrences, kwic_query, index_names, short_description, description, hidden) "
			+ "values(?,?,?,?,?,?,?,?,?,?,?)";
	private static final String selectOccurrences = "select occurrences from term where term_id = ?";
	private static final String selectIndexOccurrences = "select index_occurrences from term where term_id = ?";
	private static final String selectTermWithInternalIdentifier = "select term_id from term where internal_identifier = ?";
	private static final String updateTermIndexOccurrences = "update term set index_occurrences = ? where term_id= ?";

	private static final Logger logger = LoggerFactory
			.getLogger(TermService.class);

	private Connection connection;

	private static Map<String, IFacetTerm> termsById;
	private static Map<Facet, List<IFacetTerm>> termsByFacet;
	private IFacetService facetService;
	private static HashSet<String> knownTermIdentifier;
	private IIndexReaderWrapper documentIndexReader;
	private final SolrServer solr;
	private Multimap<Facet, IFacetTerm> facetRoots;

	public TermService(
			IFacetService facetService,
			IDBConnectionService connectionService,
			@Symbol(SemedicoSymbolConstants.TERMS_LOAD_AT_START) String loadTerms,
			SolrServer solr) throws Exception {
		this.solr = solr;
		init(facetService, connectionService);
		facetRoots = HashMultimap.create();
		if (Boolean.parseBoolean(loadTerms))
			readAllTerms();
	}

	private void init(IFacetService facetService,
			IDBConnectionService connectionService) throws Exception {
		this.connection = connectionService.getConnection();
		this.facetService = facetService;

		if (termsById == null)
			termsById = new HashMap<String, IFacetTerm>();
		if (knownTermIdentifier == null)
			knownTermIdentifier = new HashSet<String>();
		if (termsByFacet == null) {
			termsByFacet = new HashMap<Facet, List<IFacetTerm>>();
			for (Facet facet : facetService.getFacets())
				termsByFacet.put(facet, new ArrayList<IFacetTerm>());
		}
	}

	public IFacetTerm createTerm(ResultSet rs) throws SQLException {
		IFacetTerm term = createNode(rs.getString("term_id"),
				rs.getString("value"));
		Integer[] facetIds = (Integer[]) rs.getArray("facet_id").getArray();
		for (Integer facetId : facetIds) {
			Facet facet = getFacetService().getFacetWithId(facetId);
			term.addFacet(facet);
		}

		Array array = rs.getArray("index_names");
		if (array != null) {
			Collection<String> indexNames = new ArrayList<String>();
			String[] indexNamesArray = (String[]) array.getArray();
			for (String indexName : indexNamesArray)
				indexNames.add(indexName);
			term.setIndexNames(indexNames);
		} else
			term.setIndexNames(Collections.<String> emptyList());

		String description = rs.getString("description");
		if (description != null) {
			description = description.trim();
			description = description.equals("") ? null : description.replace(
					"'", "&apos;");
			if (description != null && description.endsWith(","))
				description = description
						.substring(0, description.length() - 1);

			term.setDescription(description);
		}

		term.setShortDescription(rs.getString("short_description"));
		term.setKwicQuery(rs.getString("kwic_query"));

		return term;
	}

	public ResultSet selectTermWithInternalIdentifier(String identifier)
			throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement(selectTermsWithId);
		statement.setString(1, identifier);
		return statement.executeQuery();
	}

	public final void readAllTerms() throws SQLException {
		readTermsWithSelectString(selectTerms);
	}

	public final void readTermsInFacet(Facet facet) throws SQLException {
		readTermsWithSelectString(selectTermsInFacet + facet.getId());
	}

	protected void readTermsWithSelectString(String select) throws SQLException {
		logger.info("reading terms..");
		long time = System.currentTimeMillis();
		ResultSet rs = connection.createStatement().executeQuery(select);
		Map<String, IFacetTerm> termsByTermID = new HashMap<String, IFacetTerm>();
		Map<String, List<IFacetTerm>> termsByParentID = new HashMap<String, List<IFacetTerm>>();
		int count = 0;
		// Create IMultiHierarchyNode objects
		while (rs.next()) {
			IFacetTerm term = null;
			// try {
			term = createTerm(rs);

			// registerTerm(term);

			termsByTermID.put(term.getId(), term);

			// Add this term to list of children for its parent term - no
			// matter if the parent has already been created or not.
			String parentID = rs.getString("parent_id");
			if (parentID != null && !parentID.equals("")) {
				List<IFacetTerm> children = termsByParentID
						.get(parentID);
				if (children == null) {
					children = new ArrayList<IFacetTerm>();
					termsByParentID.put(parentID, children);
				}
				children.add(term);
			}
			// Add the node to the MultiHierarchy implementation.
			addNode(term);
			// } catch (Exception e) {
			// IllegalStateException newException = new IllegalStateException(
			// e + " occured at term " + term);
			// newException.initCause(e);
			// throw newException;
			// }

			count++;
		}
		rs.close();

		for (String parentID : termsByParentID.keySet()) {
			IFacetTerm parent = termsByTermID.get(parentID);
			// Boldly commented out by EF, 28.05.2011.
			// if (parent == null) {
			// hack?
			// parent = readTermWithId(parentID);
			// if (parent == null)
			// }
			if (parent != null) {
				List<IFacetTerm> children = termsByParentID
						.get(parentID);

				// For each set parent, the child is automatically set as a
				// child to the parent.
				for (IFacetTerm child : children)
					addParent(child, parent);
			} else
				logger.warn("Parent term " + parentID + " doesn't exist!");
		}
		// for (IMultiHierarchyNode term : termsByTermID.values()) {
		//
		// try {
		// //
		// term.setKwicQuery(queryTranslationService.createKwicQueryForTerm(term));
		// } catch (Exception e) {
		// IllegalStateException newException = new IllegalStateException(
		// e + " occured at term " + term);
		// newException.initCause(e);
		// throw newException;
		// }
		// }

		// Now sort the roots according to their associated facets.
		for (IFacetTerm root : getRoots()) {
			for (Facet facet : root.getFacets())
				facetRoots.put(facet, root);
		}
		time = System.currentTimeMillis() - time;
		logger.debug("Term roots: {}", roots.size());
		logger.info("(" + count + ") .. takes " + (time / 1000) + " s");
	}

	/**
	 * Used by the Semedico tools to fill the term database from files storing
	 * the term information.
	 */
	@Override
	public void insertTerm(IFacetTerm term, List<String> occurrences)
			throws SQLException {
		PreparedStatement statement = connection.prepareStatement(insertTerm);
		statement.setString(1, term.getId());
		if (term.getFirstParent() != null)
			statement.setString(2,
					((IFacetTerm) term.getFirstParent()).getId());
		else
			statement.setNull(2, Types.NULL);

		statement.setArray(3, connection.createArrayOf("integer",
				new Integer[] { term.getFirstFacet().getId() }));
		statement.setString(4, term.getName());
		statement.setString(5, term.getId());
		if (occurrences != null) {
			Object[] occurrencesStrings = new String[occurrences.size()];
			for (int i = 0; i < occurrences.size(); i++)
				occurrencesStrings[i] = occurrences.get(i);
			statement.setArray(6,
					connection.createArrayOf("varchar", occurrencesStrings));
		} else
			statement.setNull(6, Types.NULL);

		statement.setString(7, term.getKwicQuery());

		Collection<String> indexNames = term.getIndexNames();
		if (indexNames != null) {
			Object[] indexNamesStrings = new String[indexNames.size()];
			Iterator<String> indexNamesIterator = indexNames.iterator();
			for (int i = 0; i < indexNames.size(); i++)
				indexNamesStrings[i] = indexNamesIterator.next();
			statement.setArray(8,
					connection.createArrayOf("varchar", indexNamesStrings));
		} else
			statement.setNull(8, Types.NULL);

		statement.setString(9, term.getShortDescription());
		statement.setString(10, term.getDescription());
		statement.setBoolean(11, false);

		statement.execute();
		statement.close();
	}

	@Override
	public void insertTerm(IFacetTerm term) throws SQLException {
		insertTerm(term, null);
	}

	@Override
	public void insertIndexOccurrencesForTerm(IFacetTerm term,
			Collection<String> indexOccurrences) throws SQLException {
		if (indexOccurrences.size() == 0)
			return;

		PreparedStatement statement = connection
				.prepareStatement(updateTermIndexOccurrences);
		statement.setInt(2, term.getDatabaseId());

		List<String> indexOccurrencesList = new ArrayList<String>(
				indexOccurrences);
		if (indexOccurrences != null) {
			Object[] occurrencesStrings = new String[indexOccurrences.size()];
			for (int i = 0; i < indexOccurrencesList.size(); i++)
				occurrencesStrings[i] = indexOccurrencesList.get(i);

			statement.setArray(1,
					connection.createArrayOf("varchar", occurrencesStrings));
		}

		statement.execute();
		statement.close();
	}

	public Collection<IFacetTerm> getRegisteredTerms() {
		return termsById.values();
	}

	// TODO write test
	public final IFacetTerm readTermWithInternalIdentifier(String id)
			throws SQLException {
		ResultSet rs = selectTermWithInternalIdentifier(id);
		IFacetTerm term = null;
		while (rs.next()) {
			term = createTerm(rs);
			registerTerm(term);
		}
		rs.close();
		return term;
	}

	// TODO write test
	public final IFacetTerm readTermWithId(Integer id)
			throws SQLException {
		ResultSet rs = connection.createStatement().executeQuery(
				selectTermWithId + id);
		IFacetTerm term = null;
		while (rs.next()) {
			term = createTerm(rs);
			registerTerm(term);
		}
		rs.close();
		return term;
	}

	public final void registerTerm(IFacetTerm term) {

		termsById.put(term.getId(), term);

		if (term.getFirstFacet() != null
				&& term.getFirstFacet() != Facet.KEYWORD_FACET) {
			term.setFacetIndex(termsByFacet.get(term.getFirstFacet()).size());
			termsByFacet.get(term.getFirstFacet()).add(term);
		}

		if (!knownTermIdentifier.contains(term.getId()))
			knownTermIdentifier.add(term.getId());
	}

	@Override
	public Collection<String> readOccurrencesForTerm(IFacetTerm term)
			throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement(selectOccurrences);
		statement.setString(1, term.getId());

		ResultSet resultSet = statement.executeQuery();
		Collection<String> suggestions = new ArrayList<String>();

		while (resultSet.next()) {
			Array array = resultSet.getArray(1);
			if (array == null)
				break;

			String[] suggestionsArray = (String[]) array.getArray();
			for (String suggestion : suggestionsArray)
				suggestions.add(suggestion);
		}

		return suggestions;
	}

	@Override
	public Collection<String> readIndexOccurrencesForTerm(
			IFacetTerm term) throws SQLException {
		PreparedStatement statement = connection
				.prepareStatement(selectIndexOccurrences);
		statement.setString(1, term.getId());

		ResultSet resultSet = statement.executeQuery();
		Collection<String> suggestions = new ArrayList<String>();

		while (resultSet.next()) {
			Array array = resultSet.getArray(1);
			if (array == null)
				break;

			String[] suggestionsArray = (String[]) array.getArray();
			for (String suggestion : suggestionsArray)
				suggestions.add(suggestion);
		}

		return suggestions;
	}

	public List<IFacetTerm> getTermsForFacet(Facet facet) {
		if (facet == Facet.KEYWORD_FACET) {
			List<IFacetTerm> terms = new ArrayList<IFacetTerm>();
			for (IFacetTerm term : termsById.values())
				if (term.getFirstFacet().equals(Facet.KEYWORD_FACET))
					terms.add(term);
			return terms;

		}
		return termsByFacet.get(facet);
	}

	public IFacetTerm getTermWithInternalIdentifier(String id) {
		IFacetTerm term = termsById.get(id);
		if (term == null)
			// TODO slf4j parameter logging.
			logger.warn("IMultiHierarchyNode with internal_identifier \"" + id
					+ "\" is unknown.");
		return term;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

	public boolean isTermViewable(String id) {
		IFacetTerm term = getTermWithInternalIdentifier(id);
		return term != null && term.getName() != null;
	}

	// public boolean isTermRegistered(String id) {
	// return termsById.containsKey(id);
	// }
	//
	// public boolean isTermUnkown(String id) {
	// return !knownTermIdentifier.contains(id);
	// }

	public IFacetService getFacetService() {
		return facetService;
	}

	public IIndexReaderWrapper getDocumentIndexReader() {
		return documentIndexReader;
	}

	public void setDocumentIndexReader(IIndexReaderWrapper documentIndexReader) {
		this.documentIndexReader = documentIndexReader;
	}

	// TODO write test
	@Override
	public boolean termOccuredInDocumentIndex(IFacetTerm term)
			throws IOException {
		for (String fieldName : term.getIndexNames()) {
			org.apache.lucene.index.Term indexTerm = new org.apache.lucene.index.Term(
					fieldName, term.getId());
			if (documentIndexReader.getIndexReader().docFreq(indexTerm) > 0)
				return true;
		}
		return false;
	}

	/**
	 * Filters the given <code>IMultiHierarchyNodes</code> to exclude all terms
	 * which would produce no hits in the index.
	 * 
	 * @param nodes
	 * @return
	 */
	@Override
	public Collection<IFacetTerm> filterTermsNotInIndex(
			Collection<IFacetTerm> nodes) {
		SolrQuery q = new SolrQuery();
		q.setTerms(true);
		q.setTermsMinCount(1);
		q.setTermsLimit(-1);
		q.add("terms.fl", "IMultiHierarchyNodes");
		q.add("qt", "/terms");

		Set<String> termSet = new HashSet<String>();

		try {
			TermsResponse tr = solr.query(q).getTermsResponse();
			List<Term> terms = tr.getTerms("IMultiHierarchyNodes");
			for (Term term : terms) {
				termSet.add(term.getTerm());
			}

			Collection<IFacetTerm> filteredIMultiHierarchyNodes = new ArrayList<IFacetTerm>();
			for (IFacetTerm IMultiHierarchyNode : nodes) {
				String id = IMultiHierarchyNode.getId();
				if (termSet.contains(id))
					filteredIMultiHierarchyNodes.add(IMultiHierarchyNode);
			}
			return filteredIMultiHierarchyNodes;
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return null;
	}

	// TODO write test
	@Override
	public String termIdForTerm(IFacetTerm term) {
		String termId = null;
		try {
			PreparedStatement statement = connection
					.prepareStatement(selectTermWithInternalIdentifier);
			statement.setString(1, term.getId());

			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next())
				termId = resultSet.getString(1);
		} catch (Exception e) {
			logger.error("Error while getting termId for term {} ",
					selectTermWithInternalIdentifier, e);
		}
		return termId;
	}

	@Override
	public Integer[] facetIdForTerm(IFacetTerm term) {
		Integer[] facetIds = null;
		try {
			Statement stmt = connection.createStatement();
			ResultSet resultSet = stmt
					.executeQuery("SELECT facet_id FROM term WHERE term_id ='"
							+ term.getId() + "'");

			if (resultSet.next()) {
				Array array = resultSet.getArray(1);
				facetIds = (Integer[]) array.getArray();
			}
		} catch (Exception e) {
			logger.error("Error while getting facetId for term {} ",
					selectTermWithInternalIdentifier, e);
		}
		return facetIds;
	}

	public void addFacetIdToTerm(List<Integer> facetIds, String termId) {
		try {
			Statement stmt = connection.createStatement();
			stmt.execute(String.format(
					"UPDATE term SET facet_id = '%s' WHERE term_id = '%s'",
					connection.createArrayOf("integer", facetIds.toArray()),
					termId));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public IFacetTerm createKeywordTerm(String value, String label) {
		IFacetTerm keywordTerm = createNode(value, label);
		keywordTerm.addFacet(Facet.KEYWORD_FACET);
		keywordTerm.setIndexNames(Lists
				.newArrayList(IndexFieldNames.SEARCHABLE_FIELDS));
		return keywordTerm;
	}

	public IFacetTerm createNode(String id, String name) {
		return new FacetTerm(id, name);
	}

	/* (non-Javadoc)
	 * @see de.julielab.semedico.core.services.ITermService#getFacetRoots(de.julielab.semedico.core.Facet)
	 */
	@Override
	public Collection<IFacetTerm> getFacetRoots(Facet facet) {
		return facetRoots.get(facet);
		
	}
}
