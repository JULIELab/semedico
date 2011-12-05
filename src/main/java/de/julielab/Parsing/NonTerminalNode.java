package de.julielab.Parsing;



/**
 * This class represents a node, used to build a binary parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public abstract class NonTerminalNode extends Node {

	
	private String text;


	/**
	 * Constructor for not-nodes, having only right children
	 * 
	 * @param type
	 *            Type of the node
	 * @param child
	 *            child/subtree of the node
	 */
	public NonTerminalNode(NodeType type, NonTerminalNode child) {
		if(type != NodeType.NOT)
			throw new IllegalArgumentException();
		this.rightChild = child;
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
	public NonTerminalNode(NodeType type, NonTerminalNode left, NonTerminalNode right) {
		if(!(type==NodeType.AND || type==NodeType.OR || type==NodeType.RELATION || type==NodeType.ROOT))
			throw new IllegalArgumentException();
		this.leftChild = left;
		this.rightChild = right;
	}
	
	/**
	 * Constructor for nodes with text and children (relations)
	 * 
	 * @param type
	 *            Type of the node
	 * @param text
	 *            Text of the node (kind of relation)
	 * @param left
	 *            Left child/subtree of the node
	 * @param right
	 *            Right child/subtree of the node
	 */
	public NonTerminalNode(NodeType type, String text, NonTerminalNode left, NonTerminalNode right) {
		if(type != NodeType.RELATION)
			throw new IllegalArgumentException();
		this.text = text;
		this.leftChild = left;
		this.rightChild = right;
	}
	
	/**
	 * Abstracts adding children, makes NOT nodes easy
	 * @return True if anothe child can be added
	 */
	boolean canTakeChild(){
		if(type == NodeType.TEXT)
			return false;
		else if(type == NodeType.NOT)
			return leftChild == null;	//NOT nodes have only left children
		else
			return leftChild == null || rightChild == null;
	}
	
	/**
	 * Abstracts adding children, makes NOT nodes easy
	 * @param child child to add
	 */
	void addChild(Node child){
		if(type == NodeType.TEXT)
			throw new IllegalArgumentException("Text nodes have no children!");
		if(leftChild == null)
			leftChild = child;
		else if (rightChild == null && type != NodeType.NOT)
			rightChild = child;
		else
			throw new IllegalArgumentException("No room for another child!");
	}
	
	/**
	 * Abstracts adding children, makes NOT nodes easy
	 * @return Correct child for further growth of the subtree
	 */
	Node getCornerChild(){
		if(type == NodeType.TEXT)
			throw new IllegalArgumentException("Text nodes have no children!");
		if(leftChild != null || type == NodeType.NOT)
			return leftChild;
		else if (rightChild != null)
			return rightChild;
		else if (leftChild != null )
			return leftChild;
		else 
			throw new IllegalArgumentException("No  child available!");
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
	public NonTerminalNode getParent() {
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
	 * @return The type/text of this node and all it's children
	 */
	public String toString() {
		if (type == NodeType.TEXT || type == NodeType.RELATION)
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
	void removeChild(NonTerminalNode child) {
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
	void replaceChild(NonTerminalNode oldChild, NonTerminalNode newChild){
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
	void setRightChild(NonTerminalNode child) throws Exception {
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
	void setLeftChild(NonTerminalNode child) throws Exception {
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

	public String recursiveToString(NonTerminalNode node) {
		if(node.type == NodeType.TEXT)
			return node.getText();
		else if(node.leftChild == null)
			return String.format("(%s %s)", node, recursiveToString(node.rightChild));
		else if (node.rightChild == null)
			return String.format("(%s %s)", recursiveToString(node.leftChild), node);
		else
			return String.format("(%s %s %s)", recursiveToString(node.leftChild), node, recursiveToString(node.rightChild));
	}

	@Override
	boolean canTakeChild() {
		// TODO Auto-generated method stub
		return false;
	}

}
