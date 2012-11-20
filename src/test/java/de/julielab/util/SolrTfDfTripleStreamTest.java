/**
 * SolrTfDfTripleStreamTest.java
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
 * Creation date: 19.11.2012
 **/

/**
 * 
 */
package de.julielab.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.solr.common.util.NamedList;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author faessler
 * 
 */
public class SolrTfDfTripleStreamTest {
	@Test
	public void sortTest() {
		NamedList<Integer> dummy = new NamedList<Integer>();
		NamedList<NamedList<Integer>> list1 = new NamedList<NamedList<Integer>>();
		list1.add("a", dummy);
		list1.add("b", dummy);
		list1.add("c", dummy);

		NamedList<NamedList<Integer>> list2 = new NamedList<NamedList<Integer>>();
		list2.add("b", dummy);
		list2.add("c", dummy);
		list2.add("d", dummy);

		NamedList<NamedList<Integer>> list3 = new NamedList<NamedList<Integer>>();
		list3.add("a", dummy);
		list3.add("e", dummy);

		NamedList<NamedList<Integer>> list4 = new NamedList<NamedList<Integer>>();
		list4.add("b", dummy);
		list4.add("z", dummy);

		List<Iterator<Entry<String, NamedList<Integer>>>> itLists = new ArrayList<Iterator<Entry<String, NamedList<Integer>>>>();
		itLists.add(list1.iterator());
		itLists.add(list2.iterator());
		itLists.add(list3.iterator());
		itLists.add(list4.iterator());

		SolrTfDfTripleStream stream = new SolrTfDfTripleStream(itLists);
		ArrayList<String> correctList = Lists.newArrayList("a", "a", "b", "b",
				"b", "c", "c", "d", "e", "z");
		int numElements = 0;
		while (stream.incrementTuple()) {
			String term = stream.getLeft();
			assertEquals("Element number " + numElements,
					correctList.get(numElements), term);
			numElements++;
		}

		assertEquals("Number of elements", correctList.size(), numElements);

	}
}
