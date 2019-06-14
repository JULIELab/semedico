package de.julielab.semedico.core.search.query.bool;

public class OperatorBoolElement extends PrimitiveBoolElement {
	public enum BoolOperator {
		AND, OR
	}

	private BoolOperator operator;

	public OperatorBoolElement(BoolOperator operator) {
		super(false);
		this.operator = operator;

	}

	public BoolOperator getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		return operator.toString();
	}

	/**
	 * Does nothing for operators, i.e. this class.
	 */
	@Override
	public void negate() {
	}

	/**
	 * Does nothing for operators, i.e. this class.
	 * 
	 * @param negated
	 */
	@Override
	public void setNegated(boolean negated) {
	}

}
