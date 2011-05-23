/** 
 * IndexSearcherWrapper.java
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
 * Creation date: 05.08.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.lucene;
import org.apache.lucene.search.IndexSearcher;

public class IndexSearcherWrapper implements IIndexSearcherWrapper {

	private IndexSearcher indexSearcher;
	
	public IndexSearcherWrapper(IndexSearcher indexSearcher) {
		super();
		this.indexSearcher = indexSearcher;
	}

	@Override
	public IndexSearcher getIndexSearcher() {
		return indexSearcher;
	}

}
