/** 
 * TermOccurrenceFilterServiceTest.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 28.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.TermOccurrenceFilterService;


public class TermOccurrenceFilterServiceTest extends TestCase{

	public void testFilterTermOccurrences() throws Exception{
		TermOccurrenceFilterService filterService = new TermOccurrenceFilterService();

		FacetTerm term = new FacetTerm("term1", "name");
		FacetTerm firstAuthorTerm = new FacetTerm("firstAuthorTerm", "name");
		firstAuthorTerm.setFacet(new Facet(Facet.FIRST_AUTHOR_FACET_ID));
		FacetTerm lastAuthorTerm = new FacetTerm("lastAuthorTerm", "name");
		lastAuthorTerm.setFacet(new Facet(Facet.LAST_AUTHOR_FACET_ID));
		
		List<String> occurrences = new ArrayList<String>();
		occurrences.add("tooLongTooLongTooLong");
		occurrences.add("term, termQualifier");
		occurrences.add("token1 token2 token3");
		occurrences.add("correct term");
		
		Collection<String> filteredOccurrences = filterService.filterTermOccurrences(term, occurrences);
		Iterator<String> filteredOccurrencesIterator = filteredOccurrences.iterator();
		assertEquals(1, filteredOccurrences.size());
		assertEquals("correct term", filteredOccurrencesIterator.next());

		filteredOccurrences = filterService.filterTermOccurrences(firstAuthorTerm, occurrences);
		filteredOccurrencesIterator = filteredOccurrences.iterator();
		assertEquals(2, filteredOccurrences.size());
		assertEquals("term, termQualifier", filteredOccurrencesIterator.next());
		assertEquals("correct term", filteredOccurrencesIterator.next());

		filteredOccurrences = filterService.filterTermOccurrences(lastAuthorTerm, occurrences);
		filteredOccurrencesIterator = filteredOccurrences.iterator();
		assertEquals(2, filteredOccurrences.size());
		assertEquals("term, termQualifier", filteredOccurrencesIterator.next());
		assertEquals("correct term", filteredOccurrencesIterator.next());
		
	}
}
