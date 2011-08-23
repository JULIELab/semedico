package de.julielab.lucene;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.julielab.lucene.Node.NodeType;

/**
 * A Representation of a parse tree, holding the root Node and ParseErrors.
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
		return root.recursiveToString(root);
	}

	/**
	 * 
	 * @return The root of the parse tree.
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Returns the node speccified by an id. Warning: Changing the tree causes
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
	 * @return An ParseErrors Object, representing the error encounterd during
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
		if (node.getType() == NodeType.TEXT)
			textMap.remove(node.getText());
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
		//TODO: the replacement is only text, not a parse tree, even if it looks like one in the flat form
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
			Node newNode = new Node(NodeType.AND, new Node(terms[0]), null);
			for (int i = 1; i < terms.length; ++i)
				newNode = new Node(NodeType.AND, newNode, new Node(terms[i]));
			oldNode.getParent().replaceChild(oldNode, newNode);
			remapTree();
		} else {
			textMap.remove(oldNode.getText());
			oldNode.setText(terms[0]);
			textMap.put(oldNode.getText(), oldNode);
		}

	}
	
	/**
	 * @param term Term to find in the tree.
	 * @return True if the tree has a leaf for the term.
	 */
	public boolean contains(String term){
		return textMap.containsKey(term);
	}
	
	
	/**
	 * Removes the node corresponding to the term from the parse tree.
	 * @param term Term to remove.
	 * @throws Exception 
	 */
	public void remove(String term) throws Exception{
		Node toRemove = textMap.get(term);
		if(toRemove != null)
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
	 * every node and removes some unnecessary nodes. The is done by recursing
	 * top-down left-right.
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
	private int mapTree(Node node, Node parent, int id) throws Exception {
		node.setParent(parent);
		// NOT nodes have only right children
		if(node.getType() == NodeType.NOT && node.getLeftChild() != null){
			node.setRightChild(node.getLeftChild());
			node.setLeftChild(null);
		}
		// Nodes with exactly one child are replaced by it
		else while (node.hasExactlyOneChild()) {
			Node replacement = node.getLeftChild();
			if (replacement == null)
				replacement = node.getRightChild();
			if (node == root)
				root = replacement;
			else if (node.isLeftChild())
				node.getParent().setLeftChild(replacement);
			else
				node.getParent().setRightChild(replacement);
			node = replacement;
		}

		// setting the ID
		node.setId(id);
		idMap.put(id, node);

		if (node.getType() == NodeType.TEXT)
			textMap.put(node.getText(), node);
		else {
			// Only text nodes may be leaves.
			if (node.isLeaf()) {
				parent.removeChild(node);
				idMap.remove(node.getId());
				return id - 1;
			} else { // recursing in the subtrees
				Node child = node.getLeftChild();
				if (child != null) {
					if (child.getLeftChild() != null
							&& child.getRightChild() == null)
						node.setLeftChild(child.getLeftChild());
					id = mapTree(node.getLeftChild(), node, id + 1);
				}
				child = node.getRightChild();
				if (child != null) {
					if (child.getLeftChild() != null
							&& child.getRightChild() == null)
						node.setRightChild(child.getLeftChild());
					id = mapTree(node.getRightChild(), node, id + 1);
				}
			}
		}
		return id;
	}

	/**
	 * For debugging. Tree is displayed left to right (90° rotated).
	 */
	public void displayTree(){
		displaySubtree(root, "\t");
	}
	
	
	@Deprecated
	public void displaySubtree(Node node, String indent){
		if(node.equals(NodeType.TEXT))
			System.out.println(indent + node+"-"+ node.getId());
		else
			System.out.println(indent + node+"-"+node.getId());
		if(node.getLeftChild() != null)
			displaySubtree(node.getLeftChild(), indent+"\t");
		if(node.getRightChild() != null)
			displaySubtree(node.getRightChild(), indent+"\t");
	}
}
