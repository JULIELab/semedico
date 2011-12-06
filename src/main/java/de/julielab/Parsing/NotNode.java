package de.julielab.Parsing;



/**
 * This class represents a node, used to build a binary parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class NotNode extends NonTerminalNode {

	private Node child = null;
	private final static String TEXT = "NOT";
	
	/**
	 * Constructor for NotNodes, having only one child
	 */
	public NotNode(){
	}
	
	/**
	 * Constructor for NotNodes, having only one child
	 * @param child
	 *            child/subtree of the node
	 */
	public NotNode(Node child) {
		this.child = child;
	}
	
	public void setParent(NotNode parent) {
		this.parent = parent;
	}
	
	/**
	 * @return True if another child can be added
	 */
	@Override
	boolean canTakeChild(){
		return child == null;
	}
	
	/**
	 * Abstracts adding children, makes NOT nodes easy
	 * @param child child to add
	 */
	void addChild(Node child){
		if(this.child != null)
			throw new IllegalArgumentException("No room for another child!");
		this.child = child;
	}
	
	/**
	 * @return The child of this node (may be null)
	 */
	Node getChild(){
		return child;
	}
	
	
	/**
	 * 
	 * @return True if the node is a leaf.
	 */
	@Override
	boolean isLeaf(){
		return child == null;
	}
	
	/**
	 * 
	 * @return True if the node has exactly one child.
	 */
	boolean hasExactlyOneChild(){
		return child != null;
	}



	/**
	 * Removes a child node.
	 * 
	 * @param toRemove
	 *            Child to remove.
	 */
	void removeChild(Node toRemove) {
		if (this.child == toRemove)
			this.child = null;
	}
	
	/**
	 * Replace a child.
	 * @param oldChild Child to replace.
	 * @param newChild Replacement Child.
	 */
	void replaceChild(Node oldChild, Node newChild){
		if (oldChild == child)
			child = newChild;
	}

	/**
	 * Sets the child of this node.
	 * 
	 * @param child
	 *            The child.
	 * @throws Exception
	 *             If node has type text.
	 */
	void setChild(Node child) throws Exception {
		this.child = child;
	}


	/**
	 * @return A String representation of this node and its subtree
	 */
	@Override
	public String toString() {
		return String.format("(%s %s)", TEXT, child);
	}
}
