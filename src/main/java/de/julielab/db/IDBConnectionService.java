/**
 * IDBConnectionService.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.06.2011
 **/

package de.julielab.db;

import java.sql.Connection;

/**
 * Interface for obtaining a database connection to the STAG (semantic tagging
 * of concepts) database.
 * 
 * @author faessler
 */
public interface IDBConnectionService {
	public Connection getConnection();
}
