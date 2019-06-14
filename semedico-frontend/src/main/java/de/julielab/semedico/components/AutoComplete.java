package de.julielab.semedico.components;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldValidationSupport;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.base.ExtensionEvents;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.state.SemedicoSessionState;

@SupportsInformalParameters
@Import( library =
	{
			"context:js/jquery.tokeninput.js","autocomplete.js"
	},
	stylesheet =
	{
		"context:css/token-input-suggestions.css"
	})

public class AutoComplete extends AbstractField {
	@Parameter(autoconnect = true, required = true)
	private JSONArray values;

	@Parameter(value = "true")
	private boolean doPrepopulation;

	@Parameter(required = false)
	private JSONObject parameters;

	@SessionState(create = false)
	SemedicoSessionState sessionState;

	@Inject
	private ComponentResources resources;

	@Inject
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private FieldValidationSupport fieldValidationSupport;

	@Parameter(defaultPrefix = BindingConstants.VALIDATE)
	private FieldValidator<Object> validator;

	@Inject
	private Request request;

	@Environmental
	private ValidationTracker tracker;

	@Inject
	private Logger log;

	/**
	 * This is called once the form is <em>submitted</em>, i.e. the user has
	 * input whatever tokens she wants to search for and hit the button to start
	 * the search.<br>
	 * Here, we get the tokens from the search input field in JSON format and
	 * just return those. Note that some 'token' could just be a 'freetext
	 * token', i.e. the user has not selected any suggestions but just entered
	 * text and hit the search button. This is not of relevance here, but for
	 * the query parsing algorithms.
	 */
	@Override
	protected void processSubmission(String controlName) {
		String parameterValue = request.getParameter(controlName); // Anfrage ZUM Server
		log.debug("Received JSON value from input field: {}", parameterValue);
		
		if (StringUtils.isBlank(parameterValue)) {
			parameterValue = "[]";
		}
		
		JSONArray results = new JSONArray(parameterValue);
		this.tracker.recordInput(this, parameterValue);
		values = results;

		putPropertyNameIntoBeanValidationContext("values");

		if (validator != null) {
			try {
				fieldValidationSupport.validate(values, resources, validator);
			}
			catch (final ValidationException e) {
				this.tracker.recordError(this, e.getMessage());
			}
		}

		removePropertyNameFromBeanValidationContext();
	}

	@BeginRender
	void writeFieldTag(MarkupWriter writer) {
		writer.element("input", "type", "text", "name", getControlName(), "id", getClientId(), "class", resources.getInformalParameter("class", String.class));
	}

	/**
	 * Enables the client-side functionality, i.e. activates the jQuery
	 * 
	 * @param writer
	 */
	@AfterRender
	void afterRender(MarkupWriter writer) {
		writer.end();

		JSONObject parameters = getInformalParametersAsJSON();

		// handle prepopulations
		if (doPrepopulation) {
			loadCurrentValues(parameters);
		}

		javaScriptSupport.addScript("setupAutoCompleter('%s','%s',%s)", getClientId(), getCallbackURL(), parameters);
//		javaScriptSupport.require("semedico/autocomplete").priority(InitializationPriority.EARLY).with(getClientId(), getCallbackURL(), parameters);
	}

	/**
	 * Retrieves options - attribute values in the tml template for this
	 * component - that are 'informal' i.e. have to no <code>@Parameter</code>
	 * annotated field in this component.<br/>
	 * Those options are just passed to the jQuery Token Plugin. That means, you
	 * can set arbitrary Token Plugin options in the template by setting
	 * appropriate attributes.
	 * 
	 * @return
	 */
	private JSONObject getInformalParametersAsJSON() {
		if (null == parameters) {
			parameters = new JSONObject();
		}

		for (String param : resources.getInformalParameterNames()) {
			if (param.equals("class")) {
				continue;
			}
			parameters.put(param, resources.getInformalParameter(param, String.class));
		}

		return parameters;
	}

	/**
	 * If query tokens are given at render time, those should be displayed by
	 * the TokenInput plugin after the page has loaded, i.e. after rendering has
	 * finished. This methods just sets the 'prepopulate' parameter of the
	 * plugin to the {@link #values} property. Thus, if the container the
	 * property to JSON tokens, they will be used to prepopulate the input
	 * field.
	 * 
	 * @param parameters
	 */
	private void loadCurrentValues(JSONObject parameters) {
		if (values != null) {
			log.debug("Prepopulating AutoComplete field with values: {}", values);
			parameters.put("prePopulate", values);
		}
	}

	/**
	 * Creates the callback URL for the plugin for suggestion requests. The
	 * callback is sent by the plugin on user input. On the Tapestry side,
	 * {@link #provideCompletion(String)} will be called with the user input
	 * string as parameter so that respective concepts can be suggested.
	 * 
	 * @return The event callback URL for suggestion creation, referencing the
	 *         event handler {@link #provideCompletion(String)}
	 */
	private String getCallbackURL() {
		return resources.createEventLink("provideList").toAbsoluteURI();
	}

	@OnEvent("provideList")
	JSONArray provideCompletion(@RequestParameter("q") String queryParams) {
		CaptureResultCallback<List<FacetTermSuggestionStream>> callback = new CaptureResultCallback<>();

		boolean wasTriggered
			= resources.triggerEvent(
				ExtensionEvents.PROVIDE_COMPLETION,
				new Object[] { queryParams },
				callback);

		if (!wasTriggered) {
			throw new RuntimeException("Event '" + ExtensionEvents.PROVIDE_COMPLETION + "' must be handled");
		}

		return toJSON(callback.getResult());
	}

	/**
	 * This builds the JSON objects for the current suggestions which are sent
	 * to the token plugin. They are rendered according to
	 * 'options.resultsFormatter' as defined in autocomplete.js during the token
	 * plugin initialization.
	 * 
	 * @param facets
	 * @return
	 */
	private JSONArray toJSON(List<FacetTermSuggestionStream> facets) {
		JSONArray array = new JSONArray();
	
		for (FacetTermSuggestionStream stream : facets)	{
			while (stream.incrementTermSuggestion()) {
				JSONObject params = new JSONObject();
				params.put(ITokenInputService.TERM_ID, stream.getTermId());
				params.put(ITokenInputService.NAME, stream.getTermName());
				params.put(ITokenInputService.PREFERRED_NAME, stream.getPreferredName());
				
				if (null != stream.getTermSynonyms()) {
					JSONArray synonymsArray = new JSONArray();
					
					for (String synonym : stream.getTermSynonyms())	{
						synonymsArray.put(synonym);
					}
					
					params.put(ITokenInputService.SYNONYMS, synonymsArray);
				}
				
				if (null != stream.getFacet()) {
					params.put(ITokenInputService.FACET_ID, stream.getFacet().getId());
				}
				
				if (stream.getShortFacetName() != null) {
					params.put(ITokenInputService.FACET_NAME, stream.getShortFacetName());
				} else {
					params.put(ITokenInputService.FACET_NAME, stream.getFacetName());
				}
				
				params.put(ITokenInputService.TOKEN_TYPE, stream.getInputTokenType().name());
				params.put(ITokenInputService.LEXER_TYPE, stream.getLexerType());
				params.put(ITokenInputService.BEGIN, stream.getBegin());
				params.put("type", "term");
				array.put(params);
			}
		}
		return array;
	}
}
