/** 
 * PostgreSQLDataTypeFactory.java
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
 * Creation date: 22.05.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.db.test;

import java.sql.Connection;
import java.sql.Types;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;

public class PostgreSQLDataTypeFactory extends DefaultDataTypeFactory{

	private Connection connection;
	
	public PostgreSQLDataTypeFactory(Connection connection) {
		super();
		this.connection = connection;
	}

	public DataType createDataType(int sqlType, String sqlTypeName)
	     throws DataTypeException
	   {
	      if (sqlType == Types.ARRAY)
	      {
	         return new ArrayDataType(connection);
	       }
	  
	      return super.createDataType(sqlType, sqlTypeName);
	    }
}
