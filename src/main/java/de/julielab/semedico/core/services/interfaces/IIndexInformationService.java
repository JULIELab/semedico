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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

public interface IIndexInformationService
{
	public static class Indexes
	{
		public static class SuggestionTypes
		{
			public static final String item = "item";
		}

		public static class DocumentTypes
		{
			public static final String medline = "medline";
			public static final String pmc = "pmc";
			public static final String paragraphs = "paragraphs";
			public static final String sections = "sections";
			public static final String abstractsections = "abstractsections";
			public static final String figurecaptions = "figurecaptions";
			public static final String tablecaptions = "tablecaptions";
			public static final String sentences = "sentences";
			public static final String statements = "statements";
		}
	}

	@Deprecated
	public static class PmcIndexStructure extends GeneralIndexStructure
	{
		public static final String sections = "sections";
		public static final String paragraphs = "paragraphs";
		public static final String figurecaptions = "figurecaptions";
		public static final String tablecaptions = "tablecaptions";
		public static final String zones = "zones";

		public static class Nested extends GeneralIndexStructure.Nested
		{
			public static final String sectionstext = "sections." + text;
			public static final String paragraphstext = "paragraphs." + text;
			public static final String figurecaptionstext = "figurecaptions." + text;
			public static final String tablecaptionstext = "tablecaptions." + text;
			public static final String zonestext = "zones." + text;
			public static final String sectionstitle = sections + "." + "title";
			public static final String sectiontitlelikelihood = sections + ".titlelikelihood";
		}
	}

	public static class MedlineIndexStructure extends GeneralIndexStructure
	{
		public static final String meshminor = "meshminor";
		public static final String meshmajor = "meshmajor";
		public static final String substances = "substances";
		public static final String journal = "journal";
		public static final String affiliation = "affiliation";
	}

	public static class GeneralIndexStructure
	{
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

		public static class Nested
		{
			public static final String abstractsectionstext = "abstractsections." + text;
			public static final String sentencestext = "sentences." + text;
			public static final String sentenceslikelihood = "sentences." + "likelihood";
		}

		public static class EventFields
		{
			public final static String agent = GeneralIndexStructure.events + ".agent";
			public final static String patient = GeneralIndexStructure.events + ".patient";
			public final static String allarguments = GeneralIndexStructure.events + ".allarguments";
			public final static String alleventtypes = GeneralIndexStructure.events + ".alleventtypes";
			public final static String maineventtype = GeneralIndexStructure.events + ".maineventtype";
			public final static String allargumentsandtypes = GeneralIndexStructure.events + ".allargumentsandtypes";
			public final static String likelihood = GeneralIndexStructure.events + ".likelihood";
			public final static String sentence = GeneralIndexStructure.events + ".sentence";
		}

	}

	public static final String pmcid = GeneralIndexStructure.pmcid;
	public final static String PUBMED_ID = GeneralIndexStructure.pmid;
	public final static String ABSTRACT = GeneralIndexStructure.abstracttext;
	public final static String TITLE = GeneralIndexStructure.title;
	@Deprecated
	public final static String TEXT = "text";
	public final static String JOURNAL = GeneralIndexStructure.journal;
	public final static String DATE = GeneralIndexStructure.date;
	@Deprecated
	public final static String YEAR = "year";
	@Deprecated
	public final static String PUBLICATION_TYPES = "publication_types";
	public final static String AUTHORS = GeneralIndexStructure.authors;
	@Deprecated
	public final static String FIRST_AUTHORS = "firstAuthors";
	@Deprecated
	public final static String LAST_AUTHORS = "lastAuthors";
	public final static String MESH = GeneralIndexStructure.mesh;

	@Deprecated
	public final static String FACETS = "facetCategories";
	@Deprecated
	public final static String FACET_TERMS = "facetTerms";
	@Deprecated
	public final static String FACET_AUTHORS = "facetAuthors";
	@Deprecated
	public final static String FACET_FIRST_AUTHORS = "facetFirstAuthors";
	@Deprecated
	public final static String FACET_LAST_AUTHORS = "facetLastAuthors";
	@Deprecated
	public final static String FACET_PUBTYPES = "facetPubTypes";
	@Deprecated
	public final static String FACET_YEARS = "facetYears";
	@Deprecated
	public final static String FACET_JOURNALS = "facetJournals";
	@Deprecated
	public static final String FACET_EVENTS = "facetEvents";
	@Deprecated
	public final static String FILTER_DOCUMENT_CLASSES = "documentClasses";
	@Deprecated
	public final static String BTERMS = "facetTermsNoHypernymsAggregates";

	public static final String events = GeneralIndexStructure.events;
	@Deprecated
	public final static String[] BIO_SEARCHABLE_FIELDS = new String[] { TITLE, TEXT, MESH };
	public final static List<String> MEDLINE_SEARCH_FIELDS = Arrays.asList(
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
	public final static List<String> PMC_SEARCH_FIELDS = Arrays.asList(
		GeneralIndexStructure.abstractsections,
		GeneralIndexStructure.abstracttext,
		GeneralIndexStructure.alltext,
		GeneralIndexStructure.events,
		GeneralIndexStructure.docmeta,
		GeneralIndexStructure.pmcid,
		GeneralIndexStructure.pmid,
		GeneralIndexStructure.sentences,
		GeneralIndexStructure.title,
		PmcIndexStructure.paragraphs,
		PmcIndexStructure.sections,
		PmcIndexStructure.tablecaptions,
		PmcIndexStructure.zones
		);
	@Deprecated
	public static final String PPI = "PPI";
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