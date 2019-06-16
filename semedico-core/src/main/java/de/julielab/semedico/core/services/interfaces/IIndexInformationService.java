/** 
 * IIndexInformationService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: 2.2.2 	
 * Since version:   1.0
 *
 * Creation date: 31.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services.interfaces;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @deprecated The index structure is known to the document modules now
 */
public interface IIndexInformationService {
	public static class Indexes {
		public static class SuggestionTypes {
			public static final String ITEM = "item";
		}

		public static class DocumentTypes {
			public static final String MEDLINE = "medline";
			public static final String PMC = "pmc";
		}

		public static final String DOCUMENTS = "documents";
		public static final String DOCUMENTSPMC = DOCUMENTS + "." + DocumentTypes.PMC;
		public static final String DOCUMENTSMEDLINE = DOCUMENTS + "." + DocumentTypes.MEDLINE;
		public static final String SUGGESTIONS = "suggestions";
	}

	public static class PmcIndexStructure extends GeneralIndexStructure {
		public static final String SECTIONS = "sections";
		public static final String PARAGRAPHS = "paragraphs";
		public static final String FIGURECAPTIONS = "figurecaptions";
		public static final String TABLECAPTIONS = "tablecaptions";
		public static final String ZONES = "zones";

		public static class Nested extends GeneralIndexStructure.Nested {
			public static final String SECTIONSTEXT = "sections." + text;
			public static final String PARAGRAPHSTEXT = "paragraphs." + text;
			public static final String FIGURECAPTIONSTEXT = "figurecaptions." + text;
			public static final String TABLECAPTIONSTEXT = "tablecaptions." + text;
			public static final String ZONESTEXT = "zones." + text;
			public static final String SECTIONSTITLE = SECTIONS + "." + "title";
			public static final String SECTIONSTITLELIKELIHOOD = SECTIONS + ".titlelikelihood";
		}
	}

	public static class MedlineIndexStructure extends GeneralIndexStructure {
		public static final String MESHMINOR = "meshminor";
		public static final String MESHMAJOR = "meshmajor";
		public static final String SUBSTANCES = "substances";
		public static final String journal = "journal";
		public static final String AFFILIATION = "affiliation";
	}

	public static class GeneralIndexStructure {
		/**
		 * Nested document text passages store and index their text contents in
		 * a field with this name.
		 */
		public static final String text = "text";
		public static final String pmid = "pmid";
		public static final String pmcid = "pmcid";
		public static final String title = "title";
		public static final String abstracttext = "abstracttext";
		public static final String abstractsections = "abstractsections";
		public static final String alltext = "alltext";
		public static final String sentences = "sentences";
		public static final String docmeta = "docmeta";
		public static final String mesh = "mesh";
		public static final String meshminor = "meshminor";
		public static final String meshmajor = "meshmajor";
		public static final String substances = "substances";
		public static final String keywords = "keywords";
		public static final String events = "events";
		public static final String authors = "authors";
		public static final String affiliation = "affiliation";
		public static final String conceptlist = "conceptlist";
		public static final String date = "date";
		public static final String journal = "journal";
		public static final String journaltitle = "journal.title";
		public static final String journalvolume = "journal.volume";
		public static final String journalissue = "journal.issue";
		public static final String journalpages = "journal.pages";
		public static final String pubtype = "pubtype";
		public static final String _score = "_score";

		public static class Nested {
			public static final String abstractsectionstext = "abstractsections." + text;
			public static final String sentencestext = "sentences." + text;
			public static final String sentenceslikelihood = "sentences." + "likelihood";
		}

	}

	@Deprecated
	public static final String FACETS = "facetCategories";

	public static final List<String> FIELDS = Arrays.asList("pmid", "pmcid",
			"title","abstract", "abstracttext", "all", "alltext", "mesh",
			"meshminor", "meshmajor", "substance", "substances", "sentence",
			"sentences", "docmeta", "meta", "keyword", "keywords", "author",
			"authors", "journal", "affiliation", "concept", "concepts", "date");
	
	public static final List<String> MEDLINE_SEARCH_FIELDS = Arrays.asList(
		GeneralIndexStructure.abstractsections,
		GeneralIndexStructure.abstracttext,
		GeneralIndexStructure.alltext,
		GeneralIndexStructure.events,
		GeneralIndexStructure.docmeta,
		GeneralIndexStructure.pmcid,
		GeneralIndexStructure.pmid,
		GeneralIndexStructure.sentences,
		GeneralIndexStructure.title
		);
	public static final List<String> PMC_SEARCH_FIELDS = Arrays.asList(
		GeneralIndexStructure.abstractsections,
		GeneralIndexStructure.abstracttext,
		GeneralIndexStructure.alltext,
		GeneralIndexStructure.events,
		GeneralIndexStructure.docmeta,
		GeneralIndexStructure.pmcid,
		GeneralIndexStructure.pmid,
		GeneralIndexStructure.sentences,
		GeneralIndexStructure.title,
		PmcIndexStructure.PARAGRAPHS,
		PmcIndexStructure.SECTIONS,
		PmcIndexStructure.TABLECAPTIONS,
		PmcIndexStructure.ZONES
		);
	public static final String sentences = GeneralIndexStructure.sentences;

	public static final Set<String> conceptFields = Sets.newHashSet(
		GeneralIndexStructure.mesh,
		GeneralIndexStructure.conceptlist);
	public static final Set<String> noConceptFields = Sets.newHashSet(
		GeneralIndexStructure.docmeta,
		GeneralIndexStructure.authors,
		GeneralIndexStructure.journal,
		GeneralIndexStructure.journalissue,
		GeneralIndexStructure.journalpages,
		GeneralIndexStructure.journalvolume,
		GeneralIndexStructure.journaltitle,
		GeneralIndexStructure.pmcid,
		GeneralIndexStructure.pmid,
		GeneralIndexStructure.keywords,
		GeneralIndexStructure.affiliation,
		GeneralIndexStructure.date,
		GeneralIndexStructure.pubtype);
}