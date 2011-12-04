package de.julielab.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import de.julielab.lucene.Node.NodeType;
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

	private WrappedLexer lexer;

	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed).
	 * 
	 * @param lexer
	 *            The lexer to use.
	 */
	public Parser(CombiningLexer lexer) {
		this.lexer = new WrappedLexer(lexer);
	}

	/**
	 * A recursive top-down left-right parser. Hard wired grammar, rather robust
	 * (keeps parsing if the input is malformed). Uses an default lexer.
	 * 
	 * @param toParse
	 *            Input for the parser.
	 */
	public Parser(String toParse) {
		this.lexer = new WrappedLexer(new CombiningLexer(new StringReader(
				toParse)));
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
		Node node = new Node(NodeType.ROOT, null, null);

		Symbol token = lexer.getNextToken();

		while (token != null) { // End of file signal
			boolean backUp = false;
			switch (token.sym) {
			/**** texts ****/
			case ALPHANUM:
			case APOSTROPHE:
			case NUM:
			case CJ:
			case PHRASE:
				if (node.getType() == NodeType.NOT) {
					if (node.getLeftChild() == null
							|| node.getLeftChild().getType() == NodeType.ROOT) {
						node.setLeftChild(new Node((String) token.value));
					}
					// implicit AND
					else if (node.getLeftChild().getType() == NodeType.TEXT) {
						node.setLeftChild(new Node(NodeType.AND, node
								.getLeftChild(), new Node((String) token.value)));
					}
				} else {
					if (node.getLeftChild() == null)
						node.setLeftChild(new Node((String) token.value));

					else if (node.getRightChild() == null
							|| node.getRightChild().getType() == NodeType.ROOT) {
						node.setRightChild(new Node((String) token.value));
					} else
						// implicit AND
						node.setRightChild(new Node(NodeType.AND, node
								.getRightChild(),
								new Node((String) token.value)));
				}
				break;

			/*** operators ***/
			// the last wins!
			case AND:
				if (node.getRightChild() != null)
					node = new Node(NodeType.AND, node, null);
				else {
					if (node.getType() == NodeType.OR)
						status.incIgnoredORs();
					if (node.getType() == NodeType.AND)
						status.incIgnoredANDs();
					node.setType(NodeType.AND);
				}
				break;

			case OR:
				if (node.getRightChild() != null)
					node = new Node(NodeType.OR, node, null);
				else {
					if (node.getType() == NodeType.OR)
						status.incIgnoredORs();
					if (node.getType() == NodeType.AND)
						status.incIgnoredANDs();
					node.setType(NodeType.OR);
				}
				break;

			// negation
			case NOT:
				if (node.getLeftChild() == null)
					node.setLeftChild(new Node(NodeType.NOT, null));
				else if (node.getRightChild() == null)
					node.setRightChild( new Node(NodeType.NOT, null));
				else
					node.setRightChild(new Node(NodeType.NOT, node.getRightChild()));
				break;

			// relations, having type and text (specific kind of relation)
			case RELATION:
				if (node.getRightChild() != null)
					node = new Node(NodeType.RELATION, (String) token.value,
							node, null);
				else {
					if (node.getType() == NodeType.OR)
						status.incIgnoredORs();
					if (node.getType() == NodeType.AND)
						status.incIgnoredANDs();
					node.setType(NodeType.RELATION);
					node.setText((String) token.value);
				}
				break;

			/** parentheses **/
			case LEFT_PARENTHESIS:
				status.incLeftPar();
				if (node.getLeftChild() == null)
					node.setLeftChild(recursiveParse(status));
				else if (node.getRightChild() == null) {
					if (node.getType() != NodeType.OR)
						node.setType(NodeType.AND); // implicit AND
					node.setRightChild(recursiveParse(status));
				} else{
					System.out.println(node+" "+token.value);

					// implicit AND
					node.setRightChild(new Node(NodeType.AND, node
							.getRightChild(), recursiveParse(status)));
				}
				break;
			case RIGHT_PARENTHESIS:
				status.incRightPar();
				backUp = true; // back up
			}
			// leaving the current level of recursion
			if (backUp)
				break;
			token = lexer.getNextToken();
		}

		return node;
	}

	/**
	 * Used to enable push-back for the tokens
	 * 
	 * @author johannes
	 * 
	 */
	private class WrappedLexer {
		Queue<Symbol> pushedBack = new LinkedList<Symbol>();
		CombiningLexer lexer;

		/**
		 * A wrapper which allows you to push back Symbols. Useful to insert
		 * implict AND nodes.
		 * 
		 * @param lexer
		 *            - Lexer to wrap
		 */
		public WrappedLexer(CombiningLexer lexer) {
			this.lexer = lexer;
		}

		/**
		 * 
		 * @return The correct Symbol out of the original order
		 * @throws IOException
		 */
		public Symbol getNextToken() throws IOException {
			if (pushedBack.isEmpty())
				return lexer.getNextToken();
			return pushedBack.remove();
		}

		/**
		 * Places a Symbol in a queue for later retrieval
		 * 
		 * @param token
		 *            - Symbol to push back.
		 */
		public void pushBack(Symbol token) {
			pushedBack.add(token);
		}
	}

}

// Symbol token = lexer.getNextToken();
// while (token != null) { // End of file signal
// boolean backUp = false;
// switch (token.sym) {
// /**** texts ****/
// case ALPHANUM:
// case APOSTROPHE:
// case NUM:
// case CJ:
// case PHRASE:
// if(node.getType() == NodeType.NOT)
// node = new Node(type, left, right)
// if (node.getLeftChild() == null)
// node.setLeftChild(new Node((String) token.value));
// //TODO error?
// // implicit AND
// else if (node.getRightChild() == null || node.getRightChild().getType() ==
// NodeType.ROOT) {
// if (node.getType() == NodeType.TEXT)
// node.setType(NodeType.AND);
// node.setRightChild(new Node((String) token.value));
// } else
// node.setRightChild(new Node(NodeType.AND, node
// .getRightChild(), new Node((String) token.value)));
// break;
//
// /*** operators ***/
// // the last wins!
// case AND:
// if (node.getRightChild() != null)
// node = new Node(NodeType.AND, node, null);
// else{
// if(node.getType() == NodeType.OR)
// status.incIgnoredORs();
// if(node.getType() == NodeType.AND)
// status.incIgnoredANDs();
// node.setType(NodeType.AND);
// }
// break;
//
// case OR:
// if (node.getRightChild() != null)
// node = new Node(NodeType.OR, node, null);
// else{
// if(node.getType() == NodeType.OR)
// status.incIgnoredORs();
// if(node.getType() == NodeType.AND)
// status.incIgnoredANDs();
// node.setType(NodeType.OR);
// }
// break;
//
// // negation
// case NOT:
// Node notNode = new Node(NodeType.NOT, null);
// node.setRightChild(notNode);
// node = notNode;
// break;
//
// // relations, having type and text (specific kind of relation)
// case RELATION:
// if (node.getRightChild() != null)
// node = new Node(NodeType.RELATION, (String) token.value, node, null);
// else{
// if(node.getType() == NodeType.OR)
// status.incIgnoredORs();
// if(node.getType() == NodeType.AND)
// status.incIgnoredANDs();
// node.setType(NodeType.RELATION);
// node.setText((String) token.value);
// }
// break;
//
// /** parentheses **/
// case LEFT_PARENTHESIS:
// status.incLeftPar();
// if (node.getLeftChild() == null)
// node.setLeftChild(recursiveParse(status));
// else if (node.getRightChild() == null) {
// if (node.getType() != NodeType.OR)
// node.setType(NodeType.AND); // implicit AND
// node.setRightChild(recursiveParse(status));
// } else
// // implicit AND
// node.setRightChild(new Node(NodeType.AND, node
// .getRightChild(), recursiveParse(status)));
// break;
// case RIGHT_PARENTHESIS:
// status.incRightPar();
// backUp = true; // back up
// }
// // leaving the current level of recursion
// if (backUp)
// break;
// token = lexer.getNextToken();
// }
//
// return node;
// }