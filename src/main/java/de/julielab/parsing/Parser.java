package de.julielab.parsing;

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
	 * The real parsing, working LR bottom up. 
	 * Uses implicit states given by the ability of the current root 
	 * (and its children) to take another child.
	 * Recursion is used for parentheses, seems more natural than a stack.
	 * 
	 * @param status Used to reccord errors
	 * @return The root of a parse (sub)tree.
	 */
	private Node recursiveParse(ParseErrors status) throws Exception {
		if (status == null)
			throw new IllegalArgumentException("Got no ParseStatus Object!");
		Node root = null;	//root of the current subtree
		Symbol token = lexer.getNextToken();
		while (token != null && token.sym != RIGHT_PARENTHESIS) { // null = eof
			// 3 states corresponding to current root, here: root undefined
			if (root == null)
				switch (token.sym) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
					root = new TextNode((String) token.value);
					break;
				case AND:
					status.incIgnoredANDs();
					break;
				case OR:
					status.incIgnoredORs();
					break;
				case NOT:
					root = new NotNode();
					break;
				case RELATION:
					break;
				case LEFT_PARENTHESIS:
						root = recursiveParse(status);
				}
			// root open for children
			else if (root.canTakeChild())
				switch (token.sym) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
					((BranchNode) root).add(new TextNode(
							(String) token.value));
					break;
				case AND:
					((BranchNode) root).add(new BinaryNode(
							BinaryNode.AND));
					break;
				case OR:
					((BranchNode) root).add(new BinaryNode(
							BinaryNode.OR));
					break;
				case NOT:
					((BranchNode) root).add(new NotNode());
					break;
				case RELATION:
					((BranchNode) root).add(new BinaryNode(
							(String) token.value));
					break;
				case LEFT_PARENTHESIS:
					((BranchNode) root).add(recursiveParse(status));
				}
			// root can't take a child
			else{
				switch (token.sym) {
				/**** texts ****/
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE: // implicit AND
					root = new BinaryNode(BinaryNode.AND, root, new  TextNode((String) token.value));
					break;
				case AND:
					root = new BinaryNode(BinaryNode.AND, root, null);
					break;
				case OR:
					root = new BinaryNode(BinaryNode.OR, root, null);
					break;
				case NOT: // implicit AND
					root = new BinaryNode(BinaryNode.AND, root, new NotNode());
					break;
				case RELATION:
					root = new BinaryNode((String) token.value, root, null);
					break;
				case LEFT_PARENTHESIS: // implicit AND
					root = new BinaryNode(BinaryNode.AND, root, recursiveParse(status));
					break;
				}
			}
			token = lexer.getNextToken();
		}
		return root;
	}
}
