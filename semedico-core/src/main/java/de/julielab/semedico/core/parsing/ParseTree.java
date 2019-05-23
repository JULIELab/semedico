package de.julielab.semedico.core.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.query.QueryToken;

/**
 * A representation of a parse tree, containing the root node and parse errors.
 * 
 * @author hellrich
 * 
 */
public class ParseTree {

	public enum SERIALIZATION {
		TEXT, TERMS, IDS
	}

	private static final Logger log = LoggerFactory.getLogger(ParseTree.class);

	public static final String OR = "OR";
	public static final String AND = "AND";

	private Node root;
	private Map<Long, Node> idMap = new TreeMap<>();
	private Map<String, Node> textMap = new HashMap<>();
	private List<IConcept> termList;
	private long currentNodeId;

	boolean foundTerm;
	/**
	 * We store the query tokens the user actually typed in. This way, the
	 * search bar reflects the user input. The parse tree itself has a few more
	 * nodes than input tokens due to implicit boolean operators (OR or AND,
	 * depending on that the default operator is).
	 */
	private List<QueryToken> tokens;

	/**
	 * A representation of a parse tree, containing the root node and parse
	 * errors.
	 * 
	 * @param root
	 *            Root node of the parse tree.
	 * @param errors
	 *            Object containing error messages.
	 */
	public ParseTree(Node root, ParseErrors errors) {
		this.root = root;
		mapTree(root, null, 0);
		termList = new ArrayList<>();
		termList = buildTermList(root);
		tokens = new ArrayList<>();
	}

	public String toString(SERIALIZATION serializationType) {
		if (null != root)
			return root.toString(serializationType);
		return "<empty>";
	}

	public Node getRoot() {
		return root;
	}

	/**
	 * Add a new node / subtree to the parse tree. The new node is added as a
	 * sibling to a specified existing node by inserting a new (binary) parent
	 * node of type AND or OR.
	 * 
	 * @param existingNode
	 *            The existing node that the new node is added to as a sibling.
	 * @param newNode
	 *            The new node that is added as a sibling to the existing node.
	 * @param operator
	 *            The operator type for the inserted parent node of both the
	 *            existing and the new node (either NodeType.AND or NodeType.OR)
	 */
	public void add(Node existingNode, Node newNode, NodeType operator) throws Exception {
		log.debug("Adding node {} to node {} with operator AND",
				newNode.text, existingNode.text, operator);

		Node insertedParent = null;
		BranchNode existingNodeParent = existingNode.getParent();
		insertedParent = new BinaryNode(NodeType.AND, existingNode, newNode);

		registerNode(insertedParent);
		registerRecursively(newNode);

		// set the new operator as the new root if the existingNode was the
		// root before.
		// Otherwise, set the new operator to the position where the
		// existingNode was before.
		if (existingNodeParent == null) {
			root = insertedParent;
		} else {
			existingNodeParent.replaceChild(existingNode, insertedParent);
		}
		termList = buildTermList(root);

		root.computeTreeHeight();

		tokens.add(newNode.getQueryToken());
	}

	private void registerRecursively(Node node) {
		registerNode(node);
		if (!node.isLeaf()) {
			BranchNode branchNode = (BranchNode) node;
			for (Node child : branchNode.getChildren()) {
				if (!child.isLeaf())
					registerRecursively(child);
				else
					registerNode(child);
			}
		}
	}

	public void replaceNode(Node old, Node replacement) {
		log.debug("Replacing node {} with node {}", old.text, replacement.text);
		BranchNode parent = old.getParent();
		if (null == parent) {
			// the old node was the root
			root = replacement;
		} else {
			parent.replaceChild(old, replacement);
		}
		unregisterNode(old);
		registerNode(replacement);
		termList = buildTermList(root);
		if (null != root)
			root.computeTreeHeight();

		QueryToken replaceementQt = replacement.getQueryToken();

		if (replaceementQt == null)
			throw new IllegalArgumentException(
					"A node in the parse tree should be replaced by a new node that has no query token.");

		int oldTokenIndex = getIndexOfNodeQueryToken(old);
		log.debug("Replacing old node's query token with the new node's one: {} replaced by {} on index {}",
				old.getQueryToken().getOriginalValue(), replaceementQt.getOriginalValue(),
						oldTokenIndex);
		tokens.set(oldTokenIndex, replaceementQt);
	}

	/**
	 * Add all nodes of the tree in the appropriate maps, generate an id for
	 * every node and remove some unnecessary nodes (e.g. unnecessary implicit
	 * ANDs). This is done by recursing top-down left-right.
	 * 
	 * @param node
	 *            The node to generate an id for.
	 * @param parent
	 *            The parent of this node.
	 * @param id
	 *            The id for this node.
	 * @return The id assigned to this node. Is -1 if it has been removed from
	 *         the tree or was <tt>null</tt>.
	 */
	private long mapTree(Node node, BranchNode parent, long id) {
		if (null == node)
			return -1;
		node.setParent(parent);
		// BinaryNodes with exactly one child are replaced by it.
		while ((node.getNodeType() == NodeType.AND || node.getNodeType() == NodeType.OR)
				&& ((BranchNode) node).hasExactlyOneChild()) {
			Node replacement = ((BinaryNode) node).getOnlyChild();
			if (node == root)
				root = replacement;
			else
				node.getParent().replaceChild(node, replacement);
			node = replacement;
		}

		registerNode(node);

		// Only text nodes may be leaves.
		if (node.isLeaf() && node.getClass() != TextNode.class) {
			unregisterNode(node);

			// If nothing did help, delete the illegal branch node without
			// children.
			if (null != parent)
				parent.removeChild(node);
			return -1;
			// Recursing in the subtrees.
		} else if (node instanceof BranchNode) {
			BranchNode branchNode = (BranchNode) node;
			List<Node> children = branchNode.getChildren();
			for (Node child : children) {
				id = mapTree(child, branchNode, id + 1);
			}
		}
		// }
		return id;
	}

	/**
	 * Get all terms contained in the parse tree.
	 * 
	 * @return A list of terms.
	 */
	public List<IConcept> getTerms() {
		return termList;
	}

	private List<IConcept> buildTermList(Node node) {
		if (null == node)
			return null;
		termList.clear();
		addSubtreeToTermList(node, termList);
		return termList;
	}

	/**
	 * Build a list of all the terms occurring in the subtree comprised by a
	 * specified node.
	 * 
	 * @param node
	 *            The specified node.
	 * @param termList
	 *            An empty list of terms to fill.
	 * @return A list of all the terms.
	 */
	private void addSubtreeToTermList(Node node, List<IConcept> termList) {
		if (node instanceof BranchNode) {
			BranchNode branchNode = (BranchNode) node;
			for (Node child : branchNode.getChildren())
				addSubtreeToTermList(child, termList);
		} else if (node.getClass().equals(TextNode.class)) {
			TextNode textNode = (TextNode) node;
			termList.addAll(textNode.getTerms());
		}
	}

	/**
	 * Get all text nodes in the parse tree, i.e. terms, keywords or phrases.
	 * Please not that this does not include events.
	 * 
	 * @return A list of text nodes.
	 */
	public List<String> getText() {
		List<String> text = new ArrayList<>();
		for (Node node : textMap.values()) {
			if (node.getClass().equals(TextNode.class)) {
				text.add(node.getText());
			}
		}
		return text;
	}

	private void registerNode(Node node) {
		if (null == node)
			return;
		if (node.getId() != -1)
			return;
		node.setId(getNextNodeId());
		idMap.put(node.getId(), node);
		if (node.getClass().equals(TextNode.class))
			textMap.put(node.getText(), node);
	}

	private void unregisterNode(Node node) {
		idMap.remove(node.getId());
		if (node.isLeaf())
			textMap.remove(node.getText());
		node.setId(-1);
		if (node instanceof BranchNode) {
			BranchNode branchNode = (BranchNode) node;
			for (Node child : branchNode.children)
				unregisterNode(child);
		}
	}

	private long getNextNodeId() {
		return currentNodeId++;
	}

	public boolean isEmpty() {
		return root == null;
	}

	/**
	 * Returns the number of concept query nodes, i.e. terms, events, without
	 * boolean operators.
	 * 
	 * @return
	 */
	public int getNumberConceptNodes() {
		return getConceptNodes(root, new ArrayList<Node>()).size();
	}

	public List<Node> getConceptNodes() {
		return getConceptNodes(root, new ArrayList<Node>());
	}

	public List<Node> getNodes(Node subtreeRoot, Class<?> cls, List<Node> collector) {
		Class<? extends Node> rootClass = subtreeRoot.getClass();
		if (rootClass.equals(cls)) {
			collector.add(subtreeRoot);
		}
		if (rootClass.equals(NotNode.class)) {
			NotNode not = (NotNode) subtreeRoot;
			getNodes(not.getChild(), cls, collector);
		} else if (subtreeRoot instanceof BranchNode) {
			BranchNode branchNode = (BranchNode) subtreeRoot;
			for (Node child : branchNode.getChildren())
				getNodes(child, cls, collector);
		}
		return collector;
	}

	private List<Node> getConceptNodes(Node subtreeRoot, List<Node> collector) {
		Class<? extends Node> rootClass = subtreeRoot.getClass();
		if (rootClass.equals(TextNode.class)) {
			collector.add(subtreeRoot);
		} else if (rootClass.equals(NotNode.class)) {
			NotNode not = (NotNode) subtreeRoot;
			getConceptNodes(not.getChild(), collector);
		} else if (subtreeRoot instanceof BranchNode) {
			BranchNode branchNode = (BranchNode) subtreeRoot;
			for (Node child : branchNode.getChildren())
				getConceptNodes(child, collector);
		} else {
			throw new IllegalArgumentException("Unexpected node class " + rootClass);
		}
		return collector;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(SERIALIZATION.TEXT);
	}

	public void setQueryTokens(List<QueryToken> tokens) {
		this.tokens = tokens;

	}

	/**
	 * The list of QueryTokens underlying this parse tree.
	 * 
	 * @return
	 */
	public List<QueryToken> getQueryTokens() {
		return tokens;
	}

	public int getIndexOfNodeQueryToken(Node node) {
		QueryToken qt = node.getQueryToken();

		if (qt == null)
			throw new IllegalArgumentException("The passed node has no query token");

		int oldTokenIndex = tokens.indexOf(qt);

		if (oldTokenIndex < 0)
			throw new IllegalStateException(
					"The node to be replaced has a query token that could not be found in this parse tree.");

		return oldTokenIndex;
	}
}
