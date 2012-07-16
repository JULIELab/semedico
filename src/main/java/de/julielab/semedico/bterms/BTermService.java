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

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.StringLabel;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

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

		List<List<Count>> termLists = new ArrayList<List<Count>>(
				searchNodes.size());

		for (int i = 0; i < searchNodes.size(); i++) {
			List<Count> searchNodeTermsInField = searchService
					.getSearchNodeTermsInField(searchNodes, i,
							IndexFieldNames.ABSTRACT);
			termLists.add(searchNodeTermsInField);
		}

		List<Label> ret = calculateIntersection(termLists);
		
		// TODO replace by a general ranking-algorithm, perhaps in a seperate service.
		Collections.sort(ret);

		return ret;
	}

	/**
	 * @param termLists
	 * @return
	 */
	private List<Label> calculateIntersection(List<List<Count>> termLists) {
		List<Label> ret = new ArrayList<Label>();

		int[] currentTermIndices = new int[termLists.size()];
		for (int i = 0; i < termLists.size(); i++) {
			List<Count> termList = termLists.get(i);
			if (termList.size() == 0) {
				logger.warn("A list of terms for B-Term computation is empty, probably due to an empty search result. B-Term-List cannot be calculated.");
				return null;
			}
			currentTermIndices[i] = 0;
		}
		boolean reachedEndOfAList = false;
		while (!reachedEndOfAList) {
			String potentialBTerm = termLists.get(0).get(currentTermIndices[0])
					.getName();
			boolean notEqual = false;
			int leastTermListIndex = 0;
			for (int i = 1; i < currentTermIndices.length; i++) {
				String term = termLists.get(i).get(currentTermIndices[i])
						.getName();
				String leastTerm = termLists.get(leastTermListIndex)
						.get(currentTermIndices[leastTermListIndex]).getName();
				if (!term.equals(potentialBTerm))
					notEqual = true;
				if (term.compareTo(leastTerm) < 0)
					leastTermListIndex = i;
			}
			if (notEqual) {
				currentTermIndices[leastTermListIndex]++;
				if (currentTermIndices[leastTermListIndex] >= termLists.get(
						leastTermListIndex).size())
					reachedEndOfAList = true;
				continue;
			}
			// ...else:
			StringLabel label = labelCacheService
					.getCachedStringLabel(potentialBTerm);
			long countSum = 0;
			for (int i = 0; i < currentTermIndices.length; i++)
				countSum += termLists.get(i).get(currentTermIndices[i])
						.getCount();
			label.setCount(countSum);
			ret.add(label);

			// Set the cursors of all lists to the next element as currently all
			// elements are equal anyway.
			for (int i = 0; i < currentTermIndices.length; i++) {
				currentTermIndices[i]++;
				if (currentTermIndices[i] >= termLists.get(i).size())
					reachedEndOfAList = true;
			}
		}
		return ret;
	}

}
