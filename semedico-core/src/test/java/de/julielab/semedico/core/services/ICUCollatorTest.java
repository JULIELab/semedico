/**
 * TermICUComparatorTest.java
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
 * Creation date: 11.06.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.services;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.ibm.icu.text.Collator;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <p>
 * This class tests if the String Collator works as expected.
 * </p>
 * 
 * @author faessler
 * 
 */
public class ICUCollatorTest {

	private final static Logger log = LoggerFactory
			.getLogger(ICUCollatorTest.class);

	@Test
	public void testICUCollator() throws ParseException {
		final Collator collator = SemedicoCoreModule
				.buildRuleBasedCollatorWrapper().getCollator();
		// Sorting with Collator for ue = u etc. should be: [Famler, Fäßler,
		// Faessler, Kojn, Köln, Koeln, Suff, Sühnel, Suehnel]
		// Without the Collator, it would be: [Faessler, Famler, Fäßler, Koeln,
		// Kojn, Köln, Suehnel, Suff, Sühnel]
		ArrayList<String> umlauts = Lists
				.newArrayList("Sühnel", "Suehnel", "Suff", "Köln", "Kojn",
						"Koeln", "Fäßler", "Faessler", "Famler");
		Collections.sort(umlauts, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return collator.compare(arg0, arg1);
			}

		});
		log.info("Collation-sorted names are: {}",
				Arrays.toString(umlauts.toArray()));
		assertEquals("Famler", umlauts.get(0));
		assertEquals("Fäßler", umlauts.get(1));
		assertEquals("Faessler", umlauts.get(2));
		assertEquals("Kojn", umlauts.get(3));
		assertEquals("Köln", umlauts.get(4));
		assertEquals("Koeln", umlauts.get(5));
		assertEquals("Suff", umlauts.get(6));
		assertEquals("Sühnel", umlauts.get(7));
		assertEquals("Suehnel", umlauts.get(8));

		// Check 'ß'
		assertEquals(0, collator.compare("Faessler", "Fäßler"));

		// Check capital Umlauts
		assertEquals(0, collator.compare("Ae", "Ä"));
		assertEquals(0, collator.compare("Ue", "Ü"));
		assertEquals(0, collator.compare("Oe", "Ö"));

		// Check exotic things. We must be cautious here not to let an 'e'
		// follow an 'a' because 'ae' would be collated.
		assertEquals(0, collator.compare("åÅĬØöāŅéêęȁŁ", "aAIOoaNeeeaL"));
	}

}
