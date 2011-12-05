/** 
 * QueryAnalyzer.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: landefeld
 * 
 * Current version: 0.1 	
 * Since version:   0.1
 *
 * Creation date: 01.04.2008 
 * 
 * A analyzer which supports different, consecutive operations which can be configured. 
 **/

package de.julielab.Parsing;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import de.julielab.lucene.UniqueFilter;

/**
 *  A analyzer which supports different, consecutive operations which can be configured.
 */

public class QueryAnalyzer extends Analyzer{

	public final static int OPERATION_TOKENIZE = 0;
	public final static int OPERATION_LOWERCASE = 1;
	public final static int OPERATION_REMOVE_STOPWORDS = 2;
	public final static int OPERATION_STEMMING = 3;
	public final static int OPERATION_REMOVE_DOUBLETS = 4;
	private Set<String> stopwords;
	private String snowballStemmerName;
	
	private int currentOperation;
	public QueryAnalyzer(String[] stopwords, String snowballStemmerName) {
		currentOperation = OPERATION_STEMMING;
		this.stopwords = new HashSet<String>(stopwords.length);
		for (String sw : stopwords)
			this.stopwords.add(sw);
		this.snowballStemmerName = snowballStemmerName;
	}
	
	@Override
	public TokenStream tokenStream(String field, Reader reader) {
		Tokenizer tokenizer = new QueryTokenizer(reader);
		if( currentOperation == OPERATION_TOKENIZE )
			return tokenizer;
		TokenFilter lowercaseFilter = new LowerCaseFilter(tokenizer);
		if( currentOperation == OPERATION_LOWERCASE )
			return lowercaseFilter;
		TokenFilter stopFilter = new StopFilter(Version.LUCENE_23, lowercaseFilter, stopwords);
		if( currentOperation == OPERATION_REMOVE_STOPWORDS )
			return stopFilter;
					
		TokenFilter snowballFilter = new SnowballFilter(stopFilter, snowballStemmerName);
		if( currentOperation == OPERATION_STEMMING )
			return snowballFilter;

		TokenFilter uniqueFilter = new UniqueFilter(snowballFilter);
		return uniqueFilter;
	}

	public String analyze(String string, String tokenDelimiter) throws IOException{
		tokenDelimiter = tokenDelimiter == null ? " " : tokenDelimiter;
		TokenStream tokenStream = tokenStream(null, new StringReader(string));
		CharTermAttribute termAtt = (CharTermAttribute) tokenStream.addAttribute(CharTermAttribute.class);
		
		// TODO If this class is really needed, this should be replaced by a StringBuilder.
		String result = new String();
		while( tokenStream.incrementToken() ){
			result = result.concat(termAtt.toString());
			result = result.concat(tokenDelimiter);
		}
		
		return result.trim();
	}
	
	public int getCurrentOperation() {
		return currentOperation;
	}

	public void setCurrentOperation(int currentOperation) {
		this.currentOperation = currentOperation;
	}
	
}