package de.julielab.semedico.core.parsing;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.IParsingService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
/**
 * Some simple tests for the ParseTree and Parser. The tests here work with a
 * small, manually configured set of test data.
 * 
 * @author hellrich/faessler
 * 
 */
public class ParseTreeTest {

	private static Registry registry;
	private static ILexerService lexerService;
	private static IParsingService parsingService;
	
	public List<QueryToken> convertToList(String userQuery) {
		QueryToken freetextToken = new QueryToken(0, userQuery.length(), userQuery);
		return Arrays.asList(freetextToken);
	}

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		lexerService = registry.getService(ILexerService.class);
		parsingService = registry.getService(IParsingService.class);

		// set the default operator for this test, does not depend on actual
		// productivity mode
		try {
			Field field = ParseTree.class.getDeclaredField("defaultOperator");
			field.setAccessible(true);
			field.set(null, ParseTree.AND);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}

	@Test
	public void testParse() throws Exception {
		// Lexer has special rules for tokens without whitespaces.
		ParseTree parseTree = parse("NOT(x AND y)OR c");
		assertEquals("((NOT (x AND y)) OR c)", parseTree.toString(SERIALIZATION.TEXT));
	}

	/**
	 * Runs the <tt>LexerService</tt> and the <tt>ParsingService</tt> but NOT
	 * the <tt>TermRecognitionService</tt>, thus to search for terms is done.
	 * 
	 * @param toParse
	 * @return
	 * @throws Exception
	 */
	private ParseTree parse(String toParse) throws Exception {
		List<QueryToken> lex = lexerService.lex(toParse);
		ParseTree parse = parsingService.parse(lex);
		return parse;
	}
	
	@Test
	public void testNumberConceptNodes() {
		IQueryAnalysisService queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		String query = "blood and ( death cancer )";
		ParseTree parseTree = queryAnalysisService.analyseQueryString(convertToList(query));
		assertEquals("(blood AND (death AND cancer))", parseTree.toString());
	}

}