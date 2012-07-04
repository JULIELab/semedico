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

import static de.julielab.semedico.core.services.interfaces.IStringTermService.WS_REPLACE;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.IFacetTerm;

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
		termService = createMock(ITermService.class);
		facetService = createMock(IFacetService.class);
		stringTermService = new StringTermService(termService, facetService);
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
		Pair<String,Integer> stringTermAndFacetId = stringTermService.getOriginalStringTermAndFacetId(correctAuthorId);
		assertEquals(authorName, stringTermAndFacetId.getLeft());
		assertEquals(facet.getId(), stringTermAndFacetId.getRight());
	}
	
	@Test
	public void getTermObjectForStringTerm() {
		IFacetTerm term = stringTermService.getTermObjectForStringTerm(authorName, facet);
		assertEquals("Term name", authorName, term.getName());
		assertEquals("Term ID", correctAuthorId, term.getId());
	}
	
	@Test
	public void testIsStringTermId() {
		assertTrue(stringTermService.isStringTermID(correctAuthorId));
		assertFalse(stringTermService.isStringTermID(authorName));
	}
}
