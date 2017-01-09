package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.json.JSONArray;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.facets.UIFacet;

@Import(stylesheet="context:css/termlist.css")
public class TermList {
	@Parameter
	@Property
	private UIFacet uiFacet;
	
	@Parameter
	private LabelStore labelStore;
	
	@Property
	private Label labelItem;
	
	@Property
	private int labelIndex;
	
	void onTermSelect() {
		System.out.println("TERM SELECTED!");
	}
	
	public JSONArray getLabelSynonyms() {
		JSONArray synonyms = new JSONArray();
		if (labelItem instanceof TermLabel) {
			Concept term = ((TermLabel) labelItem).getTerm();
			for (String synonym : term.getSynonyms())
				synonyms.put(synonym);
		}
		return synonyms;
	}
	
	public JSONArray getLabelDescriptions() {
		JSONArray descriptions = new JSONArray();
		if (labelItem instanceof TermLabel) {
			Concept term = ((TermLabel) labelItem).getTerm();
			for (String description : term.getDescriptions())
				descriptions.put(description);
		}
		return descriptions;
	}
	
	public String getLabelFacetName() {
		if (labelItem instanceof TermLabel) {
			Concept term = ((TermLabel) labelItem).getTerm();
			String facetname = term.getFirstFacet().getName();
			if (facetname.length() > 30 && term.getFirstFacet().getShortName() != null)
				facetname = term.getFirstFacet().getShortName();
			return facetname;
		}
		return "";
	}
}
