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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.ibm.icu.text.Collator;

/**
 * <p>
 * This class tests if the String Collator works as expected.
 * </p>
 *
 * @see SemedicoCoreModule#buildICUCollator()
 * @author faessler
 * 
 */
public class ICUCollatorTest {

	@Test
	public void testICUCollator() throws ParseException {
		final Collator collator = SemedicoCoreModule.buildICUCollator();
		// Sorting with Collator for ue = u etc. should be: [F��ler, Faessler,
		// Famler, K�ln, Koeln, Kojn, S�hnel, Suehnel, Suff]
		// Without the Collator, it would be: [Faessler, Famler, F��ler, Koeln,
		// Kojn, K�ln, Suehnel, Suff, S�hnel]
		ArrayList<String> umlauts = Lists
				.newArrayList("S�hnel", "Suehnel", "Suff", "K�ln", "Kojn",
						"Koeln", "F��ler", "Faessler", "Famler");
		Collections.sort(umlauts, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return collator.compare(arg0, arg1);
			}

		});
		// System.out.println(Arrays.toString(umlauts.toArray()));
		assertEquals("F��ler", umlauts.get(0));
		assertEquals("Faessler", umlauts.get(1));
		assertEquals("Famler", umlauts.get(2));
		assertEquals("K�ln", umlauts.get(3));
		assertEquals("Koeln", umlauts.get(4));
		assertEquals("Kojn", umlauts.get(5));
		assertEquals("S�hnel", umlauts.get(6));
		assertEquals("Suehnel", umlauts.get(7));
		assertEquals("Suff", umlauts.get(8));

		// Check '�'
		assertEquals(0, collator.compare("Faessler", "F��ler"));

		// Check capital Umlauts
		assertEquals(0, collator.compare("Ae", "�"));
		assertEquals(0, collator.compare("Ue", "�"));
		assertEquals(0, collator.compare("Oe", "�"));

		// Check exotic things.
		assertEquals(0, collator.compare("��꯿美��", "aAIOoAaeeea"));
	}

}
