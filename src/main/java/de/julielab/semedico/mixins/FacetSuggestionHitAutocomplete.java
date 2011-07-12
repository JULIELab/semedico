package de.julielab.semedico.mixins;

import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.corelib.mixins.Autocomplete;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.json.JSONString;
import org.apache.tapestry5.services.Request;

import de.julielab.semedico.core.FacetSuggestionHit;
import de.julielab.semedico.core.SuggestionHit;

public class FacetSuggestionHitAutocomplete extends Autocomplete {
	
	private static final String QUERY = "query";

	private static final String ID = "id";

	private final static int MAX_HIT_COUNT = 50;

	private static final String PARAM_NAME = "t:input";
	private final String URL_SCRIPT= "$T(\"%s\").suggestURL = \"%s\";";
	
	private static final String EVENT_NAME = "action";
	
	@InjectContainer
	private Field field;
	
	@Inject @Path("suggestions.js")
	private Asset suggestionsJS;

	@Inject
    private Request request;

	@Inject
    private ComponentResources resources;

	@Environmental
	private RenderSupport renderSupport;

	@Parameter
	private String clickedTermId;
	
	@Parameter
	private String termText;
	
	@Override
	protected void configure(JSONObject config) {
		config.put("afterUpdateElement","selectSuggestion");
		config.put("minChars","2");
	}

	@OnEvent(value="action")
	void readParameters(){
		clickedTermId = request.getParameter(ID);
		termText = request.getParameter(QUERY);
	}
	
	@AfterRender
	void addJavaScript(MarkupWriter markupWriter){
		renderSupport.addScriptLink(suggestionsJS);
		Link link = resources.createEventLink(EVENT_NAME);
		renderSupport.addScript(URL_SCRIPT, field.getClientId(), link.toAbsoluteURI());
	}
	
	@Override
	protected void generateResponseMarkup(MarkupWriter writer, List hits) {
		String query = request.getParameter(PARAM_NAME);
		
		int i = 0, j = 0;
		int suggestionsPerFacet = (int)Math.ceil((double)MAX_HIT_COUNT / (double)hits.size());
		int overflows = 0;
		int underflowSum = 0;
		
		List<FacetSuggestionHit> facetSuggestionHits = hits;
		for( FacetSuggestionHit hit : facetSuggestionHits){
			int size = hit.getCompleteSize(); 
			if( size < suggestionsPerFacet )
				underflowSum += size - suggestionsPerFacet;
			else if( size > suggestionsPerFacet )
				overflows++;
		}
		
		if( underflowSum > 0 && overflows > 0 )
			suggestionsPerFacet = suggestionsPerFacet + (int)Math.ceil((double)underflowSum / (double)overflows);
		
		writer.element("ul");
		for( FacetSuggestionHit hit : facetSuggestionHits){
			writer.element("li");
			writer.attributes("class", "facet");
		//	writer.appendAttribute("id", hit.getFacet().getCssId()+"AutocompleteHead");
			writer.element("span");
			writer.attributes("class", "informal");
			writer.element("span");
			writer.attributes("style", "font-weight:bold;");
			writer.write(hit.getFacet().getName());
			writer.end();
			writer.write(" ("+hit.getCompleteSize()+" terms)");
			writer.end();
			writer.end();
			j= 0;
			int suggestionCount = hit.getCompleteSize();
			suggestionCount = suggestionCount > suggestionsPerFacet ? suggestionsPerFacet : suggestionCount;
			for( int k = 0; k < suggestionCount; k++ ){
				SuggestionHit suggestion = hit.getTermHits().get(k);
				
				writer.element("li");
				writer.attributes("class", "term", ID, suggestion.getIdentifier());

				String suggestionName = suggestion.getName();
				int prevIndex = 0;					
				int currIndex = suggestionName.toLowerCase().indexOf(query.toLowerCase());
				while ( currIndex >= 0 ) {
					writer.write(suggestionName.substring(prevIndex, currIndex));					
					writer.element("b");
					writer.element("u");
					writer.write(suggestionName.substring(currIndex, currIndex + query.length()));
					writer.end();	
					writer.end();
					prevIndex = currIndex + query.length();
					currIndex = suggestionName.toLowerCase().indexOf(query.toLowerCase(), prevIndex);
				}
				writer.write(suggestionName.substring(prevIndex));
				
				if( suggestion.getShortDescription() != null &&  !suggestion.getShortDescription().equals("") ){
					writer.element("span");
					writer.attributes("class", "informal");
					//writer.beginEmpty("br");
					writer.element("span");
					writer.attributes("class", "description");
					writer.write(" (Synonyms: "+suggestion.getShortDescription()+")");
					writer.end();
					writer.end();
				}
				writer.end();
				j++;
			}
			i++;
		}
		
		if( hits.size() == 0 ){
			writer.element("li");
			writer.attributes("class", "emptyResult");

			writer.element("span");
			writer.attributes("class", "informal");
			writer.writeRaw("No suggestions found for \""+query+"\". Press ESC to hide this message.");
			writer.end();
			writer.end();
		}
		writer.end();
	}

}
