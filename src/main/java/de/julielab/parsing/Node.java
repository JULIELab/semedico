package de.julielab.parsing;



/**
 * This class represents any kind of node in a LR bottom-up parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public abstract class Node {
	protected int id = -1; //Negative Id as error signal
	protected BranchNode parent = null;
	protected String text = null;
	
	/**
	 * @param text Text of this node, can be actual text 
	 * (e.g. "mouse") or information about syntactic role
	 * (e.g. "AND").
	 */
	public void setText(String text) {
		this.text = text;
	}
	
	/**
	 * @param id Id of this node, used for retrieval via map.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param parent Parent of this node, used for navigation in tree.
	 */
	public void setParent(BranchNode parent) {
		this.parent = parent;
	}
	
	/**
	 * @return The parent of this node.
	 */
	public BranchNode getParent() {
		return parent;
	}
	
	/**
	 * @return ID of this node.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return True if this node is a leaf.
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

	/**
	 * @return The text of this node.
	 */
	public String getText() {
		return text;
	}
}
