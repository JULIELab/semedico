package de.julielab.parsing;



/**
 * This class represents a node with child(-ren) in a LR bottom-up parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public abstract class BranchNode extends Node {

	/**
	 * @param child - Node to add as child
	 */
	abstract void add(Node child);
	
	
	/**
	 * Removes a child node.
	 * 
	 * @param child
	 *            Child to remove.
	 */
	abstract void removeChild(Node child);
	
	
	/**
	 * Replace a child.
	 * @param oldChild Child to replace.
	 * @param newChild Replacement Child.
	 */
	abstract void replaceChild(Node oldChild, Node newChild);

}
