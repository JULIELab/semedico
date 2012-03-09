/**
 * HighlightedSemedicoDocument.java
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
 * Creation date: 23.02.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.List;

import org.slf4j.Logger;

/**
 * @author faessler
 * 
 */
public class HighlightedSemedicoDocument {

	private SemedicoDocument semedicoDocument;

	private String highlightedTitle;
	private String highlightedAbstract;

	private final Logger logger;

	public HighlightedSemedicoDocument(SemedicoDocument semedicoDocument,
			Logger logger) {
		this.semedicoDocument = semedicoDocument;
		this.logger = logger;
	}

	/**
	 * @return the highlightedTitle
	 */
	public String getHighlightedTitle() {
		if (highlightedTitle != null)
			return highlightedTitle;
		logger.debug(
				"Document with ID \"{}\" does not have title highlights. Returning plain title text.",
				semedicoDocument.getPubmedId());
		return semedicoDocument.getTitle();
	}

	/**
	 * @param highlightedTitle
	 *            the highlightedTitle to set
	 */
	public void setHighlightedTitle(String highlightedTitle) {
		this.highlightedTitle = highlightedTitle;
	}

	/**
	 * @return the highlightedAbstract
	 */
	public String getHighlightedAbstract() {
		if (highlightedAbstract != null)
			return highlightedAbstract;
		logger.debug(
				"Document with ID \"{}\" does not have abstract highlights. Returning plain abstract text.",
				semedicoDocument.getPubmedId());
		return semedicoDocument.getAbstractText();
	}

	/**
	 * @param highlightedAbstract
	 *            the highlightedAbstract to set
	 */
	public void setHighlightedAbstract(String highlightedAbstract) {
		this.highlightedAbstract = highlightedAbstract;
	}

	/**
	 * @return the semedicoDocument
	 */
	public SemedicoDocument getSemedicoDocument() {
		return semedicoDocument;
	}

	/**
	 * @return
	 */
	public Integer getPubmedId() {
		return semedicoDocument.getPubmedId();
	}

	/**
	 * @return
	 */
	public List<Author> getAuthors() {
		return semedicoDocument.getAuthors();
	}

	public Publication getPublication() {
		return semedicoDocument.getPublication();
	}
}
