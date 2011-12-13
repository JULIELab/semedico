package de.julielab.parsing;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.query.QueryDisambiguationService;

import java_cup.runtime.Symbol;

/**
 * The CombiningLexer wraps around the normal lexer and tries to combine tokens.
 * Adjunct text tokens are presented to the lingpipe chunker; the tokens are combined if
 * a term is found. 
 * @author hellrich
 *
 */
public class CombiningLexer{
	private static final int ALPHANUM = QueryTokenizer.ALPHANUM;
	private static final int APOSTROPHE = QueryTokenizer.APOSTROPHE;
	private static final int NUM = QueryTokenizer.NUM;
	private static final int CJ = QueryTokenizer.CJ;
	public static final int TEXT = QueryDisambiguationService.TEXT;
	public static final int MAPPED_TEXT = QueryDisambiguationService.MAPPED_TEXT;

	private QueryTokenizerImpl simpleLexer;
	private Queue<Symbol> returnQueue = new LinkedList<Symbol>();
	private Queue<Symbol> intermediateQueue = new LinkedList<Symbol>();
	private QueryDisambiguationService queryDisambiguationService;

	/**
	 * A lexer which combines text tokens into terms.
	 * @param stringReader 
	 * 					Reader for the text to tokenize.
	 */
	public CombiningLexer(StringReader stringReader) {
		this(new QueryTokenizerImpl(stringReader));
	}
	
	
	/**
	 * A lexer which combines text tokens into terms.
	 * @param lexer 
	 * 				A non combining lexer.
	 */
	public CombiningLexer(QueryTokenizerImpl lexer) {
		simpleLexer = lexer;
	}
	
	/**
	 * @return The next (combined) token
	 * @throws IOException 
	 * 					If problems occur during tokenization
	 */
	public Symbol getNextToken() throws IOException {
		// returning token from last run(s)
		if (!returnQueue.isEmpty())
			return returnQueue.poll();

		// otherwise filling queue
		Symbol newToken = simpleLexer.getNextToken();
		while (newToken != null) {
			switch (newToken.sym) {
			// combining text tokens
			case ALPHANUM:
			case APOSTROPHE:
			case NUM:
			case CJ:
				intermediateQueue.add(newToken);
				break;
			// a non-text (or phrase) token was found
			default:
				// text tokens were found before and are (perhaps) combined
				if (!intermediateQueue.isEmpty())
					combineSymbols();
				returnQueue.add(newToken);
				return getNextToken();
			}
			newToken = simpleLexer.getNextToken();
		}
		if (!intermediateQueue.isEmpty()) {
			combineSymbols();
		return getNextToken();
		}
		return null;	//eof
	}

	/**
	 * Idea: combine symbols in intermediateQueue into a string and try to disambiguate it.
	 * If nothing is found -> return as single tokens
	 * If matches are found -> return disambiguated queries, concatenated with OR
	 */
	private void combineSymbols() throws IOException {
		//TODO: how do I get a real disambiguation service without semedico 
		//running? tried initiating a ioc registry in unit test, didn't work
		//TODO: what string instead of id ?, remember to inject OR symbols
		//Multimap<String, IFacetTerm> combination = queryDisambiguationService.disambiguateSymbols("id", intermediateQueue.toArray(new Symbol[intermediateQueue.size()]));
		StringBuilder sb = new StringBuilder();
		for(Symbol text : intermediateQueue)
			sb.append((String)text.value).append(" ");
		String toCheck = sb.toString().trim();
		queryDisambiguationService.disambiguateQuery(query, id)
		//TODO: real implementation^^
		if(toCheck.equalsIgnoreCase("IL 2"))
			returnQueue.add(new Symbol(QueryTokenizer.ALPHANUM, "IL2"));
		else
			returnQueue.addAll(intermediateQueue);
		intermediateQueue.clear();
	}
}
