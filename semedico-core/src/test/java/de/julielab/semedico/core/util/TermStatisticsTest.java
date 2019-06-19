/**
 * TermStatisticsTest.java
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
package de.julielab.semedico.core.util;

import de.julielab.semedico.core.util.math.TermSetStatistics;
import de.julielab.semedico.core.util.math.TermStatistics;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author faessler
 * 
 */
public class TermStatisticsTest {
	@Test
	public void testGetTc() {
		TermStatistics stats = new TermStatistics();
		stats.setFc(Math.E);
		// The transformation is tc = 1 + log(fc), thus we must get 2.0.
		assertEquals(new Double(2.0), (Double) stats.getTc());

		stats = new TermStatistics();
		stats.setFc(0);
		assertEquals(new Double(0), (Double) stats.getTc());
	}

	@Test
	public void testGetIdf() {
		// Set up a TermSetStatistics mock.
		TermSetStatistics termSetStats = createMock(TermSetStatistics.class);
		expect(termSetStats.getNumDocs()).andReturn(1000000L);
		replay(termSetStats);

		TermStatistics stats = new TermStatistics();
		stats.setTermSetStats(termSetStats);
		stats.setDf(10000);
		// For testing purposes, we do a conversion to log base 10. IDF is then:
		// 1.000.000 / 10.000 = 100; log10(100) = 2
		// (Remember that the returned IDF value is log(numDocs/df). Then, be
		// reminded of the formula loga(x)/loga(b)=logb(x).
		assertEquals(new Double(2), new Double(stats.getIdf() / Math.log(10)));
	}

	@Test
	public void testGetTcIdf() {
		TermSetStatistics termSetStats = createMock(TermSetStatistics.class);
		expect(termSetStats.getNumDocs()).andReturn(1000000L);
		replay(termSetStats);

		TermStatistics stats = new TermStatistics();
		stats.setTermSetStats(termSetStats);
		stats.setFc(15);
		stats.setDf(10000);
		// tc/idf should be something like 17.076202234263047 with the above
		// numbers; for the test, we take the floor.
		assertEquals(new Double(17), new Double(Math.floor(stats.getTcIdf())));
	}

	@Test
	public void testGetBaTcIdf() {
		TermSetStatistics termSetStats = createMock(TermSetStatistics.class);
		expect(termSetStats.getNumDocs()).andReturn(50l).anyTimes();
		expect(termSetStats.getAvgFc()).andReturn(15d).anyTimes();
		expect(termSetStats.getAvgTcIdf()).andReturn(0.5d).anyTimes();
		replay(termSetStats);

		// First term: We found 2/3 of its total occurrences. However, this term
		// is very uncommon overall. We would like to take its very good tc/idf
		// ranking with a grain of salt.
		TermStatistics stats = new TermStatistics();
		stats.setTermSetStats(termSetStats);
		stats.setFc(2);
		stats.setDf(3);
		double tcidf1 = stats.getTcIdf();
		double batcidf1 = stats.getBaTcIdf();

		// This term is fairly common. But we have also found quite a lot of it,
		// this could mean something. Penalize it because its so common, but
		// take into account that we have found a very high percentage, thus it
		// could mean something.
		stats = new TermStatistics();
		stats.setTermSetStats(termSetStats);
		stats.setFc(30);
		stats.setDf(35);
		double tcidf2 = stats.getTcIdf();
		double batcidf2 = stats.getBaTcIdf();

		// We should see now: The pure tc/idf statistics for the first term is
		// higher than for the second. But applying the bayesian average, the
		// second term is ranked higher because we have a more certain statistic
		// on it.
		assertTrue(tcidf1 > tcidf2);
		assertTrue(batcidf1 < batcidf2);

	}
}
