package de.julielab.semedico.core.search.query.bool;

public class LiteralBoolElement extends PrimitiveBoolElement {
	private String literal;

	public LiteralBoolElement(String literal) {
		this(literal, false);
	}
	
	public LiteralBoolElement(String literal, boolean negated) {
		super(negated);
		this.literal = literal;
	}

	public String getLiteral() {
		return literal;
	}

	@Override
	public String toString() {
		if (!negated)
			return literal;
		return "NOT " + literal;
	}
}
