/**
 * BBatchAnalysis.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 04.02.2013
 **/

/**
 * 
 */
package de.julielab.semedico.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.exceptions.EmptySearchComplementException;
import de.julielab.semedico.core.exceptions.TooFewSearchNodesException;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.domain.BBatchTermPair;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.TermAndPositionWrapper;
import de.julielab.util.DisplayGroup;
import de.julielab.util.LabelFilter;

/**
 * @author faessler
 * 
 */
public class BBatchAnalysis {

	@Persist
	@Property
	private Stack<BBatchTermPair> termPairs;

	@Property
	private BBatchTermPair loopItemPair;

	@Property
	private int termPairIndex;

	@Persist
	@Property
	private int numPairs;

	@Property
	@Persist
	private int numTopTerms;

	@InjectComponent
	private Form termform;

	@InjectComponent
	private Zone termPairsZone;

	@Inject
	private IQueryDisambiguationService queryDisambiguationService;

	@Inject
	private IBTermService btermService;

	@Inject
	Logger log;

	@Property
	@Persist
	private DisplayGroup<Label> topBTermLabels;

	@Property
	@Persist
	private Stack<Integer> bTermFindingNumbers;

	@Persist
	private Stack<List<Label>> bTermLists;

	@Property
	private boolean filterWords;

	public void setupRender() {
		if (null == termPairs) {
			termPairs = new Stack<BBatchTermPair>();
			numPairs = 2;
		}
		if (0 == numTopTerms)
			numTopTerms = 20;
		if (null == bTermFindingNumbers)
			bTermFindingNumbers = new Stack<Integer>();
		if (null == bTermLists)
			bTermLists = new Stack<List<Label>>();
		adjustTermPairs();
	}

	public void onPrepareForRender() {
	}

	public void onValidateFromTermform() {
		for (BBatchTermPair b : termPairs) {
			if (null == b.term1 || null == b.term2) {
				termform.recordError("Please specify both terms for each term pair.");
				return;
			}
		}
		List<List<Multimap<String, IFacetTerm>>> parsedTerms = new ArrayList<List<Multimap<String, IFacetTerm>>>();
		int i = 1;
		for (BBatchTermPair b : termPairs) {
			Multimap<String, TermAndPositionWrapper> parsed1 = queryDisambiguationService
					.disambiguateQuery(b.term1, null);
			Multimap<String, IFacetTerm> parsed1Old = getOldQueryStructure(parsed1);
			log.debug("Term {},1: {}", i, getQueryString(parsed1Old));
			Multimap<String, TermAndPositionWrapper> parsed2 = queryDisambiguationService
					.disambiguateQuery(b.term2, null);
			Multimap<String, IFacetTerm> parsed2Old = getOldQueryStructure(parsed2);
			log.debug("Term {},2: {}", i, getQueryString(parsed2Old));
			parsedTerms.add(Lists.newArrayList(parsed1Old, parsed2Old));
		}

		i = 1;
		bTermFindingNumbers.clear();
		topBTermLabels = new DisplayGroup<Label>(new LabelFilter(), numPairs * numTopTerms);
		for (List<Multimap<String, IFacetTerm>> pair : parsedTerms) {
			try {
				List<Label> bTermLabelList = btermService
						.determineBTermLabelList(pair);
				bTermLists.add(bTermLabelList);
				bTermFindingNumbers.add(bTermLabelList.size());
				log.debug("Retrieved {} b-terms", bTermLabelList.size());
				getTopTerms(bTermLabelList);
			} catch (TooFewSearchNodesException e) {
				e.printStackTrace();
			} catch (EmptySearchComplementException e) {
				e.printStackTrace();
				termform.recordError("Term pair "
						+ i
						+ " has an empty result set complement, i.e. one found document set subsumes the other.");
			}
			i++;
		}
		log.debug("obtained {} top-b-terms", topBTermLabels.size());
	}

	/**
	 * Fills {@link #topBTermLabels} with all labels which are in the top-
	 * {@link #numTopTerms} after filtering is applied.
	 * 
	 * @param bTermLabelList
	 */
	private void getTopTerms(List<Label> bTermLabelList) {
		for (int j = 0; j < numTopTerms && j < bTermLabelList.size(); j++) {
			Label label = bTermLabelList.get(j);
			if (filterWords) {
				if (!(label instanceof TermLabel)) {
					j--;
					continue;
				}
			}
			topBTermLabels.add(label);
		}
	}

	public void onSuccess() {

	}

	private Multimap<String, IFacetTerm> getOldQueryStructure(
			Multimap<String, TermAndPositionWrapper> result) {
		// TODO this is for legacy reasons until the new query structure can be
		// used in the whole of Semedico.
		Multimap<String, IFacetTerm> disambiguatedQuery = HashMultimap.create();
		for (String key : result.keySet()) {
			Collection<TermAndPositionWrapper> collection = result.get(key);
			for (TermAndPositionWrapper wrapper : collection) {
				IFacetTerm term = wrapper.getTerm();
				disambiguatedQuery.put(key, term);
				// searchState.getQueryTermFacetMap().put(term,
				// term.getFirstFacet());
			}
		}
		return disambiguatedQuery;
	}

	private String getQueryString(Multimap<String, IFacetTerm> parsedQuery) {
		String ret = "\n";
		for (String key : parsedQuery.keySet()) {
			Collection<IFacetTerm> values = parsedQuery.get(key);
			ret += key + " --> ";
			for (IFacetTerm t : values)
				ret += t.getName() + " ";
			ret += "\n";
		}
		return ret;
	}

	public Object onValueChanged(int value) {
		numPairs = value;
		adjustTermPairs();
		return termPairsZone.getBody();
	}

	/**
	 * 
	 */
	private void adjustTermPairs() {
		if (numPairs == termPairs.size())
			return;
		while (numPairs > termPairs.size()) {
			termPairs.add(new BBatchTermPair());
			bTermFindingNumbers.add(0);
		}
		while (numPairs < termPairs.size()) {
			termPairs.pop();
			bTermFindingNumbers.pop();
		}
	}

	public FieldTranslator<ArrayList> getFieldTranslator() {
		FieldTranslator<ArrayList> ft = new FieldTranslator<ArrayList>() {

			@Override
			public Class<ArrayList> getType() {
				return ArrayList.class;
			}

			@Override
			public ArrayList<Label> parse(String input)
					throws ValidationException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String toClient(ArrayList value) {
				StringBuilder sb = new StringBuilder();
				for (Object o : value) {
					sb.append(((Label) o).getName());
					sb.append("\n");
				}
				return sb.toString();
			}

			@Override
			public void render(MarkupWriter writer) {
				// TODO Auto-generated method stub

			}

		};
		return ft;
	}

	public int getBTermNumber() {
		return bTermFindingNumbers.get(termPairIndex);
	}

}
