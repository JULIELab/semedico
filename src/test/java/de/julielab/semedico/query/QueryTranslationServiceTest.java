/**
 * QueryTranslationServiceTest.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler chew
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 19.04.2011
 **/

package de.julielab.semedico.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Facet.Source;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IStopWordService;
import de.julielab.semedico.core.services.StopWordService;

/**
 * This class tests whether the correct Solr queries are produced from a
 * disambiguated Query (that is a Mapping from the input Strings to associated
 * Term objects).
 * 
 * @author faessler
 */
public class QueryTranslationServiceTest {

	private QueryTranslationService queryTransService;

	@Before
	public void setup() throws IOException {
		Logger logger = LoggerFactory
				.getLogger(QueryTranslationServiceTest.class);
		IStopWordService stopWordService = new StopWordService(logger,
				"src/test/resources/test_stopwords.txt");

		queryTransService = new QueryTranslationService(logger, stopWordService);
	}

	@Test
	public void testCreateQueryFromTerms() throws Exception {

		Multimap<String, IFacetTerm> terms = LinkedHashMultimap.create();
		String rawQuery = "X OR (Y \"Z\")";

		String query = queryTransService.createQueryFromTerms(terms, rawQuery);
		assertEquals("(X OR (Y AND \"Z\"))", query);

		// TODO test with substitution. couldn't write one as facets aren't
		// working

	}

	@Test
	public void testCreateQueryForTerm() throws Exception {
		Collection<String> indexNames1 = Lists.newArrayList("fieldName1",
				"fieldName2");
		Collection<String> indexNames2 = Lists.newArrayList("fieldName3",
				"fieldName4");
		Facet facet1 = createFacet(1);

		FacetTerm term = new FacetTerm("internal_identifier_1", "name");
		term.setIndexNames(indexNames1);
		term.addFacet(facet1);

		FacetTerm termWithMinus = new FacetTerm("-keyword_identifier", "name");
		termWithMinus.setIndexNames(indexNames1);
		termWithMinus.addFacet(Facet.KEYWORD_FACET);

		FacetTerm phraseQueryTerm = new FacetTerm("phrase query", "name");
		phraseQueryTerm.setIndexNames(indexNames2);
		phraseQueryTerm.addFacet(Facet.KEYWORD_FACET);

		String solrQuery = null;

		queryTransService.setPhraseSlop(2);
		solrQuery = queryTransService.createQueryForTerm(term);

		// A single Term from the facet hierarchy should just result in a search
		// for the Term's identifier in the fields this Term could occur.
		// E.g. a MeSH Term with ID D00043 and fields "mesh" and "text" to occur
		// should give a query like this:
		// mesh:D00043 OR text:D00043 (SolrQuery; meaning: Return documents
		// containing in the field "mesh" the (Lucene) Term D00043 OR in the
		// field "text" the (Lucene) Term D00043.
		assertEquals(
				"(fieldName1:internal_identifier_1 OR fieldName2:internal_identifier_1)",
				solrQuery);

		solrQuery = queryTransService.createQueryForTerm(termWithMinus);
		// Search queries which begin with a minus sign will lead to an
		// Exception (similar in the TermSuggestionService).
		// Therefore, they must be embraced by quotes.
		assertEquals(
				"(fieldName1:\"-keyword_identifier\" OR fieldName2:\"-keyword_identifier\")",
				solrQuery);

		solrQuery = queryTransService.createQueryForTerm(phraseQueryTerm);
		// Phrases a treated quite similarly. However, they are embraced in
		// double quotes to save white spaces. A tilde (~) followed by a
		// positive number determines the allowed term proximity (or phrase
		// slop). That is, how many terms may stand in between the individual
		// white spaced separated terms of a phrase query.
		assertEquals(
				"(fieldName3:\"phrase query\"~2 OR fieldName4:\"phrase query\"~2)",
				solrQuery);

	}

	/**
	 * @param i
	 * @return
	 */
	private Facet createFacet(int i) {
		return new Facet(i);
	}

	@Test
	public void testCreateQueryForTerms() throws Exception {
		Collection<String> indexNames1 = Lists.newArrayList("fieldName1",
				"fieldName2");
		Collection<String> indexNames2 = Lists.newArrayList("fieldName3",
				"fieldName4");
		Facet facet1 = createFacet(1);
		Facet facet2 = createFacet(2);

		FacetTerm term1 = new FacetTerm("internal_identifier_1", "name");
		term1.setIndexNames(indexNames1);
		term1.addFacet(facet1);

		FacetTerm term2 = new FacetTerm("internal_identifier_2", "name");
		term2.setIndexNames(indexNames1);
		term2.addFacet(facet1);

		FacetTerm term3 = new FacetTerm("internal_identifier_3", "name");
		term3.setIndexNames(indexNames1);
		term3.addFacet(facet2);

		FacetTerm phraseQueryTerm = new FacetTerm("phrase query", "name");
		phraseQueryTerm.setIndexNames(indexNames2);
		phraseQueryTerm.addFacet(Facet.KEYWORD_FACET);

		String rawQuery = "queryString1 AND queryString2 AND phraseString";

		// One user entered query token or phrase may be associated with a set
		// of facet terms. Here, "queryString1" is ambiguous as it is associated
		// with multiple, different terms.
		Multimap<String, IFacetTerm> dQuery = LinkedHashMultimap.create();
		dQuery.put("queryString1", term1);
		dQuery.put("queryString1", term2);
		dQuery.put("queryString2", term3);
		dQuery.put("phraseString", phraseQueryTerm);

		String solrQuery = null;
		QueryTranslationService queryTransService = (QueryTranslationService) this.queryTransService;
		queryTransService.setPhraseSlop(3);

		solrQuery = (String) queryTransService.createQueryFromTerms(dQuery,
				rawQuery);

		// For each user query token, any of its associated terms must occur in
		// a document for being a hit (OR, default operator). But for every
		// query token, at least one associated facet term must produce a hit
		// somehow (AND).
		assertEquals(
				"((fieldName1:internal_identifier_1 OR fieldName2:internal_identifier_1) OR "
						+ "(fieldName1:internal_identifier_2 OR fieldName2:internal_identifier_2)) AND "
						+ "(fieldName1:internal_identifier_3 OR fieldName2:internal_identifier_3) AND "
						+ "(fieldName3:\"phrase query\"~3 OR fieldName4:\"phrase query\"~3)",
				solrQuery);
	}

}
