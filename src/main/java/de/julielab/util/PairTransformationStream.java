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
import java.util.Iterator;

/**
 * @author faessler
 * 
 */
public class PairTransformationStream<E, S extends Collection<E>, L, R> implements PairStream<L, R>{

	public interface PairTransformer<E, L, R> {
		public L transformLeft(E sourceElement);

		public R transformRight(E sourceElement);
	}

	private Iterator<E> sourceElementIterator;
	private E sourceElement;
	private PairTransformer<? super E, ? extends L, ? extends R> transformer;

	public PairTransformationStream(S source, PairTransformer<? super E, ? extends L, ? extends R> transformer) {
		this.transformer = transformer;
		sourceElementIterator = source.iterator();
	}

	@Override
	public boolean incrementPair() {
		if (sourceElementIterator.hasNext()) {
			sourceElement = sourceElementIterator.next();
			return true;
		}
		return false;
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
