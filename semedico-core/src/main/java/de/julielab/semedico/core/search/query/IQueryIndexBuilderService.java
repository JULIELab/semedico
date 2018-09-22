/** 
 * IQueryIndexBuilderService.java
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
import java.util.Collection;

import de.julielab.semedico.core.concepts.DatabaseConcept;

public interface IQueryIndexBuilderService {

	public void createTermIndex(Collection<DatabaseConcept> terms, String filePath) throws IOException, SQLException;
}
