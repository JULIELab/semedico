package de.julielab.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.query.QueryDisambiguationService;

import java_cup.runtime.Symbol;

public class CombiningLexer {
	private static final int ALPHANUM = QueryTokenizer.ALPHANUM;
	private static final int APOSTROPHE = QueryTokenizer.APOSTROPHE;
	private static final int NUM = QueryTokenizer.NUM;
	private static final int CJ = QueryTokenizer.CJ;

	private QueryTokenizerImpl dumbLexer;
	private Queue<Symbol> returnQueue = new LinkedList<Symbol>();
	private Queue<Symbol> intermediateQueue = new LinkedList<Symbol>();
	private QueryDisambiguationService queryDisambiguationService;

	public CombiningLexer(StringReader stringReader) {
		dumbLexer = new QueryTokenizerImpl(stringReader);
	}
	
	public Symbol getNextToken() throws IOException {
		// returning token from last run(s)
		if (!returnQueue.isEmpty())
			return returnQueue.poll();

		// otherwise filling queue
		Symbol newToken = dumbLexer.getNextToken();
		while (newToken != null) {
			switch (newToken.sym) {
			// combining text tokens
			case ALPHANUM:
			case APOSTROPHE:
			case NUM:
			case CJ:
				intermediateQueue.add(newToken);
				break;
			// a non-text token was found
			default:
				// text tokens were found before and are (perhaps) combined
				if (!intermediateQueue.isEmpty())
				//TODO	returnQueue.addAll(combineSymbols());
					returnQueue.add(intermediateQueue.poll());
				// newToken is returned
				return newToken;
			}
			newToken = dumbLexer.getNextToken();
		}
		if (!intermediateQueue.isEmpty()) {
			//TODO	returnQueue.addAll(combineSymbols());
			returnQueue.add(intermediateQueue.poll());
			if (!returnQueue.isEmpty())
				return returnQueue.poll();
		}
		return null;
	}

	/**
	 * Idea: combine symbols in intermediateQueue into a string and try to disambiguate it.
	 * If nothing is found -> return as single tokens
	 * If matches are found -> return disambiguated queries, concatenated with OR
	 */
	private Collection<? extends Symbol> combineSymbols() throws IOException {
		//TODO: how do I get a real disambiguation service without semedico 
		//running? tried initiating a ioc registry in unit test, didn't work
		//TODO: what string instead of id ?, remember to inject OR symbols
		Multimap<String, IFacetTerm> combination = queryDisambiguationService.disambiguateSymbols("id", intermediateQueue.toArray(new Symbol[intermediateQueue.size()]));
	
		intermediateQueue.clear();
		return null; //combination;
	}
}
