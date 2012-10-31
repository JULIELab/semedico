/**
 * BTermService.java
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
 * Creation date: 04.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.bterms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;
import de.julielab.util.TripleStream;
import de.julielab.util.math.HarmonicMean;

/**
 * @author faessler
 * 
 */
public class BTermService implements IBTermService {

	private final Logger logger;
	private final IFacetedSearchService searchService;
	private final ILabelCacheService labelCacheService;

	public BTermService(Logger logger, IFacetedSearchService searchService,
			ILabelCacheService labelCacheService) {
		this.logger = logger;
		this.searchService = searchService;
		this.labelCacheService = labelCacheService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.bterms.interfaces.IBTermService#determineBTermLabelList
	 * (java.util.List)
	 */
	@Override
	public List<Label> determineBTermLabelList(
			List<Multimap<String, IFacetTerm>> searchNodes) {
		if (searchNodes.size() < 2) {
			logger.warn(
					"B-Term computation requires at least two search nodes. Only {} have been passed.",
					searchNodes.size());
			return null;
		}

		List<TripleStream<String, Integer, Integer>> termLists = new ArrayList<TripleStream<String, Integer, Integer>>(
				searchNodes.size());

		for (int i = 0; i < searchNodes.size(); i++) {
			TripleStream<String, Integer, Integer> searchNodeTermsInField = searchService
					.getSearchNodeTermsInField(searchNodes, i,
							IndexFieldNames.BTERMS);
			termLists.add(searchNodeTermsInField);
		}

		List<Label> ret = calculateIntersection(termLists);

		// TODO replace by a general ranking-algorithm, perhaps in a seperate
		// service.
		Collections.sort(ret);

		return ret;
	}

	/**
	 * @param termLists
	 * @return
	 */
	private List<Label> calculateIntersection(
			List<TripleStream<String, Integer, Integer>> termLists) {
		List<Label> ret = new ArrayList<Label>();

		for (int i = 0; i < termLists.size(); i++) {
			TripleStream<String, Integer, Integer> termList = termLists.get(i);
			// Do a first increment to set the streams to their first element.
			if (!termList.incrementTuple()) {
				logger.warn("A list of terms for B-Term computation is empty, probably due to an empty search result. B-Term-List cannot be calculated.");
				return null;
			}
		}
		// Now, the actual Intersection is computed.
		boolean reachedEndOfAList = false;
		HarmonicMean hm = new HarmonicMean();
		TermSetStatistics termSetStats = new TermSetStatistics();
		termSetStats.setNumDocs(searchService.getNumDocs());
		while (!reachedEndOfAList) {
			String potentialBTerm = termLists.get(0).getLeft();
			boolean notEqual = false;
			int leastTermListIndex = 0;
			// Check for two things here. First: Are all current elements equal?
			// Then we have an element of the intersection. Second: Determine
			// the index of the stream with the least element. This stream will
			// be incremented if not all elements were equal.
			for (int i = 1; i < termLists.size(); i++) {
				String term = termLists.get(i).getLeft();
				String leastTerm = termLists.get(leastTermListIndex).getLeft();
				if (!term.equals(potentialBTerm))
					notEqual = true;
				if (term.compareTo(leastTerm) < 0)
					leastTermListIndex = i;
			}
			// No intersection elemenent. Increment the stream with the least
			// element and continue to check again, whether we have now an
			// element for the intersection.
			if (notEqual) {
				if (!termLists.get(leastTermListIndex).incrementTuple())
					reachedEndOfAList = true;
				continue;
			}
			// ...else: We found an intersection element. Combine the statistics
			// of the single elements since in the intersection, there will be
			// only one element.
			Label label = labelCacheService.getCachedLabel(potentialBTerm);
			TermStatistics stats = new TermStatistics();
			stats.setTermSetStats(termSetStats);
			for (int i = 0; i < termLists.size(); i++) {
				// facet count
				double fc = termLists.get(i).getMiddle();
				hm.add(fc);
			}
			stats.setFc(hm.value());
			// The document frequency should be the same for all streams in
			// their current position.
			stats.setDf(termLists.get(0).getRight());
			termSetStats.add(stats);
			hm.reset();
			label.setStats(stats);
			ret.add(label);

			// Set the cursors of all lists to the next element as currently all
			// elements are equal anyway.
			for (int i = 0; i < termLists.size(); i++) {
				if (!termLists.get(i).incrementTuple())
					reachedEndOfAList = true;
			}
		}
		return ret;
	}
}
