package de.julielab.Parsing;



/**
 * This class represents a node, used to build a binary parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class TerminalNode extends Node{

	public TerminalNode(String text) {
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
