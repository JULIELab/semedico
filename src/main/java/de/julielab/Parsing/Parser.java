package de.julielab.Parsing;

import java.io.StringReader;

import java_cup.runtime.Symbol;

/**
 * A recursive top-down left-right parser. Hard wired grammar, rather robust
 * (keeps parsing if the input is malformed).
 * 
 * @author hellrich
 * 
 */
public class Parser {

	private static final int ALPHANUM = QueryTokenizer.ALPHANUM;
	private static final int APOSTROPHE = QueryTokenizer.APOSTROPHE;
	private static final int NUM = QueryTokenizer.NUM;
	private static final int CJ = QueryTokenizer.CJ;
	private static final int PHRASE = QueryTokenizer.PHRASE;
	private static final int LEFT_PARENTHESIS = QueryTokenizer.LEFT_PARENTHESIS;
	private static final int RIGHT_PARENTHESIS = QueryTokenizer.RIGHT_PARENTHESIS;
	private static final int AND = QueryTokenizer.AND;
	private static final int OR = QueryTokenizer.OR;
	private static final int NOT = QueryTokenizer.NOT;
	private static final int RELATION = QueryTokenizer.RELATION;

	private QueryTokenizerImpl lexer;

	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed).
	 * 
	 * @param lexer
	 *            The lexer to use.
	 */
	public Parser(QueryTokenizerImpl lexer) {
		this.lexer = lexer;
	}

	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed). Uses an default lexer.
	 * 
	 * @param toParse
	 *            Input for the parser.
	 */
	public Parser(String toParse) {
		lexer = new QueryTokenizerImpl(new StringReader(toParse));
	}

	/**
	 * Parses the input from the lexer.
	 * 
	 * @return The root of the parse tree.
	 * @throws Exception
	 *             If the input could not be parsed.
	 */
	public ParseTree parse() throws Exception {
		ParseErrors status = new ParseErrors();
		Node root = recursiveParse(status);
		return new ParseTree(root, status);
	}

	/**
	 * The real parsing
	 * 
	 * @param status
	 * @return A node of the parse tree. Will return the root if called
	 *         externally.
	 * @throws Exception
	 *             If the input could not be parsed. Should not happen unless
	 *             the grammar is changed.
	 */
	private Node recursiveParse(ParseErrors status) throws Exception {
		if (status == null)
			throw new IllegalArgumentException("Got no ParseStatus Object!");
		Node root = null;
		Symbol token = lexer.getNextToken();
		while (token != null && token.sym != RIGHT_PARENTHESIS) { // null = eof
			if (token.sym == LEFT_PARENTHESIS)
				root = recursiveParse(status);
			// 3 states corresponding to current root, here: root undefined
			else if (root == null)
				switch (token.sym) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
					root = new TerminalNode((String) token.value);
					break;
				case AND:
					status.incIgnoredANDs();
					break;
				case OR:
					status.incIgnoredORs();
					break;
				case NOT:
				case RELATION:
					break;
				}
			// root open for children
			else if (root.getClass() == NonTerminalNode.class
					&& ((NonTerminalNode) root).canTakeChild())
				switch (token.sym) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
					((NonTerminalNode) root).addChild(new TerminalNode(
							(String) token.value));
					break;
				case AND:
					((NonTerminalNode) root).addChild(new BinaryNode(
							BinaryNode.AND));
					break;
				case OR:
					((NonTerminalNode) root).addChild(new BinaryNode(
							BinaryNode.OR));
					break;
				case NOT:
					((NonTerminalNode) root).addChild(new NotNode());
					break;
				case RELATION:
					((NonTerminalNode) root).addChild(new BinaryNode(
							(String) token.value));
					break;
				}
			// root can take no (more) child -> implicit AND
			//TODO 
			else{
				BinaryNode implicitAnd = new
				switch (token.sym) {
				/**** texts ****/
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
					root = new
				case AND:
				case OR:
				case NOT:
				case RELATION:
				}
			}

			token = lexer.getNextToken();
		}
		return root;
	}
}
