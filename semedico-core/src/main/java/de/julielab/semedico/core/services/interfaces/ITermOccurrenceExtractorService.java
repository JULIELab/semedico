/** 
 * ITermOccurrenceExtractorService.java
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
 * Creation date: 20.08.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;
import java.util.Collection;

import de.julielab.semedico.core.concepts.DatabaseConcept;

public interface ITermOccurrenceExtractorService {

	public Collection<String> extractMostFrequentOccurences(DatabaseConcept term, int maxNumberOfOccurrences, int minOccurrences) throws IOException;
}
