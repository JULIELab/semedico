/**
 * AbstractPairStream.java
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
package de.julielab.util;

import java.util.Iterator;

/**
 * @author faessler
 *
 */
public abstract class AbstractPairStream<E, S extends Iterable<E>, L, R> implements PairStream<L, R> {
	
	public interface PairTransformer<E, L, R> {
		public L transformLeft(E sourceElement);
		public R transformRight(E sourceElement);
	}
	
	protected Iterator<E> sourceElementIterator;
	protected E sourceElement;
	
	public AbstractPairStream(S source) {
		this.sourceElementIterator = source.iterator();
	}
	
	public boolean incrementTuple() {
		if (sourceElementIterator.hasNext()) {
			sourceElement = sourceElementIterator.next();
			return true;
		}
		return false;
	}
	
	abstract public L getLeft();
	abstract public R getRight();
}

