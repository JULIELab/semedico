/** 
 * QueryTokenizerTest.java
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
 * Creation date: 01.08.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

import de.julielab.lucene.QueryTokenizer;

public class QueryTokenizerTest {

	private static final String SIMPLE_QUERY = "schmidt, ab";
	private static final String QUERY = "term1 \"term2 term3\" term4";
	private static final String AUTHOR_QUERY = "term1 author, a t-erm3 author,b term5 author c ";

	@Test
	public void testNext() throws Exception {
		Tokenizer queryTokenizer = new QueryTokenizer(new StringReader(QUERY));
		CharTermAttribute termAtt = (CharTermAttribute) queryTokenizer
				.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAtt = (TypeAttribute) queryTokenizer
				.addAttribute(TypeAttribute.class);

		queryTokenizer.incrementToken();
		assertEquals("term1", termAtt.toString());

		queryTokenizer.incrementToken();
		assertEquals("term2 term3", termAtt.toString());
		assertEquals("<PHRASE>", typeAtt.type());

		queryTokenizer.incrementToken();
		assertEquals("term4", termAtt.toString());

	}

	@Test
	public void testNextWithSimpleQuery() throws Exception {
		Tokenizer queryTokenizer = new QueryTokenizer(new StringReader(
				SIMPLE_QUERY));
		CharTermAttribute termAtt = (CharTermAttribute) queryTokenizer
				.addAttribute(CharTermAttribute.class);

		queryTokenizer.incrementToken();
		assertEquals("schmidt", termAtt.toString());

		queryTokenizer.incrementToken();
		assertEquals("ab", termAtt.toString());

		assertFalse(queryTokenizer.incrementToken());
	}

	@Test
	public void testNextWithAuthor() throws Exception {
		Tokenizer queryTokenizer = new QueryTokenizer(new StringReader(
				AUTHOR_QUERY));
		CharTermAttribute termAtt = (CharTermAttribute) queryTokenizer
				.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAtt = (TypeAttribute) queryTokenizer
				.addAttribute(TypeAttribute.class);

		queryTokenizer.incrementToken();
		assertEquals("term1", termAtt.toString());

		queryTokenizer.incrementToken();
		assertEquals("author, a", termAtt.toString());
		assertEquals("<AUTHORS>", typeAtt.type());

		queryTokenizer.incrementToken();
		assertEquals("t-erm3", termAtt.toString());

		queryTokenizer.incrementToken();
		assertEquals("author,b", termAtt.toString());
		assertEquals("<AUTHORS>", typeAtt.type());

		queryTokenizer.incrementToken();
		assertEquals("term5", termAtt.toString());

		queryTokenizer.incrementToken();
		assertEquals("author c", termAtt.toString());
		assertEquals("<AUTHORS>", typeAtt.type());
	}

}
