package de.julielab.semedico.core.parsing;

import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;

/**
 * This class represents a binary node in a LR td parse tree.
 * 
 * @author hellrich
 * 
 */
public class BinaryNode extends BranchNode {

	private NodeType nodeType;

	/**
	 * Constructor for binary node with known children.
	 * 
	 * @param nodeType
	 *            AND or OR.
	 * @param left
	 *            Left child / subtree of the node.
	 * @param right
	 *            Right child / subtree of the node.
	 */
	public BinaryNode(NodeType nodeType, Node left, Node right) {
		super(nodeType.name());
		switch (nodeType) {
		case AND:
		case OR:
			this.nodeType = nodeType;
			break;
		default:
			throw new IllegalArgumentException("Only boolean operators AND and OR are allowed as node type, " + nodeType
					+ " is not one of them.");
		}
		add(left);
		add(right);
	}

	/**
	 * Constructor for binary node with yet unknown children.
	 */
	public BinaryNode(NodeType nodeType) {
		this(nodeType, null, null);
	}

	/**
	 * Determine if another child can be added directly or indirectly to the node.
	 * 
	 * @return True if another child can be added.
	 */
	@Override
	public boolean subtreeCanTakeNode() {
		if (getLeftChild() == null || getLeftChild().subtreeCanTakeNode())
			return true;
		if (getRightChild() == null || getRightChild().subtreeCanTakeNode())
			return true;
		return false;
	}

	/**
	 * Adds ad child as a (in-)direct child. As the parse tree grows left to right the children are added in the same
	 * order.
	 * 
	 * @param newChild
	 *            Child to add.
	 */
	public void add(Node newChild) {
		final Node leftChild = getLeftChild();
		final Node rightChild = getRightChild();

		if (null == newChild)
			return;
		if (leftChild == null) {
			setLeftChild(newChild);
			newChild.setParent(this);
		} else if (rightChild == null) {
			setRightChild(newChild);
			newChild.setParent(this);
		} else if (leftChild.subtreeCanTakeNode())
			((BranchNode) leftChild).add(newChild);
		else if (rightChild.subtreeCanTakeNode())
			((BranchNode) rightChild).add(newChild);
		else
			throw new IllegalArgumentException("No room for another child!");
	}

	/**
	 * Determine if the node is a leaf.
	 * 
	 * @return True if the node is a leaf.
	 */
	@Override
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/**
	 * Get the left child of the node.
	 * 
	 * @return Left child of the node.
	 */
	public Node getLeftChild() {
		return children.size() > 0 ? children.get(0) : null;
	}

	/**
	 * Get the right child of the node.
	 * 
	 * @return Right child of the node.
	 */
	public Node getRightChild() {
		return children.size() > 1 ? children.get(1) : null;
	}

	@Override
	public String toString() {
		return toString(SERIALIZATION.TEXT);
	}

	/**
	 * Create a string representation of this node and its subtree (mostly for debugging and test purposes).
	 * 
	 * @return A string representation of this node and its subtree.
	 */
	public String toString(SERIALIZATION serializationType) {
		final Node leftChild = children.size() > 0 ? children.get(0) : null;
		final Node rightChild = children.size() > 1 ? children.get(1) : null;
		String returnString = "";
		switch (serializationType) {
		case IDS:
			if (leftChild != null && rightChild != null)
				returnString =
						String.format("(%s %s %s)", leftChild.toString(serializationType), String.valueOf(id) + "{"
								+ text
								+ "}", rightChild.toString(serializationType));
			else if (leftChild != null)
				returnString =
						String.format("(%s %s)", leftChild.toString(serializationType), String.valueOf(id) + "{"
								+ text
								+ "}");
			else if (rightChild != null)
				returnString =
						String.format("(%s %s)", String.valueOf(id) + "{" + text + "}",
								rightChild.toString(serializationType));
			else
				returnString = String.valueOf(id) + "{" + text + "}";
			break;
		case TEXT:
		case TERMS:
			if (leftChild != null && rightChild != null)
				returnString =
						String.format("(%s %s %s)", leftChild.toString(serializationType), text,
								rightChild.toString(serializationType));
			else if (leftChild != null)
				returnString = String.format("(%s %s)", leftChild.toString(serializationType), text);
			else if (rightChild != null)
				returnString = String.format("(%s %s)", text, rightChild.toString(serializationType));
			else
				returnString = text;
			break;
		}
		return returnString;
	}

	/**
	 * Set the right child of the node.
	 * 
	 * @param child
	 *            The child to set.
	 */
	public void setRightChild(Node child) {
		if (children.size() > 1)
			children.set(1, child);
		else if (children.size() == 1)
			children.add(child);
		else {
			// children is empty
			children.add(null);
			children.add(child);
		}
	}

	/**
	 * Set the left child of the node.
	 * 
	 * @param child
	 *            The child to set.
	 */
	public void setLeftChild(Node child) {
		if (children.size() > 0)
			children.set(0, child);
		else
			children.add(child);
	}

	@Override
	public NodeType getNodeType() {
		return nodeType;
	}

	@Override
	public boolean isObsolete() {
		return getChildNumber() < 2;
	}

}
