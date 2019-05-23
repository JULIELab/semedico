package de.julielab.semedico.core.parsing;

import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;

/**
 * This class represents an unary node in a LR td parse tree. It contains methods to query and modify the properties of
 * the node, e.g. its child.
 * 
 * @author hellrich
 * 
 */
public class NotNode extends BranchNode {

	/**
	 * Constructor for NotNodes with yet unknown child.
	 */
	public NotNode() {
		this((Node)null);
	}

	/**
	 * Constructor for NotNodes, having only one child.
	 * 
	 * @param child
	 *            Child / subtree of the node.
	 */
	public NotNode(Node child) {
		super(NodeType.NOT.name());
		add(child);
	}

	public NotNode(QueryToken qt) {
		super(NodeType.NOT.name());
		this.queryToken = qt;
	}

	/**
	 * Determine if another child can be added directly or indirectly.
	 * 
	 * @return True if another child can be added.
	 */
	@Override
	public boolean subtreeCanTakeNode() {
		return children.isEmpty() || children.get(0).subtreeCanTakeNode();
	}

	/**
	 * Adds newChild as child or grandchild.
	 * 
	 * @param newChild
	 *            Child to add.
	 */
	public void add(Node newChild) {
		if (null == newChild)
			return;
		if (children.isEmpty()) {
			children.add(newChild);
			newChild.setParent(this);
		} else if (children.get(0).subtreeCanTakeNode())
			((BranchNode) children.get(0)).add(newChild);
		else
			throw new IllegalArgumentException("No room for another child!");
	}

	/**
	 * Get the child of this node.
	 * 
	 * @return The child of this node (may be null).
	 */
	public Node getChild() {
		return children.isEmpty() ? null : children.get(0);
	}

	/**
	 * Determine if this node is a leaf.
	 * 
	 * @return True if the node is a leaf.
	 */
	@Override
	public boolean isLeaf() {
		return getChildNumber() == 0;
	}

	/**
	 * Set the child of this node.
	 * 
	 * @param child
	 *            The child.
	 */
	public void setChild(Node child) {
		if (!children.isEmpty())
			children.set(0, child);
		else
			children.add(child);
	}

	/**
	 * Build a string representation of this node and its subtree.
	 * 
	 * @return The string representation of this node and its subtree
	 */
	@Override
	public String toString(SERIALIZATION serializationType) {
		Node child = getChild();
		String returnString = "";
		switch (serializationType) {
		case TEXT:
		case TERMS:
			returnString = String.format("(%s %s)", NodeType.NOT.name(), child != null ? child.toString(serializationType) : null);
			break;
		case IDS:
			returnString =
					String.format("(%s %s)", String.valueOf(id) + "{" + NodeType.NOT.name() + "}",
							child != null ? child.toString(serializationType) : null);
			break;
		}
		return returnString;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.NOT;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public boolean isObsolete() {
		return children.size() < 1;
	}

}
