package de.julielab.semedico.components;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.*;

@Import(stylesheet = { "context:css/semedico-dialogs.css", "context:css/disambiguationDialog.css" }, library = {
		"disambiguationDialog.js", "context:js/jquery.ba-outside-events.js" })
public class DisambiguationDialog {

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources componentResources;

	@Inject
	private Request request;

	@InjectComponent
	private Zone disambiguationDialogZone;

	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;

	@Persist
	@Property
	private String allocatedClientId;

	@Property
	private boolean showDisambiguationDialog;

	@Property
	private List<IConcept> terms;

	@Property
	private IConcept term;

	@Property
	private Map<Facet, List<IConcept>> termMap;

	@Property
	private Facet facet;

	@Inject
	private IConceptService termService;

	public void setupRender() {
		allocatedClientId = javaScriptSupport.allocateClientId(componentResources);
	}

	void onShowDisambiguationDialog() {
		String conceptIdsCSV = request.getParameter("q");
		terms = new ArrayList<>();
		List<String> conceptIds = Arrays.asList(conceptIdsCSV.split(","));
		for (String conceptId : conceptIds)
			terms.add(termService.getTerm(conceptId));
		termMap = getTermMapOrderedByFacet(terms);
		showDisambiguationDialog = true;
		if (request.isXHR())
			ajaxResponseRenderer.addRender(disambiguationDialogZone);
	}

	public Map<Facet, List<IConcept>> getTermMapOrderedByFacet(List<? extends IConcept> terms) {
		if (termMap == null) {
			termMap = new TreeMap<Facet, List<IConcept>>(new Comparator<Facet>() {
				@Override
				public int compare(Facet o1, Facet o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		for (IConcept term : terms) {
			List<IConcept> termsForFacet = termMap.get(term.getFirstFacet());
			if (termsForFacet == null) {
				termsForFacet = new ArrayList<>();
				termMap.put(term.getFirstFacet(), termsForFacet);
			}
			termsForFacet.add(term);
		}
		for (List<IConcept> conceptList : termMap.values())
			Collections.sort(conceptList, new Comparator<IConcept>() {

				@Override
				public int compare(IConcept o1, IConcept o2) {
					return o1.getDisplayName().compareTo(o2.getDisplayName());
				}

			});
		return termMap;
	}

	public String getDisambiguationDialogId() {
		return allocatedClientId;
	}

	public String getDisambiguationDialogZoneId() {
		return allocatedClientId + "_disambiguationDialogZone";
	}

	public Link getShowDialogLink() {
		return componentResources.createEventLink("showDisambiguationDialog");
	}

	public void afterRender() {
		JSONObject jsParameters = new JSONObject();
		jsParameters.put("dialogElementId", allocatedClientId);
		jsParameters.put("dialogZoneId", getDisambiguationDialogZoneId());
		javaScriptSupport.addInitializerCall("setupDisambiguation", jsParameters);
//		javaScriptSupport.require("semedico/disambiguationDialog").priority(InitializationPriority.LATE).invoke("init").with(jsParameters);
	}

	public JSONArray getTermSynonyms() {
		JSONArray synonyms = new JSONArray();
		for (String synonym : term.getSynonyms())
			synonyms.put(synonym);
		return synonyms;
	}
}
