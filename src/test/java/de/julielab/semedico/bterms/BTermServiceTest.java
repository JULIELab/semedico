/**
 * BTermServiceTest.java
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
 * Creation date: 09.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.bterms;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.LabelCacheService;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.util.AbstractTripleStream.TripleTransformer;
import de.julielab.util.TripleStream;
import de.julielab.util.TripleTransformationStream;

/**
 * @author faessler
 * 
 */
public class BTermServiceTest {

	private BTermService bTermService;
	private ITermService termService;

	@Before
	public void setup() {
		termService = createMock(ITermService.class);
		expect(termService.getNodes()).andReturn(
				Lists.<IFacetTerm> newArrayList());
		replay(termService);
		LabelCacheService labelCacheService = new LabelCacheService(
				LoggerFactory.getLogger(LabelCacheService.class), termService,
				100);
		IFacetedSearchService searchService = createMock(IFacetedSearchService.class);
		bTermService = new BTermService(
				LoggerFactory.getLogger(BTermService.class), searchService,
				labelCacheService);
	}

	@Test
	public void calculateIntersectionTest() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		// FacetField ff = new FacetField("testfield");
		// Count a1 = new Count(ff, "a", 1);
		// Count b1 = new Count(ff, "b", 1);
		// Count c1 = new Count(ff, "c", 1);
		// Count e1 = new Count(ff, "e", 1);
		// Count f1 = new Count(ff, "f", 1);
		// List<Count> terms1 = Lists.newArrayList(a1, b1, c1, e1, f1);
		//
		// Count a2 = new Count(ff, "a", 1);
		// Count c2 = new Count(ff, "c", 1);
		// Count e2 = new Count(ff, "e", 1);
		// List<Count> terms2 = Lists.newArrayList(a2, c2, e2);
		//
		// Count b3 = new Count(ff, "b", 1);
		// Count c3 = new Count(ff, "c", 1);
		// Count d3 = new Count(ff, "d", 1);
		// Count e3 = new Count(ff, "e", 1);
		// Count g3 = new Count(ff, "g", 1);
		// List<Count> terms3 = Lists.newArrayList(b3, c3, d3, e3, g3);

		TripleTransformer<Object[], String, Integer, Integer> transformer = new TripleTransformer<Object[], String, Integer, Integer>() {

			@Override
			public String transformLeft(Object[] sourceElement) {
				return (String) sourceElement[0];
			}

			@Override
			public Integer transformMiddle(Object[] sourceElement) {
				return (Integer) sourceElement[1];
			}

			@Override
			public Integer transformRight(Object[] sourceElement) {
				return (Integer) sourceElement[2];
			}

		};

		Object[] a1 = new Object[] { "a", 1, 1 };
		Object[] b1 = new Object[] { "b", 1, 1 };
		Object[] c1 = new Object[] { "c", 1, 1 };
		Object[] e1 = new Object[] { "e", 1, 1 };
		Object[] f1 = new Object[] { "f", 1, 1 };
		List<Object[]> terms1 = Lists.newArrayList(a1, b1, c1, e1, f1);
		TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer> stream1 = new TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer>(
				terms1, transformer);

		Object[] a2 = new Object[] { "a", 1, 1 };
		Object[] c2 = new Object[] { "c", 1, 1 };
		Object[] e2 = new Object[] { "e", 1, 1 };
		List<Object[]> terms2 = Lists.newArrayList(a2, c2, e2);
		TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer> stream2 = new TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer>(
				terms2, transformer);

		Object[] b3 = new Object[] { "b", 1, 1 };
		Object[] c3 = new Object[] { "c", 1, 1 };
		Object[] d3 = new Object[] { "d", 1, 1 };
		Object[] e3 = new Object[] { "e", 1, 1 };
		Object[] g3 = new Object[] { "g", 1, 1 };
		List<Object[]> terms3 = Lists.newArrayList(b3, c3, d3, e3, g3);
		TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer> stream3 = new TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer>(
				terms3, transformer);

		@SuppressWarnings("unchecked")
		List<TripleTransformationStream<Object[], Iterable<Object[]>, String, Integer, Integer>> listsList = Lists
				.newArrayList(stream1, stream2, stream3);

		Method method = bTermService.getClass().getDeclaredMethod(
				"calculateIntersection", List.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<Label> labels = (List<Label>) method.invoke(bTermService,
				listsList);

		Set<String> labelNames = new HashSet<String>();
		for (Label l : labels)
			labelNames.add(l.getName());

		assertTrue(labelNames.contains("c"));
		assertTrue(labelNames.contains("e"));
	}
}
