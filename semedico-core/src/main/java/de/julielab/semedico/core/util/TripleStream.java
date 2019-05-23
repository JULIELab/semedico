/**
 * TripleStream.java
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
 * Creation date: 04.09.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.util;

/**
 * @author faessler
 *
 */
public interface TripleStream<L, M, R> extends PairStream<L, R> {
	public M getMiddle();
}

