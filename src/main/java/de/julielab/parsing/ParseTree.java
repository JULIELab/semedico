package de.julielab.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 * A representation of a parse tree, containing the root Node and ParseErrors.
 * 
 * @author hellrich
 * 
 */
public class ParseTree {

	private Node root;
	private ParseErrors errors;
	private Map<Integer, Node> idMap = new TreeMap<Integer, Node>();
	private Map<String, Node> textMap = new HashMap<String, Node>();

	/**
	 * @param root
	 *            Root node of the ParseTree.
	 * @param errors
	 *            Object containing error messages.
	 * @throws Exception
	 *             If illegal changes of the tree are attempt.
	 */
	public ParseTree(Node root, ParseErrors errors) throws Exception {
		this.root = root;
		this.errors = errors;
		mapTree(root, null, 0);
	}

	/**
	 * @return A flat representation of the parse tree.
	 */
	public String toString() {
		return root.toString();
	}

	/**
	 * @return The root of the parse tree.
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Returns the node specified by an id. Warning: Changing the tree causes
	 * all ids to be reassigned!
	 * 
	 * @param id
	 *            The id of a node.
	 * @return The node mapped by this id.
	 */
	public Node getNode(int id) {
		return idMap.get(id);
	}

	/**
	 * 
	 * @return An ParseErrors Object, representing the errors encountered during
	 *         parsing.
	 */
	public ParseErrors getErrors() {
		return errors;
	}

	/**
	 * 
	 * @return True if any error occurred during parsing.
	 */
	public boolean hasParsingErrors() {
		return errors.hasParsingErrors();
	}

	/**
	 * Removes a node and everything below it from the parse tree. You can not
	 * delete the parse tree with this method!
	 * 
	 * @param node
	 *            Root of the subtree.
	 */
	public void removeSubtree(Node node) {
		if (node == root)
			throw new IllegalAccessError("You can't remove the root.");
		if (node.getClass().equals(TextNode.class))
			textMap.remove(node);
		idMap.remove(node.getId());
		node.getParent().removeChild(node);
	}

	/**
	 * Used for spelling correction. Substitutes all text nodes with subtrees of
	 * new text nodes connected by ANDs. Simply replaces the term with a new
	 * term if terms contains only 1 string.
	 * 
	 * @param terms
	 *            must map oldText:substitution.
	 * @throws Exception
	 *             If you try to replace a term which is not in the tree.
	 */
	public void expandTerms(Map<String, String> terms) throws Exception {
		Set<String> intersection = new HashSet<String>(textMap.keySet());
		intersection.retainAll(terms.keySet());
		for (String term : intersection)
			expandTerm(term, terms.get(term));
		//TODO: the replacement is only text, not a parse tree. Problem?
	}

	/**
	 * Used for spelling correction. Substitutes a text node with a subtree of
	 * new text nodes connected by ANDs. Simply replaces the term with a new
	 * term if terms contains only 1 string.
	 * 
	 * @param term
	 *            Term of the text node to expand.
	 * @param terms
	 *            Content for the new nodes.
	 * @throws Exception
	 *             If you try to replace a term which is not in the tree.
	 */
	public void expandTerm(String term, String... terms) throws Exception {
		Node oldNode = textMap.get(term);
		if (oldNode == null)
			throw new IllegalArgumentException("Term is not in parse tree.");
		if (terms.length > 1) {
			Node newNode = new BinaryNode(BinaryNode.AND, new TextNode(
					terms[0]), null, false);
			for (int i = 1; i < terms.length; ++i)
				newNode = new BinaryNode(BinaryNode.AND, newNode,
						new TextNode(terms[i]), false);
			oldNode.getParent().replaceChild(oldNode, newNode);
			remapTree();
		} else {
			textMap.remove(oldNode);
			oldNode.setText(terms[0]);
			textMap.put(oldNode.toString(), oldNode);
		}

	}

	/**
	 * @param term
	 *            Term to find in the tree.
	 * @return True if the tree has a leaf for the term.
	 */
	public boolean contains(String term) {
		return textMap.containsKey(term);
	}

	/**
	 * Removes the node corresponding to the term from the parse tree.
	 * 
	 * @param term
	 *            Term to remove.
	 * @throws Exception
	 */
	public void remove(String term) throws Exception {
		Node toRemove = textMap.get(term);
		if (toRemove != null)
			toRemove.getParent().removeChild(toRemove);
		remapTree();
	}

	/**
	 * @return IDs of all nodes in the tree.
	 */
	Set<Integer> getNodeIDs() {
		return idMap.keySet();
	}

	/**
	 * Removes unnecessary nodes and rebuilds the maps.
	 * 
	 * @throws Exception
	 */
	private void remapTree() throws Exception {
		idMap.clear();
		textMap.clear();
		mapTree(root, null, 0);
	}

	/**
	 * Adds all nodes of the tree in the appropriate maps, generates an ID for
	 * every node and removes some unnecessary nodes(e.g. unnecessary implict
	 * ANDs). The is done by recursing top-down left-right.
	 * 
	 * 
	 * @param node
	 *            Node to generate an ID for.
	 * @param parent
	 *            Parent node.
	 * @param idMap
	 * @param id
	 *            ID for the node.
	 * @return
	 * @throws Exception
	 */
	private int mapTree(Node node, BranchNode parent, int id)
			throws Exception {
		node.setParent(parent);
		// BinaryNodes with exactly one child are replaced by it
		while (node.getClass() == BinaryNode.class && ((BinaryNode) node).hasExactlyOneChild()) {
			Node replacement = ((BinaryNode) node).getOnlyChild();
			if (node == root)
				root = replacement;
			else
				node.getParent().replaceChild(node, replacement);
			node = replacement;
		}

		// setting the ID
		node.setId(id);
		idMap.put(id, node);

		if (node.getClass() == TextNode.class)
			textMap.put(node.getText(), node);
		else {
			if (node.isLeaf()) { // Only text nodes may be leaves.
				parent.removeChild(node);
				idMap.remove(node.getId());
				return id - 1;
			} else { // recursing in the subtrees
				if (node.getClass() == BinaryNode.class) {
					Node child = ((BinaryNode) node).getLeftChild();
					if (child != null)
						id = mapTree(child, (BinaryNode) node, id + 1);
					child = ((BinaryNode) node).getRightChild();
					if (child != null)
						if (child != null)
							id = mapTree(child, (BinaryNode) node, id + 1);
				}
			}
		}
		return id;
	}

	/**
	 * For debugging.
	 */
	public void displayTree() {
		System.out.println(root);
	}

	public String getRelations() {
		List<String> relations = new ArrayList<String>(5);
		for(Node n : idMap.values())
			if(n.isRelation()){
				System.out.println(n);
				relations.add("PPI:"+(n.toString().replaceAll("\\(|\\)", "")));
			}
		return StringUtils.join(relations, " AND ");
	}
}
