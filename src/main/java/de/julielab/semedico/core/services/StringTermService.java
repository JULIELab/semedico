/**
 * StringTermService.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 11.04.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import com.ibm.icu.text.Collator;

import de.julielab.db.IDBConnectionService;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.util.PairStream;

/**
 * @author faessler
 * 
 */

public class StringTermService implements IStringTermService {

	private final static int authorNameInsertBatchSize = 10000;
	private final static int queryBatchSize = 1000;
	public static final String PG_SCHEMA_AUTHOR_NAMES = "authorNames";

	public static final String TABLE_AUTHOR_NAME = "authorName";
	public static final String TABLE_CANONICAL_AUTHOR_NAME = "canonicalAuthorName";
	public static final String TABLE_HAS_CANONICAL_NAME = "hasCanonicalName";

	public static final String TABLE_HAS_CAN_TMP = "hasCanonicalNameTmp";
	public static final String TABLE_AN_TMP = "authorNamesTmp";

	private final static String COL_CANONICAL_AUTHOR_NAME = "canonical_author_name";
	private final static String COL_AUTHOR_NAME = "author_name";
	private final static String COL_CAN_ID = "can_id";
	private final static String COL_AN_ID = "an_id";

	private final static String COL_COUNT = "count";

	public static final String WS_REPLACE = "%";
	public static final String SUFFIX = "__FACET_ID:";

	private final ITermService termService;
	private final Matcher suffixMatcher;
	private final Matcher wsReplacementMatcher;
	private final Matcher wsMatcher;
	// Because the matchers have to be reset to the concrete string they are
	// supposed to work on, we must
	// synchronize their access. Since there are multiple methods using the
	// matchers, a simple "synchronized" keyword won't do it.
	private final ReentrantLock matcherLock;
	private final IFacetService facetService;
	private final IDBConnectionService dbConnectionService;
	private final SolrServer solr;
	private final Collator collator;
	private final Logger logger;
	private final ApplicationStateManager asm;

	public StringTermService(Logger logger, ITermService termService,
			IFacetService facetService,
			IDBConnectionService dbConnectionService,
			@InjectService("SolrSearcher") SolrServer solr,
			IRuleBasedCollatorWrapper collatorWrapper,
			ApplicationStateManager asm) {
		this.logger = logger;
		this.termService = termService;
		this.facetService = facetService;
		this.dbConnectionService = dbConnectionService;
		this.solr = solr;
		this.asm = asm;
		this.collator = collatorWrapper.getCollator();
		this.collator.freeze();
		suffixMatcher = Pattern.compile(SUFFIX + "([0-9]+)$").matcher("");
		wsReplacementMatcher = Pattern.compile(WS_REPLACE).matcher("");
		wsMatcher = Pattern.compile("\\s").matcher("");
		matcherLock = new ReentrantLock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#getStringTermId
	 * (java.lang.String, de.julielab.semedico.core.Facet)
	 */
	@Override
	public String getStringTermId(String stringTerm, Facet facet)
			throws IllegalStateException {
		String termId = stringTerm;
		matcherLock.lock();
		termId = wsMatcher.reset(stringTerm).replaceAll(WS_REPLACE);
		matcherLock.unlock();
		termId = termId + SUFFIX + facet.getId();
		return termId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#checkStringTermId
	 * (java.lang.String, de.julielab.semedico.core.Facet)
	 */
	@Override
	public String checkStringTermId(String stringTerm, Facet facet) {
		matcherLock.lock();
		if (wsReplacementMatcher.reset(stringTerm).find())
			throw new IllegalStateException("String term '" + stringTerm
					+ "' contains reserved character '" + WS_REPLACE + "'.");
		matcherLock.unlock();
		String id = getStringTermId(stringTerm, facet);
		if (termService.hasNode(id))
			throw new IllegalStateException(
					" The string term "
							+ stringTerm
							+ ", denoting an author, with ID '"
							+ id
							+ "' should be generated. However, there already is a term with that ID known to the term service.");
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getOriginalStringTermAndFacetId(java.lang.String)
	 */
	@Override
	public Pair<String, Integer> getOriginalStringTermAndFacetId(
			String stringTermId) throws IllegalArgumentException {
		matcherLock.lock();

		suffixMatcher.reset(stringTermId);
		if (!suffixMatcher.find())
			throw new IllegalArgumentException(
					"The given string term ID does not end with pattern '"
							+ suffixMatcher.pattern().pattern()
							+ "' with which all string term IDs must be suffixed.");
		// First, extract the facet ID.
		String facetIdString = suffixMatcher.group(1);
		Integer facetId = Integer.parseInt(facetIdString);

		// Now re-create the original string term. First, cut the suffix.
		String stringTerm = suffixMatcher.replaceAll("");
		// Then, get the white spaces back (if there were any).
		stringTerm = wsReplacementMatcher.reset(stringTerm).replaceAll(" ");

		ImmutablePair<String, Integer> pair = new ImmutablePair<String, Integer>(
				stringTerm, facetId);

		matcherLock.unlock();
		return pair;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getTermObjectForStringTermId(java.lang.String)
	 */
	@Override
	public IFacetTerm getTermObjectForStringTermId(String stringTermId) {
		Pair<String, Integer> originalStringTermAndFacetId = getOriginalStringTermAndFacetId(stringTermId);
		Facet facet = facetService.getFacetById(originalStringTermAndFacetId
				.getRight());
		FacetTerm term = new FacetTerm(stringTermId,
				originalStringTermAndFacetId.getLeft());
		term.addFacet(facet);
		term.setIndexNames(facet.getFilterFieldNames());
		if (facetService.isAnyAuthorFacetId(facet.getId())) {
			List<String> nameVariants = getVariantsOfCanonicalAuthorName(originalStringTermAndFacetId
					.getLeft());
			// TODO This is more of a legacy hack. As soon as the term database
			// format is cleaned up, this list should go as an array into a
			// setSynonyms method. This code should also be added to the below
			// method getTermObjectForStringTerm
			term.setShortDescription(StringUtils.join(nameVariants, ";"));
		}
		return term;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getTermObjectForStringTerm(java.lang.String,
	 * de.julielab.semedico.core.Facet)
	 */
	@Override
	public IFacetTerm getTermObjectForStringTerm(String stringTerm, Facet facet) {
		String stringTermId = getStringTermId(stringTerm, facet);
		FacetTerm term = new FacetTerm(stringTermId, stringTerm);
		term.addFacet(facet);
		term.setIndexNames(facet.getFilterFieldNames());
		if (facetService.isAnyAuthorFacetId(facet.getId())) {
			List<String> nameVariants = getVariantsOfCanonicalAuthorName(stringTerm);
			// TODO This is more of a legacy hack. As soon as the term database
			// format is cleaned up, this list should go as an array into a
			// setSynonyms method. This code should also be added to the above
			// method getTermObjectForStringTermId
			term.setShortDescription(StringUtils.join(nameVariants, ";"));
		}
		return term;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#isStringTermID(
	 * java.lang.String)
	 */
	@Override
	public boolean isStringTermID(String string) {
		matcherLock.lock();
		suffixMatcher.reset(string);
		boolean suffixFound = suffixMatcher.find();
		boolean noWhiteSpaces = !wsMatcher.reset(string).find();
		matcherLock.unlock();
		return suffixFound && noWhiteSpaces;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#buildAuthorSynsets
	 * ()
	 */
	@Override
	public void buildAuthorSynsets() {
		StopWatch w = new StopWatch();
		w.start();

		logger.info("Building author name synsets...");
		Connection conn = null;
		SolrQuery query = new SolrQuery("*:*");
		query.setQueryType("/terms");
		query.setTermsLimit(-1);
		query.setTermsSortString("index");
		query.addTermsField(IndexFieldNames.FACET_AUTHORS);
		try {
			logger.info("Creating database tables.");
			conn = dbConnectionService.getConnection();
			conn.createStatement().execute(
					"SET search_path TO " + PG_SCHEMA_AUTHOR_NAMES);
			// TODO: When not existing create...put in DBConnectionService and
			// build shared library to DataBaseConnector with common PGUtils
			createAuthorNameSynsetTables(conn);

			logger.info("Reading all author names from Solr...");
			List<Term> authorNames = solr.query(query).getTermsResponse()
					.getTerms(IndexFieldNames.FACET_AUTHORS);
			logger.info("Sorting {} retrieved names...", authorNames.size());
			SolrTermICUComparator termICUComparator = new SolrTermICUComparator();
			Collections.sort(authorNames, termICUComparator);

			logger.info("Computing the synsets...");
			HashMap<String, Set<String>> synSets = new HashMap<String, Set<String>>(
					authorNames.size() / 2);
			int i = 0;
			while (i < authorNames.size()) {
				String canonicalName = authorNames.get(i).getTerm();
				i++;
				String nextName = i < authorNames.size() ? authorNames.get(i)
						.getTerm() : authorNames.get(i - 1).getTerm();
				Set<String> synSet = new HashSet<String>();
				synSet.add(canonicalName);
				while (isNameVariantOf(canonicalName, nextName)) {
					synSet.add(nextName);
					canonicalName = determineCanonicalAuthorName(canonicalName,
							nextName);
					i++;
					if (i >= authorNames.size())
						break;
					nextName = authorNames.get(i).getTerm();
				}
				synSets.put(canonicalName, synSet);
			}

			logger.info("Inserting synset data into database tables...");
			insertIntoTables(conn, authorNames, synSets);

			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			e.getNextException().printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		w.stop();
		logger.info(
				"Finished building author name synsets. Procedure took {} seconds.",
				w.getTime() / 1000);
	}

	/**
	 * @param conn
	 * @param authorNames
	 * @param synSets
	 * @throws SQLException
	 */
	private void insertIntoTables(Connection conn, List<Term> authorNames,
			HashMap<String, Set<String>> synSets) throws SQLException {
		conn.setAutoCommit(false);
		PreparedStatement psan = conn.prepareStatement(String.format(
				"INSERT INTO %s (%s) values (?)", TABLE_AUTHOR_NAME,
				COL_AUTHOR_NAME));
		for (int i = 0; i < authorNames.size(); i++) {
			psan.setString(1, authorNames.get(i).getTerm());
			psan.addBatch();
			if (i % authorNameInsertBatchSize == 0)
				psan.executeBatch();
		}
		psan.executeBatch();
		conn.commit();
		logger.info("Insertion of plain author names complete.");

		PreparedStatement pscan = conn.prepareStatement(String.format(
				"INSERT INTO %s (%s) values (?)", TABLE_CANONICAL_AUTHOR_NAME,
				COL_CANONICAL_AUTHOR_NAME));
		Iterator<String> canIt = synSets.keySet().iterator();
		for (int i = 0; canIt.hasNext(); i++) {
			String canonicalAuthorName = canIt.next();
			pscan.setString(1, canonicalAuthorName);
			pscan.addBatch();
			if (i % authorNameInsertBatchSize == 0)
				pscan.executeBatch();
		}
		pscan.executeBatch();
		conn.commit();
		logger.info("Insertion of canonical author names complete.");

		conn.createStatement().execute(
				String.format("CREATE TEMP TABLE %s (%s text, %s text)",
						TABLE_HAS_CAN_TMP, COL_AUTHOR_NAME,
						COL_CANONICAL_AUTHOR_NAME));
		PreparedStatement hasCan = conn.prepareStatement(String.format("INSERT INTO %s values (?, ?)", TABLE_HAS_CAN_TMP));
		Iterator<Entry<String, Set<String>>> entryIt = synSets.entrySet()
				.iterator();
		for (int i = 0; entryIt.hasNext(); i++) {
			Entry<String, Set<String>> entry = entryIt.next();
			if (StringUtils.isEmpty(entry.getKey()))
				continue;
			hasCan.setString(2, entry.getKey());

			for (String authorName : entry.getValue()) {
				if (StringUtils.isEmpty(authorName))
					continue;
				hasCan.setString(1, authorName);
				hasCan.addBatch();
			}
			if (i % authorNameInsertBatchSize == 0)
				hasCan.executeBatch();
		}
		hasCan.executeBatch();
		conn.commit();
		logger.info("Insertion of text pairs (<author name>, <canonical author name>) complete.");

		
		String sql = 
				"INSERT INTO " + TABLE_HAS_CANONICAL_NAME + " (SELECT an."
						+ COL_AN_ID + ", can." + COL_CAN_ID + " FROM "
						+ TABLE_AUTHOR_NAME + " AS an JOIN "
						+ TABLE_HAS_CAN_TMP + " AS hcnt ON an."
						+ COL_AUTHOR_NAME + "=hcnt." + COL_AUTHOR_NAME
						+ " JOIN " + TABLE_CANONICAL_AUTHOR_NAME
						+ " AS can ON can." + COL_CANONICAL_AUTHOR_NAME
						+ "=hcnt." + COL_CANONICAL_AUTHOR_NAME + ")";
		conn.createStatement().execute(sql);
		logger.info("Computation of author-name-has-canonical-author-name relation complete.");

		conn.setAutoCommit(true);
	}

	/**
	 * <p>
	 * Creates all tables used for author name management. Drops these tables
	 * when already existing.
	 * </p>
	 * 
	 * 
	 * @throws SQLException
	 * 
	 */
	private void createAuthorNameSynsetTables(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
			if (dbConnectionService.tableExists(conn, TABLE_HAS_CANONICAL_NAME)) {
				stmt.execute(String.format("DROP TABLE %s",
						TABLE_HAS_CANONICAL_NAME));
			}
			if (dbConnectionService.tableExists(conn, TABLE_AUTHOR_NAME)) {
				stmt.execute(String.format("DROP TABLE %s", TABLE_AUTHOR_NAME));
			}
			if (dbConnectionService.tableExists(conn,
					TABLE_CANONICAL_AUTHOR_NAME)) {
				stmt.execute(String.format("DROP TABLE %s",
						TABLE_CANONICAL_AUTHOR_NAME));
			}
			stmt.execute(String.format(
					"CREATE TABLE %s (%s SERIAL PRIMARY KEY, %s text)",
					TABLE_AUTHOR_NAME, COL_AN_ID, COL_AUTHOR_NAME));
//			stmt.execute(String.format("CREATE INDEX %s ON %s (%s)",
//					TABLE_AUTHOR_NAME + "an_index", TABLE_AUTHOR_NAME,
//					COL_AUTHOR_NAME));

			stmt.execute(String.format(
					"CREATE TABLE %s (%s SERIAL PRIMARY KEY, %s text)",
					TABLE_CANONICAL_AUTHOR_NAME, COL_CAN_ID,
					COL_CANONICAL_AUTHOR_NAME));
//			stmt.execute(String.format("CREATE INDEX %s ON %s (%s)",
//					TABLE_CANONICAL_AUTHOR_NAME + "an_index",
//					TABLE_CANONICAL_AUTHOR_NAME, COL_CANONICAL_AUTHOR_NAME));

			stmt.execute(String
					.format("CREATE TABLE %s (%s INTEGER REFERENCES %s (%s), %s INTEGER REFERENCES %s (%s))",
							TABLE_HAS_CANONICAL_NAME, COL_AN_ID,
							TABLE_AUTHOR_NAME, COL_AN_ID, COL_CAN_ID,
							TABLE_CANONICAL_AUTHOR_NAME, COL_CAN_ID));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Returns the argument which should be used as canonical name, assuming
	 * both passed names are writing variants of each other.
	 * </p>
	 * <p>
	 * As canonical name the longer name writing is preferred because it is
	 * assumed to be less ambigue. <samp>Example: "Fäßler, E" vs.
	 * "Fäßler, Erik"</samp>. However, one writing variant could be only longer
	 * because of transliteration: <samp>"Fäßler, Erik" vs.
	 * "Faessler, Erik"</samp>. In this case, the name with more non-ASCII
	 * characters is returned.
	 * </p>
	 * 
	 * @param name0
	 * @param name1
	 * @return The "more" canonical name variant of the two passed names.
	 */
	private String determineCanonicalAuthorName(String name0, String name1) {
		int length0 = name0.length();
		int length1 = name1.length();
		int nonAscii0 = countNonASCIICharacters(name0);
		int nonAscii1 = countNonASCIICharacters(name1);
		// We want the longer variant as canonical name. However, if the longer
		// variant is only longer because special characters like 'ü' have been
		// transliterated - e.g. into 'ue' - we prefer the original version.
		// Thus, we give a "bonus score" for each non-ASCII character.
		int score0 = length0 + nonAscii0;
		int score1 = length1 + nonAscii1;

		// If the scores are equal, we prefer the name with more diacrits (e.g.
		// 'Sühnel, Jürgen' should win over 'Suehnel, Juergen' - both strings
		// have the same score).
		if (score0 == score1)
			return nonAscii0 < nonAscii1 ? name1 : name0;

		// With unequal scores, return the name with the higher score.
		return score0 < score1 ? name1 : name0;
	}

	/**
	 * Counts the number of non-ASCII-characters - i.e. characters with a code
	 * point greater than 127 - in <code>string</code>.
	 * 
	 * @param string
	 * @return The number of non-ASCII-characters in <code>string</code>.
	 */
	private int countNonASCIICharacters(String string) {
		int number = 0;
		for (int i = 0; i < string.length(); i++)
			if (string.codePointAt(i) > 127)
				number++;
		return number;
	}

	/**
	 * <p>
	 * Determines whether the two argument strings are regarded as writing
	 * variants from each other according to Semedico's author name comparison
	 * rules.
	 * </p>
	 * <p>
	 * It is assumed that names show up in the format <br/>
	 * <code>lastname, first1 ... firstn</code><br/>
	 * where <code>first1 ... firstn</code> may be full forms of first names or
	 * initials. <samp><br>
	 * Example: Cohen, Kevin B</samp>
	 * </p>
	 * 
	 * @param arg0
	 * @param arg1
	 * @return <code>True</code> if the two arguments are regarded as denoting
	 *         the same author, <code>false</code> otherwise.
	 */
	private boolean isNameVariantOf(String arg0, String arg1) {
		String[] arg0Split = arg0.split("\\s");
		String[] arg1Split = arg1.split("\\s");
		int minLength = Math.min(arg0Split.length, arg1Split.length);

		// First check whether even the last names are compatible.
		if (collator.compare(arg0Split[0], arg1Split[0]) != 0)
			return false;

		// Now check the first name(s).
		for (int i = 1; i < minLength; i++) {
			String arg0Part = arg0Split[i];
			String arg1Part = arg1Split[i];
			// When one part is only an initial, only compare first characters.
			if (arg0Part.length() == 1 || arg1Part.length() == 1) {
				if (collator.compare(arg0Part.substring(0, 1),
						arg1Part.substring(0, 1)) != 0)
					return false;
			} else if (collator.compare(arg0Part, arg1Part) != 0)
				return false;

		}
		return true;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	// TODO This method could be rewritten to server for batch-queries when
	// collapsing faceting results
	private List<String> getVariantsOfCanonicalAuthorNames(
			String canonicalAuthorName) throws SQLException {
		List<String> nameVariants = null;
		Connection connection = dbConnectionService.getConnection();
		try {
			Statement stmt = connection.createStatement();
			// select ant.author_name,cant.canonical_author_name from authorname
			// as ant join
			// hascanonicalname as hcnt on ant.an_id=hcnt.an_id join
			// canonicalauthorname as
			// cant on cant.can_id=hcnt.can_id WHERE author_name !=
			// canonical_author_name
			ResultSet rs = stmt.executeQuery("SELECT ant." + COL_AUTHOR_NAME
					+ ",cant." + COL_CANONICAL_AUTHOR_NAME + " FROM "
					+ TABLE_AUTHOR_NAME + " AS ant JOIN "
					+ TABLE_HAS_CANONICAL_NAME + " AS hcnt ON ant." + COL_AN_ID
					+ "=hcnt." + COL_AN_ID + " JOIN "
					+ TABLE_CANONICAL_AUTHOR_NAME + " AS cant ON cant."
					+ COL_CAN_ID + "=hcnt." + COL_CAN_ID + " WHERE "
					+ COL_AUTHOR_NAME + " != " + COL_CANONICAL_AUTHOR_NAME
					+ " AND " + COL_CANONICAL_AUTHOR_NAME + " = "
					+ canonicalAuthorName);
			nameVariants = new ArrayList<String>();
			if (true)
				throw new NotImplementedException();
			while (rs.next())
				nameVariants.add(rs.getString(1));

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		return nameVariants;
	}

	/**
	 * @return
	 */
	private List<String> getVariantsOfCanonicalAuthorName(
			String canonicalAuthorName) {
		List<String> nameVariants = null;
		Connection connection = dbConnectionService.getConnection();
		try {
			Statement stmt = connection.createStatement();
			stmt.execute("SET search_path TO " + PG_SCHEMA_AUTHOR_NAMES);
			// select ant.author_name,cant.canonical_author_name from authorname
			// as ant join
			// hascanonicalname as hcnt on ant.an_id=hcnt.an_id join
			// canonicalauthorname as
			// cant on cant.can_id=hcnt.can_id WHERE author_name !=
			// canonical_author_name
			String sql = "SELECT ant." + COL_AUTHOR_NAME + " FROM "
					+ TABLE_AUTHOR_NAME + " AS ant JOIN "
					+ TABLE_HAS_CANONICAL_NAME + " AS hcnt ON ant." + COL_AN_ID
					+ "=hcnt." + COL_AN_ID + " JOIN "
					+ TABLE_CANONICAL_AUTHOR_NAME + " AS cant ON cant."
					+ COL_CAN_ID + "=hcnt." + COL_CAN_ID + " WHERE "
					+ COL_AUTHOR_NAME + " != " + COL_CANONICAL_AUTHOR_NAME
					+ " AND " + COL_CANONICAL_AUTHOR_NAME + " = '"
					+ canonicalAuthorName + "'";
			ResultSet rs = stmt.executeQuery(sql);
			nameVariants = new ArrayList<String>();
			while (rs.next())
				nameVariants.add(rs.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return nameVariants;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * createCanonicalAuthorNameCounts(java.util.List)
	 */
	@Override
	public PairStream<String, Long> createCanonicalAuthorNameCounts(
			PairStream<String, Long> pairStream) {
		StopWatch w = new StopWatch();
		w.start();
		final Connection connection = dbConnectionService.getConnection();
		final String tmpTable = TABLE_AN_TMP
				+ asm.get(SearchState.class).getId();
		try {
			final Statement stmt = connection.createStatement();
			connection.setAutoCommit(false);
			stmt.execute("SET search_path TO " + PG_SCHEMA_AUTHOR_NAMES);
			stmt.execute("CREATE TEMP TABLE " + tmpTable + " ("
					+ COL_AUTHOR_NAME + " text, " + COL_COUNT
					+ " integer) ON COMMIT DROP");
			PreparedStatement ps = connection.prepareStatement("INSERT INTO "
					+ tmpTable + " VALUES (?,?)");
			while (pairStream.incrementPair()) {
				ps.setString(1, pairStream.getLeft());
				ps.setLong(2, pairStream.getRight());
				ps.addBatch();
			}
			ps.executeBatch();
			// connection.commit();
			// connection.setAutoCommit(true);

			final String sql = "SELECT " + COL_CANONICAL_AUTHOR_NAME + ",SUM("
					+ COL_COUNT + ") AS sum_count FROM " + tmpTable
					+ " AS tmp JOIN " + TABLE_AUTHOR_NAME + " AS an ON tmp."
					+ COL_AUTHOR_NAME + "=an." + COL_AUTHOR_NAME + " JOIN "
					+ TABLE_HAS_CANONICAL_NAME + " AS hcn ON an." + COL_AN_ID
					+ "=hcn." + COL_AN_ID + " JOIN "
					+ TABLE_CANONICAL_AUTHOR_NAME + " AS can ON hcn."
					+ COL_CAN_ID + "=can." + COL_CAN_ID + " GROUP BY "
					+ COL_CANONICAL_AUTHOR_NAME + " ORDER BY sum_count DESC";

			PairStream<String, Long> canonicalPairStream = new PairStream<String, Long>() {

				private ResultSet rs = doQuery(connection);

				private ResultSet doQuery(Connection connection)
						throws SQLException {
					// Get a statement which is set to cursor mode. This way, we
					// can get the data successively instead of getting all at
					// once.
					connection.setAutoCommit(false);
					Statement stmt = connection.createStatement();
					stmt.setFetchSize(queryBatchSize);
					return stmt.executeQuery(sql);
				}

				@Override
				public String getLeft() {
					try {
						return rs.getString(1);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public Long getRight() {
					try {
						return rs.getLong(2);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				public boolean incrementPair() {
					try {
						boolean hasNext = rs.next();
						if (!hasNext) {
							connection.setAutoCommit(true);
							// stmt.execute("DROP TABLE " + tmpTable);
							connection.close();
						}
						return hasNext;
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return false;
				}

			};

			// Iterator<Pair<String, Long>> it = new
			// ClosableIterator<Pair<String, Long>>() {
			//
			// private ResultSet rs = doQuery(connection);
			// private boolean hasNext = rs.next();
			//
			// private ResultSet doQuery(Connection connection)
			// throws SQLException {
			// // Get a statement which is set to cursor mode. This way, we
			// // can get the data successively instead of getting all at
			// // once.
			// connection.setAutoCommit(false);
			// Statement stmt = connection.createStatement();
			// stmt.setFetchSize(queryBatchSize);
			// return stmt.executeQuery(sql);
			// }
			//
			// @Override
			// public boolean hasNext() {
			// if (!hasNext)
			// close();
			// return hasNext;
			// }
			//
			// @Override
			// public Pair<String, Long> next() {
			// if (hasNext) {
			// try {
			// String canonicalAuthorName = rs.getString(1);
			// long sumCount = rs.getLong(2);
			// Pair<String, Long> pair = new ImmutablePair<String,
			// Long>(canonicalAuthorName, sumCount);
			// hasNext = rs.next();
			// if (!hasNext)
			// close();
			// return pair;
			//
			// } catch (SQLException e) {
			// e.printStackTrace();
			// }
			// }
			// return null;
			// }
			//
			// @Override
			// public void remove() {
			// throw new UnsupportedOperationException();
			// }
			//
			// @Override
			// public void close() {
			// try {
			// connection.close();
			// } catch (SQLException e) {
			// e.printStackTrace();
			// }
			// }
			//
			// };
			// return it;
			w.stop();
			logger.debug(
					"Collapsing author names to canonical author name took {} ms",
					w.getTime());
			return canonicalPairStream;
		} catch (SQLException e) {
			e.printStackTrace();
			e.getNextException().printStackTrace();
		}
		return null;
	}

	protected class SolrTermICUComparator implements Comparator<Term> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Term arg0, Term arg1) {
			return collator.compare(arg0.getTerm(), arg1.getTerm());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#getCanonicalAuthorNames
	 * ()
	 */
	@Override
	public Iterator<byte[][]> getCanonicalAuthorNames() {
		return dbConnectionService.selectRowsFromTable(
				new String[] { COL_CANONICAL_AUTHOR_NAME },
				PG_SCHEMA_AUTHOR_NAMES + "." + TABLE_CANONICAL_AUTHOR_NAME,
				null);
	}
}
