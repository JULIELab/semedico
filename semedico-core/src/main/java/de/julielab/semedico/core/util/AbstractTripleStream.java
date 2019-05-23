/**
 * AbstractTripleStream.java
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
public abstract class AbstractTripleStream<E, S extends Iterable<E>, L, M, R> extends AbstractPairStream<E, S, L, R> implements TripleStream<L, M, R> {

	public interface TripleTransformer<E, L, M, R> extends PairTransformer<E, L, R> {
		public M transformMiddle(E sourceElement);
	}
	
	/**
	 * @param source
	 */
	public AbstractTripleStream(S source) {
		super(source);
	}

	public abstract M getMiddle();
	
}

