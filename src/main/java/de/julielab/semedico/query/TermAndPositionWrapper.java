/**
 * TermAndPositionWrapper.java
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
 * Creation date: 17.08.2012
 **/

/**
 * 
 */
package de.julielab.semedico.query;

import de.julielab.semedico.core.QueryToken;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * Wrapper to store position of the found terms, used for combining Symbols
 * 
 * @author hellrich
 * 
 */
public class TermAndPositionWrapper {
	private int begin;
	private int end;
	private IFacetTerm term;

	/**
	 * Wrapper to store position of the found terms, used for combining
	 * Symbols
	 * 
	 * @param queryToken
	 *            QueryToken to extract begin/end/term from
	 */
	public TermAndPositionWrapper(QueryToken queryToken) {
		this(queryToken.getTerm(), queryToken.getBeginOffset(), queryToken
				.getEndOffset());
	}

	/**
	 * Wrapper to store position of the found terms, used for combining
	 * Symbols
	 * 
	 * @param term
	 *            wrapped term
	 * @param begin
	 *            begin offset of original string
	 * @param end
	 *            end offset of original string
	 * 
	 */
	public TermAndPositionWrapper(IFacetTerm term, int begin, int end) {
		this.term = term;
		this.begin = begin;
		this.end = end;
	}

	/**
	 * @return the begin
	 */
	public int getBegin() {
		return begin;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * @return the term
	 */
	public IFacetTerm getTerm() {
		return term;
	}

	/**
	 * @param term
	 *            Value for the term
	 */
	public void setTerm(IFacetTerm term) {
		this.term = term;
	}

}
