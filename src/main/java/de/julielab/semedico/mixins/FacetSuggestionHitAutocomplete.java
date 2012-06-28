package de.julielab.semedico.mixins;

import java.text.StringCharacterIterator;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.corelib.mixins.Autocomplete;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import com.ibm.icu.text.StringSearch;

import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.services.IRuleBasedCollatorWrapper;

public class FacetSuggestionHitAutocomplete extends Autocomplete {

	// Set in the tml by the "value" attribute of the textfield containing the
	// autocomplete mixin.
	private static final String QUERY = "query";

	// Set as an attribute by the markup writer below and then read out by the
	// javascript.
	private static final String TERM_ID = "termId";
	private static final String FACET_ID = "facetId";

	private final static int MAX_HIT_COUNT = 50;

	private static final String PARAM_NAME = "t:input";
	private final String URL_SCRIPT = "$T(\"%s\").suggestURL = \"%s\";";

	private static final String EVENT_NAME = "action";

	@InjectContainer
	private Field field;

	@Inject
	@Path("suggestions.js")
	private Asset suggestionsJS;

	@Inject
	private Request request;

	@Inject
	private ComponentResources resources;

	@Environmental
	private JavaScriptSupport renderSupport;

	@Parameter
	private String clickedTermId;

	@Parameter
	private String clickedTermFacetId;

	@Parameter
	private String termText;

	@Inject
	private IRuleBasedCollatorWrapper collatorWrapper;

	@Override
	protected void configure(JSONObject config) {
		config.put(
				"afterUpdateElement",
				new JSONLiteral(
						"function selectSuggestion(element, selectedElement) {"
								+ "var suggestURL = $T('searchInputField').suggestURL;"
								+
								// This should be one of "term selected" or
								// "facet selected" (original class
								// name plus 'selected' given by the
								// autocompleter itself).
								"var className = selectedElement.className;"
								+
								// Only trigger the search when we have clicked
								// a term. Clicking a facet
								// shouldn't do anything.
								// "if (className.indexOf('term') == -1)"
								// + "	return;"
								// +
								// Get the text of the selected <li> element but
								// ignore all elements which
								// are of class 'informal'. The class 'informal'
								// is used for the synonyms.
								// The rest of the text is just the term name.
								"var newTerm = Element.collectTextNodesIgnoreClass(selectedElement,	'informal').strip();"
								+ "var termId = selectedElement.getAttribute('termId');"
								+ "var facetId = selectedElement.getAttribute('facetId');"
								+ "var url = suggestURL + '?query=' + encodeURIComponent(newTerm) + '&termId=' + encodeURIComponent(termId)"
								+ "	+ '&facetId=' + facetId;"
								+ "window.location.href = url;}"));
		config.put("minChars", "1");
	}

	// Triggered only when a suggestion is clicked on.
	@OnEvent(value = "action")
	void readParameters() {
		// Element attribute TERM_ID and FACET_ID set by the markup writer
		// below.
		clickedTermId = request.getParameter(TERM_ID);
		clickedTermFacetId = request.getParameter(FACET_ID);
		// Textfield "value" attribute
		termText = request.getParameter(QUERY);
	}

	@AfterRender
	void addJavaScript(MarkupWriter markupWriter) {
		renderSupport.importJavaScriptLibrary(suggestionsJS);
		Link link = resources.createEventLink(EVENT_NAME);
		renderSupport.addScript(URL_SCRIPT, field.getClientId(),
				link.toAbsoluteURI());
	}

	@Override
	protected void generateResponseMarkup(MarkupWriter writer,
			@SuppressWarnings("rawtypes") List suggestions) {
		String query = request.getParameter(PARAM_NAME);

		int suggestionsPerFacet = (int) Math.ceil((double) MAX_HIT_COUNT
				/ (double) suggestions.size());
		int overflows = 0;
		int underflowSum = 0;

		@SuppressWarnings("unchecked")
		List<FacetTermSuggestionStream> facetSuggestionStreams = suggestions;
		for (FacetTermSuggestionStream hit : facetSuggestionStreams) {
			int size = hit.size();
			if (size < suggestionsPerFacet)
				underflowSum += size - suggestionsPerFacet;
			else if (size > suggestionsPerFacet)
				overflows++;
		}

		if (underflowSum > 0 && overflows > 0)
			suggestionsPerFacet = suggestionsPerFacet
					+ (int) Math.ceil((double) underflowSum
							/ (double) overflows);

		StringCharacterIterator suggestionTextIt = new StringCharacterIterator(
				"dummy");
		StringSearch search = new StringSearch("dummy", suggestionTextIt,
				collatorWrapper.getCollator());

		writer.element("ul");
		for (FacetTermSuggestionStream suggestionStream : facetSuggestionStreams) {
			writer.element("li");
			writer.attributes("class", "facet");
			// writer.appendAttribute("id",
			// hit.getFacet().getCssId()+"AutocompleteHead");
			writer.element("span");
			writer.attributes("class", "informal");
			writer.element("span");
			writer.attributes("style", "font-weight:bold;");
			writer.write(suggestionStream.getFacet().getName());
			writer.end();
			writer.write(" (" + suggestionStream.size() + " terms)");
			writer.end();
			writer.end();

			int suggestionCount = suggestionStream.size();
			suggestionCount = suggestionCount > suggestionsPerFacet ? suggestionsPerFacet
					: suggestionCount;
			int k = 0;
			while (suggestionStream.incrementTermSuggestion()
					&& k < suggestionCount) {

				writer.element("li");
				// Set attributes which will be sent in the request to the
				// action event triggered when clicking on a suggestion. This
				// information is used to identify what has been clicked on .
				writer.attributes("class", "term", TERM_ID,
						suggestionStream.getTermId(), FACET_ID,
						suggestionStream.getFacet().getId());

				String suggestionName = suggestionStream.getTermName();
				int prevIndex = 0;
				suggestionTextIt.setText(suggestionName);
				search.setPattern(query);
				search.setTarget(suggestionTextIt);
				int currIndex = search.next();
				// while (currIndex >= 0) {
				if (currIndex >= 0) {
					writer.write(suggestionName.substring(prevIndex, currIndex));
					writer.element("b");
					writer.element("u");
					writer.write(suggestionName.substring(currIndex, currIndex
							+ query.length()));
					writer.end();
					writer.end();
					prevIndex = currIndex + query.length();
					// currIndex = search.next();
					// }
				}
				writer.write(suggestionName.substring(prevIndex));

				if (suggestionStream.getTermSynonyms() != null
						&& !suggestionStream.getTermSynonyms().equals("")) {
					writer.element("span");
					writer.attributes("class", "informal");
					// writer.beginEmpty("br");
					writer.element("span");
					writer.attributes("class", "description");
					writer.write(" (Synonyms: "
							+ suggestionStream.getTermSynonyms() + ")");
					writer.end();
					writer.end();
				}
				writer.end();
				++k;
			}
		}

		if (suggestions.size() == 0) {
			writer.element("li");
			writer.attributes("class", "emptyResult");

			writer.element("span");
			writer.attributes("class", "informal");
			writer.writeRaw("No suggestions found for \"" + query
					+ "\". Press ESC to hide this message.");
			writer.end();
			writer.end();
		}
		writer.end();
	}

}
