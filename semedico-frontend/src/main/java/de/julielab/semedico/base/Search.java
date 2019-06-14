package de.julielab.semedico.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;

import de.julielab.semedico.components.DisambiguationDialog;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.services.query.IConceptRecognitionService;
import de.julielab.semedico.core.services.query.ILexerService;
import de.julielab.semedico.core.suggestions.IConceptSuggestionService;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.state.tabs.ApplicationTab;
import de.julielab.semedico.state.tabs.ApplicationTab.TabType;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Import(stylesheet =
{
	"context:css/semedico-icons.css",
	"context:css/semedico-dialogs.css",
	"context:css/semedico-tooltips.css",
	"context:css/semedico-search.css",
},
library =
{
	"context:js/semedico.js",
	"context:js/jquery.tokeninput.js",
	"context:js/jquery.dotdotdot.min.js",
	"context:js/jquery-ui/jquery-ui.min.js",
	"context:js/jquery.ui.touch-punch.min.js",
	"search.js",
	"search-tokendecoration.js",
	"search_errorDialog.js"
})

public abstract class Search
{
	@Property
	@Persist(value = PersistenceConstants.FLASH)
	private String errorMessage;

	
	@Inject
	protected IFacetService facetService;
	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private PageRenderLinkSource linkSource;

	@Inject
	private Logger logger;

	@Property
	private JSONObject autocompleteParameters;

	@Inject
	private Request request;

	@Inject
	private ComponentResources resources;

	@InjectPage
	private ResultList resultList;

	@Inject
	protected ISearchService searchService;

	@SessionState
	protected SemedicoSessionState sessionState;

	
	@Inject
	protected IConceptService termService;

	@Inject
	protected IConceptSuggestionService termSuggestionService;

	@Inject
	protected ITokenInputService tokenInputService;

	@Property
	protected JSONArray tokens;

	@Persist
	private String tutorialMode;

	@AfterRender

	public Object afterRender() {
		if (showErrorDialog()) {
			javaScriptSupport.addScript("showErrorDialog()");
		}

		javaScriptSupport.addInitializerCall("assignTokenClasses", new JSONArray());

		return null;
	}

	@InjectComponent
	private DisambiguationDialog disambiguationDialog;

	private JSONArray convertQueryToJson(List<QueryToken> queryTokens) {
		try {
			if (queryTokens != null) {
				JSONArray jsonTokens = new JSONArray();

				if (logger.isDebugEnabled()) {
					StringBuilder sb = new StringBuilder();

					for (QueryToken node : queryTokens) // lohr - Bearbeitung
														// aller eingeg. Token
														// (durch Leerzeichen
														// getrennt)
					{
						sb.append(node.getOriginalValue());

						sb.append(" ");
					}

					sb.deleteCharAt(sb.length() - 1);
					logger.debug("Filling 'token' parameter for prepopulation of AutoComplete mixin with nodes: {}",
							sb.toString());
				}

				for (QueryToken qt : queryTokens) {
					logger.debug("Now converting query token '{}'", qt.getOriginalValue());
					JSONObject currentObject = new JSONObject();
					ITokenInputService.TokenType tokenType = qt.getInputTokenType();
					QueryBuilder query = qt.getQuery();
					if (query != null) {
						String queryString = query.toString();
						currentObject.put("query", queryString);
						String priority = qt.getPriority().toString();
						currentObject.put("priority", priority);
					}

					switch (qt.getInputTokenType()) {
					case AMBIGUOUS_CONCEPT:
						// disambiguationOptions
						JSONArray disambiguationOptions = new JSONArray();

						for (IConcept concept : qt.getConceptList()) {
							disambiguationOptions.put(concept.getId());
						}

						currentObject.put("showDialogLink", disambiguationDialog.getShowDialogLink().toAbsoluteURI());
						currentObject.put("getConceptTokensLink",
								resources.createEventLink("getConceptTokens").toAbsoluteURI());
						currentObject.put("disambiguationOptions", disambiguationOptions);
						currentObject.put("name", qt.getOriginalValue());
						break;
					case CONCEPT:
						currentObject.put("termid", qt.getConceptList().get(0).getId());

						if (null != qt.getMatchedSynonym()) {
							currentObject.put("name", qt.getMatchedSynonym());

							if (!qt.getMatchedSynonym().equals(qt.getConceptList().get(0).getPreferredName())) {
								currentObject.put(ITokenInputService.PREFERRED_NAME,
										qt.getConceptList().get(0).getPreferredName());
							}
						} else if (null != qt.getOriginalValue()) {
							currentObject.put("name", qt.getOriginalValue());
							if (!qt.getOriginalValue().equals(qt.getConceptList().get(0).getPreferredName())) {
								currentObject.put(ITokenInputService.PREFERRED_NAME,
										qt.getConceptList().get(0).getPreferredName());
							}
						} else {
							currentObject.put("name", qt.getConceptList().get(0).getPreferredName());
						}

						currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
						JSONArray synonyms = new JSONArray();

						for (String synonym : qt.getConceptList().get(0).getSynonyms()) {
							synonyms.put(synonym);
						}

						currentObject.put("synonyms", synonyms);

						if (null != qt.getConceptList().get(0).getDescriptions()
								&& qt.getConceptList().get(0).getDescriptions().size() > 0) {
							JSONArray descriptions = new JSONArray();
							for (String description : qt.getConceptList().get(0).getDescriptions()) {
								descriptions.put(description);
							}
							currentObject.put("descriptions", descriptions);
						}

						currentObject.put(ITokenInputService.FACET_NAME,
								qt.getConceptList().get(0).getFirstFacet().getName());
						break;

					case KEYWORD:
						currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
						currentObject.put("name", qt.getOriginalValue());
						currentObject.put(ITokenInputService.FACET_NAME, Facet.KEYWORD_FACET.getName());
						break;
					case AND:
					case OR:
					case NOT:
					case LEXER:
						currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
						currentObject.put("name", qt.getInputTokenType().name());
						currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
						break;
					case LEFT_PARENTHESIS:
						currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
						currentObject.put("name", "(");
						currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
						break;
					case RIGHT_PARENTHESIS:
						currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
						currentObject.put("name", ")");
						currentObject.put(ITokenInputService.FACET_NAME, Facet.BOOLEAN_OPERATORS_FACET.getName());
						break;
					default:
						tokenType = TokenType.LEXER;
						currentObject.put(ITokenInputService.LEXER_TYPE, String.valueOf(qt.getType()));
						currentObject.put("name", qt.getOriginalValue());
						break;
					}
					currentObject.put(ITokenInputService.TOKEN_TYPE, tokenType.name());
					jsonTokens.put(currentObject);
					logger.debug("Adding JSON to search field: {}", currentObject);
				}
				return jsonTokens;
			}
		} catch (Exception e) {
			logger.error(
					"Exception occurred during conversion of query tokens into JSON format for token input field prepopulation:",
					e);
			// something went wrong with query translation; this could be due to
			// a corrupted query. Shouldn't happen, of course, but better reset
			// the query or we won't ever recover
			sessionState.getDocumentRetrievalSearchState().setDisambiguatedQuery(null);
		}
		return null;
	}

	abstract protected Logger getLogger();

	protected JSONArray onGetConceptTokens() {
		String conceptIdsCSV = request.getParameter("q");
		String[] conceptIds = conceptIdsCSV.split(",");
		List<QueryToken> conceptQts = new ArrayList<>();

		for (int i = 0; i < conceptIds.length; ++i) {
			String conceptId = conceptIds[i];
			IConcept concept = termService.getTerm(conceptId);
			QueryToken qt = new QueryToken(0, 0);
			qt.addConceptToList(concept);
			qt.setInputTokenType(TokenType.CONCEPT);
			qt.setUserSelected(true);
			conceptQts.add(qt);
		}
		return convertQueryToJson(conceptQts);
	}

	public List<FacetTermSuggestionStream> onProvideCompletionsFromSearchInputField(String query) {
		if (query == null) {
			return Collections.emptyList();
		}

		List<FacetTermSuggestionStream> suggestions = termSuggestionService.getSuggestionsForFragment(query, null);
		return suggestions;
	}

	public Object onSuccessFromSearch() throws IOException {
		Logger log = getLogger();

		if (tokens.length() == 0) {
			log.info("No user input given, returning to index (this page).");
			return null;
		}

		log.info("User token input from search field was: {}", tokens);

		List<QueryToken> userInputQueryTokens = tokenInputService.convertToQueryTokens(tokens);

		// if (terms == null || terms.equals("")) {
		// String autocompletionQuery = getAutocompletionQuery();
		// if (autocompletionQuery == null || autocompletionQuery.equals("")) {
		// List<InputEventQuery> eventQueries = getInputEventQueries();
		// if (eventQueries == null || eventQueries.size() == 0)
		// return null;
		// } else
		// setEnteredQuery(autocompletionQuery);
		// }

		Object resultList = performNewSearch(userInputQueryTokens);

		return resultList;
	}

	protected Object performNewSearch(List<QueryToken> userInputQueryTokens) {

		logger.info("Starting search with query \"{}\".", tokens);

		/**
		 * Originally, we used this object to search for events/relations
		 * separately from the text input field. However, it was not clear how
		 * exactly to deal with the situation when a user inputs text into the
		 * search field and also uses the event search form at the same time. So
		 * now the text search field is still the main point of entrance for any
		 * query. The Event Query Panel is just supposed to help with the event
		 * search, since it is not completely intuitive what exact kind of
		 * events with which exact arguments can be searched for. So the Event
		 * Query Panel is some kind of interactive query facility. TODO The idea
		 * is that changes in the event form should reflect in the text input
		 * field and vice versa. However, this is not yet implemented.
		 */
		UserQuery userQuery = new UserQuery();
		userQuery.tokens = userInputQueryTokens;

		// If an empty search was issued, don't do anything.
		if (userInputQueryTokens.isEmpty()) {
			return null;
		}

		ApplicationTab activeTab = sessionState.getActiveTab();

		if (null == activeTab) {
			activeTab = sessionState.addTab(TabType.DOC_RETRIEVAL);
		}

		LegacySemedicoSearchResult searchResult = null;

//		try {
//			searchResult = (LegacySemedicoSearchResult) searchService.doNewDocumentSearch(userQuery).get(); // TODO
//																											// wichtige
//																											// Zeile
//																											// zum
//																											// Ausführen
//		} catch (InterruptedException | ExecutionException e) {
//			e.printStackTrace();
//		}

		if (null == searchResult) {
			errorMessage = "An unexpected error has occured. Please reformulate your query or try again later.";
		}

		else if (searchResult.errorMessage != null) {
			errorMessage = searchResult.errorMessage;
		} else {
			resultList.setSearchResult(searchResult);
			Link link = linkSource.createPageRenderLinkWithContext(ResultList.class);
			link.addParameter(SemedicoSessionState.PARAM_ACTIVE_TAB, activeTab.getTabIndexAsString());
			// if (null != tutorialMode) {
			// link.addParameterValue("tutorialMode", tutorialMode);
			// link.addParameterValue("tutorialStep", 0);
			// }
			return link;
		}

		return null;
	}

	public ResultList performSubSearch() {
		LegacySemedicoSearchResult searchResult = null;

//		try {
//			searchResult = (LegacySemedicoSearchResult) searchService
//					.doTermSelectSearch(sessionState.getDocumentRetrievalSearchState().getSemedicoQuery(),
//							sessionState.getDocumentRetrievalSearchState().getUserQueryString())
//					.get();
//		} catch (InterruptedException | ExecutionException e) {
//			e.printStackTrace();
//		}

		if (null == searchResult) {
			errorMessage = "An unexpected error has occured. Please reformulate your query or try again later.";
		} else if (searchResult.errorMessage != null) {
			errorMessage = searchResult.errorMessage;
		} else {
			resultList.setSearchResult(searchResult);
			return resultList;
		}
		return null;
	}


	public void setupRender() {
		if (null != sessionState) {
			SearchState searchState = sessionState.getDocumentRetrievalSearchState();
			// fill the tokens parameter, that is connected to the AutoComplete
			// mixin - with the current query for prepopulation of the jQuery
			// Token Input plugin. This way, the input field always shows the
			// current query
			ParseTree query = searchState.getSemedicoQuery();

			if (null != query) {
				tokens = convertQueryToJson(query.getQueryTokens());
			}
		}
		Link conceptRecognitionLink = resources.createEventLink("conceptRecognition");
		autocompleteParameters = new JSONObject();
		autocompleteParameters.put("conceptRecognitionUrl", conceptRecognitionLink.toAbsoluteURI());
	}

	@Inject
	private IConceptRecognitionService termRecognitionService;
	@Inject
	private ILexerService lexerService;

	public JSONArray onConceptRecognition() throws IOException {
		String input = request.getParameter("q");
		List<QueryToken> lex = lexerService.lex(input);
		List<QueryToken> conceptTokens = termRecognitionService.recognizeTerms(lex, 0);
		JSONArray jsonTokens = convertQueryToJson(conceptTokens);
		return jsonTokens;
	}

	public boolean showErrorDialog() {
		return !StringUtils.isEmpty(errorMessage);
	}
}
