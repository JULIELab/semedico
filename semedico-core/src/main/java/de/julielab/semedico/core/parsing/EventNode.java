package de.julielab.semedico.core.parsing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.search.query.bool.BoolElement;
import de.julielab.semedico.core.search.query.bool.ComplexBoolElement;
import de.julielab.semedico.core.search.query.bool.EventBoolElement;
import de.julielab.semedico.core.search.query.bool.LiteralBoolElement;
import de.julielab.semedico.core.search.query.bool.OperatorBoolElement;
import de.julielab.semedico.core.search.query.bool.OperatorBoolElement.BoolOperator;

/**
 * This class represents a node constituting an event.
 * 
 * @author hellrich, faessler
 * @deprecated The attempt to recognize event structures in the user query
 *             failed due to several reasons. First of all, it certainly failed
 *             to capture an intuitive understanding of events for non-NLP
 *             people, i.e. biologist who should actually work with them. Also,
 *             the whole approach of trying to parse event structures and match
 *             them to events indexed in documents has the problem that events
 *             are complex nested structures in general. That is, the user will
 *             most like only describe in a rather fuzzy way what relation she
 *             meant. To take the query verbatim firstly excludes a large
 *             variety of possibly interesting hits and also this query facility
 *             is not able to match nested events anyway. The new approach will
 *             be to just search in flattened event structures, throwing away
 *             some information but (hopefully) still coming up with nice search
 *             results. For those reasons, this class won't be used any more in
 *             the rest of the code, i.e. if it would be used, it would be
 *             ignored or event cause errors.
 */
@Deprecated
public class EventNode extends BranchNode implements ConceptNode {
	private Node leftChild;
	private Node rightChild;
	/**
	 * List of alternatives for the likelihood.
	 */
	private List<String> likelihoods;
	/**
	 * Terms describing the type of this event. If there are multiple, the event
	 * name was ambiguous.
	 */
	private List<IConcept> eventTypes;
	/**
	 * Whether this node belongs to a binary event or not. Whether this is true
	 * or not is defined within the QueryTokens that are used to create the
	 * ParseTree, so this node does not have a say in this matter but is just
	 * defined to be binary or not.
	 */
	private boolean isBinary;

	/**
	 * Constructor for binary node with yet unknown children.
	 * 
	 * @param text
	 *            Text of the node.
	 */
	public EventNode(String text, List<IConcept> eventTypes, boolean isBinary) {
		super(text);
		this.eventTypes = eventTypes;
		this.isBinary = isBinary;
	}

	public List<String> getLikelihoods() {
		return likelihoods;
	}

	/**
	 * Returns an in-order enumeration of the child node at index
	 * </tt>childIndex</tt> and all of its descendants.
	 * 
	 * @param childIndex
	 * @return
	 */
	// Exists equivalent in BinaryNode because currently we only have
	// two-event-arguments. Perhaps our object model is
	// not adequate?!
	public List<Node> getChildrenInOrder(int childIndex) {
		List<Node> children = getChildren();
		if (childIndex >= children.size())
			return Collections.emptyList();
		Node child = children.get(childIndex);
		List<Node> inorder = new ArrayList<>();
		traverseInOrder(child, inorder);
		return inorder;
	}

	// Exists equivalent in BinaryNode because currently we only have
	// two-event-arguments. Perhaps our object model is
	// not adequate?!
	/**
	 * @deprecated use {@link ParseTree#traverseInOrder(Node, List)}
	 * @param node
	 * @param inorder
	 */
	@Deprecated
	private void traverseInOrder(Node node, List<Node> inorder) {
		if (node.getClass().equals(TextNode.class)) {
			inorder.add(node);
			return;
		}
		BranchNode branchNode = (BranchNode) node;
		List<Node> children = branchNode.getChildren();
		if (null == children || children.size() == 0)
			return;

		// Not nodes only have a right child, for our purpose here
		if (!node.getClass().equals(NotNode.class))
			traverseInOrder(children.get(0), inorder);
		inorder.add(node);
		if (children.size() > 1)
			traverseInOrder(children.get(1), inorder);
		if (node.getClass().equals(NotNode.class))
			traverseInOrder(children.get(0), inorder);

	}

	/**
	 * Adds ad child as a (in-)direct child. As the parse tree grows left to
	 * right the children are added in the same order.
	 * 
	 * @param newChild
	 *            Child to add.
	 */
	public void add(Node newChild) {
		if (isBinary() && leftChild == null) {
			leftChild = newChild;
			newChild.setParent(this);
		} else if (isBinary() && rightChild == null) {
			rightChild = newChild;
			newChild.setParent(this);
		} else if (leftChild == null) {
			leftChild = newChild;
			newChild.setParent(this);
		} else if (rightChild == null) {
			rightChild = newChild;
			newChild.setParent(this);
		} else if (isBinary() && leftChild.subtreeCanTakeNode())
			((BranchNode) leftChild).add(newChild);
		else if (isBinary() && rightChild.subtreeCanTakeNode())
			((BranchNode) rightChild).add(newChild);
		else if (leftChild.subtreeCanTakeNode())
			((BranchNode) leftChild).add(newChild);
		else
			throw new IllegalArgumentException("No room for another child!");
	}

	public List<Node> getFirstArgumentNodes() {
		return getChildrenInOrder(0);
	}

	public List<Node> getSecondArgumentNodes() {
		return getChildrenInOrder(1);
	}

	/**
	 * Removes a child node.
	 * <p>
	 * If this node is the current root and will be obsolete after the
	 * operation, the new root node will be returned.
	 * </p>
	 * 
	 * @param toRemove
	 *            Child to remove.
	 * @return the new tree node if this node was the root before, null
	 *         otherwise
	 */
	// public Node removeChild(Node toRemove) {
	// Node toKeep;
	// if (isBinary()) {
	// if (toRemove == leftChild) {
	// if (rightChild != null) {
	// leftChild = rightChild;
	// rightChild = null;
	// toKeep = leftChild;
	// } else {
	// leftChild = null;
	// toKeep = null;
	// }
	// } else if (toRemove == rightChild) {
	// rightChild = null;
	// toKeep = leftChild;
	// } else
	// throw new IllegalArgumentException(toRemove + " is no child of " + this);
	// } else {
	// if (toRemove == rightChild) {
	// rightChild = null;
	// toKeep = null;
	// } else
	// throw new IllegalArgumentException(toRemove + " is no child of " + this);
	// }
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
	// public void replaceChild(Node replaced, Node replacement) {
	// super.replaceChild(replaced, replacement);
	// replaced.parent = null;
	// replacement.parent = this;
	// if (replaced == leftChild)
	// leftChild = replacement;
	// else if (replaced == rightChild)
	// rightChild = replacement;
	// else
	// throw new IllegalArgumentException(replaced + " is no child of " + this);
	// }

	/**
	 * Set the right child of the node.
	 * 
	 * @param child
	 *            The child to set.
	 */
	public void setRightChild(Node child) {
		rightChild = child;
	}

	/**
	 * Set the left child of the node.
	 * 
	 * @param child
	 *            The child to set.
	 */
	public void setLeftChild(Node child) {
		leftChild = child;
	}

	public List<Node> getChildren() {
		List<Node> children = new ArrayList<Node>();
		if (leftChild != null) {
			children.add(leftChild);
		}
		if (rightChild != null) {
			children.add(rightChild);
		}
		return children;
	}

	@Override
	public boolean isLeaf() {
		return leftChild == null && rightChild == null;
	}

	public void setLikelihoods(List<String> likelihoods) {
		this.likelihoods = likelihoods;
	}

	public boolean isBinary() {
		return isBinary;
	}

	/**
	 * Create a string representation of this node and its subtree (mostly for
	 * debugging and test purposes).
	 * 
	 * @param useTerms
	 *            True if terms instead of original text values shall be used
	 *            for text tokens.
	 * @return A string representation of this node and its subtree.
	 */
	public String toString(Serialization serializationType) {
		String nodeRepresentation;
		String returnString = "";
		switch (serializationType) {
		case NODE_IDS:
			nodeRepresentation = String.valueOf(id) + "{" + text + "}";
			break;
		case NODE_TEXT:
			nodeRepresentation = text;
			break;
		case CONCEPT_IDS:
			List<String> termIds = new ArrayList<>();
			for (IConcept term : eventTypes)
				termIds.add(term.getId());
			String idString = StringUtils.join(termIds, "|");
			if (termIds.size() > 1)
				idString = "(" + idString + ")";
			nodeRepresentation = idString;
			break;
		default:
			nodeRepresentation = text;
		}
		if (leftChild != null && rightChild != null)
			returnString = String.format("(%s %s %s)", leftChild.toString(serializationType), nodeRepresentation, rightChild.toString(serializationType));
		else if (leftChild != null)
			returnString = String.format("(%s %s)", leftChild.toString(serializationType), nodeRepresentation);
		else if (rightChild != null)
			returnString = String.format("(%s %s)", nodeRepresentation, rightChild.toString(serializationType));
		else
			returnString = nodeRepresentation;
		return returnString;
	}

	/**
	 * Determine if another child can be added directly or indirectly to the
	 * node.
	 * 
	 * @return True if another child can be added.
	 */
	@Override
	public boolean subtreeCanTakeNode() {
		if (isBinary()) {
			if (leftChild == null || leftChild.subtreeCanTakeNode())
				return true;
			if (rightChild == null || rightChild.subtreeCanTakeNode())
				return true;
		} else {
			// Unary
			if ((leftChild == null || leftChild.subtreeCanTakeNode()) && (rightChild == null || rightChild.subtreeCanTakeNode()))
				return true;
		}
		return false;
	}

	public EventBoolElement getBooleanStructure() {
		BoolElement arg1 = null;
		BoolElement arg2 = null;
		if (null != leftChild) {
			arg1 = getBooleanStructure(leftChild);
		}
		if (null != rightChild) {
			arg2 = getBooleanStructure(rightChild);
		}
		List<String> eventTypeIDs = new ArrayList<>();
		for (IConcept eventType : eventTypes)
			eventTypeIDs.add(eventType.getId());
		return new EventBoolElement(eventTypeIDs, arg1, arg2);
	}

	public BoolElement getBooleanStructure(Node argumentSubtreeRoot) {
		BoolElement ret = null;
		Class<? extends Node> nodeClass = argumentSubtreeRoot.getClass();
		if (nodeClass.equals(TextNode.class)) {
			TextNode textNode = (TextNode) argumentSubtreeRoot;
			List<? extends IConcept> terms = textNode.getConcepts();
			if (terms.size() == 0)
				throw new IllegalStateException("Node " + argumentSubtreeRoot + " does not have any terms.");
			else if (terms.size() == 1) {
				ret = new LiteralBoolElement(terms.get(0).getId());
			} else {
				// Multiple terms means that this node is regarded ambiguous and
				// has not been disambiguated by the user.
				// Thus, build a disjunction of the term IDs to search for every
				// possibility.
				ComplexBoolElement complex = new ComplexBoolElement();
				for (int i = 0; i < terms.size(); i++) {
					complex.addElement(new LiteralBoolElement(terms.get(i).getId()));
					if (i < terms.size() - 1)
						complex.addElement(new OperatorBoolElement(BoolOperator.OR));
				}
				ret = complex;
			}
		} else if (nodeClass.equals(BinaryNode.class)) {
			BinaryNode argumentBranch = (BinaryNode) argumentSubtreeRoot;
			Node leftChild = argumentBranch.getLeftChild();
			Node rightChild = argumentBranch.getRightChild();
			ComplexBoolElement complexBoolElement = new ComplexBoolElement();
			if (null != leftChild)
				complexBoolElement.addElement(getBooleanStructure(leftChild));
			complexBoolElement.addElement(new OperatorBoolElement(BoolOperator.valueOf(argumentBranch.text)));
			if (null != rightChild)
				complexBoolElement.addElement(getBooleanStructure(rightChild));
			ret = complexBoolElement;
		}
		return ret;
	}

	@Override
	public String toString() {
		return toString(Serialization.NODE_TEXT);
	}

	public List<IConcept> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<IConcept> eventTypes) {
		this.eventTypes = eventTypes;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.EVENT;
	}

	@Override
	public boolean isAmbiguous() {
		// TODO for the moment, we don't have an exact notion of ambiguous
		// events
		return false;
	}

	@Override
	public boolean isObsolete() {
		if (getChildNumber() == 0)
			return true;
		else if (!isBinary() && getChildNumber() < 2)
			return true;
		return true;
	}

}
