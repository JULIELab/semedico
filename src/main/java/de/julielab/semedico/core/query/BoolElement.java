package de.julielab.semedico.core.query;

public abstract class BoolElement {
	protected boolean negated;

	public BoolElement() {
		this(false);
	}
	
	public BoolElement(boolean negated) {
		this.negated = negated;
	}
	
	public void negate() {
		negated = !negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}
	
	public boolean isNegated() {
		return negated;
	}
}
