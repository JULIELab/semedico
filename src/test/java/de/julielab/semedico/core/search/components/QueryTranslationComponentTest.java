package de.julielab.semedico.core.search.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Arrays;

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
import de.julielab.elastic.query.components.data.query.FunctionScoreQuery;
import de.julielab.elastic.query.components.data.query.MatchPhraseQuery;
import de.julielab.elastic.query.components.data.query.MatchQuery;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.WildcardQuery;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.query.translation.SearchTask;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.SemedicoCoreProductionModule;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class QueryTranslationComponentTest {
	private static ISearchComponent queryAnalysisComponent;
	private static ISearchComponent translationComponent;
	private static Registry registry;
	// private static TextSearchPreparationComponent
	// textSearchPreparationComponent;
	// private static ISearchComponent resultListCreationComponent;
	// private static ElasticSearchComponent searchComponent;
	private static Gson gson;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setup() {
		TestUtils.setTestConfigurationSystemProperties();

		registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreProductionModule.class);
		queryAnalysisComponent = registry.getService(ISearchComponent.class, QueryAnalysis.class);
		translationComponent = registry.getService(ISearchComponent.class, QueryTranslation.class);
		// textSearchPreparationComponent = new
		// TextSearchPreparationComponent(10);

		gson =  new GsonBuilder().setPrettyPrinting().create();
		
	}

	@Test
	public void testConceptPhraseQuery() throws ParseException {
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "signal transduction";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;
		searchCarrier.searchCmd.task = SearchTask.DOCUMENTS;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(BoolQuery.class, serverQuery.getClass());

		BoolQuery boolQuery = (BoolQuery) serverQuery;
		assertEquals(1, boolQuery.clauses.size());
		BoolClause clause = boolQuery.clauses.get(0);
		assertEquals(2, clause.queries.size());
		assertEquals(TermQuery.class, clause.queries.get(0).getClass());
		assertEquals(MatchPhraseQuery.class, clause.queries.get(1).getClass());
	}

	@Test
	public void testAmbiguousConceptQuery() throws ParseException {
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "Cytapheresis";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		// --------- adding a concept for creating ambiguity
		ITermService termService = registry.getService(ITermService.class);
		// it really doesn't matter which term this is
		IConcept someTerm = termService.getTerm(NodeIDPrefixConstants.TERM + 2080);
		// for the sake of the test, we now just add some term; the query was
		// not really ambiguous
		TextNode queryNode = (TextNode) searchCarrier.searchCmd.semedicoQuery.getRoot();
		queryNode.setTerms(Arrays.asList(queryNode.getTerms().get(0), someTerm));
		// --------- DONE adding a concept for creating ambiguity
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(BoolQuery.class, serverQuery.getClass());

		BoolQuery boolQuery = (BoolQuery) serverQuery;
		assertEquals(1, boolQuery.clauses.size());
		BoolClause clause = boolQuery.clauses.get(0);
		assertEquals(2, clause.queries.size());
		assertEquals(MatchQuery.class, clause.queries.get(0).getClass());
		MatchQuery termsQuery = (MatchQuery) clause.queries.get(0);
		assertEquals(2, termsQuery.query.split(" ").length);
		// the query is a single word so we should get a simple match query (as
		// opposed to a phrase query)
		assertEquals(MatchQuery.class, clause.queries.get(1).getClass());
	}

	@Test
	public void testBooleanQuery() throws ParseException {
		// "signal" and "transduction" should - as single words - not form a
		// concept with the test dictionary. Thus, we expect a boolean query
		// with one must clause which in turn has two queries, one for each
		// word.
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "signal and transduction";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
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
		// Since "plate-bound" is not a concept, we should end up with just the match phrase query
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "plate-bound";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
	}
	
	@Test
	public void testDashPhraseQuery2() throws ParseException {
		// when the user has input a kind of dash-compound, we assume she
		// actually want's to find the words in close proximity - thus, a
		// phrase.
		// Since "plate-bound" is not a concept, we should end up with just the match phrase query
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "2.634";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
	}
	
	@Test
	public void testDashPhraseQuery3() throws ParseException {
		// when the user has input a kind of dash-compound, we assume she
		// actually want's to find the words in close proximity - thus, a
		// phrase.
		// Since "plate-bound" is not a concept, we should end up with just the match phrase query
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "gp41-induced";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(MatchPhraseQuery.class, serverQuery.getClass());
	}
	
	@Test
	public void testWildcardSearch() throws ParseException {
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "* regulates mtor";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.events);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(NestedQuery.class, serverQuery.getClass());
		NestedQuery eventQuery = (NestedQuery) serverQuery;
		assertEquals(FunctionScoreQuery.class, eventQuery.query.getClass());
		FunctionScoreQuery fsq = (FunctionScoreQuery) eventQuery.query;
		assertEquals(BoolQuery.class, fsq.query.getClass());
		BoolQuery bq = (BoolQuery)fsq.query;
		
		System.out.println(gson.toJson(bq));
		assertEquals(WildcardQuery.class, bq.clauses.get(0).queries.get(0).getClass());
	}
	
	@Test
	public void testWildcardSearch2() throws ParseException {
		// check that normal text fields ignore the wildcard
		SemedicoSearchCarrier searchCarrier = new SemedicoSearchCarrier("find");
		String query = "* regulates mtor";

		QueryAnalysisCommand analysisCommand = new QueryAnalysisCommand();
		analysisCommand.userQuery = new UserQuery(query);
		searchCarrier.queryAnalysisCmd = analysisCommand;

		SemedicoSearchCommand searchCommand = new SemedicoSearchCommand();
		searchCommand.task = SearchTask.DOCUMENTS;
		searchCommand.searchFieldFilter = Arrays.asList(IIndexInformationService.GeneralIndexStructure.abstracttext);
		searchCarrier.searchCmd = searchCommand;

		SearchState searchState = new SearchState();
		searchCarrier.searchState = searchState;

		queryAnalysisComponent.process(searchCarrier);
		translationComponent.process(searchCarrier);

		// there should be a boolean query containing the search for the actual
		// concept and for the word/phrase itself
		SearchServerQuery serverQuery = searchCarrier.serverCmds.get(0).query;
		assertNotNull(serverQuery);
		assertEquals(BoolQuery.class, serverQuery.getClass());
		BoolQuery bq = (BoolQuery)serverQuery;
		assertEquals(2,  bq.clauses.get(0).queries.size());
	}
}
