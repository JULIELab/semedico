package de.julielab.semedico.core.services.query;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.ALPHANUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.ALPHANUM_EMBEDDED_PAR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.AND_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.APOSTROPHE;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.BINARY_EVENT;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.CJ;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.DASH;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.LEFT_PARENTHESIS;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NOT_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.OR_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.PHRASE;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.RIGHT_PARENTHESIS;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.UNARY_EVENT;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;

import de.julielab.semedico.core.facetterms.Event;
import de.julielab.semedico.core.parsing.BinaryNode;
import de.julielab.semedico.core.parsing.BranchNode;
import de.julielab.semedico.core.parsing.EventNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.NotNode;
import de.julielab.semedico.core.parsing.ParseErrors;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class ParsingService implements IParsingService {

	// public static final List<String> likelihoods = Arrays.asList("negation
	// low investigation moderate high assertion"
	// .split(" "));
	// public static final List<String> likelihoodWilcard =
	// Lists.newArrayList("*?");

	public static final Map<Integer, Integer> operatorPrecedences;

	private Logger log;

	static {
		operatorPrecedences = new HashMap<>();
		operatorPrecedences.put(OR_OPERATOR, 0);
		operatorPrecedences.put(AND_OPERATOR, 1);
		// Events don't compete; conflicting cases are resolved by
		// QueryTokenAlignmentService
		operatorPrecedences.put(UNARY_EVENT, 2);
		operatorPrecedences.put(BINARY_EVENT, 2);
		operatorPrecedences.put(NOT_OPERATOR, 3);
	}

	public ParsingService(Logger log) {
		this.log = log;
	}

	@Override
	public ParseTree parse(List<QueryToken> tokens) throws Exception {
		Queue<QueryToken> tokenQueue = new LinkedList<QueryToken>(tokens);
		ParseErrors status = new ParseErrors();
		checkParenthesis(status, tokens);
		Node root = recursiveParse(status, tokenQueue, tokens);
		ParseTree tree = new ParseTree(root, status);
		tree.setQueryTokens(tokens);
		if (root != null)
			root.computeTreeHeight();
		return tree;
	}

	/**
	 * The parsing, working LR bottom up. Uses implicit states given by the
	 * ability of the current root (and its children) to take another child.
	 * Recursion is used for parentheses, seems more natural than a stack.
	 * 
	 * original parser implementation by hellrich
	 * 
	 * @param status
	 *            Used to record errors.
	 * @param tokens
	 * @return The root of a parse (sub)tree.
	 */
	private Node recursiveParse(ParseErrors status, Queue<QueryToken> tokenQueue, List<QueryToken> tokens)
			throws Exception {
		if (status == null)
			throw new IllegalArgumentException("Got no ParseStatus Object!");

		Node root = null;
		// int lastOperatorType = Integer.MIN_VALUE;
		QueryToken qt = tokenQueue.poll();
		boolean leftParenthesisJustPassed = false;
		while (qt != null && (qt.getType() != RIGHT_PARENTHESIS || status.hasParenthesisError())) {
			// 3 states corresponding to current root. Here: root undefined.
			if (root == null)
				switch (qt.getType()) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case PHRASE:
				case DASH:
				case QueryTokenizerImpl.ALPHANUM_EMBEDDED_PAR:
					// nodeType = determineNodeType(qt);
					TextNode textNode = new TextNode(qt.getOriginalValue(), qt);
					textNode.setTokenType(qt.getType());
					textNode.setTerms(qt.getTermList());
					textNode.setBeginOffset(qt.getBeginOffset());
					textNode.setEndOffset(qt.getEndOffset());
					root = textNode;
					break;
				case BINARY_EVENT:
				case UNARY_EVENT:
					EventNode event = new EventNode(qt.getOriginalValue(), qt.getTermList(),
							qt.getType() == BINARY_EVENT);
					event.setTokenType(qt.getType());
					event.setBeginOffset(qt.getBeginOffset());
					event.setEndOffset(qt.getEndOffset());
					// Set a default value; currently it is never changed. But
					// it could be overwritten if searchers have
					// special requirements in the future.
					// event.setLikelihoods(likelihoods);
					// event.setLikelihoods(likelihoodWilcard);
					root = event;
					break;
				case AND_OPERATOR:
					status.incIgnoredANDs();
					break;
				case OR_OPERATOR:
					status.incIgnoredORs();
					break;
				case NOT_OPERATOR:
					qt.setInputTokenType(TokenType.NOT);
					NotNode notNode = new NotNode(qt);
					notNode.setTokenType(qt.getType());
					notNode.setBeginOffset(qt.getBeginOffset());
					notNode.setEndOffset(qt.getEndOffset());
					root = notNode;
					break;
				case LEFT_PARENTHESIS:
					if (status.hasParenthesisError())
						break;
					qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
					root = recursiveParse(status, tokenQueue, tokens);
					leftParenthesisJustPassed = true;
					break;
				case RIGHT_PARENTHESIS:
					// do nothing, coming here means we have met a closing
					// parenthesis but the syntactic structure is broken and we
					// ignore parenthesis as logical expression elements
					break;
				default:
					throw new IllegalArgumentException("Unhandled QueryToken type: " + qt.getType());
				}
			// Root open for children.
			else if (root.subtreeCanTakeNode() && !leftParenthesisJustPassed)
				switch (qt.getType()) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
				case DASH:
				case QueryTokenizerImpl.ALPHANUM_EMBEDDED_PAR:
					NodeType nodeType;
					// nodeType = determineNodeType(qt);
					TextNode textNode = new TextNode(qt.getOriginalValue(), qt);
					textNode.setTokenType(qt.getType());
					textNode.setTerms(qt.getTermList());
					textNode.setBeginOffset(qt.getBeginOffset());
					textNode.setEndOffset(qt.getEndOffset());
					((BranchNode) root).add(textNode);
					break;
				case BINARY_EVENT:
				case UNARY_EVENT:
					EventNode event = new EventNode(qt.getOriginalValue(), qt.getTermList(),
							qt.getType() == BINARY_EVENT);
					event.setTokenType(qt.getType());
					event.setBeginOffset(qt.getBeginOffset());
					event.setEndOffset(qt.getEndOffset());
					// Set a default value; currently it is never changed. But
					// it could be overwritten if searchers
					// have
					// special requirements in the future.
					// event.setLikelihoods(likelihoods);
					// event.setLikelihoods(likelihoodWilcard);
					((BranchNode) root).add(event);
					break;
				case AND_OPERATOR:
					qt.setInputTokenType(TokenType.AND);
					BinaryNode binaryNodeAnd = new BinaryNode(NodeType.AND);
					binaryNodeAnd.setQueryToken(qt);
					binaryNodeAnd.setTokenType(qt.getType());
					binaryNodeAnd.setBeginOffset(qt.getBeginOffset());
					binaryNodeAnd.setEndOffset(qt.getEndOffset());
					((BranchNode) root).add(binaryNodeAnd);
					break;
				case OR_OPERATOR:
					qt.setInputTokenType(TokenType.OR);
					BinaryNode binaryNodeOr = new BinaryNode(NodeType.OR);
					binaryNodeOr.setQueryToken(qt);
					binaryNodeOr.setTokenType(qt.getType());
					binaryNodeOr.setBeginOffset(qt.getBeginOffset());
					binaryNodeOr.setEndOffset(qt.getEndOffset());
					((BranchNode) root).add(binaryNodeOr);
					break;
				case NOT_OPERATOR:
					qt.setInputTokenType(TokenType.NOT);
					NotNode notNode = new NotNode(qt);
					notNode.setTokenType(qt.getType());
					notNode.setBeginOffset(qt.getBeginOffset());
					notNode.setEndOffset(qt.getEndOffset());
					((BranchNode) root).add(notNode);
					break;
				case LEFT_PARENTHESIS:
					if (status.hasParenthesisError())
						break;
					qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
					Node nestedParse = recursiveParse(status, tokenQueue, tokens);
					if (nestedParse.getNodeType() == NodeType.EVENT) {
						event = (EventNode) nestedParse;
						if (root.getNodeType() != NodeType.EVENT) {
							((BranchNode) root).add(event);
						} else {
							// The root is an event that 'wants' to take an
							// argument This argument, however, is an event
							// itself. We currently do not support nesting, so
							// root event must be replaced by the
							// default
							// operator and the event must go into a text node.
							EventNode eventRoot = (EventNode) root;
							TextNode eventTextNode = new TextNode(eventRoot.getText());
							eventTextNode.setBeginOffset(eventRoot.getBeginOffset());
							eventTextNode.setEndOffset(eventRoot.getEndOffset());
							eventTextNode.setTerms(eventRoot.getEventTypes());

							// We have at most three elements that must be
							// conjunctively connected: A potential first
							// argument of the current event root, the text node
							// induced by the current event root and
							// the
							// new event.
							BinaryNode argumentAnd;
							BinaryNode eventAnd;

							if (!eventRoot.getChildren().isEmpty()) {
								List<Node> children = eventRoot.getChildren();
								argumentAnd = new BinaryNode(NodeType.AND, children.get(0), eventTextNode);
								eventAnd = new BinaryNode(NodeType.AND, argumentAnd, event);
								root = eventAnd;
							} else {
								// Existing root event does not have children
								eventAnd = new BinaryNode(NodeType.AND, eventTextNode, event);
								root = eventAnd;
							}
						}
					} else
						((BranchNode) root).add(nestedParse);
					leftParenthesisJustPassed = true;
					break;
				case RIGHT_PARENTHESIS:
					// do nothing, coming here means we have met a closing
					// parenthesis but the syntactic structure is broken and we
					// ignore parenthesis as logical expression elements
					break;
				default:
					throw new IllegalArgumentException("Unhandled QueryToken type: " + qt.getType());
				}
			// Root can't take a child.
			else {
				switch (qt.getType()) {
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
				case PHRASE:
				case DASH:
				case QueryTokenizerImpl.ALPHANUM_EMBEDDED_PAR:
					// nodeType = determineNodeType(qt);
					TextNode textNode = new TextNode(qt.getOriginalValue(), qt);
					textNode.setTokenType(qt.getType());
					textNode.setTerms(qt.getTermList());
					textNode.setBeginOffset(qt.getBeginOffset());
					textNode.setEndOffset(qt.getEndOffset());
					BinaryNode implicitBinaryNode;
					switch (ParseTree.getDefaultOperator()) {
					case ParseTree.AND:
						implicitBinaryNode = new BinaryNode(NodeType.AND);
						implicitBinaryNode.setTokenType(AND_OPERATOR);
						break;
					case ParseTree.OR:
						implicitBinaryNode = new BinaryNode(NodeType.OR);
						implicitBinaryNode.setTokenType(OR_OPERATOR);
						break;
					default:
						throw new IllegalStateException(
								"Invalid default operator in ParseTree class: " + ParseTree.getDefaultOperator());
					}
					root = adaptCurrentParseByOperatorPrecedency(root, implicitBinaryNode);
					((BranchNode) root).add(textNode);
					break;
				case BINARY_EVENT:
					EventNode event = new EventNode(qt.getOriginalValue(), qt.getTermList(), true);
					event.setTokenType(qt.getType());
					event.setBeginOffset(qt.getBeginOffset());
					event.setEndOffset(qt.getEndOffset());
					// Set a default value; currently it is never changed. But
					// it could be overwritten if searchers have
					// special requirements in the future.
					// event.setLikelihoods(likelihoods);
					// event.setLikelihoods(likelihoodWilcard);
					// CAUTION: We currently do not support nested events. That
					// is the reason why we here make a
					// difference and do not just call
					// adaptCurrentParseByOperatorPrecedency(). From a parsing
					// perspective, this special handling is not the way to go.
					if (root.getClass().equals(EventNode.class)) {
						BinaryNode binaryAndEventNode = new BinaryNode(NodeType.AND);
						binaryAndEventNode.add(root);
						binaryAndEventNode.add(event);
						root = binaryAndEventNode;
					} else {
						root = adaptCurrentParseByOperatorPrecedency(root, event);
					}
					break;
				case UNARY_EVENT:
					event = new EventNode(qt.getOriginalValue(), qt.getTermList(), false);
					event.setTokenType(qt.getType());
					event.setBeginOffset(qt.getBeginOffset());
					event.setEndOffset(qt.getEndOffset());
					// Set a default value; currently it is never changed. But
					// it could be overwritten if searchers have
					// special requirements in the future.
					// event.setLikelihoods(likelihoods);
					// event.setLikelihoods(likelihoodWilcard);
					// CAUTION: We currently do not support nested events. That
					// is the reason why we here make a
					// difference and do not just call
					// adaptCurrentParseByOperatorPrecedency(). From a parsing
					// perspective, this special handling is not the way to go.
					if (root.getClass().equals(EventNode.class)) {
						BinaryNode binaryAndEventNode = new BinaryNode(NodeType.AND);
						binaryAndEventNode.add(root);
						binaryAndEventNode.add(event);
						root = binaryAndEventNode;
					} else {
						root = adaptCurrentParseByOperatorPrecedency(root, event);
					}
					break;
				case AND_OPERATOR:
					qt.setInputTokenType(TokenType.AND);
					BinaryNode binaryNodeAnd = new BinaryNode(NodeType.AND);
					binaryNodeAnd.setQueryToken(qt);
					binaryNodeAnd.setTokenType(qt.getType());
					binaryNodeAnd.setBeginOffset(qt.getBeginOffset());
					binaryNodeAnd.setEndOffset(qt.getEndOffset());
					// root = binaryNodeAnd;
					root = adaptCurrentParseByOperatorPrecedency(root, binaryNodeAnd);
					// lastOperatorType = qt.getType();
					break;
				case OR_OPERATOR:
					qt.setInputTokenType(TokenType.OR);
					BinaryNode binaryNodeOr = new BinaryNode(NodeType.OR);
					binaryNodeOr.setQueryToken(qt);
					binaryNodeOr.setTokenType(qt.getType());
					binaryNodeOr.setBeginOffset(qt.getBeginOffset());
					binaryNodeOr.setEndOffset(qt.getEndOffset());
					// root = binaryNodeOr;
					root = adaptCurrentParseByOperatorPrecedency(root, binaryNodeOr);
					// lastOperatorType = qt.getType();
					break;
				case NOT_OPERATOR:
					qt.setInputTokenType(TokenType.NOT);
					NotNode notNode = new NotNode(qt);
					notNode.setTokenType(qt.getType());
					notNode.setBeginOffset(qt.getBeginOffset());
					notNode.setEndOffset(qt.getEndOffset());
					root = new BinaryNode(NodeType.AND, root, notNode);
					break;
				case LEFT_PARENTHESIS:
					if (status.hasParenthesisError())
						break;
					qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
					root = new BinaryNode(NodeType.AND, root, recursiveParse(status, tokenQueue, tokens));
					leftParenthesisJustPassed = true;
					break;
				case RIGHT_PARENTHESIS:
					// do nothing, coming here means we have met a closing
					// parenthesis but the syntactic structure is broken and we
					// ignore parenthesis as logical expression elements
					break;
				default:
					throw new IllegalArgumentException("Unhandled QueryToken type: " + qt.getType());
				}
			}
			if (qt.getType() != LEFT_PARENTHESIS)
				leftParenthesisJustPassed = false;
			qt = tokenQueue.poll();
		}
		if (qt != null && qt.getType() == RIGHT_PARENTHESIS && !status.hasParenthesisError())
			qt.setInputTokenType(TokenType.RIGHT_PARENTHESIS);
//		if (!tokenQueue.isEmpty()) {
//			if (log.isDebugEnabled()) {
//				StringBuilder sbTokens = new StringBuilder();
//				for (QueryToken t : tokens)
//					sbTokens.append(t.toString()).append("\n");
//				StringBuilder sbTokenQueue = new StringBuilder();
//				for (QueryToken t : tokenQueue)
//					sbTokenQueue.append(t.toString()).append("\n");
//				log.warn(
//						"Parsing issue: The QueryToken queue has not completely been processed at the and of the parsing process. Original QueryTokens:\n{} Current QueryToken: {}\nQueryToken queue:\n{}",
//						new Object[] { sbTokens.toString(), qt, sbTokenQueue.toString() });
//			}
//		}
		if (root instanceof BranchNode)
			((BranchNode) root).setAtomic(true);
		return root;
	}

	/**
	 * Inserts <tt>newOperator</tt> in the parse tree given by
	 * <tt>currentRoot</tt> according to operator precedence.
	 * <p>
	 * The rightmost path of the current parse tree is searched from top to
	 * bottom for a node of equal or higher operator precedence (terminals, i.e.
	 * leafs, have highest precedence) than the precedence of
	 * <tt>newOperator</tt>. Then, the new operator is added directly above this
	 * node of higher or equal precedence.
	 * </p>
	 * 
	 * @param currentRoot
	 *            The root of the current - unfinished - parse tree.
	 * @param newOperator
	 *            The new operator node to be added to the current parse.
	 * @return The new root of the new parse tree which includes
	 *         <tt>newOperator</tt>.
	 */
	protected Node adaptCurrentParseByOperatorPrecedency(Node currentRoot, BranchNode newOperator) {
		try {
			Node newRoot;
			if (currentRoot.isAtomic()) {
				newOperator.add(currentRoot);
				newRoot = newOperator;
			} else {
				BranchNode branchNode = (BranchNode) currentRoot;
				Node higherPrecedenceNode = getRightmostPrecedenceSupremumNodeChild(branchNode,
						operatorPrecedences.get(newOperator.getTokenType()), null);
				if (null == higherPrecedenceNode) {
					newOperator.add(currentRoot);
					newRoot = newOperator;
				} else {
					BranchNode leafParent = higherPrecedenceNode.getParent();
					leafParent.replaceChild(higherPrecedenceNode, newOperator);
					newOperator.add(higherPrecedenceNode);
					// No new root but a new subtree to the existing parse tree
					newRoot = currentRoot;
				}
			}
			return newRoot;
		} catch (Exception e) {
			log.error("Unexpected exception. Parameter: currentRoot: {}, newOperator: {}", currentRoot, newOperator);
			throw e;
		}
	}

	/**
	 * Traverses the rightmost path of the current parse tree for which
	 * <tt>root</tt> is the root. The node is identified that satisfies the
	 * following constraints:
	 * <ul>
	 * <li>Has lower (not equal!) precedence than the given
	 * <tt>precendenceValue</tt></li>
	 * <li>Lies above a node with equal or higher precedence than
	 * <tt>precedenceValue</tt>, if existing (i.e. the identified node has lower
	 * depth then a node of higher precedence on the rightmost path).</li>
	 * </ul>
	 * <p>
	 * If such a node exists and it is a {@link BranchNode} (i.e. an inner
	 * node), its rightmost child is returned (because we will want to replace
	 * it with our higher precedence node for which this method was called).
	 * Thus, the returned node might be another <tt>BranchNode</tt> or a leaf.
	 * If no such node exists <tt>root</tt> already was of higher or equal
	 * precedence to <tt>precedenceValue</tt>. Then, <tt>null</tt> is returned.
	 * </p>
	 * <p>
	 * As a consequence, newly added operators with an equal precedence to
	 * another node on the rightmost path will be inserted <em>above</em> the
	 * already existing node in the parse tree. With other words, existing tree
	 * structures are only broken up for operators with higher precedence.
	 * </p>
	 * 
	 * @param root
	 *            Root node of the parse tree that is to to be added a new
	 *            operator node to, obeying operator precedence.
	 * @param precendenceValue
	 *            The operator precedence of the operator node that should be
	 *            added to the parse.
	 * @param lastLowerPrecendenceNode
	 * @return The child of the lowest node n, where n is of lower precedence
	 *         than <tt>precedenceValue</tt> and no other node with higher or
	 *         equal precedence than <tt>precedenceValue</tt> was traversed to
	 *         reach the child.
	 */
	private Node getRightmostPrecedenceSupremumNodeChild(Node root, int precendenceValue,
			Node lastLowerPrecendenceNode) {
		if (root.isAtomic())
			return root;
		int nodePrecendence = ParsingService.operatorPrecedences.get(root.getTokenType());
		if (nodePrecendence < precendenceValue)
			return getRightmostPrecedenceSupremumNodeChild(((BranchNode) root).getLastChild(), precendenceValue, root);
		if (null != lastLowerPrecendenceNode)
			return ((BranchNode) lastLowerPrecendenceNode).getLastChild();
		return null;
	}

	@Override
	public EventNode createEventNode(Event eventTerm) {
		// List<Concept> arguments = eventTerm.getArguments();
		// Concept arg1 = arguments.get(0);
		// Concept arg2 = arguments.size() > 1 ? arguments.get(1) : null;
		// List<IConcept> eventTypes = new ArrayList<>();
		// eventTypes.add(eventTerm.getEventTerm());
		// EventNode eventNode = new EventNode(eventTerm.getPreferredName(),
		// eventTypes, arg2 != null);
		// eventNode.setLikelihoods(likelihoodWilcard);
		// TextNode arg1Node = new TextNode(arg1.getPreferredName());
		// arg1Node.setTerms(Lists.newArrayList(arg1));
		// eventNode.add(arg1Node);
		// if (null != arg2) {
		// TextNode arg2Node = new TextNode(arg2.getPreferredName());
		// arg2Node.setTerms(Lists.newArrayList(arg2));
		// eventNode.add(arg2Node);
		// }
		// return eventNode;
		return null;
	}

	private void checkParenthesis(ParseErrors status, List<QueryToken> tokens) {
		if (null == tokens)
			return;
		for (QueryToken t : tokens) {
			if (t.getType() == QueryTokenizerImpl.LEFT_PARENTHESIS)
				status.incLeftPar();
			if (t.getType() == QueryTokenizerImpl.RIGHT_PARENTHESIS) {
				status.incRightPar();
				if (status.getRightParentheses() > status.getLeftParentheses()) {
					status.setParenthesisError(true);
					log.debug("Parenthesis error detected in input, parenthesis are ignored:\n{}",
							QueryToken.printToString(tokens));
					return;
				}
			}
		}
		status.setParenthesisError(false);
	}

}
