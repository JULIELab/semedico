package de.julielab.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.google.common.collect.Multimap;

import javassist.expr.NewArray;

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

	// TODO working right here
	public Symbol getNextToken() throws IOException {
		// returning token from last run(s)
		if (!returnQueue.isEmpty())
			return returnQueue.poll();

		// otherwise filling queue
		Symbol newToken = dumbLexer.getNextToken();
		while (newToken != null) {
			switch (newToken.sym) {
			// combining text
			case ALPHANUM:
			case APOSTROPHE:
			case NUM:
			case CJ:
				intermediateQueue.add(newToken);
				break;
			default:
				// a non-text token was found and is returned
				if (intermediateQueue.isEmpty())
					return newToken;
				// text tokens were found before and are (perhaps) combined,
				// newToken is returned
				else {
					returnQueue.addAll(combineSymbols());
					return newToken;
				}
			}
			newToken = dumbLexer.getNextToken();
		}
		if (!intermediateQueue.isEmpty()) {
			returnQueue.addAll(combineSymbols());
			if (!returnQueue.isEmpty())
				return returnQueue.poll();
		}
		System.out.println("foo!");
		return null;
	}

	private Collection<? extends Symbol> combineSymbols() throws IOException {
		System.out.println("!");
		//TODO: what string instead of id ? remember to inject OR symbols
		Multimap<String, IFacetTerm> combination = queryDisambiguationService.disambiguateSymbols("id", intermediateQueue.toArray(new Symbol[intermediateQueue.size()]));
		System.out.println("!!");
		System.out.println(combination);
		return null; //combination;
	}
}
