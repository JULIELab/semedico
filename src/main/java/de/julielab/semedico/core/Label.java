/**
 * Label.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 18.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.text.DecimalFormat;

import de.julielab.semedico.bterms.TermStatistics;

/**
 * @author faessler
 * 
 */
public abstract class Label implements Comparable<Label> {

	public enum RankMeasureStatistic {
		FACET_COUNT, BAYESIAN_TCIDF_AVG
	}

	private static final DecimalFormat doubleFormat = new DecimalFormat("0.00");
	private static final DecimalFormat intFormat = new DecimalFormat("0");

	private final String id;
	private final String name;
	private TermStatistics stats;
	private RankMeasureStatistic rankMeasure;

	public Label(String name, String id) {
		this.name = name;
		this.id = id;
		this.stats = new TermStatistics();
		this.rankMeasure = RankMeasureStatistic.FACET_COUNT;
	}

	/**
	 * @return the count
	 */
	public Long getCount() {
		return (long) stats.getFc();
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(Long count) {
		stats.setFc(count);
	}

	public double getRankScore() {
		double rankScore = -1;

		switch (rankMeasure) {
		case FACET_COUNT:
			rankScore = stats.getFc();
			break;
		case BAYESIAN_TCIDF_AVG:
			rankScore = stats.getBaTcIdf();
			break;
		}
		return rankScore;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the stats
	 */
	public TermStatistics getStatistics() {
		return stats;
	}

	/**
	 * @param stats
	 *            the stats to set
	 */
	public void setStatistics(TermStatistics stats) {
		this.stats = stats;
	}

	public int compareTo(Label label) {
		return Double.compare(label.getRankScore(), getRankScore());
	}

	public abstract boolean hasChildHitsInFacet(Facet facet);

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public void setRankScoreStatistic(RankMeasureStatistic rankScoreStatistic) {
		rankMeasure = rankScoreStatistic;
	}

	/**
	 * 
	 */
	public void reset() {
		rankMeasure = RankMeasureStatistic.FACET_COUNT;
		stats.reset();
	}

	public String toString() {
		return getName() + " (" + getRankScore() + ")";
	}

	public synchronized DecimalFormat getStatFormat() {
		switch (rankMeasure) {
		case FACET_COUNT:
			return intFormat;
		case BAYESIAN_TCIDF_AVG:
			return doubleFormat;
		}
		return doubleFormat;
	}

}
