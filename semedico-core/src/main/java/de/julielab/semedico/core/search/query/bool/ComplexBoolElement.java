package de.julielab.semedico.core.search.query.bool;

import de.julielab.semedico.core.search.query.bool.OperatorBoolElement.BoolOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * A complex boolean expression, can be nested arbitrarily deep, i.e. contain further {@link #ComplexBoolElement()}
 * instances.
 * 
 * @author faessler
 * 
 */
public class ComplexBoolElement extends BoolElement {
	public List<BoolElement> getElements() {
		return elements;
	}

	private List<BoolElement> elements;
	private ComplexBoolElement previousParentheses = null;

	public ComplexBoolElement() {
		this.elements = new ArrayList<>();
	}

	public void addElement(BoolElement element) {
		// Validity check.
		if (element.getClass().equals(ComplexBoolElement.class)) {
			ComplexBoolElement complexBoolElement = (ComplexBoolElement) element;
			if (!complexBoolElement.isValid())
				throw new IllegalArgumentException("Passed complex boolean element \"" + element.toString() + "\" is not valid: "
						+ complexBoolElement.getValidityMessage());
		}

		if (null != previousParentheses) {
			// Actually, if the previous parentheses element is not null, then 'element' must be an operator to form a
			// valid expression. However, we don't ensure validity all the time so we don't throw an exception here.
			if (element.getClass().equals(OperatorBoolElement.class)) {
				OperatorBoolElement operator = (OperatorBoolElement) element;
				// If the current operator, that should be added, is not compatible with the direct predecessor complex
				// expression, this preceding expression must be enclosed in parenthesis. If 'previousParenthesis' is
				// not null, we have flattened it (see comments below), i.e. we left away the parenthesis, and must now
				// add them.
				if ((previousParentheses.isDisjunction() && operator.getOperator() == BoolOperator.AND)
						|| previousParentheses.isConjunction() && operator.getOperator() == BoolOperator.OR) {
					// Remove the flattened elements stemming from the previous parenthesis expression.
					for (int i = 0; i < previousParentheses.size(); i++)
						elements.remove(elements.size() - 1);
					// And now add the expression un-flattened.
					elements.add(previousParentheses);
				}
			}
		}
		// If this and the incoming element are both (compatible to) disjunctions or conjunctions, don't nest the new
		// element but add its elements directly to this one.
		// We call this "flattening".
		if (element.getClass().equals(ComplexBoolElement.class)) {
			ComplexBoolElement complexBoolElement = (ComplexBoolElement) element;
			if ((this.isDisjunction() && complexBoolElement.isDisjunction())
					|| (this.isConjunction() && complexBoolElement.isConjunction())) {
				for (BoolElement newElement : complexBoolElement.elements) {
					elements.add(newElement);
				}
				previousParentheses = complexBoolElement;
			} else {
				elements.add(element);
				previousParentheses = null;
			}
		} else {
			elements.add(element);
			previousParentheses = null;
		}
	}

	public BoolElement get(int i) {
		return elements.get(i);
	}

	public int size() {
		return elements.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < elements.size(); i++) {
			BoolElement element = elements.get(i);
			boolean isComplex = element.getClass().equals(ComplexBoolElement.class);
			if (isComplex)
				sb.append("(");
			sb.append(element.toString());
			if (isComplex)
				sb.append(")");

			if (i < elements.size() - 1)
				sb.append(" ");
		}
		if (!negated)
			return sb.toString();
		return "NOT " + sb.toString();
	}

	public boolean isValid() {
		return getValidityMessage() == null;
	}

	public String getValidityMessage() {
		if (elements.get(0).getClass().equals(OperatorBoolElement.class))
			return "First element is an operator.";
		else if (elements.get(elements.size() - 1).getClass().equals(OperatorBoolElement.class))
			return "Last element is an operator.";

		boolean lastElementIsOperator = true;
		for (BoolElement element : elements) {
			if (!element.getClass().equals(OperatorBoolElement.class) && lastElementIsOperator) {
				lastElementIsOperator = false;
			} else if (element.getClass().equals(OperatorBoolElement.class) && !lastElementIsOperator) {
				lastElementIsOperator = true;
			} else if (element.getClass().equals(OperatorBoolElement.class) && lastElementIsOperator) {
				return "There are two consecutively following operators without an operand in between.";
			} else if (!element.getClass().equals(OperatorBoolElement.class) && !lastElementIsOperator) {
				return "There are two consecutively following non-operators without an operator in between.";
			}
		}
		return null;
	}

	public void clear() {
		elements.clear();
	}

	public boolean isDisjunction() {
		boolean isDisjunction = true;
		// A disjunction is when we have only primitive elements and the operators are always OR or NOT
		// I.e. single literals are accepted as disjunctions.
		for (BoolElement element : elements) {
			if (element.getClass().equals(OperatorBoolElement.class)) {
				OperatorBoolElement operatorElement = (OperatorBoolElement) element;
				if (operatorElement.getOperator() == BoolOperator.AND)
					isDisjunction = false;
			}
		}
		return isDisjunction;
	}

	public boolean isConjunction() {
		boolean isConjunction = true;
		// A conjunction is when we have only primitive elements and the operators are always AND or NOT
		// I.e. single literals are accepted as conjunctions.
		for (BoolElement element : elements) {
			if (element.getClass().equals(OperatorBoolElement.class)) {
				OperatorBoolElement operatorElement = (OperatorBoolElement) element;
				if (operatorElement.getOperator() == BoolOperator.OR)
					isConjunction = false;
			}
		}
		return isConjunction;
	}

	public boolean isNested() {
		boolean isNested = false;
		for (BoolElement element : elements) {
			if (!(element instanceof PrimitiveBoolElement))
				isNested = true;
		}

		return isNested;
	}
}
