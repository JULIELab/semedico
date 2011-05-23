/** 
 * ArrayDataType.java
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

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.TypeCastException;

public class ArrayDataType extends DataType {

	private Connection connection;
	
	protected ArrayDataType(Connection connection) {
		super();
		this.connection = connection;
	}

	
	@Override
	public int compare(Object array1, Object array2) throws TypeCastException {

		if( array1 == null && array2 == null )
			return 0;
		else if( array1 == null || array2 == null )
			return -1;
		
		String array1String = array1 instanceof Object[] ? toString((Object[])array1) : array1.toString();
		String array2String = array2 instanceof Object[] ? toString((Object[])array2) : array2.toString();
		return array1String.compareTo(array2String);
	}

	private String toString(Object[] array) {
		String string = "{";
		for( int i = 0; i < array.length; i++ ){
			string += array[i];
			if( i < array.length - 1 )
				string+=", ";
		}
		string += "}";
			
		return string;
	}


	@Override
	public int getSqlType() {
		
		return Types.ARRAY;
	}

	@Override
	public Object getSqlValue(int column, ResultSet resultSet) throws SQLException, TypeCastException {
		Array array = resultSet.getArray(column);
		if( array != null )
			return array.getArray();
		else
			return null;
	}

	@Override
	public Class getTypeClass() {
		
		return String[].class;
	}

	@Override
	public boolean isDateTime() {
		return false;
	}

	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public void setSqlValue(Object array, int column, PreparedStatement statement)
			throws SQLException, TypeCastException {

		statement.setArray(column, (Array) typeCast(array));
	}

	@Override
	public Object typeCast(Object array) throws TypeCastException {
		try {
			String className = array.getClass().getCanonicalName();
			if( className != null && className.endsWith("[]") )
				className = className.substring(0, className.length() - 2);
			
			Class clazz = Class.forName(className);
			for( String typeName : connection.getTypeMap().keySet() ){
				if( connection.getTypeMap().get(typeName).equals(clazz) )
					return connection.createArrayOf(typeName, (Object[]) array);
			}
			
			return connection.createArrayOf("varchar", (Object[]) array);
			
		} catch (Exception e) {
			throw new TypeCastException(e);		
		}
	}

}
