package de.julielab.parsing;

import java.io.IOException;
import java.io.StringReader;

import java_cup.runtime.Symbol;

/**
 * A recursive top-down left-right parser. Hard wired grammar, rather robust
 * (keeps parsing if the input is malformed). Utilizes "smart" nodes. These 
 * can check for free space in all descendants and add new nodes in the proper
 * place (for a LR bottom-up parse!).
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
	private static final boolean DEFAULT_COMBINING = false;

	private Lexer lexer;

	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed).
	 * 
	 * @param lexer
	 *            The lexer to use.
	 */
	public Parser(QueryTokenizerImpl lexer) {
		this.lexer = new Lexer(lexer);
	}

	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed). Uses an default lexer.
	 * 
	 * @param toParse
	 *            Input for the parser.
	 * @param combine
	 * 			  True if tokens shall be combined to terms 
	 */
	public Parser(String toParse, boolean combine) {
		lexer = new Lexer(new StringReader(toParse), combine);
	}
	
	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed). Uses an default lexer.
	 * 
	 * @param toParse
	 *            Input for the parser.
	 */
	public Parser(String toParse) {
		this(toParse, DEFAULT_COMBINING);
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
					root = new TextNode(token.value);
					break;
				case PHRASE:
					root = new TextNode(token.value, true);
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
					((BranchNode) root).add(new TextNode(token.value));
					break;
				case PHRASE:
					((BranchNode) root).add(new TextNode(token.value, true));
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
					((BranchNode) root).add(new BinaryNode(token.value));
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
				case CJ: // implicit AND
					root = new BinaryNode(BinaryNode.AND, root, new  TextNode(token.value));
					break;
				case PHRASE: // implicit AND
					root = new BinaryNode(BinaryNode.AND, root, new  TextNode(token.value, true));
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
					root = new BinaryNode(token.value, root, null);
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
	
	/**
	 * Wrapper class for different Lexer types.
	 * @author hellrich
	 *
	 */
	private class Lexer{
		private QueryTokenizerImpl simpleLexer;
		private CombiningLexer combiningLexer;
		private final boolean combine;
		
		/**
		 * Constructor for wrapper around a simple lexer.
		 * @param lexer 
		 * 				The wrapped lexer.
		 */
		public Lexer(QueryTokenizerImpl lexer) {
			this.simpleLexer = lexer;
			this.combine = false;
		}
		
		/**
		 * Constructor for wrapper around a lexer
		 * which tries to combine text tokens into terms.
		 * @param lexer 
		 * 				The wrapped lexer.
		 */
		public Lexer(CombiningLexer lexer) {
			this.combiningLexer = lexer;
			this.combine = true;
		}
		
		/**
		 * Constructor for a Lexer with optional support for token combining.
		 * @param stringReader
		 *					Reader for the text to parse.
		 * @param combine 
		 * 					True if text tokens shall be combined into terms.
		 */
		public Lexer(StringReader stringReader, boolean combine) {
			if(combine)
				this.combiningLexer = new CombiningLexer(stringReader);
			else
				this.simpleLexer = new QueryTokenizerImpl(stringReader);
			this.combine = combine;
		}

		/**
		 * @return 
		 * 			The next token of the parsed text.
		 * @throws IOException
		 * 					If problems occur during tokenization.
		 */
		public Symbol getNextToken() throws IOException{
			if(combine)
				return combiningLexer.getNextToken(); 
			else
				return simpleLexer.getNextToken();
			
		}
	}
}
