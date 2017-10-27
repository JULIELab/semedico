/**
 * TripleTransformationStream.java
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
public class TripleTransformationStream<E, S extends Iterable<E>, L, M, R> extends AbstractTripleStream<E, S, L, M, R> {

	private TripleTransformer<? super E, ? extends L, ? extends M, ? extends R> transformer;

	public TripleTransformationStream(S source, TripleTransformer<? super E, ? extends L, ? extends M, ? extends R> transformer) {
		super(source);
		this.transformer = transformer;
	}
	
	/* (non-Javadoc)
	 * @see de.julielab.util.PairStream#getLeft()
	 */
	@Override
	public L getLeft() {
		L left = transformer.transformLeft(sourceElement);
		return left;
	}

	/* (non-Javadoc)
	 * @see de.julielab.util.TripleStream#getMiddle()
	 */
	@Override
	public M getMiddle() {
		M middle = transformer.transformMiddle(sourceElement);
		return middle;
	}
	
	/* (non-Javadoc)
	 * @see de.julielab.util.PairStream#getRight()
	 */
	@Override
	public R getRight() {
		R right = transformer.transformRight(sourceElement);
		return right;
	}
}

