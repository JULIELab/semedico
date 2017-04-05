package de.julielab.semedico.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.core.query.InputEventQuery;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.util.ConceptValueEncoder;

/**
 * Start page of application semedico-frontend.
 */
@Import(
	stylesheet =
	{
//		"context:js/jquery-ui/jquery-ui.min.css",
//		"context:css/semedico-index.css",
//		"context:css/semedico-base.css",
//		"context:css/semedico-tutorial.css"
			"context:less/pages/index.less",
			"context:less/semedico.less"
	},
	library =
	{
//		"context:js/jquery.min.js",
//		"context:js/jquery-ui/jquery-ui.min.js",
//		"index.js",
//		"context:js/tutorial.js"
	}
)

public class Index extends Search
{
	@Inject
	private Request request;

	@SessionState
	private SemedicoSessionState sessionState;

	@Persist
	@Property
	@Deprecated
	private String arg1TermId;

	@Persist
	@Deprecated
	private String arg1Text;

	@Persist
	@Property
	@Deprecated
	private String arg2TermId;

	@Persist
	@Deprecated
	private String arg2Text;
	@Deprecated
	@Persist
	@Property
	private String arg1FacetId;
	@Deprecated
	@Persist
	@Property
	private String arg2FacetId;

	@Deprecated
	@Property
	@Persist
	private List<String> eventTypeList;

	@Deprecated
	@Property
	@Persist
	private List<String> likelihoodIndicatorList;
	@Deprecated
	@Property
	@Persist
	private InputEventQuery currentEvent;

//	@InjectComponent
//	private Zone eventZone;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources resources;

	@Deprecated
	@Property
	private SelectModel conceptSelectModel;
	@Deprecated
	@Inject
	SelectModelFactory selectModelFactory;

	@Deprecated
	@Inject
	protected ITokenInputService tokenInputService;
	
	@Inject
	private Logger log;

//	public Object onActivate() {
//		super.onActivate();
//		if (null == request.getParameter("t:formdata") && request.getPath().contains("index.search")) {
//			return this;
//		}
//		return null;
//	}
	
	public void setupRender()
	{
		super.setupRender();

//		List<IConcept> eventTerms = termService.getTermsByLabel(TermLabels.GeneralLabel.EVENT_TERM.name(), true);
//		List<IConcept> eventTermsSelectionList = new ArrayList<>();
//		eventTermsSelectionList.add(termService.getCoreTerm(CoreTermType.ANY_MOLECULAR_INTERACTION));
//		eventTermsSelectionList.addAll(eventTerms);
//		conceptSelectModel = selectModelFactory.create(eventTermsSelectionList, "preferredName");

		resetQueryState();
		currentEvent.setSecondArgument(arg2TermId);
		currentEvent.setFirstArgumentText(arg1Text == null ? "[None]" : arg1Text);
		currentEvent.setSecondArgumentText(arg2Text == null ? "[None]" : arg2Text);
		eventQueries.add(currentEvent);
		currentEvent = new InputEventQuery();
		String tutorialMode = request.getParameter("tutorialMode");
		
		// avoid creation of the session state if possible
		if (null != tutorialMode)
		{
			sessionState.setTutorialMode(Boolean.parseBoolean(tutorialMode));
		}
	}

//	public Object onSuccessFromSearch() throws IOException {
//		if (tokens.length() == 0) {
//			log.info("No user input given, returning to index (this page).");
//			return null;
//		}
//		
//		log.info("User token input from search field was: {}", tokens);
//		
//		List<QueryToken> userInputQueryTokens = tokenInputService.convertToQueryTokens(tokens);
//		
////		if (terms == null || terms.equals("")) {
////			String autocompletionQuery = getAutocompletionQuery();
////			if (autocompletionQuery == null || autocompletionQuery.equals("")) {
////				List<InputEventQuery> eventQueries = getInputEventQueries();
////				if (eventQueries == null || eventQueries.size() == 0)
////					return null;
////			} else
////				setEnteredQuery(autocompletionQuery);
////		}
//
//		Object resultList = performNewSearch(userInputQueryTokens);
//
//		setEnteredQuery(null);
//		// setTermId(null);
//		setFacetId(null);
//		setInputEventQueries(null);
//
//		return resultList;
//	}

	@Deprecated
	Object onAddEventQuery() {
		currentEvent.setFirstArgument(arg1TermId);
		currentEvent.setSecondArgument(arg2TermId);
		currentEvent.setFirstArgumentText(arg1Text == null ? "[None]" : arg1Text);
		currentEvent.setSecondArgumentText(arg2Text == null ? "[None]" : arg2Text);
		eventQueries.add(currentEvent);
		currentEvent = new InputEventQuery();
		// return request.isXHR() ? eventZone.getBody() : null;
		return null;
	}

//	@Deprecated
//	Object onRemoveEventQuery() {
//		if (eventQueries.size() > 1) {
//			eventQueries.remove(eventQueries.size() - 1);
//			return request.isXHR() ? eventZone.getBody() : null;
//		}
//		return null;
//	}

//	@Deprecated
//	public void onSuggClickFromArg1Input() {
//		arg1Text = getEnteredQuery();
//	}
//
//	@Deprecated
//	public void onSuggClickFromArg2Input() {
//		arg2Text = getEnteredQuery();
//	}
//
//	public void onSelectEventType(String type) {
//		// this is only the selected event name, not an ID. We will have to
//		// analyze it later and somehow ensure that
//		// this is canonical.
//		currentEvent.setEventType(type);
//	}

	protected void resetQueryState()
	{
		// if (null == eventTypeList) {
		// eventTypeList = Lists.newArrayList("Any molecular interaction",
		// "Gene expression", "Binding", "Negative regulation",
		// "Transcription", "Positive regulation", "Regulation",
		// "Localization", "Phosphorylation", "Protein catabolism");
		// }
		// if (null == likelihoodIndicatorList) {
		// likelihoodIndicatorList = Lists.newArrayList("low", "moderate",
		// "high", "negation", "assertion", "investigation");
		// }
		arg1TermId = null;
		arg2TermId = null;
		arg1FacetId = null;
		arg2FacetId = null;
		eventQueries = new ArrayList<>();
		currentEvent = new InputEventQuery();
		arg1Text = null;
		arg2Text = null;
	}

//	public List<FacetTermSuggestionStream> onProvideCompletionsFromArg1Input(String query) throws IOException,
//			SQLException {
//
//		if (query == null)
//			return Collections.emptyList();
//
//		// there should be a facet label like "event compatible" or something,
//		// after all also miRNAs can be used here in
//		// theory!
//		List<FacetTermSuggestionStream> suggestionsForFragment =
//				termSuggestionService.getSuggestionsForFragment(query,
//						Lists.newArrayList(facetService.getFacetByName("Genes and Proteins")));
//		CoreTerm ct = termService.getCoreTerm(CoreTermType.ANY_TERM);
//		FacetTermSuggestionStream coreFacetStream = new FacetTermSuggestionStream(Facet.CORE_TERMS_FACET);
//		coreFacetStream.addTermSuggestion(query, ct.getPreferredName(), StringUtils.join(ct.getSynonyms(), ", "), null, QueryTokenizerImpl.ALPHANUM);
//		suggestionsForFragment.add(coreFacetStream);
//
//		return suggestionsForFragment;
//	}
//
//	public List<FacetTermSuggestionStream> onProvideCompletionsFromArg2Input(String query) throws IOException,
//			SQLException {
//
//		if (query == null)
//			return Collections.emptyList();
//
//		List<FacetTermSuggestionStream> suggestionsForFragment =
//				termSuggestionService.getSuggestionsForFragment(query,
//						Lists.newArrayList(facetService.getFacetByName("Genes and Proteins")));
//		CoreTerm ct = termService.getCoreTerm(CoreTermType.ANY_TERM);
//		FacetTermSuggestionStream coreFacetStream = new FacetTermSuggestionStream(Facet.CORE_TERMS_FACET);
//		coreFacetStream.addTermSuggestion(query, ct.getPreferredName(), StringUtils.join(ct.getSynonyms(), ", "), null, QueryTokenizerImpl.ALPHANUM);
//		suggestionsForFragment.add(coreFacetStream);
//
//		return suggestionsForFragment;
//	}

	public ConceptValueEncoder getConceptValueEncoder()
	{
		return new ConceptValueEncoder(termService);
	}

	@AfterRender
	public Object afterRender()
	{
		super.afterRender();
//		Link eventTypeSelectLink = resources.createEventLink("selectEventType");
//		javaScriptSupport.addScript("var eventTypeSelect = new EventTypeSelect('%s');", eventTypeSelectLink);
		return null;
	}

	@Override
	protected Logger getLogger()
	{
		return log;
	}
	
	public String getGoogleFontStyle()
	{
		return "https://fonts.googleapis.com/css?family=Open+Sans:400,300&subset=latin,greek,greek-ext,vietnamese,cyrillic-ext,cyrillic,latin-ext";
	}
	
}