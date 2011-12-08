package de.julielab.parsing;



/**
 * This class represents a binary node in a LR bottom-up parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class BinaryNode extends BranchNode{
	private Node leftChild;
	private Node rightChild;
	public static final String AND = "AND";
	public static final String OR = "OR";
	
	/**
	 * Constructor for binary nodes
	 * 
	 * @param text
	 *            Text of the node (AND/OR/kind of relation)
	 * @param left
	 *            Left child/subtree of the node
	 * @param right
	 *            Right child/subtree of the node
	 */
	public BinaryNode(String text, Node left, Node right) {
		this.text = text;
		this.leftChild = left;
		this.rightChild = right;
	}
	
	/**
	 * Constructor for binary nodes with yet unknown children
	 * 
	 * @param text
	 *  		Text of the node (AND/OR/kind of relation)
	 */
	public BinaryNode(String text){
		this(text, null, null);
	}

	
	/**
	 * @return True if another child can be added directly or indirectly
	 */
	@Override
	boolean canTakeChild(){
		if(leftChild==null || leftChild.canTakeChild())
			return true;
		if(rightChild==null || rightChild.canTakeChild())
			return true;
		return false;
	}
	
	/**
	 * Adds the newChild as a (in-)direct child
	 * As the parse tree grow left to right the children are added 
	 * in the same order.
	 * 
	 * @param newChild child to add
	 */
	void add(Node newChild){
		if(leftChild == null)
			leftChild = newChild;
		else if (rightChild == null)
			rightChild = newChild;
		else if (leftChild.canTakeChild())
			((BranchNode)rightChild).add(newChild);
		else if (rightChild.canTakeChild())
			((BranchNode)rightChild).add(newChild);
		else
			throw new IllegalArgumentException("No room for another child!");
	}
	
	/** 
	 * @return True if the node is a leaf.
	 */
	@Override
	boolean isLeaf(){
		return leftChild == null && rightChild == null;
	}
	
	/**
	 * @return True if the node has exactly one child.
	 */
	public boolean hasExactlyOneChild(){
		return leftChild != null ^ rightChild != null;
	}
	
	/**
	 * @return Left child of the node.
	 */
	public Node getLeftChild() {
		return leftChild;
	}

	/**
	 * 
	 * @return Right child of the node.
	 */
	public Node getRightChild() {
		return rightChild;
	}

	/**
	 * @return A String representation of this node and its subtree(s)
	 */
	@Override
	public String toString() {	
		if (leftChild != null && rightChild != null)
			return String.format("(%s %s %s)", leftChild, text, rightChild);
		else if (leftChild != null)
			return String.format("(%s %s)", leftChild, text);
		else if (rightChild != null)
			return String.format("(%s %s)", text, rightChild);
		else
			return text;
	}

	/**
	 * Removes a child node.
	 * 
	 * @param toRemove
	 *            Child to remove.
	 *            
	 * @throws
	 * 		IllegalArgumentException if child is no child 
	 * 		of this node.
	 */
	void removeChild(Node toRemove) {
		if (toRemove == leftChild)
			leftChild = null;
		else if (toRemove == rightChild)
			rightChild = null;
		else
			throw new IllegalArgumentException(toRemove+" is no child of "+this);
	}
	
	
	/**
	 * Replaces a child.
	 * 
	 * @param replaced Child to replace.
	 * @param replacement Replacement Child.
     * @throws
	 * 		IllegalArgumentException if child is no child 
	 * 		of this node.
	 */
	void replaceChild(Node replaced, Node replacement){
		if (replaced == leftChild)
			leftChild = replacement;
		else if (replaced == rightChild)
			rightChild = replacement;
		else
			throw new IllegalArgumentException(replaced+" is no child of "+this);
	}

	
	/**
	 * May only be called for nodes with exactly one child!
	 * @return The single child of this node
	 */
	Node getOnlyChild(){
		if(!hasExactlyOneChild())
			throw new IllegalArgumentException("Node doesn't have exactly one child");
		if(leftChild != null)
			return leftChild;
		return rightChild;
	}
	

	/**
	 * Sets the right child of the node.
	 * 
	 * @param child
	 *            The child.
	 */
	void setRightChild(Node child){
			rightChild = child;
	}

	/**
	 * Sets the left child of the node.
	 * 
	 * @param child
	 *            The child.
	 */
	void setLeftChild(Node child){
			leftChild = child;
	}

}
