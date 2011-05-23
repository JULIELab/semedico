/** 
 * IndexFieldNames.java
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
 * Creation date: 31.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.stemnet;

public class IndexFieldNames {

	public final static String PUBMED_ID = "pubmedID";
	public final static String ABSTRACT = "text";
	public final static String TITLE = "title";
	public final static String TEXT = "text";
	public final static String JOURNAL = "journal";
	public final static String FULLTEXT_LINKS = "fulltext_links";
	public final static String RELATED_ARTICLES = "related_articles";
	public final static String DATE = "date";
	public final static String YEARS = "years";
	public final static String AUTHORS = "authors";
	public final static String PUBLICATION_TYPES = "publication_types";
	public final static String FIRST_AUTHORS = "first_authors";
	public final static String LAST_AUTHORS = "last_authors";
	public final static String MESH = "mesh";
	public final static String LABEL_IDS = "labelIDs";

	public final static String[] BIO_SEARCHABLE_FIELDS = new String[]{TITLE, TEXT, MESH};
	public final static String[] SEARCHABLE_FIELDS = new String[]{TITLE, TEXT, MESH, PUBMED_ID, JOURNAL, YEARS, AUTHORS, PUBLICATION_TYPES};
}
