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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
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
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.ibm.icu.text.Collator;

import de.julielab.db.IDBConnectionService;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.QueryToken;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
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
	private final static String COL_FACET_ID = "facet_id";
	private final static String COL_SCORE = "score";
	private final static String COL_BEGIN = "begin_offset";
	private final static String COL_END = "end_offset";

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

		String termName = originalStringTermAndFacetId.getLeft();
		// Check whether the hit is an author name. If yes, map it to its
		// canonical form.
		// if
		// (facetService.isAnyAuthorFacetId(originalStringTermAndFacetId.getRight()))
		// {
		// }

		Facet facet = facetService.getFacetById(originalStringTermAndFacetId
				.getRight());
		FacetTerm term = new FacetTerm(stringTermId, termName);
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
		// When done at once for each author, there is a performance issues with
		// query analysis when a rather ambigue author name like "parkinson".
		// For several hundreds of terms, this lookup has to be performed then.
		// This should be done batchwise somehow.
		// Sketch: New methods for private accumulation of terms and one method
		// like "and now do all lookups and return the final terms".
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
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * getTermObjectForStringTerm(java.lang.String, int)
	 */
	@Override
	public IFacetTerm getTermObjectForStringTerm(String stringTerm, int facetId) {
		Facet facet = facetService.getFacetById(facetId);
		return getTermObjectForStringTerm(stringTerm, facet);
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
			dbConnectionService.createSchema(PG_SCHEMA_AUTHOR_NAMES);
			conn.createStatement().execute(
					"SET search_path TO " + PG_SCHEMA_AUTHOR_NAMES);
			// TODO: When not existing create...put in DBConnectionService and
			// build shared library to DataBaseConnector with common PGUtils
			createAuthorNameSynsetTables(conn);

			logger.info("Reading all author names from Solr...");
			List<Term> authorNameTerms = solr.query(query).getTermsResponse()
					.getTerms(IndexFieldNames.FACET_AUTHORS);
			List<String> authorNames = new ArrayList<String>(
					authorNameTerms.size());
			for (Term authorNameTerm : authorNameTerms)
				authorNames.add(authorNameTerm.getTerm());

			HashMap<String, Set<String>> synSets = computeAuthorSynsets(authorNames);

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
	 * <p>
	 * Organizes a list of names into sets ("synsets) of writing variants for
	 * one canonical form of this set.
	 * </p>
	 * <p>
	 * It is assumed that the given names are in the form
	 * "&lt;last name&gt;, &lt;first name or initial&gt; &lt;second name or initial&gt; &lt;third name or inital&gt; &lt;...&gt;"
	 * . Examples include <br>
	 * <samp>
	 * <ul>
	 * <li>Faessler, E</li>
	 * <li>Fäßler, Erik</li>
	 * <li>Parkinson, E K</li>
	 * <li>Parkinson, E Ken</li>
	 * <li>Parkinson, E Kenneth</li>
	 * <li>Parkinson, Eric Kenneth</li>
	 * <li>Parkinson, Susan E</li>
	 * </ul>
	 * </samp> The names are organized in sets such that each string in a set is
	 * considered a writing variant of the other names in the same set. Writing
	 * variants of a name string are found when one form uses diacritics or
	 * other variants on character level (e.g. <samp>ä. ö, ü, "Faessler, E" vs.
	 * "Fäßler, E"</samp>). Additionally, the same name with different
	 * combinations of abbreviation by use of initials is considered
	 * (<samp>"Parkinson, E K" vs. "Parkinson, E Kenneth" vs.
	 * "Parkinson, Eric Kenneth"</samp>).
	 * </p>
	 * 
	 * 
	 * @param authorNames
	 *            A list of author names to organize in SynSets.
	 * @return The computed SynSets where the key of each set is the determined
	 *         canonical form of the set.
	 * @see #isNameVariantOf(String, String)
	 * @see #determineCanonicalAuthorName(String, String)
	 */
	protected HashMap<String, Set<String>> computeAuthorSynsets(
			List<String> authorNames) {
		// This algorithm works as follow: The names are sorted so that variants
		// stand in a sequence (diacritics as well as abbreviation by initials
		// are accounted for). The sorted list is traversed linearly,
		// determining the points where a set of writing variants ends. When
		// such a border is determined, these names are added to the synsets map
		// where the canonical variant is made the key. The canonical variant is
		// determined on-the-fly.
		// This approach is not sufficient for the resolution of abbreviations,
		// however. Consider the example in the Java-Doc: "Parkinson, E K" ist
		// an abbreviation for "Parkinson, E Ken" as well as it is for
		// "Parkinson, E Kenneth". This means, we must memorize abbreviated
		// forms until we reach names for which this form does no longer apply
		// (e.g. in the Java-Doc example, as soon as we reach
		// "Parkinson, Susan E", the "Parkinson, E K" abbreviation is not valid
		// any more).
		// Now there are multiple "levels of generality" for abbreviations of
		// the same name. Consider "Parkinson, E K", "Parkinson, Eric K" and
		// "Parkinson, Eric Kenneth". The first is very general, eligible as an
		// abbreviation for a wide range of names. The second variant is more
		// specific, forcing "Eric" to be the first name, but still allowing
		// every second name beginning with "K". Thus, the second variant is
		// more specific than the first one. The third variant is the most
		// specific variant here, as there are no more initials left.
		// In the algorithm below, the current applying abbreviated name forms
		// are stored in a stack. At the bottom there is the most general form,
		// then comes the second level of generality, the third level and so on.
		// The levels are determined by sorting order, performed by the employed
		// comparator.
		logger.info("Sorting {} author names...", authorNames.size());
		ICUAuthorNameComparator termICUComparator = new ICUAuthorNameComparator();
		Collections.sort(authorNames, termICUComparator);

		logger.info("Computing the synsets...");
		HashMap<String, Set<String>> synSets = new HashMap<String, Set<String>>(
				authorNames.size() / 2);
		int i = 0;
		Stack<String> s = new Stack<String>();
		while (i < authorNames.size()) {
			String canonicalName = authorNames.get(i);
			// This is always the first name of the synset, thus the most
			// general variant.
			s.push(canonicalName);
			i++;
			String nextName = i < authorNames.size() ? authorNames.get(i)
					: authorNames.get(i - 1);

			// Fill the stack with levels of generality, becoming more and more
			// specific. Due to the comparator employed, the names in a sequence
			// always become more specific or stay at the same level of
			// generality until a complete new name occurs.
			while (nameIsMoreGeneralThan(canonicalName, nextName)) {
				s.push(nextName);
				// This is safe since the canonical name is always on the
				// most specific level.
				canonicalName = nextName;
				i++;
				if (i >= authorNames.size())
					break;
				nextName = authorNames.get(i);
			}

			// Now that we are at the deepest level of generality for the
			// current name, just collect all variants on this level (mostly
			// diacritic differences) and determine the canonical form.
			// Remember: The canonical form is the most specific, thus we don't
			// have to look for the canonical variant before.
			Set<String> synSet = new HashSet<String>();
			synSet.add(canonicalName);
			while (isNameVariantOf(canonicalName, nextName) == 0) {
				synSet.add(nextName);
				canonicalName = determineCanonicalAuthorName(canonicalName,
						nextName);
				i++;
				if (i >= authorNames.size())
					break;
				nextName = authorNames.get(i);
			}
			// The end of writing variants for the current name is reached. Add
			// the more general variants of the current name to the synset.
			for (String name : s)
				synSet.add(name);
			synSets.put(canonicalName, synSet);

			// Now remove the more specific name variants from the stack that do
			// not match the next name. When e.g. "Parkinson, E Kenneth" follows
			// on "Parkinson, E Ken", we could keep the (hypothetical)
			// abbreviations "Parkinson, E K" and "Parkinson, E".
			while (!s.isEmpty()
					&& !nameIsMoreGeneralThan(s.lastElement(), nextName)) {
				s.pop();
			}
		}

		return synSets;
	}

	/**
	 * @param conn
	 * @param authorNames
	 * @param synSets
	 * @throws SQLException
	 */
	private void insertIntoTables(Connection conn, List<String> authorNames,
			HashMap<String, Set<String>> synSets) throws SQLException {
		/*
		 * Map<String, Integer> authorNameIds = new HashMap<String,
		 * Integer>(authorNames.size()); Map<String, Integer>
		 * canonicalAuthorNameIds = new HashMap<String,
		 * Integer>(synSets.keySet().size());
		 * 
		 * conn.setAutoCommit(false); PreparedStatement psan =
		 * conn.prepareStatement(String.format(
		 * "INSERT INTO %s (%s,%s) values (?,?)", TABLE_AUTHOR_NAME,
		 * COL_AUTHOR_NAME, COL_AN_ID)); // PreparedStatement psan =
		 * conn.prepareStatement(String.format( //
		 * "INSERT INTO %s (%s) values (?)", TABLE_AUTHOR_NAME, //
		 * COL_AUTHOR_NAME)); for (int i = 0; i < authorNames.size(); i++) {
		 * psan.setString(1, authorNames.get(i).getTerm()); psan.setInt(2,
		 * authorNameIds.size());
		 * authorNameIds.put(authorNames.get(i).getTerm(),
		 * authorNameIds.size()); psan.addBatch(); if (i %
		 * authorNameInsertBatchSize == 0) psan.executeBatch(); }
		 * psan.executeBatch(); conn.commit();
		 * logger.info("Insertion of plain author names complete.");
		 * 
		 * PreparedStatement pscan = conn.prepareStatement(String.format(
		 * "INSERT INTO %s (%s,%s) values (?,?)", TABLE_CANONICAL_AUTHOR_NAME,
		 * COL_CANONICAL_AUTHOR_NAME, COL_CAN_ID)); // PreparedStatement pscan =
		 * conn.prepareStatement(String.format( //
		 * "INSERT INTO %s (%s) values (?)", TABLE_CANONICAL_AUTHOR_NAME, //
		 * COL_CANONICAL_AUTHOR_NAME)); Iterator<String> canIt =
		 * synSets.keySet().iterator(); for (int i = 0; canIt.hasNext(); i++) {
		 * String canonicalAuthorName = canIt.next(); pscan.setString(1,
		 * canonicalAuthorName); pscan.setInt(2, canonicalAuthorNameIds.size());
		 * canonicalAuthorNameIds.put(canonicalAuthorName,
		 * canonicalAuthorNameIds.size()); pscan.addBatch(); if (i %
		 * authorNameInsertBatchSize == 0) pscan.executeBatch(); }
		 * pscan.executeBatch(); conn.commit();
		 * logger.info("Insertion of canonical author names complete.");
		 * 
		 * PreparedStatement pshcan =
		 * conn.prepareStatement(String.format("INSERT INTO %s (%s,%s) values(?,?)"
		 * , TABLE_HAS_CANONICAL_NAME, COL_AN_ID, COL_CAN_ID));
		 * Iterator<Entry<String, Set<String>>> entryIt = synSets.entrySet()
		 * .iterator(); for (int i = 0; entryIt.hasNext(); i++) { Entry<String,
		 * Set<String>> entry = entryIt.next(); if
		 * (StringUtils.isEmpty(entry.getKey())) continue; pshcan.setInt(2,
		 * canonicalAuthorNameIds.get(entry.getKey()));
		 * 
		 * for (String authorName : entry.getValue()) { if
		 * (StringUtils.isEmpty(authorName)) continue; pshcan.setInt(1,
		 * authorNameIds.get(authorName)); pshcan.addBatch(); } if (i %
		 * authorNameInsertBatchSize == 0) pshcan.executeBatch(); }
		 * pshcan.executeBatch(); conn.commit(); logger.info(
		 * "Computation of author-name-has-canonical-author-name relation complete."
		 * );
		 */

		// conn.createStatement().execute(
		// String.format("CREATE TEMP TABLE %s (%s text, %s text)",
		// TABLE_HAS_CAN_TMP, COL_AUTHOR_NAME,
		// COL_CANONICAL_AUTHOR_NAME));
		PreparedStatement hasCan = conn.prepareStatement(String.format(
				"INSERT INTO %s values (?, ?)", TABLE_HAS_CANONICAL_NAME));
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
		//
		// String sql = "INSERT INTO " + TABLE_HAS_CANONICAL_NAME +
		// " (SELECT an."
		// + COL_AN_ID + ", can." + COL_CAN_ID + " FROM "
		// + TABLE_AUTHOR_NAME + " AS an JOIN " + TABLE_HAS_CAN_TMP
		// + " AS hcnt ON an." + COL_AUTHOR_NAME + "=hcnt."
		// + COL_AUTHOR_NAME + " JOIN " + TABLE_CANONICAL_AUTHOR_NAME
		// + " AS can ON can." + COL_CANONICAL_AUTHOR_NAME + "=hcnt."
		// + COL_CANONICAL_AUTHOR_NAME + ")";
		// conn.createStatement().execute(sql);
		// logger.info("Computation of author-name-has-canonical-author-name relation complete.");

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
			// if (dbConnectionService.tableExists(conn, TABLE_AUTHOR_NAME)) {
			// stmt.execute(String.format("DROP TABLE %s", TABLE_AUTHOR_NAME));
			// }
			// if (dbConnectionService.tableExists(conn,
			// TABLE_CANONICAL_AUTHOR_NAME)) {
			// stmt.execute(String.format("DROP TABLE %s",
			// TABLE_CANONICAL_AUTHOR_NAME));
			// }
			// stmt.execute(String.format(
			// "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s text)",
			// TABLE_AUTHOR_NAME, COL_AN_ID, COL_AUTHOR_NAME));
			// stmt.execute(String.format("CREATE INDEX %s ON %s (%s)",
			// TABLE_AUTHOR_NAME + "an_index", TABLE_AUTHOR_NAME,
			// COL_AUTHOR_NAME));
			//
			// stmt.execute(String.format(
			// "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s text)",
			// TABLE_CANONICAL_AUTHOR_NAME, COL_CAN_ID,
			// COL_CANONICAL_AUTHOR_NAME));
			// stmt.execute(String.format("CREATE INDEX %s ON %s (%s)",
			// TABLE_CANONICAL_AUTHOR_NAME + "an_index",
			// TABLE_CANONICAL_AUTHOR_NAME, COL_CANONICAL_AUTHOR_NAME));

			// stmt.execute(String
			// .format("CREATE TABLE %s (%s INTEGER REFERENCES %s (%s), %s INTEGER REFERENCES %s (%s))",
			// TABLE_HAS_CANONICAL_NAME, COL_AN_ID,
			// TABLE_AUTHOR_NAME, COL_AN_ID, COL_CAN_ID,
			// TABLE_CANONICAL_AUTHOR_NAME, COL_CAN_ID));
			// stmt.execute(String
			// .format("CREATE TABLE %s (%s INTEGER, %s INTEGER)",
			// TABLE_HAS_CANONICAL_NAME, COL_AN_ID,
			// COL_CAN_ID));
			//
			// stmt.execute(String.format("CREATE INDEX hcn_an_id_index ON %s (%s)",
			// TABLE_HAS_CANONICAL_NAME, COL_AN_ID));
			// stmt.execute(String.format("CREATE INDEX hcn_can_id_index ON %s (%s)",
			// TABLE_HAS_CANONICAL_NAME, COL_CAN_ID));

			stmt.execute(String.format(
					"CREATE TABLE %s (%s text, %s text)",
					TABLE_HAS_CANONICAL_NAME, COL_AUTHOR_NAME,
					COL_CANONICAL_AUTHOR_NAME));
			stmt.execute(String.format(
					"CREATE INDEX author_name_index ON %s (%s)",
					TABLE_HAS_CANONICAL_NAME, COL_AUTHOR_NAME));
			stmt.execute(String.format(
					"CREATE INDEX canonical_name_index ON %s (%s)",
					TABLE_HAS_CANONICAL_NAME, COL_CANONICAL_AUTHOR_NAME));
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
	 * initials.<br>
	 * <samp>Example: Cohen, Kevin B</samp>
	 * </p>
	 * 
	 * @param arg0
	 * @param arg1
	 * @return <code>0</code> if the two arguments are regarded as denoting the
	 *         same author, <code>-1</code> when the first argument should be
	 *         sorted to stand before the second argument, <code>1</code>
	 *         otherwise.
	 */
	private int isNameVariantOf(String arg0, String arg1) {
		int outcome = 0;
		// Split on one or more whitespaces characters; it is important to do it
		// on all whitespace characters because sometimes there are two
		// whitespaces in a row.
		String[] arg0Split = arg0.split("[\\s,]+");
		String[] arg1Split = arg1.split("[\\s,]+");

		// First check whether the last
		// names are compatible at all (i.e. only secondary differences).
		outcome = collator.compare(arg0Split[0], arg1Split[0]);

		// When the last names are compatible, check whether one name has more
		// elements (i.e. first names)
		// than the other; the name with more elements is always 'greater' (in
		// relational terms) than the name with less elements.
		if (outcome == 0)
			outcome = arg0Split.length - arg1Split.length;

		// If the last names equal each other, continue by comparison of
		// initials. Note that not complete names are compared but only the
		// initials. We consider different initials to be primary differences
		// and name differences other then initials to be secondary differences.
		for (int i = 1; i < arg0Split.length && outcome == 0; i++) {
			String arg0Part = arg0Split[i];
			String arg1Part = arg1Split[i];
			outcome = collator.compare(arg0Part.substring(0, 1),
					arg1Part.substring(0, 1));
		}
		// Check the first name(s); stop at the first difference (or don't even
		// begin when the last names already were different).
		for (int i = 1; i < arg0Split.length && outcome == 0; i++) {
			String arg0Part = arg0Split[i];
			String arg1Part = arg1Split[i];
			// When one part is only an initial, only compare first
			// characters.
			if (arg0Part.length() == 1 || arg1Part.length() == 1) {
				outcome = collator.compare(arg0Part.substring(0, 1),
						arg1Part.substring(0, 1));
			} else
				outcome = collator.compare(arg0Part, arg1Part);
		}
		return outcome;
	}

	protected boolean nameIsMoreGeneralThan(String arg0, String arg1) {
		String[] arg0Split = arg0.split("[\\s,]+");
		String[] arg1Split = arg1.split("[\\s,]+");

		// Different last names?
		if (!arg0Split[0].equals(arg1Split[0]))
			return false;

		// Is the test candidate for generality longer in terms of name elements
		// than the second parameter? (the shorter the more general)
		if (arg0Split.length > arg1Split.length)
			return false;

		int minLength = Math.min(arg0Split.length, arg1Split.length);

		for (int i = 1; i < minLength; i++) {
			String arg0Part = arg0Split[i];
			String arg1Part = arg1Split[i];

			if (arg0Part.length() > arg1Part.length())
				return false;

			if (arg0Part.length() == 1
					&& collator.compare(arg0Part.substring(0, 1),
							arg1Part.substring(0, 1)) != 0)
				return false;
			else if (arg0Part.length() > 1
					&& collator.compare(arg0Part, arg1Part) != 0)
				return false;
		}

		return !arg0.equals(arg1);
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
			String sql = "SELECT " + COL_AUTHOR_NAME + " FROM "
					+ TABLE_HAS_CANONICAL_NAME + " WHERE " + COL_AUTHOR_NAME
					+ " != " + COL_CANONICAL_AUTHOR_NAME + " AND "
					+ COL_CANONICAL_AUTHOR_NAME + " = '" + canonicalAuthorName
					+ "'";
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
			while (pairStream.incrementTuple()) {
				ps.setString(1, pairStream.getLeft());
				ps.setLong(2, pairStream.getRight());
				ps.addBatch();
			}
			ps.executeBatch();
			// connection.commit();
			// connection.setAutoCommit(true);

			// final String sql = "SELECT " + COL_CANONICAL_AUTHOR_NAME +
			// ",SUM("
			// + COL_COUNT + ") AS sum_count FROM " + tmpTable
			// + " AS tmp JOIN " + TABLE_AUTHOR_NAME + " AS an ON tmp."
			// + COL_AUTHOR_NAME + "=an." + COL_AUTHOR_NAME + " JOIN "
			// + TABLE_HAS_CANONICAL_NAME + " AS hcn ON an." + COL_AN_ID
			// + "=hcn." + COL_AN_ID + " JOIN "
			// + TABLE_CANONICAL_AUTHOR_NAME + " AS can ON hcn."
			// + COL_CAN_ID + "=can." + COL_CAN_ID + " GROUP BY "
			// + COL_CANONICAL_AUTHOR_NAME + " ORDER BY sum_count DESC";

			final String sql = "SELECT " + COL_CANONICAL_AUTHOR_NAME + ",SUM("
					+ COL_COUNT + ") AS sum_count FROM " + tmpTable
					+ " AS tmp JOIN " + TABLE_HAS_CANONICAL_NAME
					+ " AS hcan ON tmp." + COL_AUTHOR_NAME + "=hcan."
					+ COL_AUTHOR_NAME + " GROUP BY "
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
				public boolean incrementTuple() {
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

	protected class ICUAuthorNameComparator implements Comparator<String> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(String arg0, String arg1) {
			// return collator.compare(arg0.getTerm(), arg1.getTerm());
			return isNameVariantOf(arg0, arg1);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * mapQueryStringTerms(java.util.Collection)
	 */
	@Override
	public Collection<QueryToken> mapQueryStringTerms(
			Collection<QueryToken> inputTokens) {
		Collection<QueryToken> outputTokens = new ArrayList<QueryToken>();

		List<QueryToken> authorTokens = new ArrayList<QueryToken>();
		List<Integer> authorFacetIds = new ArrayList<Integer>();
		for (QueryToken it : inputTokens) {
			Pair<String, Integer> originalStringTermAndFacetId = getOriginalStringTermAndFacetId(it
					.getValue());
			Integer facetId = originalStringTermAndFacetId.getRight();
			if (facetService.isAnyAuthorFacetId(facetId)) {
				it.setValue(originalStringTermAndFacetId.getLeft());
				authorTokens.add(it);
				authorFacetIds.add(facetId);
			}
		}
		mapQueryAuthorNames(authorTokens, authorFacetIds, outputTokens);
		return outputTokens;
	}

	/**
	 * @param inputTokens
	 * @param outputTokens
	 */
	private void mapQueryAuthorNames(List<QueryToken> inputTokens,
			List<Integer> authorFacetIds, Collection<QueryToken> outputTokens) {
		if (inputTokens.size() == 0)
			return;

		Connection connection = dbConnectionService.getConnection();

		final String tmpTable = TABLE_AN_TMP
				+ asm.get(SearchState.class).getId();
		try {
			final Statement stmt = connection.createStatement();
			connection.setAutoCommit(false);
			stmt.execute("SET search_path TO " + PG_SCHEMA_AUTHOR_NAMES);
			stmt.execute("CREATE TEMP TABLE " + tmpTable + " ("
					+ COL_AUTHOR_NAME + " text, " + COL_FACET_ID + " INTEGER ,"
					+ COL_SCORE + " REAL, " + COL_BEGIN + " INTEGER, "
					+ COL_END + " INTEGER) ON COMMIT DROP");

			connection.setAutoCommit(false);
			PreparedStatement ps = connection.prepareStatement("INSERT INTO "
					+ tmpTable + " VALUES (?,?,?,?,?)");
			for (int i = 0; i < inputTokens.size(); i++) {
				QueryToken queryToken = inputTokens.get(i);
				Integer facetId = authorFacetIds.get(i);
				ps.setString(1, queryToken.getValue());
				ps.setInt(2, facetId);
				ps.setDouble(3, queryToken.getScore());
				ps.setInt(4, queryToken.getBeginOffset());
				ps.setInt(5, queryToken.getEndOffset());
				ps.addBatch();
			}
			ps.executeBatch();

			ResultSet rs = stmt.executeQuery("SELECT "
					+ COL_CANONICAL_AUTHOR_NAME + "," + COL_FACET_ID + ",MAX("
					+ COL_SCORE + ")," + COL_BEGIN + "," + COL_END + " FROM "
					+ tmpTable + " AS t1 JOIN " + TABLE_HAS_CANONICAL_NAME
					+ " AS t2 ON t1." + COL_AUTHOR_NAME + "=t2."
					+ COL_AUTHOR_NAME + " GROUP BY "
					+ COL_CANONICAL_AUTHOR_NAME + "," + COL_FACET_ID + ","
					+ COL_BEGIN + "," + COL_END);

			while (rs.next()) {
				String canonicalName = rs.getString(1);
				Integer facetId = rs.getInt(2);
				double score = rs.getDouble(3);
				int start = rs.getInt(4);
				int end = rs.getInt(5);
				QueryToken qt = new QueryToken(start, end, canonicalName);
				qt.setScore(score);
				qt.setTerm(getTermObjectForStringTerm(canonicalName, facetId));
				outputTokens.add(qt);
			}
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
