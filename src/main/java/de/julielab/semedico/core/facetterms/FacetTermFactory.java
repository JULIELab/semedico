package de.julielab.semedico.core.facetterms;

import org.apache.tapestry5.json.JSONArray;

import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.ITermCreator;

public class FacetTermFactory implements IFacetTermFactory {

	private ITermCreator termCreator;

	public FacetTermFactory(ITermCreator termCreator) {
		this.termCreator = termCreator;

	}

	@Override
	public IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels) {
		return termCreator.createFacetTermFromJson(jsonString, termLabels);
	}

	@Override
	public IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels,
			Class<? extends IFacetTerm> termClass) {
		return termCreator.createFacetTermFromJson(jsonString, termLabels, termClass);
	}

	@Override
	public IFacetTerm createDatabaseProxyTerm(String id, Class<? extends SyncFacetTerm> termClass) {
		return termCreator.createFacetTerm(id, termClass);
	}

	@Override
	public void updateProxyTermFromJson(IFacetTerm proxy, String termRow, JSONArray termLabels) {
		termCreator.updateFacetTermFromJson(proxy, termRow, termLabels);
	}

	@Override
	public KeywordTerm createKeywordTerm(String id, String name) {
		return termCreator.createKeywordTerm(id, name);
	}

}
