/** 
 * AbbreviationFormatter.java
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
 * Creation date: 26.06.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class AbbreviationFormatter extends Format{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maxLength;
	
	
	public AbbreviationFormatter(int maxLength) {
		super();
		this.maxLength = maxLength;
	}

	@Override
	public StringBuffer format(Object object, StringBuffer buffer,
			FieldPosition position) {
		
		String string = object.toString();
		if( string.length() >= maxLength )
			return buffer.append(string.substring(0, maxLength - 3) + "...");
		else
			buffer.append(string);
		
		return buffer;
	}

	@Override
	public Object parseObject(String arg0, ParsePosition arg1) {
		if( true )
			throw new UnsupportedOperationException();
		return null;
	}

}
