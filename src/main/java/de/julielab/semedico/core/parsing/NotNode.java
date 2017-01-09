package de.julielab.semedico.core.parsing;

import java.util.ArrayList;
import java.util.List;

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
	@Deprecated
	private Node child = null;
	@Deprecated
	private final static String TEXT = "NOT";

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
		// this.child = child;
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
	 * Remove a child.
	 * 
	 * @param toRemove
	 *            Child to remove.
	 */
	// public Node removeChild(Node toRemove) {
	// if (this.child == toRemove)
	// this.child = null;
	// else
	// throw new IllegalArgumentException(toRemove + " is no child of " + this);
	//
	// if (null != getParent()) {
	// return getParent().removeChild(this);
	// } else {
	// return null;
	// }
	// }

	/**
	 * Replace a child.
	 * 
	 * @param replaced
	 *            Child to replace.
	 * @param replacement
	 *            Replacement Child.
	 */
	// public void replaceChild(Node replaced, Node replacement) {
	// super.replaceChild(replaced, replacement);
	// replaced.parent = null;
	// replacement.parent = this;
	// if (replaced == child)
	// child = replacement;
	// else
	// throw new IllegalArgumentException(replaced + " is no child of " + this);
	// }

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

//	public List<Node> getChildren() {
//		List<Node> children = new ArrayList<Node>();
//		if (child != null) {
//			children.add(child);
//		}
//		return children;
//	}

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
