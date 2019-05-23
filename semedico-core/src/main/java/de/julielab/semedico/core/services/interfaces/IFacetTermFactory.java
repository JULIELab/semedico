package de.julielab.semedico.core.services.interfaces;

import org.apache.tapestry5.json.JSONArray;

import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.facetterms.SyncFacetTerm;
import de.julielab.semedico.core.services.AsyncCacheLoader;

public interface IFacetTermFactory {

	/**
	 * Creates an almost empty term that only knows about its ID. Such terms are intended to be used as proxy objects
	 * that can be used immediately in data structures while their actual data is requested from the term database.
	 * Thus, this method should only be used if the term's data is loaded soon and written into the instance via
	 * {@link #updateProxyTermFromJson(IFacetTerm, String, JSONArray)}.
	 * <p>
	 * For concurrent loading - e.g. as done by {@link AsyncCacheLoader} - a {@link SyncFacetTerm} should be requested.
	 * </p>
	 * 
	 * @param id
	 * @param termClass
	 * @return
	 */
	IFacetTerm createDatabaseProxyTerm(String id, Class<? extends SyncFacetTerm> termClass);

	/**
	 * This method just calls {@link #createFacetTermFromJson(String, JSONArray, Class)} with the default class
	 * <tt>FacetTerm.class</tt>.
	 * 
	 * @param jsonString
	 * @param termLabels
	 * @return
	 */
	IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels);

	/**
	 * Creates a complete Semedico term from the string in JSON format given by <tt>jsonString</tt>. The JSON input is
	 * expected to include all data necessary to create a Semedico term. The <tt>termLabels</tt> must not be empty since
	 * it should at least contain the <tt>TERM</tt> label. However, this label would be ignored. An interesting label
	 * would be {@link TermLabels.GeneralLabel#EVENT_TERM}, for example, since this identifies a term to be an event
	 * trigger.
	 * 
	 * @param jsonString
	 * @param termLabels
	 * @return
	 */
	IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels, Class<? extends IFacetTerm> termClass);

	void updateProxyTermFromJson(IFacetTerm proxy, String termRow, JSONArray termLabels);

}
