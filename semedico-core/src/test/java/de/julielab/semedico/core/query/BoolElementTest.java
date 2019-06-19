package de.julielab.semedico.core.query;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.search.query.bool.ComplexBoolElement;
import de.julielab.semedico.core.search.query.bool.LiteralBoolElement;
import de.julielab.semedico.core.search.query.bool.OperatorBoolElement;
import de.julielab.semedico.core.search.query.bool.OperatorBoolElement.BoolOperator;
import org.testng.annotations.Test;

public class BoolElementTest {

	private final static Logger log = LoggerFactory.getLogger(BoolElementTest.class);

	@Test
	public void testToString() {
		// Test primitive boolean elements.
		LiteralBoolElement literal = new LiteralBoolElement("x");
		assertEquals("x", literal.toString());
		OperatorBoolElement operator = new OperatorBoolElement(OperatorBoolElement.BoolOperator.AND);
		assertEquals(OperatorBoolElement.BoolOperator.AND.toString(), operator.toString());

		// Test complex boolean expressions.
		ComplexBoolElement complexBoolElement = new ComplexBoolElement();
		complexBoolElement.addElement(literal);
		complexBoolElement.addElement(operator);
		// Until now, this is not a complete boolean expression and should be invalid.
		assertEquals("x AND", complexBoolElement.toString());
		assertFalse(complexBoolElement.isValid());
		log.debug(complexBoolElement.getValidityMessage());

		LiteralBoolElement literal2 = new LiteralBoolElement("y");
		// Now, with a second boolean literal, this is valid.
		complexBoolElement.addElement(literal2);
		assertTrue(complexBoolElement.isValid());
		assertEquals("x AND y", complexBoolElement.toString());

		ComplexBoolElement flattenendComplexElement = new ComplexBoolElement();
		flattenendComplexElement.addElement(complexBoolElement);
		flattenendComplexElement.addElement(new OperatorBoolElement(BoolOperator.OR));
		flattenendComplexElement.addElement(literal);
		flattenendComplexElement.addElement(operator);
		flattenendComplexElement.addElement(literal2);
		assertEquals("(x AND y) OR x AND y", flattenendComplexElement.toString());

		ComplexBoolElement nestedComplexElement = new ComplexBoolElement();
		complexBoolElement.clear();
		complexBoolElement.addElement(literal);
		complexBoolElement.addElement(new OperatorBoolElement(BoolOperator.OR));
		complexBoolElement.addElement(literal2);
		nestedComplexElement.addElement(complexBoolElement);
		nestedComplexElement.addElement(new OperatorBoolElement(BoolOperator.AND));
		nestedComplexElement.addElement(literal);
		nestedComplexElement.addElement(new OperatorBoolElement(BoolOperator.OR));
		nestedComplexElement.addElement(literal2);
		assertEquals("(x OR y) AND x OR y", nestedComplexElement.toString());

		// Validity checks.
		complexBoolElement.clear();
		complexBoolElement.addElement(literal);
		complexBoolElement.addElement(operator);
		complexBoolElement.addElement(operator);
		complexBoolElement.addElement(literal2);
		assertFalse(complexBoolElement.isValid());
		log.debug(complexBoolElement.getValidityMessage());

		complexBoolElement.clear();
		complexBoolElement.addElement(literal);
		complexBoolElement.addElement(literal2);
		assertFalse(complexBoolElement.isValid());
		log.debug(complexBoolElement.getValidityMessage());
	}

	@Test
	public void testConjunction() {
		ComplexBoolElement complex1 = new ComplexBoolElement();
		complex1.addElement(new LiteralBoolElement("x"));
		complex1.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex1.addElement(new LiteralBoolElement("y"));
		assertTrue(complex1.isConjunction());
		assertFalse(complex1.isDisjunction());
		assertFalse(complex1.isNested());

		ComplexBoolElement complex2 = new ComplexBoolElement();
		complex2.addElement(new LiteralBoolElement("a"));
		complex2.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex2.addElement(new LiteralBoolElement("b"));
		assertTrue(complex2.isConjunction());
		assertFalse(complex2.isDisjunction());
		assertFalse(complex2.isNested());

		complex1.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex1.addElement(complex2);
		assertTrue(complex1.isConjunction());
		assertFalse(complex1.isNested());
		assertTrue(complex1.isValid());

		ComplexBoolElement complex3 = new ComplexBoolElement();
		complex3.addElement(new LiteralBoolElement("c"));
		complex3.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex3.addElement(new LiteralBoolElement("d"));
		assertTrue(complex3.isDisjunction());
		assertFalse(complex3.isConjunction());
		assertFalse(complex3.isNested());

		complex1.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex1.addElement(complex3);
		assertTrue(complex1.isConjunction());
		assertTrue(complex1.isNested());
		assertTrue(complex1.isValid());

		assertEquals("x AND y AND a AND b AND (c OR d)", complex1.toString());

		complex1.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex1.addElement(new LiteralBoolElement("z"));
		assertFalse(complex1.isConjunction());
		assertTrue(complex1.isNested());
		assertTrue(complex1.isValid());

	}

	@Test
	public void testDisjunction() {
		ComplexBoolElement complex1 = new ComplexBoolElement();
		complex1.addElement(new LiteralBoolElement("x"));
		complex1.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex1.addElement(new LiteralBoolElement("y"));
		assertTrue(complex1.isDisjunction());
		assertFalse(complex1.isConjunction());
		assertFalse(complex1.isNested());

		ComplexBoolElement complex2 = new ComplexBoolElement();
		complex2.addElement(new LiteralBoolElement("a"));
		complex2.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex2.addElement(new LiteralBoolElement("b"));
		assertTrue(complex2.isDisjunction());
		assertFalse(complex2.isConjunction());
		assertFalse(complex2.isNested());

		complex1.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex1.addElement(complex2);
		assertTrue(complex1.isDisjunction());
		assertFalse(complex1.isNested());
		assertTrue(complex1.isValid());

		ComplexBoolElement complex3 = new ComplexBoolElement();
		complex3.addElement(new LiteralBoolElement("c"));
		complex3.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex3.addElement(new LiteralBoolElement("d"));
		assertTrue(complex3.isConjunction());
		assertFalse(complex3.isDisjunction());
		assertFalse(complex3.isNested());

		complex1.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex1.addElement(complex3);
		assertTrue(complex1.isDisjunction());
		assertTrue(complex1.isNested());
		assertTrue(complex1.isValid());

		assertEquals("x OR y OR a OR b OR (c AND d)", complex1.toString());

		complex1.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex1.addElement(new LiteralBoolElement("z"));
		assertFalse(complex1.isDisjunction());
		assertTrue(complex1.isNested());
		assertTrue(complex1.isValid());
	}

	@Test
	public void testFlatteningAndReNesting() {
		// For the sake of readability we leave away parenthesis when they are not necessary. This means when a complex
		// boolean expression consists only of "OR" disjuncted expressions and we add (x OR y), then we will just append
		// "x OR y" without the parenthesis. Technically, we do not add the ComplexBoolElement but its elements
		// directly. Sometimes, this can be a problem, namely when we now add an "AND" operator that now would bind the
		// "y" to the AND, effectively creating the following parenthesis: "... x OR (y AND ...". So before adding the
		// "AND" we have re re-nest the expression to "... (x OR y)" - adding the parenthesis again - and then add the
		// "AND" operator.

		ComplexBoolElement complex = new ComplexBoolElement();
		complex.addElement(new LiteralBoolElement("x"));
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		ComplexBoolElement nested = new ComplexBoolElement();
		nested.addElement(new LiteralBoolElement("y"));
		nested.addElement(new OperatorBoolElement(BoolOperator.OR));
		nested.addElement(new LiteralBoolElement("z"));
		complex.addElement(nested);
		assertEquals("x OR y OR z", complex.toString());
		complex.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex.addElement(new LiteralBoolElement("a"));
		assertEquals("x OR (y OR z) AND a", complex.toString());
	}

	@Test
	public void testMiscelleneousExpressions() {
		// Various and arbitrary nesting, no disjunctive or conjunctive form.
		ComplexBoolElement complex = new ComplexBoolElement();
		complex.addElement(new LiteralBoolElement("x"));
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		ComplexBoolElement nested = new ComplexBoolElement();
		nested.addElement(new LiteralBoolElement("y"));
		nested.addElement(new OperatorBoolElement(BoolOperator.OR));
		nested.addElement(new LiteralBoolElement("z"));
		nested.addElement(new OperatorBoolElement(BoolOperator.AND));
		ComplexBoolElement nestedDepth1 = new ComplexBoolElement();
		nestedDepth1.addElement(new LiteralBoolElement("a"));
		nestedDepth1.addElement(new OperatorBoolElement(BoolOperator.OR));
		nestedDepth1.addElement(new LiteralBoolElement("b"));
		nested.addElement(nestedDepth1);
		assertEquals("y OR z AND (a OR b)", nested.toString());
		complex.addElement(nested);
		assertEquals("x OR (y OR z AND (a OR b))", complex.toString());
		complex.addElement(new OperatorBoolElement(BoolOperator.AND));
		complex.addElement(new LiteralBoolElement("a"));
		assertEquals("x OR (y OR z AND (a OR b)) AND a", complex.toString());

		// Begin a disjunction
		complex.clear();
		complex.addElement(new LiteralBoolElement("x"));
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex.addElement(new LiteralBoolElement("y"));
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		// Add a conjunction to the disjunction, should end up in parenthesis
		nested.clear();
		nested.addElement(new LiteralBoolElement("a"));
		nested.addElement(new OperatorBoolElement(BoolOperator.AND));
		nested.addElement(new LiteralBoolElement("b"));
		complex.addElement(nested);
		// Continue the disjunction
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		// This nested disjunction should just be added on top-level because we still have a disjunction, so no
		// parenthesis necessary
		ComplexBoolElement nested2 = new ComplexBoolElement();
		nested2.addElement(new LiteralBoolElement("i"));
		nested2.addElement(new OperatorBoolElement(BoolOperator.OR));
		nested2.addElement(new LiteralBoolElement("j"));
		complex.addElement(nested2);
		assertEquals("x OR y OR (a AND b) OR i OR j", complex.toString());
		
		// Check whether negation renders as expected. Just take the expression from above and put some negations in there.
		complex.clear();
		complex.addElement(new LiteralBoolElement("x"));
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		complex.addElement(new LiteralBoolElement("y", true));
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		nested.clear();
		nested.addElement(new LiteralBoolElement("a"));
		nested.addElement(new OperatorBoolElement(BoolOperator.AND));
		LiteralBoolElement element = new LiteralBoolElement("b");
		element.negate();
		nested.addElement(element);
		complex.addElement(nested);
		complex.addElement(new OperatorBoolElement(BoolOperator.OR));
		nested2.clear();
		LiteralBoolElement element2 = new LiteralBoolElement("i");
		element2.setNegated(true);
		nested2.addElement(element2);
		nested2.addElement(new OperatorBoolElement(BoolOperator.OR));
		nested2.addElement(new LiteralBoolElement("j"));
		complex.addElement(nested2);
		assertEquals("x OR NOT y OR (a AND NOT b) OR NOT i OR j", complex.toString());
	}
}
