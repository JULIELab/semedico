/**
 * EmptySearchComplementException.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 08.11.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.exceptions;

/**
 * @author faessler
 * 
 */
public class EmptySearchComplementException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7367288546342267672L;

	/**
	 * 
	 */
	public EmptySearchComplementException() {
		super();
	}

	/**
 * 
 */
	public EmptySearchComplementException(String string) {
		super(string);
	}
}
