package de.julielab.semedico.core.search.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.MatchPhraseQuery;
import de.julielab.elastic.query.components.data.query.MatchQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.DocumentQuery;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IQueryAnalysisService;

public class QueryTranslationComponentTest {
	private static ISearchComponent translationComponent;
	private static Registry registry;
	private static IQueryAnalysisService queryAnalysisService;
	@SuppressWarnings("unused")
	private static Gson gson;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup() {
		registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreTestModule.class);
		queryAnalysisService = registry.getService(IQueryAnalysisService.class);
		translationComponent = registry.getService(ISearchComponent.class, QueryTranslation.class);
		gson = new GsonBuilder().setPrettyPrinting().create();

	}

	@Test
	public void testBooleanQuery() throws ParseException {
		// "signal" and "transduction" should - as single words - not form a
		// concept with the test dictionary. Thus, we expect a boolean query
		// with one must clause which in turn has two queries, one for each
		// word.
		SemedicoSearchCarrier<DocumentQuery, SemedicoSearchResult> searchCarrier = new SemedicoSearchCarrier<>("find");
		String query = "signal and transduction";

		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		DocumentQuery parseTreeQuery = new DocumentQuery(parseTree,
				new HashSet<>(Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext)));
		searchCarrier.query = parseTreeQuery;
		searchCarrier.query.setIndex("myIndex");
		searchCarrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverRequests.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(BoolQuery.class, serverQuery.getClass());

		BoolQuery boolQuery = (BoolQuery) serverQuery;
		assertEquals(1, boolQuery.clauses.size());
		BoolClause clause = boolQuery.clauses.get(0);
		assertEquals(2, clause.queries.size());
		assertEquals(Occur.MUST, clause.occur);
		assertEquals(MatchQuery.class, clause.queries.get(0).getClass());
		// the query is a single word so we should get a simple match query (as
		// opposed to a phrase query)
		assertEquals(MatchQuery.class, clause.queries.get(1).getClass());
	}

	@Test
	public void testDashPhraseQuery() throws ParseException {
		// when the user has input a kind of dash-compound, we assume she
		// actually want's to find the words in close proximity - thus, a
		// phrase.
		// Since "plate-bound" is not a concept, we should end up with just the
		// match phrase query
		SemedicoSearchCarrier<DocumentQuery, SemedicoSearchResult> searchCarrier = new SemedicoSearchCarrier<>("find");
		String query = "plate-bound";

		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		DocumentQuery parseTreeQuery = new DocumentQuery(parseTree,
				new HashSet<>(Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext)));
		searchCarrier.query = parseTreeQuery;
		searchCarrier.query.setIndex("myIndex");
		searchCarrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverRequests.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
	}

	@Test
	public void testDashPhraseQuery2() throws ParseException {
		// when the user has input a kind of dash-compound, we assume she
		// actually want's to find the words in close proximity - thus, a
		// phrase.
		// Since "plate-bound" is not a concept, we should end up with just the
		// match phrase query
		SemedicoSearchCarrier<DocumentQuery, SemedicoSearchResult> searchCarrier = new SemedicoSearchCarrier<>("find");
		String query = "2.634";

		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		DocumentQuery parseTreeQuery = new DocumentQuery(parseTree,
				new HashSet<>(Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext)));
		searchCarrier.query = parseTreeQuery;
		searchCarrier.query.setIndex("myIndex");
		searchCarrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverRequests.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
	}

	@Test
	public void testDashPhraseQuery3() throws ParseException {
		// when the user has input a kind of dash-compound, we assume she
		// actually want's to find the words in close proximity - thus, a
		// phrase.
		// Since "plate-bound" is not a concept, we should end up with just the
		// match phrase query
		SemedicoSearchCarrier<DocumentQuery, SemedicoSearchResult> searchCarrier = new SemedicoSearchCarrier<>("find");
		String query = "gp41-induced";

		ParseTree parseTree = queryAnalysisService.analyseQueryString(query);
		DocumentQuery parseTreeQuery = new DocumentQuery(parseTree,
				new HashSet<>(Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext)));
		searchCarrier.query = parseTreeQuery;
		searchCarrier.query.setIndex("myIndex");
		searchCarrier.query.setIndexTypes(Arrays.asList(IIndexInformationService.Indexes.DocumentTypes.medline));

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverRequests.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
	}

}
