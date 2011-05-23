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
import org.apache.lucene.index.IndexReader;

public class IndexReaderWrapper implements IIndexReaderWrapper {

	private IndexReader indexReader;
	
	public IndexReaderWrapper(IndexReader indexSearcher) {
		super();
		this.indexReader = indexSearcher;
	}

	@Override
	public IndexReader getIndexReader() {
		return indexReader;
	}

}
