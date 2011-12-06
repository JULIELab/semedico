package de.julielab.Parsing;



/**
 * This class represents a node, used to build a binary parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class BinaryNode extends NonTerminalNode{
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
	
	public BinaryNode(String text){
		this(text, null, null);
	}

	/**
	 * @return True if another child can be added
	 */
	@Override
	boolean canTakeChild(){
		return leftChild == null || rightChild == null;
	}
	
	/**
	 * @param child child to add
	 */
	void addChild(Node child){
		if(leftChild == null)
			leftChild = child;
		else if (rightChild == null)
			rightChild = child;
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
	 * @param child
	 *            Child to remove.
	 */
	void removeChild(Node child) {
		if (child == leftChild)
			leftChild = null;
		else if (child == rightChild)
			rightChild = null;
	}
	/**
	 * Replaces a child.
	 * @param oldChild Child to replace.
	 * @param newChild Replacement Child.
	 */
	void replaceChild(Node oldChild, Node newChild){
		if (oldChild == leftChild)
			leftChild = newChild;
		else if (oldChild == rightChild)
			rightChild = newChild;
	}

	/**
	 * May only be called for nodes with exactly one child!
	 * @return The single child of this node
	 */
	Node getOnlyChild(){
		if(!hasExactlyOneChild())
			throw new IllegalArgumentException("Node has not exactly one child");
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
	void setRightChild(Node child) throws Exception {
			rightChild = child;
	}

	/**
	 * Sets the left child of the node.
	 * 
	 * @param child
	 *            The child.
	 */
	void setLeftChild(Node child) throws Exception {
			leftChild = child;
	}

}
