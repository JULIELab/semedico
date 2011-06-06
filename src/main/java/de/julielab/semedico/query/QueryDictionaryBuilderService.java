/** 
 * QueryDictionaryBuilderService.java
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.util.TermVariantGenerator;

public class QueryDictionaryBuilderService implements
		IQueryDictionaryBuilderService {

	private static Logger LOGGER = LoggerFactory.getLogger(QueryDictionaryBuilderService.class);
	private ITermService termService;
	private ITermOccurrenceFilterService filterService;
	private Set<String> stopWords;
	
	
	@Override
	public void createTermDictionary(Collection<FacetTerm> terms, String filePath)
			throws SQLException, IOException {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		TermVariantGenerator termVariantGenerator = TermVariantGenerator.getDefaultInstance();
		LOGGER.info("creating query term dictionary " + filePath + "...");		
		int i = 0;

		for( FacetTerm term : terms ){
			if( term.getFacet().getType() != Facet.BIBLIOGRAPHY )
				if( !termService.termOccuredInDocumentIndex(term) )
					continue;
			
			LOGGER.info(++i + ". "+ term.getInternalIdentifier());
			Collection<String> occurrences = termService.readOccurrencesForTerm(term);
			occurrences.addAll(termService.readIndexOccurrencesForTerm(term));
			occurrences = filterService.filterTermOccurrences(term, occurrences);

			Set<String> uniqueOccurrences = new HashSet<String>();			
			uniqueOccurrences.addAll(occurrences);
			
			Integer facetId = term.getFacet().getId();			
			if( facetId.equals(Facet.FIRST_AUTHOR_FACET_ID) || facetId.equals(Facet.LAST_AUTHOR_FACET_ID)){
				for( String occurrence: occurrences ){
					String[] splitts = occurrence.split(",");
					String lastName = splitts[0];
					uniqueOccurrences.add(lastName.toLowerCase());
					uniqueOccurrences.add(occurrence.toLowerCase());
				}
			}
			else{
				for( String occurrence: occurrences ){
					Collection<String> variants = termVariantGenerator.makeTermVariants(occurrence.toLowerCase());
					variants = filterService.filterTermOccurrences(term, variants);
					uniqueOccurrences.addAll(variants);
				}
			}
			for( String occurrence: uniqueOccurrences )
				if( !stopWords.contains(occurrence) )
					writer.write(occurrence + "\t" + term.getInternalIdentifier()+"\n");
		}
		
		writer.close();
		LOGGER.info("query term dictionary finished");

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

	public Set<String> getStopWords() {
		return stopWords;
	}

	public void setStopWords(Set<String> stopWords) {
		this.stopWords = stopWords;
	}
}
