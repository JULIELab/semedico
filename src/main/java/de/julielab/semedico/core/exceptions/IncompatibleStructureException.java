/**
 * IncompatibleStructureException.java
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
 * Creation date: 03.11.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.exceptions;

/**
 * @author faessler
 * 
 */
public class IncompatibleStructureException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1779120124255963839L;

	public IncompatibleStructureException() {
		super();
	}
	
	public IncompatibleStructureException(String string) {
		super(string);
	}
}
