/** 
 * TermVariantGenerator.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 29.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.util;

import java.util.Set;
import java.util.TreeSet;

public class TermVariantGenerator {

	private static final int NUM_HYPHENS4VARIANTS = 7;
	private static TermVariantGenerator defaultInstance;
	
	
	public static TermVariantGenerator getDefaultInstance(){
		if( defaultInstance == null )
			defaultInstance = new TermVariantGenerator();
		
		return defaultInstance;
	}
	
	/**
	 * Generates a {@link Set} of variants for a given term. 
	 * @param term
	 * @return {@link Set} of variants
	 */
	public Set<String> makeTermVariants(String term) {

		TreeSet<String> termVariants = new TreeSet<String>();
		String termVariant = "";

		// replace hyphens with white space unless too many hyphens in term
		String[] splits = term.split("\\-");

		int limit = splits.length + 1;

		if (limit < NUM_HYPHENS4VARIANTS) {

			for (int i = 0; i < limit; i++) {
				splits = term.split("\\-", i);
				String result = "";
				for (String split : splits) {
					result += " " + split;
				}
				// System.err.println(result.trim());
				termVariants.add(result.trim());
				result = result.replaceFirst("\\-", " ");
				termVariants.add(result.trim());
			}

			termVariant = term.replaceAll("\\-", " ");
			termVariants.add(termVariant);
			termVariant = term.replaceFirst("\\-", " ");
			termVariants.add(termVariant);

			// replace hyphens with empty string iff term.length > NUM
			if (term.length() > 8) {

				splits = term.split("\\-");
				limit = splits.length + 1;
				for (int i = 0; i < limit; i++) {
					splits = term.split("\\-", i);
					String result = " ";
					for (String split : splits) {
						result += "" + split;
					}
					// System.err.println(i + " " + result);
					termVariants.add(result.trim());
					result = result.replaceFirst("\\-", "");
					termVariants.add(result.trim());
				}

				termVariant = term.replaceAll("\\-", "");
				termVariants.add(termVariant);
				termVariant = term.replaceFirst("\\-", "");
				termVariants.add(termVariant);
			}

		}
		// replace internal parentheses with ""
		// in addition: add [hyphen to ""] variants
		if (term.contains("(") && term.contains(")")) {

			termVariant = term.replaceFirst("\\(", "");
			termVariant = termVariant.replaceFirst("\\)", "");
			termVariants.add(termVariant);
			termVariant = termVariant.replaceFirst("\\-", "");
			termVariants.add(termVariant);
			termVariant = termVariant.replaceAll("\\-", "");
			termVariants.add(termVariant);

			termVariant = term.replaceAll("\\(", "");
			termVariant = termVariant.replaceAll("\\)", "");
			termVariants.add(termVariant);
			termVariant = termVariant.replaceFirst("\\-", "");
			termVariants.add(termVariant);
			termVariant = termVariant.replaceAll("\\-", "");
			termVariants.add(termVariant);

		}

		// replace white spaces with hyphens
		splits = term.split(" ");
		limit = splits.length + 1;
		for (int i = 0; i < limit; i++) {
			splits = term.split(" ", i);
			String result = "";
			for (String split : splits) {
				result += "-" + split;
			}
			result = result.substring(1).trim();
			// System.err.println(i + " " + result);
			termVariants.add(result.trim());
			result = result.replaceFirst(" ", "-");
			termVariants.add(result.trim());
		}

		termVariant = term.replaceAll(" ", "-");
		termVariants.add(termVariant);
		termVariant = term.replaceFirst(" ", "-");
		termVariants.add(termVariant);

		// For author names.
		termVariant = term.replaceAll(",", "");
		termVariants.add(termVariant);
		
		// genitive 's
		termVariant = term.replaceFirst("'s", "");
		termVariants.add(termVariant);
		termVariant = term.replaceFirst("'s", "s");
		termVariants.add(termVariant);
				
		return termVariants;
	}

}
