/**
 * ClosableIterator.java
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
 * Creation date: 14.06.2012
 **/

/**
 * 
 */
package de.julielab.util;

import java.util.Iterator;

/**
 * Iterator interface with an additional <code>close</code> method. This method
 * is meant to allow for cleanup-purposes of resources held by the iterator.
 * 
 * @author faessler
 * 
 */
public interface ClosableIterator<T> extends Iterator<T> {
	public void close();
}
