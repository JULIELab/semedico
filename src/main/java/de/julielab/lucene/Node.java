package de.julielab.lucene;



/**
 * This class represents a node, used to build a binary parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class Node {
	
	public enum NodeType{
		AND, OR, TEXT, ROOT, NOT;	
		/* Do not remove ROOT! 
		 * It is necessary for the Parser.
		 * (You will get wrong error messages 
		 *  if you use another type for the root node)
		 */
	}

	private Node leftChild;
	private Node rightChild;
	private NodeType type;
	private int id;
	private String text;
	public void setId(int id) {
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	private Node parent;

	/**
	 * Constructor for text nodes (leaves!)
	 * 
	 * @param text
	 *            Text in the leaf.
	 */
	public Node(String text) {
		this.type = NodeType.TEXT;
		this.text = text;
	}

	/**
	 * Constructor for non-text nodes
	 * 
	 * @param type
	 *            Type of the node
	 * @param left
	 *            Left child/subtree of the node
	 * @param right
	 *            Right child/subtree of the node
	 */
	public Node(NodeType type, Node left, Node right) {
		this.type = type;
		leftChild = left;
		rightChild = right;
	}
	
	
	/**
	 * 
	 * @return True if the node is a leaf.
	 */
	boolean isLeaf(){
		return leftChild == null && rightChild == null;
	}

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
	 * @return True if the node has exactly one child.
	 */
	boolean hasExactlyOneChild(){
		return leftChild != null ^ rightChild != null;
	}
	
	
	/**
	 * 
	 * @return The parent of the node.
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * 
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
	 * 
	 * @return ID of the node.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return The text if it's a text node, otherwise its type.
	 */
	public String toString() {
		if (type == NodeType.TEXT)
			return text;
		else
			return type.toString();
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
	 * Replace a child.
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
	 * 
	 * @return The type of the node.
	 */
	NodeType getType() {
		return type;
	}

	/**
	 * 
	 * @return The text of the node.
	 */
	String getText() {
		return text;
	}

	/**
	 * Sets the right child of the node.
	 * 
	 * @param child
	 *            The child.
	 * @throws Exception
	 *             If node has type text.
	 */
	void setRightChild(Node child) throws Exception {
		if (type != NodeType.TEXT)
			rightChild = child;
		else
			throw new Exception("Text nodes must not have children.");
	}

	/**
	 * Sets the left child of the node.
	 * 
	 * @param child
	 *            The child.
	 * @throws Exception
	 *             If node has type text.
	 */
	void setLeftChild(Node child) throws Exception {
		if (type != NodeType.TEXT)
			leftChild = child;
		else
			throw new Exception("Text nodes must not have children.");
	}

	/**
	 * Sets the type of the node.
	 * 
	 * @param type
	 *            New Type of the node.
	 * @throws Exception
	 *             If setting type to TEXT for a node with children.
	 */
	void setType(NodeType type) throws Exception {
		if ((type != NodeType.TEXT)
				|| (leftChild == null && rightChild == null))
			this.type = type;
		else
			throw new Exception("Text nodes must not have children.");
	}

	public String recursiveToString(Node node) {
		if(node.type == NodeType.TEXT)
			return node.getText();
		else if(node.leftChild == null)
			return String.format("(%s %s)", node, recursiveToString(node.rightChild));
		else if (node.rightChild == null)
			return String.format("(%s %s)", recursiveToString(node.leftChild), node);
		else
			return String.format("(%s %s %s)", recursiveToString(node.leftChild), node, recursiveToString(node.rightChild));
	}

}
