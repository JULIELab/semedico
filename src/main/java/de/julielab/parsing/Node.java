package de.julielab.parsing;



/**
 * This class represents any kind of node in a LR bottom-up parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public abstract class Node {
	protected int id = -1; //Negative Id as error signal
	protected BranchNode parent;
	protected String text;
	
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setParent(BranchNode parent) {
		this.parent = parent;
	}
	
	/**
	 * 
	 * @return The parent of the node.
	 */
	public BranchNode getParent() {
		return parent;
	}
	
	/**
	 * 
	 * @return ID of the node.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * 
	 * @return True if the node has exactly one child.
	 */
	abstract boolean hasExactlyOneChild();
	
	/**
	 * 
	 * @return True if the node is a leaf.
	 */
	abstract boolean isLeaf();
	
	/**
	 * @return The text if it's a text node, otherwise the subtree underneath this nonterminal.
	 */
	abstract public String toString();
	
	
	/**
	 * @return True if a child can be added
	 */
	abstract boolean canTakeChild();

	public String getText() {
		return text;
	}
}
