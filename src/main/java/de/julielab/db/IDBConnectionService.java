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
import java.util.Iterator;

/**
 * Interface for obtaining a database connection to the STAG (semantic tagging
 * of concepts) database.
 * 
 * @author faessler
 */
public interface IDBConnectionService {
	
	public static final String TABLE_PG_NAMESPACE = "pg_namespace";
	
	public static final String COL_NAMESPACE_NAME = "nspname";
	
	public Connection getConnection();
	/**
	 * @param conn
	 * @param table
	 * @return
	 */
	public boolean tableExists(Connection conn, String table);
	
	/**
	 * @param columns
	 * @param tableName
	 * @param whereStatement
	 * @return
	 */
	Iterator<byte[][]> selectRowsFromTable(String[] columns, String tableName,
			String whereStatement);
	/**
	 * @param pgSchemaName
	 */
	public void createSchema(String pgSchemaName);
	
	/**
	 * 
	 * @param pgSchemaName
	 * @return
	 */
	public boolean schemaExists(String pgSchemaName);
}
