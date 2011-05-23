/** 
 * QueryPhrase.java
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
 * Creation date: 27.11.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core;

public class QueryPhrase {

	private int beginOffset;
	private int endOffset;

	public QueryPhrase(int beginOffset, int endOffset) {
		super();
		this.beginOffset = beginOffset;
		this.endOffset = endOffset;
	}

	public int getBeginOffset() {
		return beginOffset;
	}

	public void setBeginOffset(int beginOffset) {
		this.beginOffset = beginOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

}
