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

package de.julielab.stemnet.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import com.google.common.collect.Multimap;

import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.services.FacetService;
import de.julielab.stemnet.core.services.ITermService;

/**
 * Builds query strings corresponding to the Apache Solr query syntax from a
 * disambiguated query.
 * 
 * @see <a
 *      href="http://wiki.apache.org/solr/SolrQuerySyntax">http://wiki.apache.org/solr/SolrQuerySyntax</a>
 *      for Solr query syntax.
 * @see <a
 *      href="http://lucene.apache.org/java/2_9_1/queryparsersyntax.html">http://lucene.apache.org/java/2_9_1/queryparsersyntax.html</a>
 *      for Lucene query syntax. As the Solr query Syntax is mostly Lucene query
 *      syntax (proper superset), the Lucene syntax could be of greater
 *      interest.
 * 
 * @author faessler
 * 
 */
public class QueryTranslationService implements IQueryTranslationService {

	static final Logger LOG = Logger.getLogger(QueryTranslationService.class);
	protected ITermService termService;

	static final int DEFAULT_MAX_QUERY_SIZE = 256;
	public static final int DEFAULT_PHRASE_SLOP = 1;

	private int maxQuerySize;

	private int phraseSlop;

	public String createQueryFromTerms(Multimap<String, FacetTerm> terms) {
		List<String> facetTermDisjunctions = new ArrayList<String>(terms.size());

		for (String queryTerm : terms.keySet()) {
			Collection<FacetTerm> mappedTerms = terms.get(queryTerm);
			List<String> termClauses = new ArrayList<String>();

			if (mappedTerms.size() > 0) {
				for (FacetTerm term : mappedTerms) {
					termClauses.add(createQueryForTerm(term));
				}
				String facetTermDisjunction = StringUtils
						.join(termClauses, " ");
				if (mappedTerms.size() > 1)
					facetTermDisjunction = "(" + facetTermDisjunction + ")";
				facetTermDisjunctions.add(facetTermDisjunction);
			} else {
				throw new IllegalArgumentException(
						"No facet term mapping for user input query term "
								+ queryTerm + "found!");
			}
		}
		String query = StringUtils.join(facetTermDisjunctions, " AND ");
		LOG.debug("Created query: " + query);
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
	protected String createQueryForTerm(FacetTerm term) {
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
		if (term.getFacet().equals(FacetService.KEYWORD_FACET)) {
			// It's a phrase query
			if (term.getInternalIdentifier().indexOf(" ") > 0) {
				// For FacetTerms in the keyword facet - that is, user query
				// words
				// which could not associated with a real facet term - their
				// internal_identifier is just the literal user query.
				String phrase = term.getInternalIdentifier();

				for (String indexName : term.getIndexNames()) {
					queryClauses.add(indexName + ":" + "\"" + phrase + "\"~"
							+ phraseSlop);
				}
			} else {
				String keyword = term.getInternalIdentifier();
				// A Solr search query starting with a minus sign leads to an
				// exception if left unquoted.
				if (keyword.startsWith("-"))
					keyword = "\"" + keyword + "\"";
				for (String indexName : term.getIndexNames()) {
					queryClauses.add(indexName + ":" + "" + keyword);
				}
			}
		} else {
			String internal_identifier = term.getInternalIdentifier();

			for (String indexName : term.getIndexNames()) {
				queryClauses.add(indexName + ":" + "" + internal_identifier);
			}
		}
		return "(" + StringUtils.join(queryClauses, " ") + ")";
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
			Multimap<String, FacetTerm> queryTerms) {
		String query = "";
		for (FacetTerm term : queryTerms.values()) {
			String kwicQuery = term.getKwicQuery();
			if (kwicQuery == null) {
				kwicQuery = term.getInternalIdentifier();

				if (kwicQuery.indexOf(" ") > 0)
					kwicQuery = "\"" + kwicQuery + "\"";
			}
			query = query.concat(kwicQuery.concat(" "));
		}
		LOG.info("kwic query: " + query);
		return query.trim();
	}

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
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

	public void setPhraseSlop(int phraseSlop) {
		this.phraseSlop = phraseSlop;
	}

}
