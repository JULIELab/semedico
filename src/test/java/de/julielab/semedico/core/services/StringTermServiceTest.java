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
import static de.julielab.semedico.core.util.MergingTripleStreamTest.getCountPair;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.db.IDBConnectionService;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.MergingTripleStreamTest.TestTermCountCursor;

/**
 * @author faessler
 * 
 */
public class StringTermServiceTest {

	private StringTermService stringTermService;
	private ITermService termService;
	private IFacetService facetService;

	private Facet facet = new Facet(NodeIDPrefixConstants.FACET + "42");
	private String authorName = "Rowling, J K";
	private String correctAuthorId = "Rowling," + WS_REPLACE + "J" + WS_REPLACE
			+ "K__FACET_ID:" + NodeIDPrefixConstants.FACET + "42";

	@Before
	public void setup() {
		Logger logger = LoggerFactory.getLogger(StringTermServiceTest.class);
		termService = createMock(ITermService.class);
		IDBConnectionService dbConnectionService = createMock(IDBConnectionService.class);
		facetService = createMock(IFacetService.class);
		ISearchService searchService = createMock(ISearchService.class);
		IRuleBasedCollatorWrapper collatorWrapper = SemedicoCoreBaseModule
				.buildRuleBasedCollatorWrapper();
		stringTermService = new StringTermService(logger, termService,
				facetService, dbConnectionService, searchService, collatorWrapper);
	}

	@Test
	public void testGetStringTermId() {
		String stringTermId = stringTermService.getStringTermId(authorName,
				facet);
		assertEquals("String term ID", correctAuthorId, stringTermId);
	}

	@Test
	public void testCheckStringTermId() {
		expect(termService.hasTerm(correctAuthorId)).andReturn(false);
		replay(termService);

		String outcome = stringTermService.checkStringTermId(authorName, facet);
		verify(termService);
		assertEquals("ID creation check outcome", correctAuthorId, outcome);
	}

	@Test
	public void testGetOriginalStringTermAndFacetId() {
		Pair<String, String> stringTermAndFacetId = stringTermService
				.getOriginalStringTermAndFacetId(correctAuthorId);
		assertEquals(authorName, stringTermAndFacetId.getLeft());
		assertEquals(facet.getId(), stringTermAndFacetId.getRight());
	}

	@Test
	public void getTermObjectForStringTerm() {
		Concept term = stringTermService.getTermObjectForStringTerm(
				authorName, facet);
		assertEquals("Term name", authorName, term.getPreferredName());
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

	@SuppressWarnings("unchecked")
	@Test
	public void testComputeAuthorNameCountSynsets() {
		Pair<String,Long> c1 = getCountPair("Parkinson, Eric Kenneth", 1L);
		Pair<String, Long> c2 = getCountPair("Parkinson, Eric K", 2L);
		Pair<String, Long> c3 = getCountPair("Parkinson, E K", 3L);
		Pair<String, Long> c4 = getCountPair( "Sühnel, J", 4L);
		Pair<String, Long> c5 = getCountPair("Suehnel, Jurgen", 5L);
		List<Pair<String, Long>> nameCounts = Lists.newArrayList(c1, c2, c3, c4, c5);
		TestTermCountCursor nameCountCursor = new TestTermCountCursor(nameCounts);
		
		Map<Pair<String, Long>, Set<String>> countSynsets = stringTermService
				.computeAuthorNameCountSynsets(nameCountCursor);
		assertEquals(2, countSynsets.size());

		List<Pair<String, Long>> sortedCanonicalCounts = new ArrayList<>();
		Iterator<Pair<String, Long>> it = countSynsets.keySet().iterator();
		sortedCanonicalCounts.add(it.next());
		sortedCanonicalCounts.add(it.next());
		Collections.sort(sortedCanonicalCounts, new Comparator<Pair<String, Long>>() {
			@Override
			public int compare(Pair<String, Long> arg0, Pair<String, Long> arg1) {
				return arg0.getLeft().compareTo(arg1.getLeft());
			}
		});

		Pair<String, Long> parkinsonCanon = sortedCanonicalCounts.get(0);
		Set<String> parkinsons = countSynsets.get(parkinsonCanon);
		assertEquals("Parkinson, Eric Kenneth", parkinsonCanon.getLeft());
		assertEquals(3, parkinsons.size());
		assertTrue(parkinsons.contains(c1.getLeft()));
		assertTrue(parkinsons.contains(c2.getLeft()));
		assertTrue(parkinsons.contains(c3.getLeft()));
		assertEquals(new Long(6), parkinsonCanon.getRight());

		Pair<String,Long> suehnelCanon = sortedCanonicalCounts.get(1);
		Set<String> suehnels = countSynsets.get(suehnelCanon);
		// The variant with full first- and last name wins the
		// "Mr. Canonical Name" contest (despite not containing a diacritic).
		assertEquals("Suehnel, Jurgen", suehnelCanon.getLeft());
		assertEquals(2, suehnels.size());
		assertTrue(suehnels.contains(c4.getLeft()));
		assertTrue(suehnels.contains(c5.getLeft()));
		assertEquals(new Long(9), suehnelCanon.getRight());
	}
}
