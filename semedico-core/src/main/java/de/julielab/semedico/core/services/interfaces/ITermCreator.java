package de.julielab.semedico.core.services.interfaces;

import org.apache.tapestry5.json.JSONArray;

import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.facetterms.KeywordTerm;

public interface ITermCreator {
	IFacetTerm createFacetTerm(String id);

	IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels);

	IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels, Class<? extends IFacetTerm> termClass);

	void updateFacetTermFromJson(IFacetTerm proxy, String jsonString, JSONArray termLabels);

	IFacetTerm createFacetTerm(String id, Class<? extends IFacetTerm> termClass);

	KeywordTerm createKeywordTerm(String id, String name);

}
