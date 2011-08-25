package de.julielab.lucene;

import java.io.StringReader;

import de.julielab.lucene.Node.NodeType;
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

	private CombiningLexer lexer;

	
	/**
	 * A recursive top-down left-right parser. 
	 * Hard wired grammar, rather robust (keeps parsing if the input is malformed).
	 * @param lexer The lexer to use.
	 */
	public Parser(CombiningLexer lexer) {
		this.lexer = lexer;
	}
	
	/**
	 * A recursive top-down left-right parser. 
	 * Hard wired grammar, rather robust (keeps parsing if the input is malformed).
	 * Uses an default lexer.
	 * @param toParse Input for the parser.
	 */
	public Parser(String toParse){
		lexer = new CombiningLexer(new StringReader(toParse));
	}
	
	
	/**
	 * Parses the input from the lexer.
	 * @return The root of the parse tree.
	 * @throws Exception If the input could not be parsed.
	 */
	public ParseTree parse() throws Exception {
		ParseErrors status = new ParseErrors();
		Node root = recursiveParse(status);
		return new ParseTree(root, status);
	}

	/**
	 * The real parsing
	 * @param status 
	 * @return A node of the parse tree. Will return the root if called externally.
	 * @throws Exception If the input could not be parsed. Should not happen unless the grammar is changed.
	 */
	private Node recursiveParse(ParseErrors status) throws Exception {
		if(status == null)
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
				if (node.getLeftChild() == null)
					node.setLeftChild(new Node((String) token.value));
				else
				// implicit AND
				if (node.getRightChild() == null) {
					if (node.getType() != NodeType.OR)
						node.setType(NodeType.AND);
					node.setRightChild(new Node((String) token.value));
				} else
					node.setRightChild(new Node(NodeType.AND, node
							.getRightChild(), new Node((String) token.value)));
				break;
				
			/*** operators ***/
			// the last wins!
			case AND:
				if (node.getRightChild() != null)
					node = new Node(NodeType.AND, node, null);
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
					node = new Node(NodeType.OR, node, null);
				else{
					if(node.getType() == NodeType.OR)
						status.incIgnoredORs();
					if(node.getType() == NodeType.AND)
						status.incIgnoredANDs();
					node.setType(NodeType.OR);
				}
				break;
				
			// negation
			case NOT:
				Node notNode = new Node(NodeType.NOT, null, null);
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
					node.setRightChild(new Node(NodeType.AND, node
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
