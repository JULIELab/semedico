package de.julielab.parsing;



/**
 * This class represents a leaf in a LR bottom-up parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class TextNode extends Node{

	private boolean isPhrase;

	/**
	 * Constructor for the leaves of the LR bottom-up parse tree.
	 * @param value
	 *            value of a Symbol, may be a String (text)
	 *            or a String[], containing text and mapped text.
	 * @throws IllegalArgumentException 
	 * 			  if value is neither String nor String[]
	 */
	public TextNode(Object value) {
		this(value, false);
	}

	public TextNode(Object value, boolean isPhrase) {
		if(value.getClass().isInstance(String.class))
			this.text = (String) value;
		else if (value.getClass().isInstance(String[].class)){
			this.text = ((String[]) value)[TEXT];
			this.setMappedText(((String[]) value)[MAPPED_TEXT]);
		}
		else
			throw new IllegalArgumentException("Value must be a String or a String[]");
		this.isPhrase = true;
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
}
