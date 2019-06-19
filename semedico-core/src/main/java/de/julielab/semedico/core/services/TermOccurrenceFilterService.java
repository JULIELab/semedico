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

package de.julielab.semedico.core.services;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.interfaces.IStemmerService;
import de.julielab.semedico.core.services.interfaces.ITermOccurrenceFilterService;

import java.util.*;
import java.util.regex.Pattern;

public class TermOccurrenceFilterService implements ITermOccurrenceFilterService {

	private int maxLength;
	private int maxTokenCount;
	private int minLength;
	public int DEFAULT_MAX_LENGTH = 80;
	public int DEFAULT_MIN_LENGTH = 2;
	public int DEFAULT_MAX_TOKEN_COUNT = 10;
	private Pattern inverseQualified;
	private IStemmerService stemmer;

	public TermOccurrenceFilterService(IStemmerService stemmerService) {
		this.stemmer = stemmerService;
		this.maxLength = DEFAULT_MAX_LENGTH;
		this.maxTokenCount = DEFAULT_MAX_TOKEN_COUNT;
		this.minLength = DEFAULT_MIN_LENGTH;
		this.inverseQualified = Pattern.compile(".*[\\w\\+\\-\\*]+, [\\w\\+\\-\\*]+.*");
	}

	@Override
	public List<String> filterTermOccurrences(IConcept term, Collection<String> termOccurrences) {
		List<String> ret = new ArrayList<>(termOccurrences);
		Set<String> normalizedForms = new HashSet<>();
		for (Iterator<String> iterator = ret.iterator(); iterator.hasNext();) {
			String occurrence = iterator.next().trim();
			if (occurrence.length() >= maxLength)
				iterator.remove();
			else if (occurrence.length() < minLength)
				iterator.remove();
			else if (occurrence.split(" ").length > maxTokenCount)
				iterator.remove();
			else if (inverseQualified.matcher(occurrence).matches())
				iterator.remove();
			else {
				String normalizedOccurrence = occurrence.toLowerCase();
				normalizedOccurrence = stemmer.stem(normalizedOccurrence);
				normalizedOccurrence.replaceAll("[\\p{Punct} ]", "");
				if (normalizedForms.contains(normalizedOccurrence))
					iterator.remove();
				normalizedForms.add(normalizedOccurrence);
			}
		}

		return ret;
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
