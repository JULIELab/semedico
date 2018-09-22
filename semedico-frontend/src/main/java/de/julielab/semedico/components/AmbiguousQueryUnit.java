package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;

@Import(stylesheet = { "context:css/semedico-dialogs.css",
		"context:js/jquery-ui/jquery-ui.min.css" }, library = {
		"ambiguousQueryUnit.js", "context:js/jquery.min.js",
		"context:js/jquery-ui/jquery-ui.min.js", "context:js/jquery.ba-outside-events.js" })
public class AmbiguousQueryUnit {

	@Parameter(required = true)
	@Property
	private ParseTree semedicoQuery;

	@Parameter(required = true)
	@Property
	private int conceptNodeId;

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
	private List<? extends IConcept> terms;

	@Property
	private IConcept term;

	@Property
	private Map<Facet, List<IConcept>> termMap;

	@Property
	private Facet facet;

	@Property
	private long disambiguationNodeId;

	/**
	 * The {@link #conceptNodeId} parameter is only valid while the whole page
	 * renders. However, this component uses Ajax requests to render the
	 * contents of the disambiguation dialog and an event to trigger the actual
	 * disambiguation. For these event requests, the outside page does not
	 * render again and thus does not provide the {@link #conceptNodeId}
	 * parameter again. In order to always have access to the correct value, we
	 * store the value of the parameter in this helper property. At all places
	 * where the concept node ID is required within an Ajax render request, we
	 * have to use the helper.
	 */
	@Property
	private int conceptNodeIdHelper;

	public void setupRender() {
		allocatedClientId = javaScriptSupport
				.allocateClientId(componentResources);
		conceptNodeIdHelper = conceptNodeId;
	}

	void onShowDisambiguationDialog(int selectedConceptNodeId, String allocatedClientId) {
		conceptNodeIdHelper = selectedConceptNodeId;
		this.allocatedClientId = allocatedClientId;
		TextNode node = (TextNode) semedicoQuery.getNode(selectedConceptNodeId);
		terms = node.getConcepts();
		termMap = getTermMapOrderedByFacet(terms);
		disambiguationNodeId = selectedConceptNodeId;
		showDisambiguationDialog = true;
		if (request.isXHR())
			ajaxResponseRenderer.addRender(disambiguationDialogZone);
	}

	void onSelectDisambiguationTerm(String selectedTermId,
			int selectedConceptNodeId) {
		/**
		 * todo: handler for selecting the term
		 */
		// conceptNodeId = selectedConceptNodeId;
		TextNode node = (TextNode) semedicoQuery.getNode(selectedConceptNodeId);
		List<? extends IConcept> nodeTerms = node.getConcepts();
		List<IConcept> selectedTerm = new ArrayList<>();
		for (IConcept term : nodeTerms) {
			if (term.getId().equals(selectedTermId)) {
				selectedTerm.add(term);
			}
		}
		node.setConcepts(selectedTerm);
	}

	public String getUserQueryTerm() {
		return semedicoQuery.getNode(conceptNodeIdHelper).getText();
	}

	public Map<Facet, List<IConcept>> getTermMapOrderedByFacet(
			List<? extends IConcept> terms) {
		if (termMap == null) {
			termMap = new HashMap<Facet, List<IConcept>>();
		}
		for (IConcept term : terms) {
			List<IConcept> termsForFacet = termMap.get(term.getFirstFacet());
			if (termsForFacet == null) {
				termsForFacet = new ArrayList<>();
				termMap.put(term.getFirstFacet(), termsForFacet);
			}
			termsForFacet.add(term);
		}
		return termMap;
	}
	
	public void onOpenDisambiguationDialog() {
		javaScriptSupport.addScript("openDisambiguationDialog('%s')",
				getDisambiguationDialogId());
	}

	public String getDisambiguationDialogId() {
		return allocatedClientId + "_disambiguationDialog";
	}

	public String getDisambiguationDialogZoneId() {
		return allocatedClientId + "_disambiguationDialogZone";
	}

	public void afterRender() {
		javaScriptSupport.addScript("setupDisambiguationDialog('%s', '%s', '%s')",
				getDisambiguationDialogId(), getUserQueryTerm(), allocatedClientId);
	}

}
