/** 
 * FacetRecommenderTest.java
 * 
 * Copyright (c) 2014, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: matthies
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: Oct 2, 2014 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;

public class FacetRecommenderTest {
	
	static List<String> test_tids = Arrays.asList("tid188830_p3","tid189037_s","tid188594_n");
	static List<String> sorted_facet_list = Arrays.asList(	"fid194","fid14",									// w: 24.5
															"fid337","fid106",									// w: 18
															"fid318","fid261","fid220","fid185","fid18",		// w: 16.5
															"fid350",											// w: 10
															"fid95","fid67","fid40","fid283","fid259","fid208");// w: 8
	
	static String first_tid = "tid188830";
	static List<String> first_facets = Arrays.asList("fid261","fid185","fid14","fid194","fid318","fid18","fid220");
	static String second_tid = "tid189037";
	static List<String> second_facets = Arrays.asList("fid67","fid40","fid14","fid194","fid208","fid95","fid106","fid337","fid283","fid259");
	static String third_tid = "tid188594";
	static List<String> third_facets = Arrays.asList("fid261","fid185","fid350","fid14","fid194","fid318","fid18","fid106","fid220","fid337");
	
	IConceptService tMock;
	IHierarchicalConcept fMock;
	
	@BeforeClass
	public void initialize() {
		tMock = createMock(IConceptService.class);
		fMock = createMock(IHierarchicalConcept.class);
		expect(tMock.getTerm(first_tid))
			.andReturn(fMock);
		expect(fMock.getFacets())
			.andReturn(getFacetList(first_facets));
		expect(tMock.getTerm(second_tid))
			.andReturn(fMock);
		expect(fMock.getFacets())
			.andReturn(getFacetList(second_facets));
		expect(tMock.getTerm(third_tid))
			.andReturn(fMock);
		expect(fMock.getFacets())
			.andReturn(getFacetList(third_facets));
		replay(tMock);
		replay(fMock);
	}
	
	@Test
	public void testGetSortedFacets() {
		FacetRecommenderService facetRecommender = new FacetRecommenderService(tMock);
		assertEquals(sorted_facet_list,
				facetRecommender.getSortedFacets(test_tids) ,"Sorting is not correct.");
	}
	
	@Test
	public void testGetSortedFacetsByRange() {
		FacetRecommenderService facetRecommender = new FacetRecommenderService(tMock);
		assertEquals(sorted_facet_list.subList(5, 10),
				facetRecommender.getSortedFacetsByRange(test_tids,5,10), "Sublist is not correct.");
	}
	
	@Test
	public void testGetSortedFacetsByQuantity() {
		FacetRecommenderService facetRecommender = new FacetRecommenderService(tMock);
		assertEquals(sorted_facet_list.subList(0, 5),
				facetRecommender.getSortedFacetsByQuantity(test_tids,5), "Sublist is not correct.");
	}
	
	private List<Facet> getFacetList(List<String> idlist) {
		ArrayList<Facet> flist = new ArrayList<Facet>();
		for (String fid : idlist) {
			flist.add(new Facet(fid));
		}
		return flist;
	}
}
