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
package de.julielab.semedico.core.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author faessler
 * 
 */
public class MergingTripleStreamTest {
	@Test
	public void sortTest() {
		List<Pair<String, Long>> list1 = new ArrayList<>();
		list1.add(getCountPair("a", 1L));
		list1.add(getCountPair("b", 2L));
		list1.add(getCountPair("c", 3L));

		List<Pair<String, Long>> list2 = new ArrayList<>();
		list2.add(getCountPair("b", 4L));
		list2.add(getCountPair("c", 5L));
		list2.add(getCountPair("d", 6L));

		List<Pair<String, Long>> list3 = new ArrayList<>();
		list3.add(getCountPair("a", 7L));
		list3.add(getCountPair("e", 8L));

		List<Pair<String, Long>> list4 = new ArrayList<>();
		list4.add(getCountPair("b", 9L));
		list4.add(getCountPair("z", 10L));

		List<TermCountCursor> cursorLists = new ArrayList<>();
		cursorLists.add(new TestTermCountCursor(list1));
		cursorLists.add(new TestTermCountCursor(list2));
		cursorLists.add(new TestTermCountCursor(list3));
		cursorLists.add(new TestTermCountCursor(list4));

		MergingTfDfTripleStream stream = new MergingTfDfTripleStream(
				cursorLists);
		ArrayList<String> correctList = Lists.newArrayList("a", "a", "b", "b",
				"b", "c", "c", "d", "e", "z");
		ArrayList<Long> correctCountList = Lists.newArrayList(1L, 7L, 2L, 4L,
				9L, 3L, 5L, 6L, 8L, 10L);
		int numElements = 0;
		while (stream.incrementTuple()) {
			String term = stream.getLeft();
			Long count = stream.getRight();
			assertEquals("Element number " + numElements,
					correctList.get(numElements), term);
			assertEquals("Element count " + numElements,
					correctCountList.get(numElements), count);
			numElements++;
		}

		assertEquals("Number of elements", correctList.size(), numElements);

	}

	public static Pair<String, Long> getCountPair(String string, Long i) {
		return new MutablePair<>(string, i);
	}

	public static class TestTermCountCursor implements TermCountCursor {

		private List<Pair<String, Long>> countList;
		private int index;

		public TestTermCountCursor(List<Pair<String, Long>> countList) {
			this.countList = countList;
			index = -1;
		}

		@Override
		public boolean forwardCursor() {
			index++;
			return index < countList.size();
		}

		@Override
		public String getName() {
			return countList.get(index).getLeft();
		}

		@Override
		public Number getFacetCount(String type) {
			// For the test we just ignore different facet types.
			return countList.get(index).getRight();
		}

		@Override
		public long size() {
			return countList.size();
		}

		@Override
		public boolean isValid() {
			return index < countList.size() && index > -1;
		}

		@Override
		public void reset() {
			index = -1;
		}

	}
}
