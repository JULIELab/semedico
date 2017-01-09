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
import de.julielab.semedico.core.query.ISemedicoQuery;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.query.translation.DocumentQueryTranslator;
import de.julielab.semedico.core.query.translation.SearchTask;
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

	@Test
	public void testPunctationAutoRecognitionConceptTranslation() {
		// In this test we have a concept node. We assume an automatic term
		// recognition which means the original text should also be searched as
		// a keyword/phrase.
		TestQueryTranslator translator = new TestQueryTranslator();
		TextNode node = new TextNode("il-10");
		// the term name shouldn't be searched at all, we rather search for the
		// text the user actually typed in; for synonyms etc. we have the
		// concept after all
		FacetTerm term = new FacetTerm(NodeIDPrefixConstants.TERM + 0, "INTERLEUKIN 10");
		node.setTerms(Collections.singletonList(term));
		ParseTree query = new ParseTree(node, new ParseErrors());
		SearchServerQuery serverQuery = translator.translateToBooleanQuery(query, "text", null);

		assertEquals(BoolQuery.class, serverQuery.getClass());
		BoolQuery bool = (BoolQuery) serverQuery;
		assertEquals(1, bool.clauses.size());
		BoolClause clause = bool.clauses.get(0);
		assertEquals(2, clause.queries.size());
		assertEquals(TermQuery.class, clause.queries.get(0).getClass());
		TermQuery termQuery = (TermQuery) clause.queries.get(0);
		assertEquals(NodeIDPrefixConstants.TERM + 0, termQuery.term);
		assertEquals(MatchPhraseQuery.class, clause.queries.get(1).getClass());
		MatchPhraseQuery matchPhrase = (MatchPhraseQuery) clause.queries.get(1);
		assertEquals("il 10", matchPhrase.phrase);
	}

	@Test
	public void testUserSelectedConceptTranslation() {
		// Here we just have a node where the user has specified the term
		// exactly, i.e. without automatic concept recognition. Only the concept
		// ID should be searched for.
		TestQueryTranslator translator = new TestQueryTranslator();
		TextNode node = new TextNode("il-10");
		// create a QueryToken that "has been selected" by the user
		QueryToken qt = new QueryToken(0, 5, "il-10");
		qt.setUserSelected(true);
		node.setQueryToken(qt);
		// the term name shouldn't be searched at all, we rather search for the
		// text the user actually typed in; for synonyms etc. we have the
		// concept after all
		FacetTerm term = new FacetTerm(NodeIDPrefixConstants.TERM + 0, "INTERLEUKIN 10");
		node.setTerms(Collections.singletonList(term));
		ParseTree query = new ParseTree(node, new ParseErrors());
		SearchServerQuery serverQuery = translator.translateToBooleanQuery(query, "text", null);

		assertEquals(TermQuery.class, serverQuery.getClass());
		TermQuery termQuery = (TermQuery) serverQuery;
		assertEquals(NodeIDPrefixConstants.TERM + 0, termQuery.term);
	}

	@Test
	public void testMeshFieldQueryTranslation() {
		// The MeSH field is a field that contains only term IDs, nothing else.
		// We don't need a word query but just term queries which are very fast.
		// We assume a non-user-selected concept because otherwise we would only
		// search for concept IDs anyway.
		TestQueryTranslator translator = new TestQueryTranslator();
		TextNode node = new TextNode("cell communication");
		FacetTerm term = new FacetTerm(NodeIDPrefixConstants.TERM + 0, "Cell Communication");
		node.setTerms(Collections.singletonList(term));
		ParseTree query = new ParseTree(node, new ParseErrors());
		SearchServerQuery serverQuery = translator.translateToBooleanQuery(query, GeneralIndexStructure.mesh, null);

		assertEquals(TermQuery.class, serverQuery.getClass());
		TermQuery termQuery = (TermQuery) serverQuery;
		assertEquals(NodeIDPrefixConstants.TERM + 0, termQuery.term);
		
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

		// public SearchServerQuery translateToBooleanQueryTest(ParseTree query,
		// String field) {
		// return translateToBooleanQuery(query, field);
		// }

	}
}
