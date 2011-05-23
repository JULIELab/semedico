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
	private String kwicAbstractText;
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
		return kwicTitle;
	}
	public void setKwicTitle(String kwicTitle) {
		this.kwicTitle = kwicTitle;
	}
	public String getKwicAbstractText() {
		return kwicAbstractText;
	}
	public void setKwicAbstractText(String kwicAbstractText) {
		this.kwicAbstractText = kwicAbstractText;
	}
	public String[] getKwics() {
		return kwics;
	}
	public void setKwics(String[] kwics) {
		this.kwics = kwics;
	}
	
}
