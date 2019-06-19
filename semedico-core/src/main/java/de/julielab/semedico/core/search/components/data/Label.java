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
package de.julielab.semedico.core.search.components.data;

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.util.math.TermStatistics;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * @author faessler
 * 
 */
public abstract class Label implements Comparable<Label>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6185954061410397826L;

	public enum RankMeasureStatistic {
		FACET_COUNT, BAYESIAN_TCIDF_AVG
	}

	private static final DecimalFormat doubleFormat = new DecimalFormat("0.00");
	private static final DecimalFormat intFormat = new DecimalFormat("0");

	private TermStatistics stats;
	private RankMeasureStatistic rankMeasure;
	private boolean showRankScore;

	public Label() {
		this.stats = new TermStatistics();
		this.rankMeasure = RankMeasureStatistic.FACET_COUNT;
		this.setShowRankScore(true);
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
	public abstract String getName();

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
		int c = Double.compare(label.getRankScore(), getRankScore());
		// If we would just return c, two labels with the same rank would be
		// considered the same object, e.g. by Sets. This, when the rank is
		// equal, we resort to lexicographical ordering.
		if (0 == c && null != getName() && null != label.getName())
			return getName().compareTo(label.getName());
		return c;
	}

	public abstract boolean hasChildHitsInFacet(Facet facet);

	/**
	 * @return the id
	 */
	public abstract String getId();

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

	public boolean showRankScore() {
		return showRankScore;
	}

	public void setShowRankScore(boolean showRankScore) {
		this.showRankScore = showRankScore;
	}
	
	public abstract boolean isTermLabel();
	public abstract boolean isStringLabel();
	public abstract boolean isMessageLabel();

}
