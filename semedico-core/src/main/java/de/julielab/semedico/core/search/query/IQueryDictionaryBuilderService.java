/** 
 * IQueryDictionaryBuilderService.java
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

package de.julielab.semedico.core.search.query;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Meant for external tools: Creates a dictionary file for use with the LingpipeChunker.
 * @author faessler
 *
 */
public interface IQueryDictionaryBuilderService {
	public void createTermDictionary(String filePath) throws SQLException, IOException;
}
