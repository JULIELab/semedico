/**
 * StringTermService.java
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
 * Creation date: 11.04.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.services;

import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

/**
 * @author faessler
 * 
 */
public class StringTermService implements IStringTermService {

	private final ITermService termService;
	private final Matcher suffixMatcher;
	private final Matcher wsReplacementMatcher;
	private final Matcher wsMatcher;
	// Because the matchers have to be reset to the concrete string they are
	// supposed to work on, we must
	// synchronize their access. Since there are multiple methods using the
	// matchers, a simple "synchronized" keyword won't do it.
	private final ReentrantLock matcherLock;
	private final IFacetService facetService;

	public StringTermService(ITermService termService,
			IFacetService facetService) {
		this.termService = termService;
		this.facetService = facetService;
		suffixMatcher = Pattern.compile(SUFFIX + "([0-9]+)$").matcher("");
		wsReplacementMatcher = Pattern.compile(WS_REPLACE).matcher("");
		wsMatcher = Pattern.compile("\\s").matcher("");
		matcherLock = new ReentrantLock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#getStringTermId
	 * (java.lang.String, de.julielab.semedico.core.Facet)
	 */
	@Override
	public String getStringTermId(String stringTerm, Facet facet)
			throws IllegalStateException {
		String termId = stringTerm;
		matcherLock.lock();
		termId = wsMatcher.reset(stringTerm).replaceAll(WS_REPLACE);
		matcherLock.unlock();
		termId = termId + SUFFIX + facet.getId();
		return termId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#checkStringTermId
	 * (java.lang.String, de.julielab.semedico.core.Facet)
	 */
	@Override
	public String checkStringTermId(String stringTerm, Facet facet) {
		matcherLock.lock();
		if (wsReplacementMatcher.reset(stringTerm).find())
			throw new IllegalStateException("String term '" + stringTerm
					+ "' contains reserved character '" + WS_REPLACE + "'.");
		matcherLock.unlock();
		String id = getStringTermId(stringTerm, facet);
		if (termService.hasNode(id))
			throw new IllegalStateException(
					" The string term "
							+ stringTerm
							+ ", denoting an author, with ID '"
							+ id
							+ "' should be generated. However, there already is a term with that ID known to the term service.");
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getOriginalStringTermAndFacetId(java.lang.String)
	 */
	@Override
	public Pair<String, Integer> getOriginalStringTermAndFacetId(
			String stringTermId) throws IllegalArgumentException {
		matcherLock.lock();

		suffixMatcher.reset(stringTermId);
		if (!suffixMatcher.find())
			throw new IllegalArgumentException(
					"The given string term ID does not end with pattern '"
							+ suffixMatcher.pattern().pattern()
							+ "' with which all string term IDs must be suffixed.");
		// First, extract the facet ID.
		String facetIdString = suffixMatcher.group(1);
		Integer facetId = Integer.parseInt(facetIdString);

		// Now re-create the original string term. First, cut the suffix.
		String stringTerm = suffixMatcher.replaceAll("");
		// Then, get the white spaces back (if there were any).
		stringTerm = wsReplacementMatcher.reset(stringTerm).replaceAll(" ");

		ImmutablePair<String, Integer> pair = new ImmutablePair<String, Integer>(
				stringTerm, facetId);

		matcherLock.unlock();
		return pair;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getTermObjectForStringTermId(java.lang.String)
	 */
	@Override
	public IFacetTerm getTermObjectForStringTermId(String stringTermId) {
		Pair<String, Integer> originalStringTermAndFacetId = getOriginalStringTermAndFacetId(stringTermId);
		Facet facet = facetService.getFacetById(originalStringTermAndFacetId
				.getRight());
		FacetTerm term = new FacetTerm(stringTermId,
				originalStringTermAndFacetId.getLeft());
		term.addFacet(facet);
		term.setIndexNames(facet.getSearchFieldNames());
		return term;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getTermObjectForStringTerm(java.lang.String,
	 * de.julielab.semedico.core.Facet)
	 */
	@Override
	public IFacetTerm getTermObjectForStringTerm(String stringTerm, Facet facet) {
		String stringTermId = getStringTermId(stringTerm, facet);
		FacetTerm term = new FacetTerm(stringTermId, stringTerm);
		term.addFacet(facet);
		term.setIndexNames(facet.getFilterFieldNames());
		return term;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#isStringTermID(
	 * java.lang.String)
	 */
	@Override
	public boolean isStringTermID(String string) {
		matcherLock.lock();
		suffixMatcher.reset(string);
		boolean suffixFound = suffixMatcher.find();
		boolean noWhiteSpaces = !wsMatcher.reset(string).find();
		matcherLock.unlock();
		return suffixFound && noWhiteSpaces;
	}

}
