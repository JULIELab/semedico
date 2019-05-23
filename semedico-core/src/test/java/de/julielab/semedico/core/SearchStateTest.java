package de.julielab.semedico.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.query.ParsingService;

@Ignore
public class SearchStateTest {
	
	private SearchState state;
	private ParsingService parser = new ParsingService(LoggerFactory.getLogger(ParsingService.class));

	@Test
	public void testTerm() throws Exception {
		newState();
		assertEquals("internal_identifier_1", state.getSemedicoQuery().toString(SERIALIZATION.TERMS));
	}

	private SearchState newState() throws Exception {
		state = new SearchState();
		ParseTree parseTree = prepareSimpleParseTree();
		state.setDisambiguatedQuery(parseTree);
		return state;
	}
	
	private ParseTree prepareSimpleParseTree() throws Exception {
		String[] tokenValues = { "foo" };
		QueryToken.Category[] tokenTypes = { QueryToken.Category.ALPHANUM };
		int[] tokenBegins = { 0 };
		int[] tokenEnds = { 3 };
		List<QueryToken> tokens = new ArrayList<>();
		for (int i = 0; i < tokenValues.length; i++) {
			QueryToken qt = new QueryToken(tokenBegins[i], tokenEnds[i]);
			qt.setType(tokenTypes[i]);
			qt.setOriginalValue(tokenValues[i]);
			tokens.add(qt);
		}
		Facet facet = new Facet(NodeIDPrefixConstants.FACET + 1);
		facet.setSearchFieldNames(Lists.newArrayList("fieldName1"));
		FacetTerm term = new FacetTerm("internal_identifier_1", "name");
		term.addFacet(facet);
		tokens.get(0).addTermToList(term);

		ParseTree parseTree = parser.parse(tokens);
		return parseTree;
	}
}