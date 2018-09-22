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

package de.julielab.semedico.core.services.interfaces;

import java.util.Set;

import com.google.common.collect.Sets;

public interface IIndexInformationService {
	
	public static class Indices {

		public static class All {
			public static final String scope = "scope";
			public static String conceptlist = "conceptlist";
		}
		
		public static class Documents {
			public static final String name = "documents";
			public static final String documenttext = "documenttext";
			public static final String title = "title";
			public static final String titletext = title + ".text";
			public static final String titlebegin = title + ".begin";
			public static final String titleend = title + ".end";
			public static final String titlelikelihood  = title + ".likelihood";
			public static final String abstracttext = "abstracttext";
			public static final String abstracttexttext = abstracttext + ".text";
			public static final String abstracttextbegin = abstracttext + ".begin";
			public static final String abstracttextend = abstracttext + ".end";
			
			public static final String mesh = "mesh";
			public static final String conceptlist = "conceptlist";
			
			public static final String docmeta = "docmeta";
			public static final String authors = "authors";
			public static String affiliation = "affiliation";

		}
		public abstract static class DocumentSpan {
			public static final String text = "text";
			public static final String begin = "begin";
			public static final String end = "end";
		}

		public abstract static class DocumentEMSpan extends DocumentSpan {
			public static final String likelihood = "likelihood";
		}
		
		public static class Sentences extends DocumentEMSpan {
			public static final String name = "sentences";
		}
		
		public static class Relations extends DocumentEMSpan {
			public static final String name = "relations";
			public static final String arguments = "arguments";
			public static final String argumentwords = "argumentwords";
			public static final String types = "types";
			public static final String sentence = "sentence";
			public static final String sentencebegin = "sentencebegin";
			public static final String sentenceend = "sentenceend";
			public static final String numarguments = "numarguments";
			public static final String numdistinctarguments = "numdistinctarguments";
			public static final String source = "facetSource";
		}
		public static class Chunks extends DocumentSpan {
			public static String name = "chunks";
		}
		
		public static class AbstractSections extends DocumentEMSpan {
			public static final String name = "abstractsections";
			public static final String nlmcategory = "nlmcategory";
			public static final String label = "label";
		}

		public static class SuggestionTypes {
			public static final String item = "item";
		}

	}

	public static final Set<String> conceptFields = Sets.newHashSet(Indices.Documents.mesh,
			Indices.Documents.conceptlist);
	public static final Set<String> noConceptFields = Sets.newHashSet(Indices.Documents.docmeta,
			Indices.Documents.authors
			// TODO not yet added into the mapping or into the Indexes.Documents class
//			,GeneralIndexStructure.journal, GeneralIndexStructure.journalissue,
//			GeneralIndexStructure.journalpages, GeneralIndexStructure.journalvolume, GeneralIndexStructure.journaltitle,
//			GeneralIndexStructure.pmcid, GeneralIndexStructure.pmid, GeneralIndexStructure.keywords,
//			GeneralIndexStructure.affiliation, GeneralIndexStructure.date, GeneralIndexStructure.pubtype
			);
}