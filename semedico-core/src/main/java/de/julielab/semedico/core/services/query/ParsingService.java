package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.*;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.util.*;

/**
 * @deprecated Use {@link SecopiaParsingService} instead
 */
public class ParsingService implements IParsingService {

    public static final Map<QueryToken.Category, Integer> operatorPrecedences;

    private Logger log;
    private NodeType defaultBoolNodeType;

    static {
        operatorPrecedences = new HashMap<>();
        operatorPrecedences.put(QueryToken.Category.OR, 0);
        operatorPrecedences.put(QueryToken.Category.AND, 1);
        operatorPrecedences.put(QueryToken.Category.NOT, 3);
    }

    public ParsingService(Logger log, @Symbol(SemedicoSymbolConstants.PARSING_DEFAULT_OPERATOR) NodeType defaultOperator) {
        this.log = log;
        this.defaultBoolNodeType = defaultOperator;
    }

    @Override
    public ParseTree parse(List<QueryToken> tokens) {
        Queue<QueryToken> tokenQueue = new LinkedList<>(tokens);
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
    {
        if (status == null)
            throw new IllegalArgumentException("Got no ParseStatus Object!");

        Node root = null;
        // int lastOperatorType = Integer.MIN_VALUE;
        QueryToken qt = tokenQueue.poll();
        boolean leftParenthesisJustPassed = false;
        while (qt != null && (qt.getType() != QueryToken.Category.RPAR || status.hasParenthesisError())) {
            // 3 states corresponding to current root. Here: root undefined.
            if (root == null)
                switch (qt.getType()) {
                    case ALPHA:
                    case ALPHANUM:
                    case APOSTROPHE:
                    case NUM:
                    case KW_PHRASE:
                    case DASH:
                    case COMPOUND:
                    case PREFIXED:
                    case HASHTAG:
                        // nodeType = determineNodeType(qt);
                        TextNode textNode = new TextNode(qt.getOriginalValue(), qt);
                        textNode.setTokenType(qt.getType());
                        textNode.setConcepts(qt.getConceptList());
                        textNode.setBeginOffset(qt.getBegin());
                        textNode.setEndOffset(qt.getEnd());
                        root = textNode;
                        break;
                    case AND:
                        status.incIgnoredANDs();
                        break;
                    case OR:
                        status.incIgnoredORs();
                        break;
                    case NOT:
                        qt.setInputTokenType(TokenType.NOT);
                        NotNode notNode = new NotNode(qt);
                        notNode.setTokenType(qt.getType());
                        notNode.setBeginOffset(qt.getBegin());
                        notNode.setEndOffset(qt.getEnd());
                        root = notNode;
                        break;
                    case LPAR:
                        if (status.hasParenthesisError())
                            break;
                        qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
                        root = recursiveParse(status, tokenQueue, tokens);
                        leftParenthesisJustPassed = true;
                        break;
                    case RPAR:
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
                    case ALPHA:
                    case ALPHANUM:
                    case APOSTROPHE:
                    case NUM:
                    case KW_PHRASE:
                    case DASH:
                    case COMPOUND:
                    case HASHTAG:
                        TextNode textNode = new TextNode(qt.getOriginalValue(), qt);
                        textNode.setTokenType(qt.getType());
                        textNode.setConcepts(qt.getConceptList());
                        textNode.setBeginOffset(qt.getBegin());
                        textNode.setEndOffset(qt.getEnd());
                        ((BranchNode) root).add(textNode);
                        break;
                    case AND:
                        qt.setInputTokenType(TokenType.AND);
                        BinaryNode binaryNodeAnd = new BinaryNode(NodeType.AND);
                        binaryNodeAnd.setQueryToken(qt);
                        binaryNodeAnd.setTokenType(qt.getType());
                        binaryNodeAnd.setBeginOffset(qt.getBegin());
                        binaryNodeAnd.setEndOffset(qt.getEnd());
                        ((BranchNode) root).add(binaryNodeAnd);
                        break;
                    case OR:
                        qt.setInputTokenType(TokenType.OR);
                        BinaryNode binaryNodeOr = new BinaryNode(NodeType.OR);
                        binaryNodeOr.setQueryToken(qt);
                        binaryNodeOr.setTokenType(qt.getType());
                        binaryNodeOr.setBeginOffset(qt.getBegin());
                        binaryNodeOr.setEndOffset(qt.getEnd());
                        ((BranchNode) root).add(binaryNodeOr);
                        break;
                    case NOT:
                        qt.setInputTokenType(TokenType.NOT);
                        NotNode notNode = new NotNode(qt);
                        notNode.setTokenType(qt.getType());
                        notNode.setBeginOffset(qt.getBegin());
                        notNode.setEndOffset(qt.getEnd());
                        ((BranchNode) root).add(notNode);
                        break;
                    case LPAR:
                        if (status.hasParenthesisError()) {
                            break;
                        }
                        qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
                        Node nestedParse = recursiveParse(status, tokenQueue, tokens);

                        ((BranchNode) root).add(nestedParse);
                        leftParenthesisJustPassed = true;
                        break;
                    case RPAR:
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
                    case ALPHA:
                    case APOSTROPHE:
                    case NUM:
                    case KW_PHRASE:
                    case DASH:
                    case COMPOUND:
                    case HASHTAG:
                        TextNode textNode = new TextNode(qt.getOriginalValue(), qt);
                        textNode.setTokenType(qt.getType());
                        textNode.setConcepts(qt.getConceptList());
                        textNode.setBeginOffset(qt.getBegin());
                        textNode.setEndOffset(qt.getEnd());
                        BinaryNode implicitBinaryNode = new BinaryNode(defaultBoolNodeType);

                        switch (defaultBoolNodeType) {
                            case AND:
                                implicitBinaryNode.setTokenType(QueryToken.Category.AND);
                                break;
                            case OR:
                                implicitBinaryNode.setTokenType(QueryToken.Category.OR);
                                break;
                            default:
                                throw new IllegalStateException(
                                        "Invalid default operator in ParseTree class: " + defaultBoolNodeType);
                        }

                        root = adaptCurrentParseByOperatorPrecedency(root, implicitBinaryNode);
                        ((BranchNode) root).add(textNode);
                        break;
                    case AND:
                        qt.setInputTokenType(TokenType.AND);
                        BinaryNode binaryNodeAnd = new BinaryNode(NodeType.AND);
                        binaryNodeAnd.setQueryToken(qt);
                        binaryNodeAnd.setTokenType(qt.getType());
                        binaryNodeAnd.setBeginOffset(qt.getBegin());
                        binaryNodeAnd.setEndOffset(qt.getEnd());
                        root = adaptCurrentParseByOperatorPrecedency(root, binaryNodeAnd);
                        break;
                    case OR:
                        qt.setInputTokenType(TokenType.OR);
                        BinaryNode binaryNodeOr = new BinaryNode(NodeType.OR);
                        binaryNodeOr.setQueryToken(qt);
                        binaryNodeOr.setTokenType(qt.getType());
                        binaryNodeOr.setBeginOffset(qt.getBegin());
                        binaryNodeOr.setEndOffset(qt.getEnd());
                        root = adaptCurrentParseByOperatorPrecedency(root, binaryNodeOr);
                        break;
                    case NOT:
                        qt.setInputTokenType(TokenType.NOT);
                        NotNode notNode = new NotNode(qt);
                        notNode.setTokenType(qt.getType());
                        notNode.setBeginOffset(qt.getBegin());
                        notNode.setEndOffset(qt.getEnd());
                        root = new BinaryNode(NodeType.AND, root, notNode);
                        break;
                    case LPAR:
                        if (status.hasParenthesisError())
                            break;
                        qt.setInputTokenType(TokenType.LEFT_PARENTHESIS);
                        Node oldRoot = root;
                        root = new BinaryNode(NodeType.AND);
                        root.setTokenType(QueryToken.Category.AND);
                        ((BinaryNode)root).setLeftChild(oldRoot);
                        ((BinaryNode)root).setRightChild(recursiveParse(status, tokenQueue, tokens));
                        leftParenthesisJustPassed = true;
                        break;
                    case RPAR:
                        // do nothing, coming here means we have met a closing
                        // parenthesis but the syntactic structure is broken and we
                        // ignore parenthesis as logical expression elements
                        break;
                    default:
                        throw new IllegalArgumentException("Unhandled QueryToken type: " + qt.getType());
                }
            }
            if (qt.getType() != QueryToken.Category.LPAR) {
                leftParenthesisJustPassed = false;
            }
            qt = tokenQueue.poll();
        }
        if (qt != null && qt.getType() == QueryToken.Category.RPAR && !status.hasParenthesisError()) {
            qt.setInputTokenType(TokenType.RIGHT_PARENTHESIS);
        }

        if (root instanceof BranchNode) {
            ((BranchNode) root).setAtomic(true);
        }
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
     * @param currentRoot The root of the current - unfinished - parse tree.
     * @param newOperator The new operator node to be added to the current parse.
     * @return The new root of the new parse tree which includes
     * <tt>newOperator</tt>.
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
     * @param root                     Root node of the parse tree that is to to be added a new
     *                                 operator node to, obeying operator precedence.
     * @param precendenceValue         The operator precedence of the operator node that should be
     *                                 added to the parse.
     * @param lastLowerPrecendenceNode
     * @return The child of the lowest node n, where n is of lower precedence
     * than <tt>precedenceValue</tt> and no other node with higher or
     * equal precedence than <tt>precedenceValue</tt> was traversed to
     * reach the child.
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

    private void checkParenthesis(ParseErrors status, List<QueryToken> tokens) {
        if (null == tokens)
            return;
        for (QueryToken t : tokens) {
            if (t.getType() == QueryToken.Category.LPAR)
                status.incLeftPar();
            if (t.getType() == QueryToken.Category.RPAR) {
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
