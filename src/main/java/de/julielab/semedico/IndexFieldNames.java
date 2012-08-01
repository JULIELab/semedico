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

package de.julielab.semedico;

public class IndexFieldNames {

	public final static String PUBMED_ID = "pubmedID";
	public final static String ABSTRACT = "text";
	public final static String TITLE = "title";
	public final static String TEXT = "text";
	public final static String JOURNAL = "journal";
	public final static String DATE = "date";
	public final static String YEAR = "year";
	public final static String AUTHORS = "authors";
	public final static String PUBLICATION_TYPES = "publication_types";
	public final static String FIRST_AUTHORS = "first_authors";
	public final static String LAST_AUTHORS = "last_authors";
	public final static String MESH = "mesh";
	
	public final static String FACETS = "facetCategories";
	public final static String FACET_TERMS = "facetTerms";
	public final static String FACET_AUTHORS = "facetAuthors";
	public final static String FACET_FIRST_AUTHORS = "facetFirstAuthors";
	public final static String FACET_LAST_AUTHORS = "facetLastAuthors";
	public final static String FACET_PUBTYPES = "facetPubTypes";
	public final static String FACET_YEARS = "facetYears";
	public final static String FACET_JOURNALS = "facetJournals";
	
	public final static String FILTER_DOCUMENT_CLASSES = "documentClasses";
	
	public final static String BTERMS = "bterms";
	

	public final static String[] BIO_SEARCHABLE_FIELDS = new String[]{TITLE, TEXT, MESH};
	public final static String[] SEARCHABLE_FIELDS = new String[]{TITLE, TEXT, MESH, PUBMED_ID, JOURNAL, YEAR, AUTHORS, PUBLICATION_TYPES};
}
