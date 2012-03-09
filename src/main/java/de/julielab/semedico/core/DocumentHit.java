/** 
 * DocumentHit.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core;

public class DocumentHit {

	private SemedicoDocument document;
	private String kwicTitle;
	private String[] kwics;

	public DocumentHit(SemedicoDocument document) {
		this.document = document;
	}

	public SemedicoDocument getDocument() {
		return document;
	}

	public void setDocument(SemedicoDocument document) {
		this.document = document;
	}

	public String getKwicTitle() {
		if (kwicTitle != null && kwicTitle.length() > 0)
			return kwicTitle;
		return document.getTitle();
	}

	public void setKwicTitle(String kwicTitle) {
		this.kwicTitle = kwicTitle;
	}

	public String getKwicAbstractText() {
		String abstractText = document.getAbstractText();
		if (abstractText != null && abstractText.length() > 250)
			return abstractText.substring(0, 250) + "...";
		return abstractText;
	}

	public String[] getKwics() {
		return kwics;
	}

	public void setKwics(String[] kwics) {
		this.kwics = kwics;
	}

}
