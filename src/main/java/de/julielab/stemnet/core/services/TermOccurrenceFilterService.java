/** 
 * TermOccurrenceFilterService.java
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
 * Creation date: 28.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.stemnet.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetTerm;

public class TermOccurrenceFilterService implements ITermOccurrenceFilterService {

	private int maxLength;
	private int maxTokenCount;
	private int minLength;
	public int DEFAULT_MAX_LENGTH = 40;
	public int DEFAULT_MIN_LENGTH = 2;
	public int DEFAULT_MAX_TOKEN_COUNT = 3;
	private Pattern inverseQualified;
	
	public TermOccurrenceFilterService() {
		this.maxLength = DEFAULT_MAX_LENGTH;
		this.maxTokenCount = DEFAULT_MAX_TOKEN_COUNT;
		this.minLength = DEFAULT_MIN_LENGTH;
		this.inverseQualified = Pattern.compile(".*[\\w\\+\\-\\*]+, [\\w\\+\\-\\*]+.*");
	}

	@Override
	public Collection<String> filterTermOccurrences(FacetTerm term, Collection<String> termOccurrences) {
		Facet facet = term.getFacet();
		boolean isAuthor = facet != null && (facet.getId().equals(Facet.FIRST_AUTHOR_FACET_ID) || 
				facet.getId().equals(Facet.LAST_AUTHOR_FACET_ID))
				; 
		termOccurrences = new ArrayList<String>(termOccurrences);
		for( Iterator<String> iterator = termOccurrences.iterator(); iterator.hasNext(); ){
			String occurrence = iterator.next().trim();
			if( occurrence.length() >= maxLength )
				iterator.remove();
			else if( occurrence.length() < minLength )
				iterator.remove();
			else if( occurrence.split(" ").length > maxTokenCount )
				iterator.remove();
			else if( inverseQualified.matcher(occurrence).matches() && !isAuthor )
				iterator.remove();
			else if( term.getFacet().getId() == Facet.PROTEIN_FACET_ID && term.getParent() != null )
				iterator.remove();
		}
		
		return termOccurrences;
	}
	

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxTokenCount() {
		return maxTokenCount;
	}

	public void setMaxTokenCount(int maxTokenCount) {
		this.maxTokenCount = maxTokenCount;
	}



}
