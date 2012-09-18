/**
 * PairTransformationStream.java
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
 * Creation date: 21.06.2012
 **/

/**
 * 
 */
package de.julielab.util;

import java.util.Collection;

/**
 * @author faessler
 * 
 */
public class PairTransformationStream<E, S extends Collection<E>, L, R> extends AbstractPairStream<E, S, L, R>{

	private PairTransformer<? super E, ? extends L, ? extends R> transformer;

	public PairTransformationStream(S source, PairTransformer<? super E, ? extends L, ? extends R> transformer) {
		super(source);
		this.transformer = transformer;
	}

	@Override
	public L getLeft() {
		L left = transformer.transformLeft(sourceElement);
		return left;
	}

	@Override
	public R getRight() {
		R right = transformer.transformRight(sourceElement);
		return right;
	}
}
