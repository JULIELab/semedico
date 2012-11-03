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
package de.julielab.semedico.bterms;

/**
 * @author faessler
 * 
 */
public class TermStatistics {

	private double fc;
	private double tc;
	private long df;
	private double idf;
	private double batcidf;
	private double tcidf;
	private TermSetStatistics termSetStats;

	/**
	 * 
	 */
	public TermStatistics() {
		reset();
	}

	/**
	 * @return the termSetStats
	 */
	public TermSetStatistics getTermSetStats() {
		return termSetStats;
	}

	/**
	 * @param termSetStats
	 *            the termSetStats to set
	 */
	public void setTermSetStats(TermSetStatistics termSetStats) {
		this.termSetStats = termSetStats;
	}

	/**
	 * @return the facet count
	 */
	public double getFc() {
		return fc;
	}

	/**
	 * @param fc
	 *            the facet count to set
	 */
	public void setFc(double fc) {
		if (this.fc != -1d)
			throw new IllegalAccessError(
					"The base statistics may not be changed anymore once set.");
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
		if (tc == -1)
			tc = fc == 0 ? 0 : 1 + Math.log(fc);
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

		if (df <= 0)
			throw new IllegalStateException(
					"The document frequency of a term must be greater than zero in order to calculate the IDF of this term.");
		if (termSetStats == null)
			throw new IllegalStateException(
					"For calculation of the IDF statistic, the TermSetStats must not be null.");

		long numDocs = termSetStats.getNumDocs();
		idf = Math.log((double) numDocs / (double) df);
		return idf;
	}

	public double getTcIdf() {
		if (tcidf == -1)
			tcidf = getTc() * getIdf();

		return tcidf;
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
	public double getBaTcIdf() {
		if (batcidf == -1) {

			if (termSetStats == null)
				throw new IllegalStateException(
						"For calculation of the bayesian average TC/IDF statistic, the TermSetStats must not be null.");

			double avgFc = termSetStats.getAvgFc();
			double avgTcIdf = termSetStats.getAvgTcIdf();

			// Better call the getter - we can't know whether the tc/idf value
			// has already been calculated.
			double tcidf = getTcIdf();

			batcidf = (avgFc * avgTcIdf + fc * tcidf) / (avgFc + fc);
		}
		return batcidf;
	}

	/**
	 * 
	 */
	public void reset() {
		fc = -1;
		tc = -1;
		df = -1;
		idf = -1;
		tcidf = -1;
		batcidf = -1;
	}

	/**
	 * 
	 */
	void normalizeBaTcIdfStatistic() {
		double maxBaTcIdf = termSetStats.getMaxBaTcIdf();
		batcidf = batcidf / maxBaTcIdf;
	}

}
