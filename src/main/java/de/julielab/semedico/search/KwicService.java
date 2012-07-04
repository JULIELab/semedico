/** 
 * KwicService.java
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
 * Creation date: 04.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import java.util.List;
import java.util.Map;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.search.interfaces.IKwicService;

public class KwicService implements IKwicService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IKwicService#getHighlightedTitle(java.util
	 * .Map)
	 */
	@Override
	public String getHighlightedTitle(Map<String, List<String>> docHighlights) {
		String highlightedTitle = null;

		if (docHighlights != null) {
			List<String> titleHighlights = docHighlights
					.get(IndexFieldNames.TITLE);

			if (titleHighlights != null && titleHighlights.size() > 0)
				highlightedTitle = titleHighlights.get(0);
		}
		return highlightedTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IKwicService#getHighlightedAbstract(java.
	 * util.Map, int)
	 */
	@Override
	public String getHighlightedAbstract(
			Map<String, List<String>> docHighlights, int pmid) {
		String highlightedTitle = null;

		if (docHighlights != null) {
			List<String> titleHighlights = docHighlights
					.get(IndexFieldNames.TEXT);

			if (titleHighlights != null && titleHighlights.size() > 0)
				highlightedTitle = titleHighlights.get(0);
		}
		return highlightedTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IKwicService#getAbstractHighlights(java.util
	 * .Map)
	 */
	@Override
	public String[] getAbstractHighlights(
			Map<String, List<String>> docHighlights) {
		List<String> abstractHighlights = docHighlights
				.get(IndexFieldNames.TEXT);

		if (abstractHighlights != null && abstractHighlights.size() > 0) {
			for (int i = 0; i < abstractHighlights.size(); i++) {
				String kwic = abstractHighlights.get(i).trim();
				// To determine whether to prefix the fragment with "..." or
				// not,
				// check if the first char is upper case (mostly sentence
				// beginning). If the char is '<', the first word is
				// highlighted, e.g. '<em>Interleukin-2<em> has proven to
				// [...]'. So the first char is the char after the closing brace
				// '>'.
				char firstChar = kwic.charAt(0);
				if (firstChar == '<')
					firstChar = kwic.charAt(kwic.indexOf('>') + 1);

				if (!Character.isUpperCase(firstChar))
					kwic = "..." + kwic;
				if (kwic.charAt(kwic.length() - 1) != '.')
					kwic = kwic + "...";
				abstractHighlights.set(i, kwic);
			}

			return abstractHighlights.toArray(new String[abstractHighlights
					.size()]);
		}
		return null;
	}
}