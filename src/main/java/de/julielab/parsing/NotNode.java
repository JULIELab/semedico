package de.julielab.parsing;



/**
 * This class represents an unary node in a LR td parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its child.
 * @author hellrich
 *
 */
public class NotNode extends BranchNode {

	private Node child = null;
	private final static String TEXT = "NOT";
	
	
	/**
	 * Constructor for NotNodes with yet unknown child.
	 */
	public NotNode(){
		this(null);
	}
	
	/**
	 * Constructor for NotNodes, having only one child
	 * @param child
	 *            child/subtree of the node
	 */
	public NotNode(Node child) {
		this.child = child;
	}
	
	
	/**
	 * @return True if another child can be added
	 * directly or indirectly
	 */
	@Override
	boolean subtreeCanTakeNode(){
		return child == null || child.subtreeCanTakeNode();
	}
	
	/**
	 * Adds newChild as child or grandchild
	 * @param newChild child to add
	 */
	void add(Node newChild){
		if(child == null)
			child = newChild;
		else if(child.subtreeCanTakeNode())
				((BranchNode) child).add(newChild);
		else
		    throw new IllegalArgumentException("No room for another child!");	
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
	 * Removes a child node.
	 * 
	 * @param toRemove
	 *            Child to remove.
	 */
	void removeChild(Node toRemove) {
		if (this.child == toRemove)
			this.child = null;
		else
			throw new IllegalArgumentException(toRemove+" is no child of "+this);
	}
	
	/**
	 * Replace a child.
	 * @param replaced Child to replace.
	 * @param replacement Replacement Child.
	 */
	void replaceChild(Node replaced, Node replacement){
		if (replaced == child)
			child = replacement;
		else
			throw new IllegalArgumentException(replaced+" is no child of "+this);
	}

	/**
	 * Sets the child of this node.
	 * 
	 * @param child
	 *            The child.
	 */
	void setChild(Node child) {
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
