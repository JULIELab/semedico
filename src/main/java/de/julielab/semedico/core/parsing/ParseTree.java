package de.julielab.semedico.core.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

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

	/**
	 * Constant for {@link #getDefaultOperator()}
	 */
	public static final String OR = "OR";
	/**
	 * Constant for {@link #getDefaultOperator()}
	 */
	public static final String AND = "AND";
	/**
	 * One of {@link ParseTree#OR} or {@link ParseTree#AND}
	 */
	private static String defaultOperator = OR;

	private Node root;
	private ParseErrors errors;
	private Map<Long, Node> idMap = new TreeMap<Long, Node>();
	private Map<String, Node> textMap = new HashMap<String, Node>();
	private List<IConcept> termList;
	private long currentNodeId;

	boolean foundTerm;
	private boolean compress;
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
		this(root, errors, false);
	}

	public ParseTree(Node root, ParseErrors errors, boolean compress) {
		this.root = root;
		this.errors = errors;
		this.compress = compress;
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
	 * Returns the node specified by an id.<br>
	 * WARNING: Changing the tree causes all ids to be reassigned!
	 * 
	 * @param id
	 *            The id of the node.
	 * @return The node mapped by this id.
	 */
	public Node getNode(long id) {
		return idMap.get(id);
	}

	/**
	 * Returns the node specified by a text.<br>
	 * TODO does not work properly when the same text has been entered multiple
	 * times
	 * 
	 * @param text
	 *            The text of the node.
	 * @return The node mapped by this text.
	 */
	public Node getNode(String text) {
		return textMap.get(text);
	}

	/**
	 * Get the parsing errors.
	 * 
	 * @return An object representing the errors encountered during parsing.
	 */
	public ParseErrors getErrors() {
		return errors;
	}

	public boolean hasParsingErrors() {
		return errors.hasParsingErrors();
	}

	/**
	 * Remove a node and everything below it from the parse tree.<br>
	 * WARNING: You can not delete the parse tree with this method!
	 * 
	 * @param node
	 *            Root of the subtree.
	 */
	public void removeSubtree(Node node) {
		if (node == root)
			throw new IllegalAccessError("You can't remove the root.");
		// if (node.getClass().equals(TextNode.class) ||
		// node.getClass().equals(EventNode.class)) {
		// textMap.remove(node);
		// }
		// idMap.remove(node.getId());
		unregisterNode(node);
		node.getParent().removeChild(node);
	}

	/**
	 * Used for spelling correction. Substitutes all text nodes with subtrees of
	 * new text nodes connected by ANDs. Simply replaces the node text with a
	 * new node text if texts contains only one string.
	 * 
	 * @param texts
	 *            Must map oldText:substitution.
	 */
	public void expandTerms(Map<String, String> texts) throws Exception {
		Set<String> intersection = new HashSet<String>(textMap.keySet());
		intersection.retainAll(texts.keySet());
		for (String text : intersection) {
			expandTerm(text, texts.get(text));
		}
		// TODO: The replacement is only text, not a parse tree. Problem?
	}

	/**
	 * Used for spelling correction. Substitutes a text node with a subtree of
	 * new text nodes connected by ANDs. Simply replaces the text with a new
	 * text if texts contains only one string.
	 * 
	 * @param text
	 *            Text node to expand.
	 * @param texts
	 *            Content for the new nodes.
	 */
	public void expandTerm(String text, String... texts) throws Exception {
		Node oldNode = textMap.get(text);
		if (oldNode == null)
			throw new IllegalArgumentException("Text node \"" + text + "\" is not in the parse tree.");
		if (texts.length > 1) {
			TextNode newTextNode = new TextNode(texts[0]);
			BinaryNode newBinaryNode = new BinaryNode(NodeType.AND, newTextNode, null);
			registerNode(newBinaryNode);
			registerNode(newTextNode);
			for (int i = 1; i < texts.length; ++i) {
				newTextNode = new TextNode(texts[i]);
				if (i == texts.length - 1) {
					newBinaryNode.setRightChild(newTextNode);
				} else {
					newBinaryNode = new BinaryNode(NodeType.AND, newBinaryNode, newTextNode);
					registerNode(newBinaryNode);
				}
				registerNode(newTextNode);
			}
			oldNode.getParent().replaceChild(oldNode, newBinaryNode);
			unregisterNode(oldNode);
			termList = buildTermList(root);
			// remapTree();
		} else {
			textMap.remove(text);
			oldNode.setText(texts[0]);
			textMap.put(oldNode.text, oldNode);
		}
		root.computeTreeHeight();
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
		log.debug("Adding node {} to node {} with operator {}",
				new Object[] { newNode.text, existingNode.text, operator });
		switch (operator) {
		case OR:
		case AND:
			Node insertedParent = null;
			BranchNode existingNodeParent = existingNode.getParent();

			// Create a new operator node, if necessary, or add the newNode to a
			// fitting, already existing operator node
			if (compress) {
				if (existingNode.getClass().equals(CompressedBooleanNode.class)
						&& existingNode.getNodeType() == operator) {
					// if the existingNode is an operator itself and compatible
					// with operator we can just add the
					// newNode and be done
					CompressedBooleanNode compressedExistingNode = (CompressedBooleanNode) existingNode;
					compressedExistingNode.add(newNode);
					registerRecursively(newNode);
					return;
				} else if (null != existingNodeParent
						&& existingNodeParent.getClass().equals(CompressedBooleanNode.class)
						&& existingNodeParent.getNodeType() == operator) {
					// the existingNode is not a (compressed) operator node or
					// not compatible, but its parent is, so we
					// add the newNode to that one (existingNode already IS a
					// child of that operator node, so we don't
					// have to do anything for it)
					CompressedBooleanNode matchingParentOperator = (CompressedBooleanNode) existingNodeParent;
					matchingParentOperator.add(newNode);
				} else {
					// neither existingNode nor its parent are compatible
					// operator nodes. We have to create a new one.
					insertedParent = new CompressedBooleanNode(operator.name(), operator, existingNode, newNode);
				}
			} else {
				// non-compressed mode. We have to create a new binary node in
				// any case (because it is not valid for a
				// ParseTree to be incomplete when this method is called).
				insertedParent = new BinaryNode(operator, existingNode, newNode);
			}
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
			break;
		default:
			throw new IllegalArgumentException(
					"New nodes can only by appended by an OR or an AND operator, " + operator + " is not supported.");
		}
		root.computeTreeHeight();

		tokens.add(newNode.getQueryToken());
	}

	private void registerRecursively(Node node) {
		registerNode(node);
		if (!node.isLeaf()) {
			BranchNode branchNode = (BranchNode) node;
			for (Node child : branchNode.getChildren()) {
				if (!child.isLeaf())
					registerRecursively((BranchNode) child);
				else
					registerNode(child);
			}
		}
	}

	private void unregisterRecursively(Node node) {
		unregisterNode(node);
		if (!node.isLeaf()) {
			BranchNode branchNode = (BranchNode) node;
			for (Node child : branchNode.getChildren()) {
				if (!child.isLeaf())
					unregisterRecursively((BranchNode) child);
				else
					unregisterNode(child);
			}
		}
	}

	/**
	 * Check whether the parse tree contains a certain text.
	 * 
	 * @param text
	 *            Text to find in the tree.
	 * @return True if the tree has a leaf for the text.
	 */
	public boolean contains(String text) {
		return textMap.containsKey(text);
	}

	/**
	 * Check whether the parse tree contains a certain node id.
	 * 
	 * @param id
	 *            Node id to find in the tree.
	 * @return True if the tree has a leaf for the id.
	 */
	public boolean contains(int id) {
		return idMap.containsKey(id);
	}

	/**
	 * Remove the node corresponding to the text from the parse tree.
	 * 
	 * @param text
	 *            Text to remove.
	 */
	public void remove(String text) throws Exception {
		Node toRemove = textMap.get(text);
		if (null == toRemove)
			throw new IllegalArgumentException(
					"Node with text \"" + text + "\" cannot be removed because no such node exists.");
		remove(toRemove);
	}

	/**
	 * Remove the node corresponding to the id from the parse tree.
	 * 
	 * @param id
	 *            Id to remove.
	 */
	public void remove(long id) throws Exception {
		Node toRemove = idMap.get(id);
		if (null == toRemove)
			throw new IllegalArgumentException(
					"Node with ID \"" + id + "\" cannot be removed because no such node exists.");
		remove(toRemove);
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
				new Object[] { old.getQueryToken().getOriginalValue(), replaceementQt.getOriginalValue(),
						oldTokenIndex });
		tokens.set(oldTokenIndex, replaceementQt);
	}

	public void remove(Node toRemove) {
		log.debug("Removing node {}", toRemove.text);
		if (toRemove != null) {
			BranchNode parent = toRemove.getParent();
			if (null == parent) {
				// this is the root
				root = null;
				unregisterRecursively(toRemove);
			} else {
				parent.removeChild(toRemove);

				if (parent.isObsolete()) {
					if (parent.hasExactlyOneChild()) {
						if (parent.getParent() != null) {
							parent.getParent().replaceChild(parent, parent.getFirstChild());
						} else {
							root = parent.getFirstChild();
							root.setParent(null);
						}
						parent.children = Collections.emptyList();
						unregisterNode(parent);
					} else if (parent.getChildNumber() == 0) {
						remove(parent);
					} else if (parent.getChildNumber() > 1) {
						throw new UnsupportedOperationException(
								"The case where a BranchNode is obsolete but has more than a single child is currently not supported.");
					}
				}

				// the root can only change (indicated by newRoot != null) if:
				// 1) The parent is the current root
				// 2) After removal of the child, the parent is obsolete
				// if (null != newRoot) {
				// root = newRoot;
				// unregisterNode(parent);
				// }
				unregisterRecursively(toRemove);
				// Node newRoot = removeChild(parent, toRemove);
				// // Node newRoot = parent.removeChild(toRemove);
				// if (null != newRoot) {
				// root = newRoot;
				// }
			}
		} 
		termList = buildTermList(root);
		if (null != root)
			root.computeTreeHeight();
	}

	private Node removeChild(Node parent, Node child) {
		if (parent.getClass().equals(BinaryNode.class)) {
			BinaryNode binaryParent = (BinaryNode) parent;
			Node toKeep;
			Node toRemove;
			if (child == binaryParent.getLeftChild()) {
				toRemove = binaryParent.getLeftChild();
				unregisterNode(toRemove);
				toRemove = null;
				toKeep = binaryParent.getRightChild();
			} else if (child == binaryParent.getRightChild()) {
				toRemove = binaryParent.getRightChild();
				unregisterNode(toRemove);
				toRemove = null;
				toKeep = binaryParent.getLeftChild();
			} else
				throw new IllegalArgumentException(child + " is no child of " + this);

			if (null != binaryParent.getParent()) {
				binaryParent.getParent().replaceChild(parent, toKeep);
				unregisterNode(parent);
				// parent = null;
				return null;
			} else {
				unregisterNode(parent);
				// parent = null;
				toKeep.setParent(null);
				return toKeep;
			}
		} else if (parent.getClass().equals(NotNode.class)) {
			NotNode notParent = (NotNode) parent;
			Node toRemove;
			if (child == notParent.getChild()) {
				toRemove = notParent.getChild();
				unregisterNode(toRemove);
				toRemove = null;
			} else
				throw new IllegalArgumentException(child + " is no child of " + this);

			if (null != notParent.getParent()) {
				return removeChild(notParent.getParent(), notParent);
			} else {
				return null;
			}
		} else
			throw new IllegalArgumentException("Only BinaryNodes and NotNodes can have children.");
	}

	/**
	 * Get the ids of all nodes in the tree.
	 * 
	 * @return The ids of all nodes in the tree.
	 */
	public Set<Long> getNodeIds() {
		return idMap.keySet();
	}

	/**
	 * Remove unnecessary nodes and rebuild the maps.
	 */
	// private void remapTree() throws Exception {
	// idMap.clear();
	// textMap.clear();
	// mapTree(root, null, 0);
	// termList.clear();
	// termList = buildTermList(root, termList);
	// }

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
			// Try to fix the situation: Perhaps we can just convert the branch
			// node to a text node.
			if (node.getClass().equals(EventNode.class)) {
				EventNode eventNode = (EventNode) node;
				TextNode textNode = new TextNode(node.getText());
				textNode.setTerms(eventNode.getEventTypes());
				if (null != parent)
					parent.replaceChild(node, textNode);
				registerNode(textNode);
				return textNode.getId();
			}
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

		// if (node.getClass().equals(BinaryNode.class)) {
		// BinaryNode binaryNode = (BinaryNode) node;
		// if (binaryNode.getLeftChild() != null) {
		// termList = addSubtreeToTermList(binaryNode.getLeftChild(), termList);
		// }
		// if (binaryNode.getRightChild() != null) {
		// termList = addSubtreeToTermList(binaryNode.getRightChild(),
		// termList);
		// }
		// } else if (node.getClass().equals(NotNode.class)) {
		// NotNode notNode = (NotNode) node;
		// if (notNode.getChild() != null) {
		// termList = addSubtreeToTermList(notNode.getChild(), termList);
		// }
		// } else if (node.getClass().equals(TextNode.class)) {
		// TextNode textNode = (TextNode) node;
		// for (IConcept term : textNode.getTerms()) {
		// termList.add(term);
		// }
		// }
	}

	/**
	 * Look for a specified term in the text nodes and select only this one
	 * (deleting all other terms) on the first text node this term is found in.
	 * 
	 * @param term
	 *            The term to search for.
	 * @throws Exception
	 */
	public void selectTerm(IConcept term) throws Exception {
		foundTerm = false;
		selectTerm(root, term);
		// remapTree();
		termList = buildTermList(root);
	}

	/**
	 * Look for a specified term in the text nodes comprised by the subtree of a
	 * specified node and select only this one (deleting all other terms) on the
	 * first text node this term is found in.
	 * 
	 * @param term
	 *            The term to search for.
	 */
	private void selectTerm(Node node, IConcept term) {
		if (!foundTerm) {
			if (node.getClass().equals(BinaryNode.class)) {
				BinaryNode binaryNode = (BinaryNode) node;
				if (binaryNode.getLeftChild() != null) {
					selectTerm(binaryNode.getLeftChild(), term);
				}
				if (binaryNode.getRightChild() != null) {
					selectTerm(binaryNode.getRightChild(), term);
				}
			} else if (node.getClass().equals(NotNode.class)) {
				NotNode notNode = (NotNode) node;
				if (notNode.getChild() != null) {
					selectTerm(notNode.getChild(), term);
				}
			} else if (node.getClass().equals(TextNode.class)) {
				TextNode textNode = (TextNode) node;
				ArrayList<IConcept> existingTerms = new ArrayList<IConcept>();
				for (IConcept existingTerm : textNode.getTerms()) {
					existingTerms.add(existingTerm);
				}
				for (IConcept termInList : existingTerms) {
					if (termInList.equals(term)) {
						ArrayList<IConcept> newTermList = new ArrayList<IConcept>();
						newTermList.add(termInList);
						textNode.setTerms(newTermList);
						foundTerm = true;
					}
				}
			}
		}
	}

	/**
	 * Get all text nodes in the parse tree, i.e. terms, keywords or phrases.
	 * Please not that this does not include events.
	 * 
	 * @return A list of text nodes.
	 */
	public List<TextNode> getTextNodes() {
		List<TextNode> textNodes = new ArrayList<TextNode>();
		for (Node node : textMap.values()) {
			if (node.getClass().equals(TextNode.class)) {
				textNodes.add((TextNode) node);
			}
		}
		return textNodes;
	}

	public List<TextNode> getUnambigousTextNodes() {
		List<TextNode> textNodes = new ArrayList<TextNode>();
		for (Node node : textMap.values()) {
			TextNode textNode = node.asTextNode();
			if (null != textNode && textNode.getTerms().size() == 1) {
				textNodes.add((TextNode) node);
			}
		}
		return textNodes;
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

	public List<Node> getEventNodes() {
		return getNodes(root, EventNode.class, new ArrayList<Node>());
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

	public Collection<Long> getConceptNodeIds() {
		return Collections2.transform(getConceptNodes(), new Function<Node, Long>() {
			@Override
			public Long apply(Node input) {
				return input.getId();
			}
		});
	}

	private List<Node> getConceptNodes(Node subtreeRoot, List<Node> collector) {
		Class<? extends Node> rootClass = subtreeRoot.getClass();
		if (rootClass.equals(EventNode.class)) {
			collector.add(subtreeRoot);
		} else if (rootClass.equals(TextNode.class)) {
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

	/**
	 * Returns the number of leafs in this ParseTree. Note that this is not
	 * equivalent to the number of concept query nodes. See
	 * {@link #getNumberConceptNodes()}.
	 * 
	 * @return
	 */
	public int getNumberLeafs() {
		int count = 0;
		for (Node node : idMap.values()) {
			if (node.isLeaf())
				count++;
		}
		return count;
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

	public int getNumberNodes() {
		return idMap.size();
	}

	public List<Node> traversePreOrder() {
		return traversePreOrder(true);
	}

	public Collection<Long> getPreOrderNodeIds() {
		return Collections2.transform(traversePreOrder(), new Function<Node, Long>() {
			@Override
			public Long apply(Node input) {
				return input.getId();
			}
		});
	}

	public Collection<Long> getPreOrderNodeIds(final boolean descendIntoEvents) {
		return Collections2.transform(traversePreOrder(descendIntoEvents), new Function<Node, Long>() {
			@Override
			public Long apply(Node input) {
				return input.getId();
			}
		});
	}

	/**
	 * 
	 * @param descendIntoEvents
	 *            does nothing.
	 * @return
	 */
	public List<Node> traversePreOrder(boolean descendIntoEvents) {
		List<Node> traversal = new ArrayList<>(idMap.size());
		traversePreOrder(root, traversal, descendIntoEvents);
		return traversal;
	}

	public static void traversePreOrder(Node node, List<Node> traversal) {
		traversePreOrder(node, traversal, true);
	}

	public static void traversePreOrder(Node node, List<Node> traversal, boolean descendIntoEvents) {
		if (node.getClass().equals(TextNode.class)) {
			traversal.add(node);
			return;
		}
		BranchNode branchNode = (BranchNode) node;
		if (!descendIntoEvents && branchNode.getNodeType() == NodeType.EVENT) {
			traversal.add(node);
			return;
		}
		List<Node> children = branchNode.getChildren();
		if (null == children || children.size() == 0)
			return;

		traversal.add(node);
		// Not nodes only have a right child, for our purpose here
		if (!node.getClass().equals(NotNode.class))
			traversePreOrder(children.get(0), traversal, descendIntoEvents);
		if (children.size() > 1) {
			for (int i = 1; i < children.size(); ++i)
				traversePreOrder(children.get(i), traversal, descendIntoEvents);
		}
		if (node.getClass().equals(NotNode.class))
			traversePreOrder(children.get(0), traversal, descendIntoEvents);
	}

	public List<Node> traverseInOrder() {
		List<Node> traversal = new ArrayList<>(idMap.size());
		traverseInOrder(root, traversal);
		return traversal;
	}

	public static void traverseInOrder(Node node, List<Node> inorder) {
		traverseInOrder(node, inorder, true);
	}

	public static void traverseInOrder(Node node, List<Node> inorder, boolean descendIntoEvents) {
		if (node.getClass().equals(TextNode.class)) {
			inorder.add(node);
			return;
		}
		BranchNode branchNode = (BranchNode) node;
		if (!descendIntoEvents && branchNode.getNodeType() == NodeType.EVENT) {
			inorder.add(node);
			return;
		}
		List<Node> children = branchNode.getChildren();
		if (null == children || children.size() == 0)
			return;

		// Not nodes only have a right child, for our purpose here
		if (!node.getClass().equals(NotNode.class)) {
			for (int i = 0; i < children.size(); ++i) {
				traverseInOrder(children.get(i), inorder, descendIntoEvents);
				// if the node has only one child, the node itself is traversed
				// right after the child; for more than one child, the node is
				// traversed after each child that is not the last
				if (children.size() == 1 || i < children.size() - 1)
					inorder.add(node);
			}
		}
		// if (children.size() > 1)
		// traverseInOrder(children.get(1), inorder, descendIntoEvents);
		if (node.getClass().equals(NotNode.class))
			traverseInOrder(children.get(0), inorder, descendIntoEvents);
	}

	/**
	 * This method serves to compress a strictly binary parse tree to a new
	 * parse tree where the operator nodes are not necessarily binary any more
	 * but may take an arbitrary number of children as long as associativity
	 * rules are not hurt.
	 * 
	 * @return
	 */
	public ParseTree compress() {
		ParseTree compressedTree = new ParseTree(compressNode(root), null, true);
		compressedTree.setQueryTokens(tokens);
		if (null != compressedTree.root)
			compressedTree.root.computeTreeHeight();
		return compressedTree;
	}

	/**
	 * Compresses strictly binary nodes in the tree, whose root is <tt>node</tt>
	 * by replacing binary operator nodes through {@link CompressedBooleanNode}
	 * instances that take as many children as possible without changing the
	 * tree semantics.
	 * 
	 * @param node
	 * @return
	 */
	public static Node compressNode(Node node) {
		if (null == node)
			return null;
		switch (node.getNodeType()) {
		case AND:
		case OR:
			BranchNode branchNode = (BranchNode) node;
			CompressedBooleanNode compressedNode = new CompressedBooleanNode(node.getText(), node.getNodeType());
			compressedNode.setTokenType(node.getTokenType());
			compressedNode.setQueryToken(node.getQueryToken());
			List<Node> associativeChildren = new ArrayList<>();
			getAssociativeChildren(branchNode, associativeChildren, 1);
			for (Node child : associativeChildren)
				compressedNode.add(compressNode(child));
			return compressedNode;
		case NOT:
			BranchNode notNode = (BranchNode) node;
			List<Node> notChild = new ArrayList<>();
			int numNotOperators = getAssociativeChildren(notNode, notChild, 1);
			if (numNotOperators % 2 == 0) {
				if (!notChild.isEmpty())
					return notChild.get(0);
				return null;
			} else {
				CompressedBooleanNode compressedNot = new CompressedBooleanNode(node.getText(), node.getNodeType());
				compressedNot.setTokenType(node.getTokenType());
				compressedNot.setQueryToken(node.getQueryToken());
				compressedNot.add(notChild.get(0));
				return compressedNot;
			}
		default:
			return node.copy();
		}
	}

	/**
	 * Returns all 'associative' children of <tt>node</tt>. This means that all
	 * binary intermediate nodes of the same node type to <tt>node</tt> are
	 * traversed and all of their children with another node type are returned.
	 * This can be used to get all text nodes in a long branch of nested binary
	 * AND nodes, for example.
	 * 
	 * @param node
	 * @return the length of the right most path. Used to compress NOT node
	 *         branches.
	 */
	public static int getAssociativeChildren(BranchNode node, List<Node> collector, int level) {
		int endLevel = 0;
		for (Node child : node.getChildren()) {
			NodeType childType = child.getNodeType();
			if (childType == node.getNodeType()) {
				endLevel = getAssociativeChildren((BranchNode) child, collector, level + 1);
			} else {
				collector.add(child);
				endLevel = level;
			}
		}
		return endLevel;
	}

	public static String getDefaultOperator() {
		return defaultOperator;
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
