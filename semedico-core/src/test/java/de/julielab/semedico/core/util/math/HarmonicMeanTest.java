/**
 * HarmonicMeanTest.java
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
 * Creation date: 30.10.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.util.math;

import static org.junit.Assert.*;

import org.junit.Test;

import de.julielab.semedico.core.util.math.HarmonicMean;

/**
 * @author faessler
 * 
 */
public class HarmonicMeanTest {

	@Test
	public void testHarmonicMean() {
		// First check with two numbers; formula: hm(x,y)=2*x*y/x+y
		HarmonicMean hm = new HarmonicMean();
		hm.add(3d);
		hm.add(7d);
		assertEquals("Harmonic mean of two numbers", Double.valueOf(4.2),
				hm.value());

		// And now check for more numbers after a reset.
		// We choose the number so that for the formular n / (1/x1 + 1/x2 + ...
		// + 1/xn) the denominator sums to 1.
		hm.reset();
		hm.add(4d);
		hm.add(2d);
		hm.add(8d);
		hm.add(8d);
		assertEquals("Harmonic mean of four numbers", Double.valueOf(4), hm.value());
	}

}
