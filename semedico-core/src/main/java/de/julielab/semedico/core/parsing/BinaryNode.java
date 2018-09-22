package de.julielab.semedico.core.parsing;

import java.util.List;

import de.julielab.semedico.core.parsing.ParseTree.Serialization;

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
	 * @param text
	 *            Text of the node.
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
	 * 
	 * @param text
	 *            Text of the node.
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
//			children.add(newChild);
			newChild.setParent(this);
		} else if (rightChild == null) {
			setRightChild(newChild);
//			children.add(newChild);
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
		return toString(Serialization.NODE_TEXT);
	}

	/**
	 * Create a string representation of this node and its subtree (mostly for debugging and test purposes).
	 * 
	 * @param useTerms
	 *            True if terms instead of original text values shall be used for text tokens.
	 * @return A string representation of this node and its subtree.
	 */
	public String toString(Serialization serializationType) {
		final Node leftChild = children.size() > 0 ? children.get(0) : null;
		final Node rightChild = children.size() > 1 ? children.get(1) : null;
		String returnString = "";
		switch (serializationType) {
		case NODE_IDS:
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
		case NODE_TEXT:
		case CONCEPT_IDS:
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
		case CONCEPT_NAME_TYPE:
			// Adds [OP] for "operator" to the text of this node.
			if (leftChild != null && rightChild != null)
				returnString =
						String.format("(%s %s %s)", leftChild.toString(serializationType), text + "[OP]",
								rightChild.toString(serializationType));
			else if (leftChild != null)
				returnString = String.format("(%s %s)", leftChild.toString(serializationType), text + "[OP]");
			else if (rightChild != null)
				returnString = String.format("(%s %s)", text + "[OP]", rightChild.toString(serializationType));
			else
				returnString = text;
			break;
		default:
			break;
		}
		return returnString;
	}

	/**
	 * Removes a child node.
	 * <p>
	 * If this node is the current root and will be obsolete after the operation, the new root node will be returned.
	 * </p>
	 * 
	 * @param toRemove
	 *            Child to remove.
	 * @return the new tree node if this node was the root before, null otherwise
	 */
	// public Node removeChild(Node toRemove) {
	// Node toKeep;
	// if (toRemove == leftChild) {
	// leftChild = null;
	// toKeep = rightChild;
	// } else if (toRemove == rightChild) {
	// rightChild = null;
	// toKeep = leftChild;
	// } else
	// throw new IllegalArgumentException(toRemove + " is no child of " + this);
	//
	// if (null != getParent()) {
	// getParent().replaceChild(this, toKeep);
	// return null;
	// } else {
	// toKeep.setParent(null);
	// return toKeep;
	// }
	// }

	/**
	 * Replaces a child.
	 * 
	 * @param replaced
	 *            Child to replace.
	 * @param replacement
	 *            Replacement Child.
	 */
//	public void replaceChild(Node replaced, Node replacement) {
//		super.replaceChild(replaced, replacement);
//		replaced.parent = null;
//		replacement.parent = this;
//		if (replaced == leftChild)
//			leftChild = replacement;
//		else if (replaced == rightChild)
//			rightChild = replacement;
//		else
//			throw new IllegalArgumentException(replaced + " is no child of " + this);
//	}

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

//	@Deprecated
//	public List<Node> getChildren() {
//		List<Node> retChildren = new ArrayList<Node>();
//		if (getLeftChild() != null) {
//			retChildren.add(getLeftChild());
//		}
//		if (getRightChild() != null) {
//			retChildren.add(getRightChild());
//		}
//		return retChildren;
//	}

	/**
	 * Returns an in-order enumeration of the child node at index </tt>childIndex</tt> and all of its descendants.
	 * 
	 * @param childIndex
	 * @return
	 */
	// Exists equivalent in EventNode because currently we only have two-event-arguments. Perhaps our object model is
	// not adequate?!
//	@Deprecated
//	public List<Node> getChildrenInOrder(int childIndex) {
//		List<Node> children = getNonNullChildren();
//		if (childIndex >= children.size())
//			return Collections.emptyList();
//		Node child = children.get(childIndex);
//		List<Node> inorder = new ArrayList<>();
//		traverseInOrder(child, inorder);
//		return inorder;
//	}

	// Exists equivalent in EventNode because currently we only have two-event-arguments. Perhaps our object model is
	// not adequate?!
	/**
	 * @deprecated use {@link ParseTree#traverseInOrder(Node, List)}
	 * @param node
	 * @param inorder
	 */
//	@Deprecated
//	private void traverseInOrder(Node node, List<Node> inorder) {
//		if (node.getClass().equals(TextNode.class)) {
//			inorder.add(node);
//			return;
//		}
//		BranchNode branchNode = (BranchNode) node;
//		List<Node> children = branchNode.getChildren();
//		if (null == children || children.size() == 0)
//			return;
//
//		// Not nodes only have a right child, for our purpose here
//		if (!node.getClass().equals(NotNode.class))
//			traverseInOrder(children.get(0), inorder);
//		inorder.add(node);
//		if (children.size() > 1)
//			traverseInOrder(children.get(1), inorder);
//		if (node.getClass().equals(NotNode.class))
//			traverseInOrder(children.get(0), inorder);
//
//	}

	@Override
	public NodeType getNodeType() {
		return nodeType;
	}

	@Override
	public boolean isObsolete() {
		return getChildNumber() < 2;
	}

}
