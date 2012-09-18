/**
 * StringTermServiceTest.java
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

import static de.julielab.semedico.core.services.StringTermService.WS_REPLACE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.db.IDBConnectionService;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * @author faessler
 * 
 */
public class StringTermServiceTest {

	private StringTermService stringTermService;
	private ITermService termService;
	private IFacetService facetService;

	private Facet facet = new Facet(42);
	private String authorName = "Rowling, J K";
	private String correctAuthorId = "Rowling," + WS_REPLACE + "J" + WS_REPLACE
			+ "K__FACET_ID:42";

	@Before
	public void setup() {
		Logger logger = LoggerFactory.getLogger(StringTermServiceTest.class);
		termService = createMock(ITermService.class);
		IDBConnectionService dbConnectionService = createMock(IDBConnectionService.class);
		facetService = createMock(IFacetService.class);
		SolrServer solr = createMock(SolrServer.class);
		IRuleBasedCollatorWrapper collatorWrapper = SemedicoCoreModule
				.buildRuleBasedCollatorWrapper();
		ApplicationStateManager asm = createMock(ApplicationStateManager.class);
		stringTermService = new StringTermService(logger, termService,
				facetService, dbConnectionService, solr, collatorWrapper, asm);
	}

	@Test
	public void testGetStringTermId() {
		String stringTermId = stringTermService.getStringTermId(authorName,
				facet);
		assertEquals("String term ID", correctAuthorId, stringTermId);
	}

	@Test
	public void testCheckStringTermId() {
		expect(termService.hasNode(correctAuthorId)).andReturn(false);
		replay(termService);

		String outcome = stringTermService.checkStringTermId(authorName, facet);
		verify(termService);
		assertEquals("ID creation check outcome", correctAuthorId, outcome);
	}

	@Test
	public void testGetOriginalStringTermAndFacetId() {
		Pair<String, Integer> stringTermAndFacetId = stringTermService
				.getOriginalStringTermAndFacetId(correctAuthorId);
		assertEquals(authorName, stringTermAndFacetId.getLeft());
		assertEquals(facet.getId(), stringTermAndFacetId.getRight());
	}

	@Test
	public void getTermObjectForStringTerm() {
		IFacetTerm term = stringTermService.getTermObjectForStringTerm(
				authorName, facet);
		assertEquals("Term name", authorName, term.getName());
		assertEquals("Term ID", correctAuthorId, term.getId());
	}

	@Test
	public void testIsStringTermId() {
		assertTrue(stringTermService.isStringTermID(correctAuthorId));
		assertFalse(stringTermService.isStringTermID(authorName));
	}

	@Test
	public void testNameIsMoreGeneralThan() {
		assertTrue(stringTermService.nameIsMoreGeneralThan("Parkinson, E K",
				"Parkinson, E Kenneth"));
		assertTrue(stringTermService.nameIsMoreGeneralThan("Parkinson, E",
				"Parkinson, E Kenneth"));
		assertTrue(stringTermService.nameIsMoreGeneralThan("Parkinson, E",
				"Parkinson, E K"));
		assertFalse(stringTermService.nameIsMoreGeneralThan("Parkinson, E K",
				"Parkinson, E"));
		assertFalse(stringTermService.nameIsMoreGeneralThan("Parkinson, E K",
				"Parkinson, E K"));
		assertFalse(stringTermService.nameIsMoreGeneralThan("Parkinson,",
				"Muller, M A"));
	}

	@Test
	public void testComputeAuthorSynsets() throws FileNotFoundException,
			IOException {
		List<String> authorNames = IOUtils.readLines(new FileInputStream(
				"src/test/resources/authornames.txt"));

		HashMap<String, Set<String>> synSets = stringTermService
				.computeAuthorSynsets(authorNames);

		// In case you want to see the whole output
		// List<String> canList = new ArrayList<String>(synSets.keySet());
		// Collections.sort(canList);
		// for (String can : canList) {
		// Set<String> set = synSets.get(can);
		// System.out.println(can);
		// for (String an : set)
		// System.out.println("\t" + an);
		// }

		// The variant "Parkinson, E K" should be present for both, Eric Kenneth
		// and E Ken.
		assertTrue(synSets.get("Parkinson, Eric Kenneth").contains(
				"Parkinson, Eric Kenneth"));
		assertTrue(synSets.get("Parkinson, Eric Kenneth").contains(
				"Parkinson, E Kenneth"));
		assertTrue(synSets.get("Parkinson, Eric Kenneth").contains(
				"Parkinson, Eric K"));
		assertTrue(synSets.get("Parkinson, Eric Kenneth").contains(
				"Parkinson, E K"));

		assertTrue(synSets.get("Parkinson, E Ken").contains("Parkinson, E Ken"));
		assertTrue(synSets.get("Parkinson, E Ken").contains("Parkinson, E K"));
	}
}
