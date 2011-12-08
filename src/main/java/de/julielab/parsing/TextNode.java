package de.julielab.parsing;



/**
 * This class represents a leaf in a LR bottom-up parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class TextNode extends Node{

	public TextNode(String text) {
		this.text = text;
	}

	@Override
	boolean isLeaf() {
		return true;
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	boolean canTakeChild() {
		return false;
	}

	@Override
	public boolean hasExactlyOneChild() {
		return false;
	}
	
}
