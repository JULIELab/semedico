/** 
 * IDictionaryReaderService.java
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
 * Creation date: 05.08.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.lingpipe;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

public interface IDictionaryReaderService {

	public abstract MapDictionary<String> getMapDictionary(String dictionaryFilePath) throws IOException;

	public abstract TrieDictionary<String> getTrieDictionary(String dictionaryFilePath);

}