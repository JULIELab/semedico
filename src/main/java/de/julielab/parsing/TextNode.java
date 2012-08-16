package de.julielab.parsing;



/**
 * This class represents a leaf in a LR td parse tree.
 * It contains methods to query and modify the properties of the node, e.g. its children.
 * @author hellrich
 *
 */
public class TextNode extends Node{

	private boolean isPhrase;
	private static final String[] testArray = new String[0];

	/**
	 * Constructor for the leaves of the LR td parse tree.
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
		if(value.getClass() == String.class)
			this.text = (String) value;
		else if (value.getClass().isInstance(testArray)){
			this.text = ((String[]) value)[TEXT];
			this.setMappedText(((String[]) value)[MAPPED_TEXT]);
		}
		else
			throw new IllegalArgumentException("Value must be a String or a String[]");
		this.isPhrase = isPhrase;
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
	boolean subtreeCanTakeNode() {
		return false;
	}
	
	boolean isPharse(){
		return isPhrase;
	}
}
