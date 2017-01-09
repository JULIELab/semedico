package de.julielab.semedico.core.services.query;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.ALPHANUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.AND_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.BINARY_EVENT;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.CJ;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.LEFT_PARENTHESIS;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NOT_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.OR_OPERATOR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.PHRASE;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.RIGHT_PARENTHESIS;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.UNARY_EVENT;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;

/**
 * Hard to read, no useful tests (any more), will be removed.
 * @author faessler
 *
 */
@Ignore
@Deprecated
public class ParsingServiceTest {

	private ParsingService parser;

	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final String[] tokenValues = { "(", "foo", "bar", "AND",
			"\"foo bar\"", ")", "or", "-", "mouse" };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] tokenTypes = { LEFT_PARENTHESIS, ALPHANUM,
			ALPHANUM, AND_OPERATOR, PHRASE, RIGHT_PARENTHESIS, OR_OPERATOR,
			NOT_OPERATOR, ALPHANUM };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] tokenBegins = { 0, 1, 5, 9, 13, 22, 24, 27, 28 };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] tokenEnds = { 1, 4, 8, 12, 22, 23, 26, 28, 33 };

	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final String[] eventTokenValues = { "fizz", "AND", "foo",
			"ppi", "bar", "buzz" };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] eventTokenTypes = { ALPHANUM, AND_OPERATOR,
			ALPHANUM, BINARY_EVENT, ALPHANUM, PHRASE };

	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final String[] eventTokenValues2 = { "foo", "ppi", "bar" };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] eventTokenTypes2 = { PHRASE, UNARY_EVENT, PHRASE };

	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final String[] eventTokenValues3 = { "(", "foo", "ppi",
			"bar", ")", "or", "ppi2", "buzz" };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] eventTokenTypes3 = { LEFT_PARENTHESIS, ALPHANUM,
			BINARY_EVENT, PHRASE, RIGHT_PARENTHESIS, OR_OPERATOR, UNARY_EVENT,
			CJ }; // () necessary as UNARY_EVENTs follow their argument, yet
					// can't modify existing parse trees

	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final String[] manipulationValues = { "(", "x", "OR", "y",
			"(", "a", ")", ")", ")", "b" };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] manipulationTypes = { LEFT_PARENTHESIS,
			ALPHANUM, OR_OPERATOR, ALPHANUM, LEFT_PARENTHESIS, ALPHANUM,
			RIGHT_PARENTHESIS, RIGHT_PARENTHESIS, RIGHT_PARENTHESIS, ALPHANUM };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] manipulationBegins = { 0, 2, 4, 7, 9, 11, 13,
			15, 17, 19 };
	/**
	 * @deprecated Please don't use such complex global structures since it makes the individual tests very hard to understand.
	 */
	@Deprecated
	private static final int[] manipulationEnds = { 1, 3, 6, 8, 10, 12, 14, 16,
			18, 20 };

	private ParseTree parse(List<QueryToken> tokens) throws Exception {
		parser = new ParsingService(LoggerFactory.getLogger(ParsingService.class));
		ParseTree parseTree = parser.parse(tokens);
		return parseTree;
	}

	@Test
	public void testParse() throws Exception {
		ParseTree parseTree = parse(prepareQueryTokensForSimpleParse());

		assertEquals("(((foo AND bar) AND \"foo bar\") OR (NOT mouse))",
				parseTree.toString());
		assertEquals("(((testId AND testId) AND \"foo bar\") OR (NOT testId))",
				parseTree.toString(SERIALIZATION.TERMS));
	}

	@Test
	public void testEventParse() throws Exception {
		String[] eventTokenValues = { "fizz", "AND", "foo",
				"ppi", "bar", "buzz" };
		int[] eventTokenTypes = { ALPHANUM, AND_OPERATOR,
				ALPHANUM, BINARY_EVENT, ALPHANUM, PHRASE };
		List<QueryToken> tokens = new ArrayList<QueryToken>();
		for (int i = 0; i < eventTokenValues.length; i++) {
			QueryToken qt = new QueryToken(1, 2);
			qt.setType(eventTokenTypes[i]);
			qt.setOriginalValue(eventTokenValues[i]);
			tokens.add(qt);
		}
		IConcept testTerm = new FacetTerm("testId", "testFacet");
		tokens.get(0).addTermToList(testTerm);
		tokens.get(2).addTermToList(testTerm);
		tokens.get(3).addTermToList(testTerm);
		tokens.get(4).addTermToList(testTerm);
		ParseTree parseTree = parse(tokens);
		assertEquals("((fizz AND (foo ppi bar)) AND buzz)",
				parseTree.toString(SERIALIZATION.TEXT));
	}

	@Test
	public void testEventParse2() throws Exception {
		ParseTree parseTree = parse(prepareQueryTokensForEventParse2());
		assertEquals("((foo ppi) AND bar)", parseTree.toString());
	}

	@Test
	public void testEventParse3() throws Exception {
		ParseTree parseTree = parse(prepareQueryTokensForEventParse3());
		assertEquals("((foo ppi bar) OR (buzz ppi2))", parseTree.toString());
	}

	@Test
	public void testManipulation() throws Exception {
		ParseTree parseTree = parse(prepareQueryTokensForManipulation());

		// Left parentheses errors are repaired. Right parentheses errors too,
		// but text after superfluous ) is lost.
		assertEquals("((x OR y) AND a)", parseTree.toString());

		// Test is replaced.
		parseTree.expandTerm("a", "b");
		assertEquals("((x OR y) AND b)", parseTree.toString());
		parseTree.expandTerm("y", "(Tom AND Jerry)");
		assertEquals("((x OR (Tom AND Jerry)) AND b)", parseTree.toString());

		// Nodes are removed.
		parseTree.remove("x");
		assertEquals("((Tom AND Jerry) AND b)", parseTree.toString());

		// Nodes are added as siblings to specified nodes by inserting new
		// parent nodes.
		Node existingNode = parseTree.getNode("b");
		TextNode newNode = new TextNode("added");
		parseTree.add(existingNode, newNode, NodeType.OR);
		assertEquals("((Tom AND Jerry) AND (b OR added))", parseTree.toString());
		existingNode = parseTree.getRoot();
		parseTree.add(existingNode, newNode, NodeType.AND);
		assertEquals("(((Tom AND Jerry) AND (b OR added)) AND added)",
				parseTree.toString());
	}

	// @Test
	// public void testEventManipulation() throws Exception {
	// ParseTree parseTree = parse(prepareQueryTokensForEventParse3()); //((foo
	// ppi bar) OR (ppi2 buzz))
	//
	// // Nodes are removed.
	// parseTree.remove("bar");
	// System.out.println(parseTree.toString());
	//
	//
	// }

	private List<QueryToken> prepareQueryTokensForSimpleParse() {
		List<QueryToken> tokens = new ArrayList<QueryToken>();
		for (int i = 0; i < tokenValues.length; i++) {
			QueryToken qt = new QueryToken(tokenBegins[i], tokenEnds[i]);
			qt.setType(tokenTypes[i]);
			qt.setOriginalValue(tokenValues[i]);
			tokens.add(qt);
		}
		IConcept testTerm = new FacetTerm("testId", "testFacet");
		tokens.get(1).addTermToList(testTerm);
		tokens.get(2).addTermToList(testTerm);
		tokens.get(8).addTermToList(testTerm);
		return tokens;
	}

	private List<QueryToken> prepareQueryTokensForEventParse() {
		List<QueryToken> tokens = new ArrayList<QueryToken>();
		for (int i = 0; i < eventTokenValues.length; i++) {
			QueryToken qt = new QueryToken(1, 2);
			qt.setType(eventTokenTypes[i]);
			qt.setOriginalValue(eventTokenValues[i]);
			tokens.add(qt);
		}
		IConcept testTerm = new FacetTerm("testId", "testFacet");
		tokens.get(0).addTermToList(testTerm);
		tokens.get(2).addTermToList(testTerm);
		tokens.get(3).addTermToList(testTerm);
		tokens.get(4).addTermToList(testTerm);
		return tokens;
	}

	private List<QueryToken> prepareQueryTokensForEventParse2() {
		List<QueryToken> tokens = new ArrayList<QueryToken>();
		for (int i = 0; i < eventTokenValues2.length; i++) {
			QueryToken qt = new QueryToken(1, 2);
			qt.setType(eventTokenTypes2[i]);
			qt.setOriginalValue(eventTokenValues2[i]);
			tokens.add(qt);
		}
		IConcept testTerm = new FacetTerm("testId", "testFacet");
		tokens.get(0).addTermToList(testTerm);
		tokens.get(1).addTermToList(testTerm);
		tokens.get(2).addTermToList(testTerm);
		return tokens;
	}

	private List<QueryToken> prepareQueryTokensForEventParse3() {
		IConcept testTerm = new FacetTerm("testId", "testFacet");
		List<QueryToken> tokens = new ArrayList<QueryToken>();
		for (int i = 0; i < eventTokenValues3.length; i++) {
			QueryToken qt = new QueryToken(1, 2);
			qt.setType(eventTokenTypes3[i]);
			qt.setOriginalValue(eventTokenValues3[i]);
			qt.addTermToList(testTerm);
			tokens.add(qt);
		}
		return tokens;
	}

	private List<QueryToken> prepareQueryTokensForManipulation() {
		List<QueryToken> tokens = new ArrayList<QueryToken>();
		for (int i = 0; i < manipulationValues.length; i++) {
			QueryToken qt = new QueryToken(manipulationBegins[i],
					manipulationEnds[i]);
			qt.setType(manipulationTypes[i]);
			qt.setOriginalValue(manipulationValues[i]);
			tokens.add(qt);
		}
		return tokens;
	}
}
