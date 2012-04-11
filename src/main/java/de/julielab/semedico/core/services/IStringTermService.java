/**
 * IStringTermService.java
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

import org.apache.commons.lang3.tuple.Pair;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

/**
 * <p>
 * This interface is for services which can manage all things special to
 * <em>string terms</em>.
 * </p>
 * <p>
 * A string term is a term we don't know more about as a particular string
 * representation. An example would be author names. Taken from MEDLINE, these
 * are just plain strings. We don't know synonyms and we don't even know whether
 * two equal strings denote the same real-world person. Furthermore, there are
 * nearly as much author names as there are documents, which makes holding all
 * of them in memory - like the other terms - both clumsy and unnecessary.
 * Unnecessary because all names will be stored in the search index anyway -
 * where for normal terms, we only have IDs stored in the index as a
 * normalization to synonyms and variants.
 * </p>
 * <p>
 * The current list of string term types are:
 * <ul>
 * <li>Authors</li>
 * <li>Journals (subject to change; Journals have an ISSN, for example, and
 * sometimes multiple names)</li>
 * <li>Years</li>
 * </ul>
 * </p>
 * 
 * @author faessler
 * 
 */
public interface IStringTermService {

	public static final String WS_REPLACE = "#";
	public static final String SUFFIX = "__FACET_ID:";

	/**
	 * <p>
	 * Creates a unique term ID for a string term <code>stringTerm</code> - e.g.
	 * author or journal names, years etc. - in facet <code>facet</code>.
	 * </p>
	 * 
	 * @param stringTerm
	 *            The string term to get an ID for.
	 * @return A unique ID for <code>stringTerm</code>.
	 * @throws IllegalStateException
	 *             In case the ID generation algorithm did produce an existing
	 *             ID by coincidence. In this case the algorithm has to be
	 *             changed (to use different special characters, for example).
	 */
	public String getStringTermId(String stringTerm, Facet facet)
			throws IllegalStateException;

	/**
	 * <p>
	 * Checks whether a generated ID for a string term is invalid for any
	 * reason.
	 * </p>
	 * <p>
	 * Note that the <code>String</code> <code>stringTerm</code> must represent
	 * the original string term, not an already generated. It will be tested
	 * whether a unambiguous and unique ID for this string term can be
	 * generated. If this is the case, <code>null</code> will be returned.
	 * Otherwise, a <code>String</code> containing an error message will be
	 * returned.
	 * </p>
	 * 
	 * @param stringTerm
	 *            The original string term to check its ID-generation.
	 * @param facet
	 *            The facet <code>stringTerm</code> belongs to.
	 * @return <code>null</code> if there is no problem with the ID generation
	 *         of this term, a <code>String</code> containing an err
	 */
	public String checkStringTermId(String stringTerm, Facet facet);

	/**
	 * <p>
	 * Given an ID string produced by {@link #getStringTermId(String, Facet)},
	 * returns the original term string and the facet ID the term is associated
	 * with.
	 * </p>
	 * 
	 * @param stringTermId
	 *            String term ID to reconstruct the original string term
	 *            (possibly with normalized white spaces) from..
	 * @return The reconstructed original term string, e.g. full author name and
	 *         the associated facet ID.
	 * @throws IllegalArgumentException
	 *             If the passed string does not represent a string term ID as
	 *             constructed by {@link #getStringTermId(String, Facet)}.
	 *             {@code #getStringTermId(String, Facet)}
	 */
	public Pair<String, Integer> getOriginalStringTermAndFacetId(
			String stringTermId) throws IllegalArgumentException;

	/**
	 * <p>
	 * Creates an <code>IFacetTerm</code> instance for <code>stringTerm</code>
	 * which can be used internally.
	 * </p>
	 * <p>
	 * For this method, <code>stringTerm</code> should be the original term
	 * string, e.g. full author name. An ID will be created with respect to the
	 * given facet ID.
	 * </p>
	 * 
	 * @param stringTerm
	 *            The string term to create an <code>IFacetTerm</code> for.
	 * @return An <code>IFacetTerm</code> representing <code>stringTerm</code>.
	 */
	public IFacetTerm getTermObjectForStringTerm(String stringTerm, int facetId);

	/**
	 * <p>
	 * Determines whether an input string represents an ID for a string term as
	 * constructed by {@link #getStringTermId(String, Facet)}.
	 * </p>
	 * 
	 * @param string
	 *            The String for which should be determined whether it
	 *            represents a string term ID or not.
	 * @return <code>true</code> if <code>string</code> represents a string term
	 *         ID, <code>false</code> otherwise.
	 */
	public boolean isStringTermID(String string);
}
