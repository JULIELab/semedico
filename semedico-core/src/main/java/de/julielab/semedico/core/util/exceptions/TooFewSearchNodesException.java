/**
 * TooFewSearchNodesException.java
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
 * Creation date: 01.11.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.util.exceptions;

/**
 * @author faessler
 *
 */
public class TooFewSearchNodesException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 255002669343246L;

	/**
	 * @param string
	 */
	public TooFewSearchNodesException(String string) {
		super(string);
	}
}

