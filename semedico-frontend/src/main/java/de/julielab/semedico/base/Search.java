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
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ILexerService;
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.services.IStatefulSearchService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.state.tabs.ApplicationTab;
import de.julielab.semedico.state.tabs.ApplicationTab.TabType;

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
	/**
	 * @deprecated not required anymore with the token input method
	 */
	@Persist
	@Deprecated
	private String autocompletionQuery;

	@Persist
	@Deprecated
	private String enteredQuery;

	@Property
	@Persist(value = PersistenceConstants.FLASH)
	private String errorMessage;

	@Property
	@Deprecated
	private int eventQueryLoopIndex;
	@Deprecated
	@Persist
	private String facetId;
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
	protected IStatefulSearchService searchService;

	@SessionState
	protected SemedicoSessionState sessionState;

	@Deprecated
	@Persist
	@Property
	private String termId;

	@Inject
	protected ITermService termService;

	@Inject
	protected ITermSuggestionService termSuggestionService;

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
							sb);
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

						for (IConcept concept : qt.getTermList()) {
							disambiguationOptions.put(concept.getId());
						}

						currentObject.put("showDialogLink", disambiguationDialog.getShowDialogLink().toAbsoluteURI());
						currentObject.put("getConceptTokensLink",
								resources.createEventLink("getConceptTokens").toAbsoluteURI());
						currentObject.put("disambiguationOptions", disambiguationOptions);
						currentObject.put("name", qt.getOriginalValue());
						break;
					case CONCEPT:
						currentObject.put("termid", qt.getTermList().get(0).getId());

						if (null != qt.getMatchedSynonym()) {
							currentObject.put("name", qt.getMatchedSynonym());

							if (!qt.getMatchedSynonym().equals(qt.getTermList().get(0).getPreferredName())) {
								currentObject.put(ITokenInputService.PREFERRED_NAME,
										qt.getTermList().get(0).getPreferredName());
							}
						} else if (null != qt.getOriginalValue()) {
							currentObject.put("name", qt.getOriginalValue());
							if (!qt.getOriginalValue().equals(qt.getTermList().get(0).getPreferredName())) {
								currentObject.put(ITokenInputService.PREFERRED_NAME,
										qt.getTermList().get(0).getPreferredName());
							}
						} else {
							currentObject.put("name", qt.getTermList().get(0).getPreferredName());
						}

						currentObject.put(ITokenInputService.USER_SELECTED, qt.isUserSelected());
						JSONArray synonyms = new JSONArray();

						for (String synonym : qt.getTermList().get(0).getSynonyms()) {
							synonyms.put(synonym);
						}

						currentObject.put("synonyms", synonyms);

						if (null != qt.getTermList().get(0).getDescriptions()
								&& !qt.getTermList().get(0).getDescriptions().isEmpty()) {
							JSONArray descriptions = new JSONArray();
							for (String description : qt.getTermList().get(0).getDescriptions()) {
								descriptions.put(description);
							}
							currentObject.put("descriptions", descriptions);
						}

						currentObject.put(ITokenInputService.FACET_NAME,
								qt.getTermList().get(0).getFirstFacet().getName());
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

	protected abstract Logger getLogger();

	protected JSONArray onGetConceptTokens() {
		String conceptIdsCSV = request.getParameter("q");
		String[] conceptIds = conceptIdsCSV.split(",");
		List<QueryToken> conceptQts = new ArrayList<>();

		for (int i = 0; i < conceptIds.length; ++i) {
			String conceptId = conceptIds[i];
			IConcept concept = termService.getTerm(conceptId);
			QueryToken qt = new QueryToken(0, 0);
			qt.addTermToList(concept);
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
		return termSuggestionService.getSuggestionsForFragment(query, null);
	}

	public Object onSuccessFromSearch()  {
		Logger log = getLogger();

		if (tokens.length() == 0) {
			log.info("No user input given, returning to index (this page).");
			return null;
		}

		log.info("User token input from search field was: {}", tokens);

		List<QueryToken> userInputQueryTokens = tokenInputService.convertToQueryTokens(tokens);

		Object results = performNewSearch(userInputQueryTokens);

		setEnteredQuery(null);
		setFacetId(null);

		return results;
	}

	protected Object performNewSearch(List<QueryToken> userInputQueryTokens) {
		logger.info("Starting search with query \"{}\".", tokens);

		// If an empty search was issued, don't do anything.
		if (userInputQueryTokens.isEmpty()) {
			return null;
		}

		ApplicationTab activeTab = sessionState.getActiveTab();

		if (null == activeTab) {
			activeTab = sessionState.addTab(TabType.DOC_RETRIEVAL);
		}

		LegacySemedicoSearchResult searchResult = null;

		try {
			searchResult = (LegacySemedicoSearchResult) searchService.doNewDocumentSearch(userInputQueryTokens).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		if (null == searchResult) {
			errorMessage = "An unexpected error has occured. Please reformulate your query or try again later.";
		}

		else if (searchResult.errorMessage != null) {
			errorMessage = searchResult.errorMessage;
		} else {
			resultList.setSearchResult(searchResult);
			setEnteredQuery(null);
			setFacetId(null);
			Link link = linkSource.createPageRenderLinkWithContext(ResultList.class);
			link.addParameter(SemedicoSessionState.PARAM_ACTIVE_TAB, activeTab.getTabIndexAsString());

			return link;
		}

		return null;
	}

	public ResultList performSubSearch() {
		LegacySemedicoSearchResult searchResult = null;

		try {
			searchResult = (LegacySemedicoSearchResult) searchService
					.doTermSelectSearch(sessionState.getDocumentRetrievalSearchState().getSemedicoQuery(),
							sessionState.getDocumentRetrievalSearchState().getUserQueryString())
					.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		if (null == searchResult) {
			errorMessage = "An unexpected error has occured. Please reformulate your query or try again later.";
		} else if (searchResult.errorMessage != null) {
			errorMessage = searchResult.errorMessage;
		} else {
			resultList.setSearchResult(searchResult);
			setEnteredQuery(null);
			setFacetId(null);
			return resultList;
		}
		return null;
	}

	public void setAutocompletionQuery(String autocompletionQuery) {
		this.autocompletionQuery = autocompletionQuery;
	}

	public void setEnteredQuery(String query) {
		this.enteredQuery = query;
	}

	/**
	 * @param facetId
	 *            the facetId to set
	 */
	public void setFacetId(String facetId) {
		this.facetId = facetId;
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
	private ITermRecognitionService termRecognitionService;
	@Inject
	private ILexerService lexerService;

	public JSONArray onConceptRecognition() throws IOException {
		String input = request.getParameter("q");
		List<QueryToken> lex = lexerService.lex(input);
		List<QueryToken> conceptTokens = termRecognitionService.recognizeTerms(lex); 
		return convertQueryToJson(conceptTokens);
	}

	public boolean showErrorDialog() {
		return !StringUtils.isEmpty(errorMessage);
	}
}
