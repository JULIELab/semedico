package de.julielab.semedico.core.parsing;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;

public class CompressedBooleanNode extends BranchNode {

//	private List<Node> children;
	private NodeType nodeType;

	public CompressedBooleanNode(String text, NodeType nodeType, Node... initChildren) {
		super(text);
		switch (nodeType) {
		case AND:
		case NOT:
		case OR:
			this.nodeType = nodeType;
			break;
		default:
			throw new IllegalArgumentException("Only boolean operators are allowed as node type, " + nodeType
					+ " is not one of them.");
		}
		children = new ArrayList<>();
		if (initChildren.length > 0) {
			for (int i = 0; i < initChildren.length; i++) {
				Node child = initChildren[i];
				add(child);
			}
		}
	}

	/**
	 * Adds <tt>child</tt> to this exact node, appending it to the list of already existing children.
	 * 
	 * @param child
	 */
	@Override
	public void add(Node child) {
		if (null == child)
			return;
		child.setParent(this);
		children.add(child);
	}

//	@Override
//	public Node removeChild(Node child) {
//		int childIndex = children.indexOf(child);
//		if (childIndex >= 0) {
//			Node removedChild = children.remove(childIndex);
//			removedChild.setParent(null);
//			// Collapse this subtree if there is only one child left.
//			// Of course, this operation should never be done in case of a NOT node. However, a NOT node would have zero
//			// children left after the removal, so no danger here.
//			if (children.size() == 1) {
//				if (null != parent) {
//					parent.replaceChild(this, children.get(0));
//				} else {
//					Node newRoot = children.get(0);
//					newRoot.setParent(null);
//					return newRoot;
//				}
//			}
//			if (children.isEmpty()) {
//				parent.removeChild(this);
//			}
//		}
//		return null;
//	}


	@Override
	public boolean isLeaf() {
		return false;
	}

	@Override
	public String toString(SERIALIZATION serializationType) {
		List<String> childStrings = new ArrayList<>();
		for (Node child : children)
			childStrings.add(child.toString(serializationType));
		if (nodeType != NodeType.NOT)
			return "(" + StringUtils.join(childStrings, " " + nodeType.name() + " ") + ")";
		else
			return "(" + nodeType.name() + " " + childStrings.get(0) + ")";
	}

	@Override
	public boolean subtreeCanTakeNode() {
		return true;
	}

	@Override
	public NodeType getNodeType() {
		return nodeType;
	}

	@Override
	public boolean isObsolete() {
		switch (nodeType) {
		case AND:
		case OR:
			return children.size() < 2;
		case NOT:
			return children.size() < 1;
		default:
			throw new IllegalStateException("This boolean node has no boolean node type, which is illegal.");
		}
	}

}
