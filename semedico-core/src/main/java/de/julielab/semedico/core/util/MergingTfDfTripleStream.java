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
package de.julielab.semedico.core.util;

import java.util.List;

import de.julielab.scicopia.core.elasticsearch.legacy.IFacetField.FacetType;
import de.julielab.scicopia.core.elasticsearch.legacy.TermCountCursor;
import de.julielab.semedico.core.services.interfaces.ITermDocumentFrequencyService;

/**
 * <p>
 * A triple stream that merges sorted inputs into one single sorted output.
 * </p>
 * <p>
 * When the input cursors are sorted on the <code>getName()</code> values, the output will be sorted as well. Time is
 * linear to the iterator with the most elements. In every step - i.e. each call of {@link #incrementTuple()} - the
 * least name - alpha-numerical sorted - is determined. The cursor from which the name originated is then forwarded
 * once.
 * </p>
 * <p>
 * The class was originally written for potential B-term retrieval from multiple fields. After the retrieval, another
 * algorithm would determine the intersection between the streams, assuming they are sorted.
 * </p>
 * 
 * @author faessler
 * 
 */
public class MergingTfDfTripleStream implements TripleStream<String, Long, Long> {
	private final List<TermCountCursor> bTermDfCountLists;
	// private Map<Entry<String, NamedList<Integer>>, Iterator<Entry<String,
	// NamedList<Integer>>>> currentTermDfCounts;
	// private Entry<String, NamedList<Integer>> currentCount;
	private int leastNameIndex = -1;
	/**
	 * Must be set to true in initialize if any TermCountCursor was forwarded successfully. Then, the current
	 * TermCountCursor is forwarded in "incrementTuple" and this variable stores whether the increment was successful.
	 */
	private boolean hasMoreElements = false;
	private ITermDocumentFrequencyService termDocFreqService;

	/**
	 * 
	 */
	public MergingTfDfTripleStream(List<TermCountCursor> bTermCountLists) {
		this.bTermDfCountLists = bTermCountLists;
		initialize();
	}

	/**
	 * 
	 */
	private void initialize() {
		for (TermCountCursor it : bTermDfCountLists) {
			hasMoreElements = it.forwardCursor() || hasMoreElements;
		}

	}

	@Override
	public String getLeft() {
		return bTermDfCountLists.get(leastNameIndex).getName();
	}

	@Override
	public Long getMiddle() {
		return (Long) bTermDfCountLists.get(leastNameIndex).getFacetCount(FacetType.count);
	}

	@Override
	public Long getRight() {
		// With the aggregations in ElastiSearch, we lost the capability to retrieve the document frequencies together
		// with the term counts. Thus, the term document frequency service was created that caches the document
		// frequencies and updates them on a regular basis (see SemedicoCoreModule for details).
		if (null != termDocFreqService) {
			String termId = getLeft();
			return termDocFreqService.getDocumentFrequencyForTerm(termId);
		}
		return (Long) bTermDfCountLists.get(leastNameIndex).getFacetCount(FacetType.documentFrequency);
	}

	@Override
	public boolean incrementTuple() {
		// For the first call, we must not forward the cursor because
		// initialize() did this already. After that, we must here forward the
		// cursor used in the last iteration step so that its next element is
		// used for the determination of the new least term.
		if (leastNameIndex > -1)
			bTermDfCountLists.get(leastNameIndex).forwardCursor();
		determineLeastTermDfCount();
		return hasMoreElements;
	}

	/**
	 * 
	 */
	private void determineLeastTermDfCount() {
		String leastTerm = null;
		// We will have more elements when we find a new least term to return.
		hasMoreElements = false;
		for (int i = 0; i < bTermDfCountLists.size(); i++) {
			TermCountCursor cursor = bTermDfCountLists.get(i);
			if (cursor.isValid()) {
				String term = cursor.getName();
				if (null == leastTerm || term.compareTo(leastTerm) < 0) {
					leastTerm = term;
					leastNameIndex = i;
					hasMoreElements = true;
				}
			}
		}

	}

	public void setTermDocumentFrequencyProvider(ITermDocumentFrequencyService termDocFreqService) {
		this.termDocFreqService = termDocFreqService;
	}

	@Override
	public void reset() {
		for (TermCountCursor cursor : bTermDfCountLists)
			cursor.reset();
		leastNameIndex = -1;
		initialize();
	}

}
