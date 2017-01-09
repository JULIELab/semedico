package de.julielab.semedico.core.parsing;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a node with child(-ren) in a LR td parse tree. It contains methods to query and modify the
 * properties of the node, e.g. its children.
 * 
 * @author hellrich
 * 
 */
public abstract class BranchNode extends Node {
	protected List<Node> children;
	private boolean atomic;

	@Override
	public boolean isAtomic() {
		return atomic;
	}

	public void setAtomic(boolean atomic) {
		this.atomic = atomic;
	}

	/**
	 * 
	 * @param text
	 *            The textual representation of this node.
	 */
	public BranchNode(String text) {
		super(text);
		children = new ArrayList<>();
	}

	/**
	 * Adds a child.
	 * 
	 * @param child
	 *            Node to add as child.
	 */
	public abstract void add(Node child);

	/**
	 * Removes a child node.
	 * 
	 * @param child
	 *            Child to remove.
	 * @return The new tree node if it has changed through the operation, null otherwise
	 */
	// public abstract Node removeChild(Node child);
	public void removeChild(Node child) {
		int childIndex = children.indexOf(child);
		if (childIndex >= 0) {
			Node removedChild = children.remove(childIndex);
			removedChild.setParent(null);
			// Collapse this subtree if this branch node is obsolete after the removal.
			// if (isObsolete() && hasExactlyOneChild()) {
			// if (null != parent) {
			// parent.replaceChild(this, children.get(0));
			// return null;
			// } else {
			// Node newRoot = children.get(0);
			// newRoot.setParent(null);
			// return newRoot;
			// }
			// }
		}
		// return null;
	}

	public abstract boolean isObsolete();

	/**
	 * Get an only child. May only be called for nodes with exactly one child!
	 * 
	 * @return The single child of this node.
	 */
	public Node getOnlyChild() {
		if (!hasExactlyOneChild())
			throw new IllegalArgumentException("Node doesn't have exactly one child");
		return children.get(0);
	}

	/**
	 * Returns the number of non-null children.
	 * 
	 * @return
	 */
	public int getChildNumber() {
//		int num = 0;
//		for (Node child : children) {
//			if (null != child)
//				++num;
//		}
//		return num;
		return children.size();
	}

	/**
	 * Replaces a child.
	 * 
	 * @param replaced
	 *            Child to replace.
	 * @param replacement
	 *            Replacement Child.
	 */
	public void replaceChild(Node replaced, Node replacement) {
		if (null == replacement) {
			throw new IllegalArgumentException("The replacement cannot be null. Use removeChild() instead.");
		}
		int childIndex = children.indexOf(replaced);
		if (childIndex >= 0) {
			if (replaced.parent == this)
				replaced.parent = null;
			replacement.parent = this;
			children.set(childIndex, replacement);
		}
	}

	public List<Node> getChildren() {
//		List<Node> ret = new ArrayList<>();
//		for (Node child : children) {
//			if (null != child)
//				ret.add(child);
//		}
//		return ret;
		return children;
	}

	public Node getFirstChild() {
		List<Node> children = getChildren();
		if (null == children || children.isEmpty())
			return null;
		return children.get(0);
	}

	public Node getLastChild() {
		List<Node> children = getChildren();
		if (null == children || children.isEmpty())
			return null;
		return children.get(children.size() - 1);
	}

	public Node getRightmostLeaf() {
		Node rightmostChild = getLastChild();
		if (!(rightmostChild instanceof BranchNode))
			return rightmostChild;
		return ((BranchNode) rightmostChild).getRightmostLeaf();
	}

	/**
	 * Determine if the node has exactly one child.
	 * 
	 * @return True if the node has exactly one child.
	 */
	public boolean hasExactlyOneChild() {
		return getChildren().size() == 1;
	}

}
