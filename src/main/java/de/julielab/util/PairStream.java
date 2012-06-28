/**
 * PairStream.java
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
 * Creation date: 22.06.2012
 **/

/**
 * 
 */
package de.julielab.util;

/**
 * @author faessler
 *
 */
public interface PairStream<L, R> {
	public L getLeft();
	public R getRight();
	public boolean incrementPair();
}

