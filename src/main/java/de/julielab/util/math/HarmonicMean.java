/**
 * HarmonicMean.java
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
 * Creation date: 29.10.2012
 **/

/**
 * 
 */
package de.julielab.util.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Serves to compute the harmonic mean defined as <math
 * xmlns="http://www.w3.org/1998/Math/MathML"> <mstyle displaystyle="true"> <mi>
 * H </mi> <mo> = </mo> <mfenced> <mrow> <mfrac> <mrow> <mn> 1 </mn> </mrow>
 * <mrow> <mi> n </mi> </mrow> </mfrac> <munderover> <mrow> <mo>
 * &#x2211;<!--n-ary summation--> </mo> </mrow> <mrow> <mi> i </mi> <mo> = </mo>
 * <mn> 1 </mn> </mrow> <mrow> <mi> n </mi> </mrow> </munderover> <msubsup>
 * <mrow> <mi> x </mi> </mrow> <mrow> <mi> i </mi> </mrow> <mrow> <mo> - </mo>
 * <mn> 1 </mn> </mrow> </msubsup> </mrow> </mfenced> </mstyle> </math>.
 * 
 * @see https://en.wikipedia.org/wiki/Harmonic_mean
 * 
 * @author faessler
 * 
 */
public class HarmonicMean {
	private List<Double> numbers;

	/**
	 * 
	 */
	public HarmonicMean() {
		numbers = new ArrayList<Double>();
	}

	public void add(Double d) {
		numbers.add(d);
	}

	public Double value() {
		// The harmonic mean can be computed as n / (1/x1 + 1/x2 + ... + 1/xn)
		Double hm = null;
		Double reciprocalSum = 0d;
		for (Double d : numbers)
			reciprocalSum += 1 / d;
		Double n = (double) numbers.size();
		hm = n / reciprocalSum;
		return hm;
	}
	
	public void reset() {
		numbers.clear();
	}
}
