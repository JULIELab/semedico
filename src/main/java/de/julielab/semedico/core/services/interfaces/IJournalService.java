/** 
 * IJournalService.java
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
 * Creation date: 27.05.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import de.julielab.semedico.core.Journal;

public interface IJournalService {

	public abstract Iterator<Journal> readJournalFile(String filePath)
			throws IOException;

	public abstract void insertJournalsAsTerms(Collection<Journal> journals)
			throws SQLException;

}