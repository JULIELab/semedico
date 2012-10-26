/**
 * TermStatistics.java
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
 * Creation date: 26.10.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core;

/**
 * @author faessler
 * 
 */
public class TermStatistics {

	private long numDocs;
	private long fc;
	private double tc;
	private long df;
	private double idf;
	private long C;
	private double m;
	private double batcidf;

	/**
	 * 
	 */
	public TermStatistics() {
		numDocs = -1;
		fc = -1;
		tc = -1;
		df = -1;
		idf = -1;
		batcidf = -1;
	}

	/**
	 * @return the numDocs
	 */
	public long getNumDocs() {
		return numDocs;
	}

	/**
	 * @param numDocs
	 *            the numDocs to set
	 */
	public void setNumDocs(long numDocs) {
		this.numDocs = numDocs;
	}

	/**
	 * @return the facet count
	 */
	public long getFc() {
		return fc;
	}

	/**
	 * @param fc
	 *            the facet count to set
	 */
	public void setFc(long fc) {
		this.fc = fc;
	}

	/**
	 * The term count of a term <code>t</code> is a transformation of the facet
	 * count for computational reasons:<br>
	 * <math xmlns="http://www.w3.org/1998/Math/MathML"> <mstyle
	 * displaystyle="true"> <mi> tc </mi> <mfenced> <mrow> <mi> t </mi> </mrow>
	 * </mfenced> <mo> = </mo> <mn> 1 </mn> <mo> + </mo> <mi> log </mi>
	 * <mfenced> <mrow> <mi> f </mi> <mi> c </mi> <mfenced> <mrow> <mi> t </mi>
	 * </mrow> </mfenced> </mrow> </mfenced> </mstyle> </math> for <math >
	 * <mstyle displaystyle="true"> <mi> fc </mi> <mfenced> <mrow> <mi> t </mi>
	 * </mrow> </mfenced> <mo> &gt; </mo> <mn> 0 </mn> </mstyle> </math> and
	 * <math > <mstyle displaystyle="true"> <mi> tc </mi> <mfenced> <mrow> <mi>
	 * t </mi> </mrow> </mfenced> <mo> = </mo> <mn> 0 </mn> </mstyle> </math>
	 * else.
	 * 
	 * @return the term count
	 */
	public double getTc() {
		return tc;
	}

	/**
	 * 
	 * The document frequency for this term <code>t</code>:<br>
	 * <math xmlns="http://www.w3.org/1998/Math/MathML"> <mstyle
	 * displaystyle="true"> <mi> d </mi> <mi> f </mi> <mfenced> <mrow> <mi> t
	 * </mi> <mo> , </mo> <mi> D </mi> </mrow> </mfenced> <mo> = </mo> <mfenced
	 * open="|" close="|"> <mrow> <mfenced open="{" close="}"> <mrow> <mi> d
	 * </mi> <mo> &#x2208;<!--element of--> </mo> <mi> D </mi> <mo> : </mo> <mi>
	 * t </mi> <mo> &#x2208;<!--element of--> </mo> <mi> d </mi> </mrow>
	 * </mfenced> </mrow> </mfenced> </mstyle> </math>
	 * 
	 * @return the document frequency of this term
	 */
	public long getDf() {
		return df;
	}

	/**
	 * @param df
	 *            the df to set
	 */
	public void setDf(long df) {
		this.df = df;
	}

	/**
	 * The inverse document frequency is derived from the document frequency by<br>
	 * <math xmlns="http://www.w3.org/1998/Math/MathML"> <mstyle
	 * displaystyle="true"> <mi> idf </mi> <mfenced> <mrow> <mi> t </mi> <mo> ,
	 * </mo> <mi> D </mi> </mrow> </mfenced> <mo> = </mo> <mi> log </mi>
	 * <mfenced> <mrow> <mfrac> <mrow> <mfenced open="|" close="|"> <mrow> <mi>
	 * D </mi> </mrow> </mfenced> </mrow> <mrow> <mi> df </mi> <mfenced> <mrow>
	 * <mi> t </mi> </mrow> </mfenced> </mrow> </mfrac> </mrow> </mfenced>
	 * </mstyle> </math> where <math
	 * xmlns="http://www.w3.org/1998/Math/MathML"><mi>D</mi></math> denotes the
	 * set of all available documents.
	 * 
	 * @return the inverse document frequency of this term
	 */
	public double getIdf() {
		if (idf != -1)
			return idf;

		assert numDocs > 0 : "The total number of available documents must be set in order to calculate the IDF value of a term.";
		assert df > 0 : "The document frequency of a term must be greater than zero in order to calculate the IDF of this term.";

		idf = Math.log(numDocs / df);
		return idf;
	}

	/**
	 * @return the c
	 */
	public long getC() {
		return C;
	}

	/**
	 * @param c the c to set
	 */
	public void setC(long c) {
		C = c;
	}

	/**
	 * @return the m
	 */
	public double getM() {
		return m;
	}

	/**
	 * @param m the m to set
	 */
	public void setM(double m) {
		this.m = m;
	}

	/**
	 * The <em>bayesian average</em> of the <code>tc/idf</code> value for this
	 * term. This value tends towards the average <code>tc/idf</code> for all
	 * terms when this term's <code>fc</code> statistic is low. It tends to the
	 * real <code>tc/idf</code> value for this term, when <code>fc</code> is
	 * high.
	 * 
	 * @return the bayesian average for this term's <code>tc/idf</code> value.
	 */
	public double getBatcidf() {
		return batcidf;
	}

}
