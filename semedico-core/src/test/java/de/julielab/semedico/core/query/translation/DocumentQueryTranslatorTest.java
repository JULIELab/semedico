package de.julielab.semedico.core.query.translation;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.MatchPhraseQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.parsing.ParseErrors;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.search.query.translation.DocumentQueryTranslator;
import de.julielab.semedico.core.search.query.translation.SearchTask;
import de.julielab.semedico.core.services.IndexInformationService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.GeneralIndexStructure;

public class DocumentQueryTranslatorTest {
	@Test
	public void testPunctationKeywordTranslation() {
		// In this test we expect the given keyword to be split at punctuation
		// and the part to be searched as a phrase
		TestQueryTranslator translator = new TestQueryTranslator();
		TextNode node = new TextNode("p70(s6)k");
		ParseTree query = new ParseTree(node, new ParseErrors());
		SearchServerQuery serverQuery = translator.translateToBooleanQuery(query, "text", null);

		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
		MatchPhraseQuery matchPhrase = (MatchPhraseQuery) serverQuery;
		assertEquals("p70 s6 k", matchPhrase.phrase);
	}

	@Test
	public void testPunctationKeywordTranslation2() {
		// In this test we expect the given keyword to be split at punctuation
		// and the part to be searched as a phrase
		TestQueryTranslator translator = new TestQueryTranslator();
		TextNode node = new TextNode("p70(s6)-kinase");
		ParseTree query = new ParseTree(node, new ParseErrors());
		SearchServerQuery serverQuery = translator.translateToBooleanQuery(query, "text", null);

		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
		MatchPhraseQuery matchPhrase = (MatchPhraseQuery) serverQuery;
		assertEquals("p70 s6 kinase", matchPhrase.phrase);
	}

	/**
	 * An instantiable test class to grant access to the methods of the abstract
	 * class {@link DocumentQueryTranslator}.
	 * 
	 * @author faessler
	 *
	 */
	private class TestQueryTranslator extends DocumentQueryTranslator {

		public TestQueryTranslator() {
			super(LoggerFactory.getLogger(TestQueryTranslator.class), "TestTranslator");
		}

		@Override
		public void translate(ISemedicoQuery query, Set<SearchTask> tasks, Set<String> indexTypes,
				List<SearchServerQuery> searchQueries, Map<String, SearchServerQuery> namedQueries) {

		}
	}
}
