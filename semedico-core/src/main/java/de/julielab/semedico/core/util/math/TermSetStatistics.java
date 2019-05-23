/**
 * SearchNodeStatistics.java
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
package de.julielab.semedico.core.util.math;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author faessler
 * 
 */
public class TermSetStatistics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2989369159590601754L;

	private Set<TermStatistics> termStats;

	private double avgFc;
	private double avgTcIdf;
	private double maxBaTcIdf;
	private boolean isBaTcIdfNormalized;
	private long numDocs;

	/**
	 * 
	 */
	public TermSetStatistics() {
		avgFc = -1;
		avgTcIdf = -1;
		numDocs = -1;
		maxBaTcIdf = -1;
		isBaTcIdfNormalized = false;
		termStats = new HashSet<>();
	}

	public void add(TermStatistics stats) {
		avgFc = -1;
		avgTcIdf = -1;
		termStats.add(stats);
	}

	public double getAvgFc() {
		if (avgFc == -1) {
			double sum = 0;
			for (TermStatistics stats : termStats)
				sum += stats.getFc();
			avgFc = sum / termStats.size();
		}
		
		return avgFc;
	}

	public double getAvgTcIdf() {
		if (avgTcIdf == -1) {
			double sum = 0;
			for (TermStatistics stats : termStats)
				sum += stats.getTcIdf();
			avgTcIdf = sum / termStats.size();
		}
		return avgTcIdf;
	}

	/**
	 * @return the numDocs
	 */
	public long getNumDocs() {
		assert numDocs > 0 : "The total number of documents must be set before using it.";
		
		return numDocs;
	}

	/**
	 * @param numDocs the numDocs to set
	 */
	public void setNumDocs(long numDocs) {
		this.numDocs = numDocs;
	}
	
	public void normalizeBaTcIdfStatistic() {
		if (!isBaTcIdfNormalized) {
			for (TermStatistics stats : termStats)
				stats.normalizeBaTcIdfStatistic();
			isBaTcIdfNormalized = true;
		}
	}
	
	public double getMaxBaTcIdf() {
		if (maxBaTcIdf == -1) {
			for (TermStatistics stats : termStats) {
				if (stats.getBaTcIdf() > maxBaTcIdf)
					maxBaTcIdf = stats.getBaTcIdf();
			}
		}
		return maxBaTcIdf;
	}
	
}
