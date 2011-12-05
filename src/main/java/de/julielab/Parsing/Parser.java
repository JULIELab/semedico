package de.julielab.Parsing;

import java.io.StringReader;

import de.julielab.Parsing.NonTerminalNode.NodeType;
import java_cup.runtime.Symbol;

/**
 * A recursive top-down left-right parser. 
 * Hard wired grammar, rather robust (keeps parsing if the input is malformed).
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
	private static final int NOT  = QueryTokenizer.NOT;
	private static final int RELATION = QueryTokenizer.RELATION;

	private QueryTokenizerImpl lexer;

	
	/**
	 * A recursive top-down left-right parser. 
	 * Hard wired grammar, rather robust (keeps parsing if the input is malformed).
	 * @param lexer The lexer to use.
	 */
	public Parser(QueryTokenizerImpl lexer) {
		this.lexer = lexer;
	}
	
	/**
	 * A recursive top-down left-right parser. 
	 * Hard wired grammar, rather robust (keeps parsing if the input is malformed).
	 * Uses an default lexer.
	 * @param toParse Input for the parser.
	 */
	public Parser(String toParse){
		lexer = new QueryTokenizerImpl(new StringReader(toParse));
	}
	
	
	/**
	 * Parses the input from the lexer.
	 * @return The root of the parse tree.
	 * @throws Exception If the input could not be parsed.
	 */
	public ParseTree parse() throws Exception {
		ParseErrors status = new ParseErrors();
		NonTerminalNode root = recursiveParse(status);
		return new ParseTree(root, status);
	}

	/**
	 * The real parsing
	 * @param status 
	 * @return A node of the parse tree. Will return the root if called externally.
	 * @throws Exception If the input could not be parsed. Should not happen unless the grammar is changed.
	 */
	//TODO: The lexer could be changed to generate Nodes, Parser would only connect them. Requires reworking of other classes which use the lexer
	private NonTerminalNode recursiveParse(ParseErrors status) throws Exception {
		if(status == null)
			throw new IllegalArgumentException("Got no ParseStatus Object!");
		NonTerminalNode node = new NonTerminalNode(NodeType.ROOT, null, null);

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
					if (node.getLeftChild() == null) {
						node.setLeftChild(new NonTerminalNode((String) token.value));
					}
					// implicit AND
					else if (node.getLeftChild().getType() == NodeType.TEXT) {
						node.setLeftChild(new NonTerminalNode(NodeType.AND, node
								.getLeftChild(), new NonTerminalNode((String) token.value)));
					}
				} else {
					if (node.getLeftChild() == null)
						node.setLeftChild(new NonTerminalNode((String) token.value));
					// implicit AND
					else if (node.getRightChild() == null) {
						if (node.getType() != NodeType.OR)
							node.setType(NodeType.AND);
						node.setRightChild(new NonTerminalNode((String) token.value));
					} else
						node.setRightChild(new NonTerminalNode(NodeType.AND, node
								.getRightChild(), new NonTerminalNode((String) token.value)));
				}
				break;
				
			/*** operators ***/
			// the last wins!
			case AND:
				if (node.getRightChild() != null)
					node = new NonTerminalNode(NodeType.AND, node, null);
				else{
					if(node.getType() == NodeType.OR)
						status.incIgnoredORs();
					if(node.getType() == NodeType.AND)
						status.incIgnoredANDs();
					node.setType(NodeType.AND);
				}
				break;
			case OR:
				if (node.getRightChild() != null)
					node = new NonTerminalNode(NodeType.OR, node, null);
				else{
					if(node.getType() == NodeType.OR)
						status.incIgnoredORs();
					if(node.getType() == NodeType.AND)
						status.incIgnoredANDs();
					node.setType(NodeType.OR);
				}
				break;
				
			// relations, having type and text (specific kind of relation)
			case RELATION:
				if (node.getRightChild() != null)
					node = new NonTerminalNode(NodeType.RELATION, (String) token.value,
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
				
			// negation
			case NOT:
				NonTerminalNode notNode = new NonTerminalNode(NodeType.NOT, null);
				node.setRightChild(notNode);
				node = notNode;
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
				} else
					// implicit AND
					node.setRightChild(new NonTerminalNode(NodeType.AND, node
							.getRightChild(), recursiveParse(status)));
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
}

		
