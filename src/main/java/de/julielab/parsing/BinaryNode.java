package de.julielab.parsing;

/**
 * This class represents a binary node in a LR td parse tree. It contains
 * methods to query and modify the properties of the node, e.g. its children.
 * 
 * @author hellrich
 * 
 */
public class BinaryNode extends BranchNode {
	private Node leftChild;
	private Node rightChild;
	boolean relation;
	public static final String AND = "AND";
	public static final String OR = "OR";

	/**
	 * Constructor for binary nodes
	 * 
	 * @param value
	 *            value of a Symbol, may be a String (AND/OR/kind of relation)
	 *            or a String[], containing text and mapped text.
	 * @param left
	 *            Left child/subtree of the node
	 * @param right
	 *            Right child/subtree of the node
	 * @param relation
	 *            true if this node encodes a relation (important for printing)
	 * @throws IllegalArgumentException
	 *             if value is neither String nor String[]
	 */
	public BinaryNode(Object value, Node left, Node right, boolean relation) {
		if (value instanceof String)
			this.text = (String) value;
		else if (value instanceof String[]) {
			this.text = ((String[]) value)[TEXT];
			this.setMappedText(((String[]) value)[MAPPED_TEXT]);
		} else
			throw new IllegalArgumentException(
					"Value must be a String or a String[]");
		this.leftChild = left;
		this.rightChild = right;
		this.relation = relation;
	}

	/**
	 * Constructor for binary nodes with yet unknown children
	 * 
	 * @param value
	 *            value of a Symbol, may be a String (AND/OR/kind of relation)
	 *            or a String[], containing text and mapped text.
	 * @param relation
	 *            true if this node encodes a relation (important for printing)
	 * @throws IllegalArgumentException
	 *             if value is neither String nor String[]
	 */
	public BinaryNode(Object value, boolean relation) {
		this(value, null, null, relation);
	}

	/**
	 * @return True if another child can be added directly or indirectly
	 */
	@Override
	boolean subtreeCanTakeNode() {
		if (leftChild == null || leftChild.subtreeCanTakeNode())
			return true;
		if (rightChild == null || rightChild.subtreeCanTakeNode())
			return true;
		return false;
	}

	/**
	 * Adds the newChild as a (in-)direct child As the parse tree grow left to
	 * right the children are added in the same order.
	 * 
	 * @param newChild
	 *            child to add
	 */
	void add(Node newChild) {
		if (leftChild == null)
			leftChild = newChild;
		else if (rightChild == null)
			rightChild = newChild;
		else if (leftChild.subtreeCanTakeNode())
			((BranchNode) rightChild).add(newChild);
		else if (rightChild.subtreeCanTakeNode())
			((BranchNode) rightChild).add(newChild);
		else
			throw new IllegalArgumentException("No room for another child!");
	}

	/**
	 * @return True if the node is a leaf.
	 */
	@Override
	boolean isLeaf() {
		return leftChild == null && rightChild == null;
	}

	/**
	 * @return True if the node has exactly one child.
	 */
	public boolean hasExactlyOneChild() {
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
		if (relation) {
			if (leftChild != null && rightChild != null)
				return String.format("(%s-%s-%s)", leftChild.toString().equals("protein")?"*":leftChild, text, rightChild.toString().equals("protein")?"*":rightChild);
			else if (leftChild != null)
				return String.format("(%s-%s-*)", leftChild.toString().equals("protein")?"*":leftChild, text);
			else if (rightChild != null)
				return String.format("(*-%s-%s)", text, rightChild.toString().equals("protein")?"*":rightChild);
			else
				return text;
		} else {
			if (leftChild != null && rightChild != null)
				return String.format("(%s %s %s)", leftChild, text, rightChild);
			else if (leftChild != null)
				return String.format("(%s %s)", leftChild, text);
			else if (rightChild != null)
				return String.format("(%s %s)", text, rightChild);
			else
				return text;
		}
	}

	/**
	 * Removes a child node.
	 * 
	 * @param toRemove
	 *            Child to remove.
	 * 
	 * @throws IllegalArgumentException
	 *             if child is no child of this node.
	 */
	void removeChild(Node toRemove) {
		if (toRemove == leftChild)
			leftChild = null;
		else if (toRemove == rightChild)
			rightChild = null;
		else
			throw new IllegalArgumentException(toRemove + " is no child of "
					+ this);
	}

	/**
	 * Replaces a child.
	 * 
	 * @param replaced
	 *            Child to replace.
	 * @param replacement
	 *            Replacement Child.
	 * @throws IllegalArgumentException
	 *             if child is no child of this node.
	 */
	void replaceChild(Node replaced, Node replacement) {
		if (replaced == leftChild)
			leftChild = replacement;
		else if (replaced == rightChild)
			rightChild = replacement;
		else
			throw new IllegalArgumentException(replaced + " is no child of "
					+ this);
	}

	/**
	 * May only be called for nodes with exactly one child!
	 * 
	 * @return The single child of this node
	 */
	Node getOnlyChild() {
		if (!hasExactlyOneChild())
			throw new IllegalArgumentException(
					"Node doesn't have exactly one child");
		if (leftChild != null)
			return leftChild;
		return rightChild;
	}

	/**
	 * Sets the right child of the node.
	 * 
	 * @param child
	 *            The child.
	 */
	void setRightChild(Node child) {
		rightChild = child;
	}

	/**
	 * Sets the left child of the node.
	 * 
	 * @param child
	 *            The child.
	 */
	void setLeftChild(Node child) {
		leftChild = child;
	}

}
