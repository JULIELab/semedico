package de.julielab.Parsing;



/**
 * This class represents a node, used to build a binary parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public abstract class Node {

	protected int id;
	protected NonTerminalNode parent;
	protected String text;
	
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setParent(NonTerminalNode parent) {
		this.parent = parent;
	}
	
	/**
	 * Abstracts adding children, makes NOT nodes easy
	 * @return True if another child can be added
	 */
	abstract boolean canTakeChild();	
	
	/**
	 * 
	 * @return True if the node is a leaf.
	 */
	abstract boolean isLeaf();

	/**
	 * 
	 * @return True if this node is the left child of its parent.
	 */
	boolean isLeftChild(){
		if(parent != null && parent.leftChild == this)
			return true;
		return false;
	}
	
	/**
	 * 
	 * @return True if this node is the right child of its parent.
	 */
	boolean isRightChild(){
		if(parent != null && parent.rightChild == this)
			return true;
		return false;
	}
	
	/**
	 * 
	 * @return The parent of the node.
	 */
	public NonTerminalNode getParent() {
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
	 * @return The text if it's a text node, otherwise the subtree underneath this nonterminal.
	 */
	abstract public String toString();

}
