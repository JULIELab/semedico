/** 
 * UniqueFilter.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: 0.1 	
 * Since version:   0.1
 *
 * Creation date: 26.03.2008 
 * 
 * A UniqueFilter filters multiple occurrences of {@link org.apache.lucene.analysis.Token tokens} tokens with the 
 * same token text and removes them.
 **/

package de.julielab.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * A UniqueFilter filters multiple occurrences of
 * {@link org.apache.lucene.analysis.Token tokens} tokens with the same token
 * text and removes them.
 * 
 * @author landefeld
 * 
 */
public class UniqueFilter extends TokenFilter {

	private TokenStream input;
	private Collection<String> termTexts;
	CharTermAttribute termAtt;

	public UniqueFilter(TokenStream input) {
		super(input);
		this.input = input;
		termTexts = new HashSet<String>();
		termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			String termText = termAtt.toString();
			
			while (termTexts.contains(termText)) {
				if (input.incrementToken())
					termText = termAtt.toString();
				else
					termText = null;
			}

			if (termText == null)
				return false;

			termTexts.add(termText);

			termAtt.setEmpty();
			termAtt.append(termText);
			
			return true;
		}
		return false;
	}

	@Override
	public void reset() throws IOException {
		input.reset();
		termTexts.clear();
	}

}
