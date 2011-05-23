/** 
 * QueryIndexBuilderService.java
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
 * Creation date: 30.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.query;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;

public class QueryIndexBuilderService implements IQueryIndexBuilderService {

	private static Logger LOGGER = Logger
			.getLogger(QueryIndexBuilderService.class);
	private ITermService termService;
	private ITermOccurrenceFilterService filterService;

	public static final String PHRASES_INDEX_FIELD_NAME = "phrases";
	public static final String ID_INDEX_FIELD_NAME = "id";

	@Override
	public void createTermIndex(Collection<FacetTerm> terms, String filePath)
			throws IOException, SQLException {
		// reformulated for Lucene 3.1 without check if this actually works. Blame me if you run into unexpected trouble :-) - Erik Faessler 20.05.2011
		IndexWriter writer = new IndexWriter(FSDirectory.open(new File(filePath)), new IndexWriterConfig(Version.LUCENE_31, new WhitespaceAnalyzer(Version.LUCENE_31)));
//		IndexWriter writer = new IndexWriter(filePath, new WhitespaceAnalyzer()); 
		LOGGER.info("creating query term index..");
		int i = 0;
		for( FacetTerm term: terms ){
			if( term.getFacet().getType() != Facet.BIBLIOGRAPHY )
				if( !termService.termOccuredInDocumentIndex(term) )
				continue;
			
			LOGGER.info(++i + ". "+ term.getInternalIdentifier());
			Document document = new Document();
			Field field = new Field(ID_INDEX_FIELD_NAME, term.getInternalIdentifier(), Store.YES, Index.NO);
			document.add(field);
			Collection<String> occurrences = termService.readOccurrencesForTerm(term);
			occurrences.addAll(termService.readIndexOccurrencesForTerm(term));

			Integer facetId = term.getFacet().getId();
			if( !(facetId.equals(Facet.FIRST_AUTHOR_FACET_ID) || facetId.equals(Facet.LAST_AUTHOR_FACET_ID)))			
				filterService.filterTermOccurrences(term, occurrences);

			for( String occurrence: occurrences ){
				field = new Field(PHRASES_INDEX_FIELD_NAME, occurrence, Store.NO, Index.ANALYZED);
				document.add(field);
			}
			writer.addDocument(document);
		}
		
		writer.optimize();
		writer.close();
		LOGGER.info("query term index finished");

	}

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public ITermOccurrenceFilterService getFilterService() {
		return filterService;
	}

	public void setFilterService(ITermOccurrenceFilterService filterService) {
		this.filterService = filterService;
	}

}
