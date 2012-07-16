/** 
 * QueryTranslationService.java
 * 
 * Copyright (c) 2011, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: faessler
 * 
 *
 * Creation date: 19.04.2011 
 * 
 **/

package de.julielab.semedico.query;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.parsing.QueryAnalyzer;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * Builds query strings corresponding to the Apache Solr query syntax from a
 * disambiguated query.
 * 
 * @see <a
 *      href="http://wiki.apache.org/solr/SolrQuerySyntax">http://wiki.apache.org/solr/SolrQuerySyntax</a>
 *      for Solr query syntax.
 * @see <a
 *      href="http://lucene.apache.org/java/2_9_1/queryparsersyntax.html">http
 *      ://lucene.apache.org/java/2_9_1/queryparsersyntax.html</a> for Lucene
 *      query syntax. As the Solr query Syntax is mostly Lucene query syntax
 *      (proper superset), the Lucene syntax could be of greater interest.
 * 
 * @author faessler
 * 
 */
public class QueryTranslationService implements IQueryTranslationService {

	protected ITermService termService;

	static final int DEFAULT_MAX_QUERY_SIZE = 256;
	public static final int DEFAULT_PHRASE_SLOP = 1;

	private int maxQuerySize;

	private int phraseSlop;

	protected QueryAnalyzer queryAnalyzer;
	public static String[] stopWords;
	static final String STEMMER_NAME = "Porter";

	private final Logger logger;

	public QueryTranslationService(Logger logger, ITermService termService,
			IStopWordService stopWords) throws IOException {
		this.logger = logger;
		this.termService = termService;
		queryAnalyzer = new QueryAnalyzer(stopWords.getAsArray(), STEMMER_NAME);
	}

	public String createQueryFromTerms(Multimap<String, IFacetTerm> terms,
			String rawQuery) {
		List<String> facetTermDisjunctions = new ArrayList<String>();

		// Create the Solr search strings for all individual terms.
		// These strings will represent a search for the term ID in the fields
		// in which the term's facet should be searched (see documentation of
		// 'createQueryForTerm' below).
		// If a search query token was ambiguous, there are multiple terms
		// associated with it. A disjunction of the individual term search
		// expressions will be created.
		for (String queryTerm : terms.keySet()) {
			Collection<IFacetTerm> mappedTerms = terms.get(queryTerm);
			List<String> termClauses = new ArrayList<String>();

			if (mappedTerms.size() > 0) {
				for (IFacetTerm term : mappedTerms) {
					termClauses.add(createQueryForTerm(term));
				}
				String facetTermDisjunction;
				// queryTerm not ambiguous
				if (mappedTerms.size() == 1)
					facetTermDisjunction = termClauses.get(0);
				// queryTerm is ambiguous, there a multiple terms associated
				// with it
				else
					facetTermDisjunction = String.format("(%s)",
							StringUtils.join(termClauses, " OR "));
				facetTermDisjunctions.add(facetTermDisjunction);
			} else {
				throw new IllegalArgumentException(
						"No facet term mapping for user input query term "
								+ queryTerm + "found!");
			}
		}

		String query = StringUtils.join(facetTermDisjunctions, " AND ");

		// NOTE: The following raises problems when deleting terms. This
		// approach has been abandoned in favor of a full-features parse tree
		// which is developed in the 'Parser' class. This work is not yet
		// finished. Until then, the old system of simple conjunction is used.

		// Above, the Solr search expressions for all terms associated with the
		// user query have been created.
		// However, the boolean structure entered by the user has not yet been
		// accounted for.
		// We will do this by exchanging the exact substring in the user query
		// which have been mapped to terms with the term search expressions,
		// thus conserving the overall boolean structure of the user query.
		// StringBuilder queryBuilder = new StringBuilder(rawQuery);
		// String token;
		// String replacement;
		// int offset = 0;
		// // This must be equivalent to how the query has been analyzed in the
		// first place (in queryDesambiguationService)!
		// TokenStream tokenStream = queryAnalyzer.tokenStream(null,
		// new StringReader(rawQuery));
		// OffsetAttribute offsetAtt = (OffsetAttribute) tokenStream
		// .addAttribute(OffsetAttribute.class);
		// try {
		// while (tokenStream.incrementToken()) {
		// int begin = offsetAtt.startOffset();
		// int end = offsetAtt.endOffset();
		// token = rawQuery.substring(begin, end);
		// if (facetTermDisjunctions.containsKey(token)) {
		// replacement = facetTermDisjunctions.get(token);
		// queryBuilder.replace(begin + offset, end + offset,
		// replacement);
		// offset += replacement.length() - (end - begin);
		// }
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// // empty parentheses are invalid Solr syntax, except in quotes
		// String query = queryBuilder.toString().replaceAll("\\(\\)[^\"]", "");

		logger.debug("Created query: {}", query);
		return query;
	}

	/**
	 * Generates a Solr query which searches for <code>term</code> (i.e. for its
	 * <code>internal_identifier</code>) in all fields associated with
	 * <code>term</code>.
	 * <p>
	 * <exemp> <b>Example:</b><br>
	 * The term with internal_identifier "<em>D00001</em>" and index names
	 * (document fields to search for this query) "<em>mesh</em>" and "
	 * <em>text</em>" would produce these clauses:<br>
	 * - mesh:D00001<br>
	 * - text:D00001<br>
	 * The clauses are then concatenated by white spaces, thus using the default
	 * boolean operator of the employed search engine (for Solr defined in
	 * solr.xml).<br>
	 * The result would be <em>(mesh:D00001 text:D00001)</em>. </exemp>
	 * </p>
	 * 
	 * @param term
	 * @param queryClauses
	 */
	protected String createQueryForTerm(IFacetTerm term) {
		// First check some prerequisites.
		if (term.getIndexNames() == null || term.getIndexNames().size() == 0)
			throw new IllegalArgumentException("Term '" + term.getName()
					+ "' with ID '" + term.getId()
					+ "' has not been given index field names to search in.");

		// list which will be filled with single clauses derived from term.
		// For instance, the term with internal_identifier "D00001" and index
		// names (document fields to search for this query) "mesh" and "text"
		// would produce these clauses:
		// - mesh:D00001
		// - text:D00001
		// The clauses are then concatenated by white spaces, thus using the
		// default boolean operator of the employed search engine (for Solr
		// defined in solr.xml).

		List<String> queryClauses = new ArrayList<String>();
		if (term.getFirstFacet().equals(Facet.KEYWORD_FACET)) {
			// It's a phrase query
			if (term.getId().indexOf(" ") > 0) {
				// For FacetTerms in the keyword facet - that is, user query
				// words
				// which could not associated with a real facet term - their
				// internal_identifier is just the literal user query.
				String phrase = term.getId();

				for (String indexName : term.getIndexNames()) {
					queryClauses.add(indexName + ":" + "\"" + phrase + "\"~"
							+ phraseSlop);
				}
			} else {
				String keyword = term.getId();
				// A Solr search query starting with a minus sign leads to an
				// exception if left unquoted.
				if (keyword.startsWith("-"))
					keyword = "\"" + keyword + "\"";
				for (String indexName : term.getIndexNames()) {
					queryClauses.add(indexName + ":" + "" + keyword);
				}
			}
		} else {
			List<String> internal_identifiers = new ArrayList<String>();
			if (termService.isStringTermID(term.getId())) {
				internal_identifiers.add("\""
						+ termService.getOriginalStringTermAndFacetId(
								term.getId()).getLeft() + "\"");
				if (!StringUtils.isEmpty(term.getSynonyms()))
					for (String stringSynonym : term.getSynonyms().split(";")) {
						if (!StringUtils.isEmpty(stringSynonym))
							internal_identifiers.add("\"" + stringSynonym
									+ "\"");
					}
			} else
				internal_identifiers.add(term.getId());

			for (String internal_identifier : internal_identifiers) {
				for (String indexName : term.getIndexNames()) {
					queryClauses
							.add(indexName + ":" + "" + internal_identifier);
				}
			}
		}
		return String.format("(%s)", StringUtils.join(queryClauses, " OR "));
	}

	// List<String> internal_identifiers = Lists.newArrayList();
	// if (termService.isStringTermID(term.getId())) {
	// Pair<String, Integer> originalStringTermAndFacetId = termService
	// .getOriginalStringTermAndFacetId(term.getId());
	//
	// if
	// (facetService.isAnyAuthorFacetId(originalStringTermAndFacetId.getRight()))
	// {
	// termService.
	// }
	//
	// internal_identifiers.add("\""
	// + originalStringTermAndFacetId.getLeft() + "\"");
	// } else
	// internal_identifiers.add(term.getId());
	//
	// for (String internal_identifier : internal_identifiers) {
	// for (String indexName : term.getIndexNames()) {
	// queryClauses.add(indexName + ":" + ""
	// + internal_identifier);
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.query.IQueryTranslationService#createQueryForSearchNode
	 * (java.util.List, int)
	 */
	@Override
	public String createQueryForSearchNode(
			List<Multimap<String, IFacetTerm>> searchNodes, int targetSNIndex) {
		List<String> nodeQueries = new ArrayList<String>();
		for (int i = 0; i < searchNodes.size(); i++) {
			String substractionNodeQuery = createQueryFromTerms(
					searchNodes.get(i), null);
			nodeQueries.add("(" + substractionNodeQuery + ")");
		}
		return StringUtils.join(nodeQueries, " AND NOT ");
	}

	/**
	 * Returns a concatenation of the kwicQuery string of each terms in
	 * <code>queryTerms</code>. These string are set in the SemedicoSTAG
	 * database. If not set, the internalIdentifier as kwicQuery for this term.
	 * All kwicQueries are concatenated by a white space.
	 * 
	 * @param queryTerms
	 *            The disambiguated user query.
	 * @return The terms' white-space-concatenated kwicTerms.
	 */
	public String createKwicQueryFromTerms(
			Multimap<String, IFacetTerm> queryTerms) {
		String query = "";
		for (IFacetTerm term : queryTerms.values()) {
			String kwicQuery = term.getKwicQuery();
			if (kwicQuery == null) {
				kwicQuery = term.getId();

				if (kwicQuery.indexOf(" ") > 0)
					kwicQuery = "\"" + kwicQuery + "\"";
			}
			query = query.concat(kwicQuery.concat(" "));
		}
		logger.info("kwic query: " + query);
		return query.trim();
	}

	// This method as well as "readStopWordFile" below and all related Fields
	// and the Connstructor are only needed for "TermImport" of the
	// stemnet-tools.
	public String createKwicQueryForTerm(FacetTerm term, List<String> phrases)
			throws IOException {
		String query = "";

		String identifier = term.getId();
		if (identifier.contains(" "))
			throw new IllegalStateException(
					"Term ID \""
							+ identifier
							+ "\" contains a white space which is not allowed for term IDs.");
		String phraseQuery = "";

		Set<String> treatedPhrases = new HashSet<String>();
		queryAnalyzer.setCurrentOperation(QueryAnalyzer.OPERATION_STEMMING);
		for (String phrase : phrases) {

			phrase = queryAnalyzer.analyze(phrase, " ");
			if (phrase.contains(" "))
				phrase = "\"" + phrase + "\"";

			if (!treatedPhrases.contains(phrase)) {
				treatedPhrases.add(phrase);
				phraseQuery += phrase + " ";
			}
		}
		query += " " + phraseQuery.trim();

		if (!query.contains(identifier)) {
			query += " ";
			query += term.getId() + " ";
		}

		return query;
	}

	protected Set<String> readStopwordFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		Set<String> stopwordList = new HashSet<String>();

		String stopword = reader.readLine();
		while (stopword != null) {
			stopwordList.add(stopword.trim());
			stopword = reader.readLine();
		}

		reader.close();
		return stopwordList;
	}

	public int getMaxQuerySize() {
		return maxQuerySize;
	}

	public void setMaxQuerySize(int maxQuerySize) {
		this.maxQuerySize = maxQuerySize;
	}

	public int getPhraseSlop() {
		return phraseSlop;
	}

	@Override
	public void setPhraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
	}

	@Override
	public String createKwicQueryForTerm(IFacetTerm term, List<String> phrases)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
