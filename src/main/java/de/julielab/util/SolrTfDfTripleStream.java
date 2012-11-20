/**
 * SolrTfDfTripleStream.java
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.util.NamedList;

/**
 * <p>
 * A triple stream that merges sorted inputs into one single sorted output.
 * </p>
 * <p>
 * When the input iterators are sorted on the <code>Entry</code> keys, the
 * output will be sorted as well. Time is linear to the iterator with the most
 * elements. In every step - i.e. each call of {@link #incrementTuple()} - the
 * least key is determined. The iterator from which the key originated is then
 * proceeded to the next <code>Entry</code>.
 * </p>
 * <p>
 * The class was originally written for potential B-term retrieval from multiple
 * fields. After the retrieval, another algorithm would determine the
 * intersection between the streams, assuming they are sorted.
 * </p>
 * 
 * @author faessler
 * 
 */
public class SolrTfDfTripleStream implements
		TripleStream<String, Integer, Integer> {
	private final List<Iterator<Entry<String, NamedList<Integer>>>> bTermDfCountLists;
	private Map<Entry<String, NamedList<Integer>>, Iterator<Entry<String, NamedList<Integer>>>> currentTermDfCounts;
	private Entry<String, NamedList<Integer>> currentCount;

	/**
	 * 
	 */
	public SolrTfDfTripleStream(
			List<Iterator<Entry<String, NamedList<Integer>>>> bTermDfCountLists) {
		this.bTermDfCountLists = bTermDfCountLists;
		this.currentTermDfCounts = new HashMap<Entry<String, NamedList<Integer>>, Iterator<Entry<String, NamedList<Integer>>>>(
				bTermDfCountLists.size());
		initialize();
	}

	/**
	 * 
	 */
	private void initialize() {
		for (Iterator<Entry<String, NamedList<Integer>>> it : bTermDfCountLists) {
			if (it.hasNext()) {
				Entry<String, NamedList<Integer>> termDfCount = it.next();
				currentTermDfCounts.put(termDfCount, it);
			}
		}

	}

	@Override
	public String getLeft() {
		return currentCount.getKey();
	}

	@Override
	public Integer getMiddle() {
		NamedList<Integer> stats = currentCount.getValue();
		Integer termFrequency = stats.get("tf");
		return termFrequency;
	}

	@Override
	public Integer getRight() {
		NamedList<Integer> stats = currentCount.getValue();
		Integer documentFrequency = stats.get("df");
		return documentFrequency;
	}

	@Override
	public boolean incrementTuple() {
		if (currentTermDfCounts.size() > 0) {
			determineLeastTermDfCount();
			incrementLeastIterator();
			return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void determineLeastTermDfCount() {
		String leastTerm = null;
		for (Entry<String, NamedList<Integer>> termDfCount : currentTermDfCounts
				.keySet()) {
			String term = termDfCount.getKey();
			if (null == leastTerm || term.compareTo(leastTerm) < 0) {
				leastTerm = term;
				currentCount = termDfCount;
			}
		}
	}

	/**
	 * 
	 */
	private void incrementLeastIterator() {
		Iterator<Entry<String, NamedList<Integer>>> it = currentTermDfCounts
				.get(currentCount);
		if (it.hasNext()) {
			Entry<String, NamedList<Integer>> nextTermDfCount = it.next();
			currentTermDfCounts.put(nextTermDfCount, it);
		}
		currentTermDfCounts.remove(currentCount);
	}
}
