package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.state.SemedicoSessionState;

@Import(stylesheet = "context:css/termlist.css")
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

	@SessionState
	private SemedicoSessionState sessionState;

	@Inject
	private ITermService termService;

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

	public String getTermIndexAndFacetId() {
		return labelItem.getId() + "_" + uiFacet.getId();
	}

	/**
	 * Sets {@link #selectedConcept} to the selected concept and, if the facet
	 * is hierarchical, drills down the hierarchy to the selected node to then
	 * display its children. The <tt>selectedConcept</tt> property may be used
	 * by subclasses.
	 * 
	 * @param termIndexAndFacetId
	 */
	public void onTermSelect(String termIndexAndFacetId) {
		String[] termIdxFacetId = termIndexAndFacetId.split("_");
//		int termIndex = Integer.parseInt(termIdxFacetId[0]);
		String termId = termIdxFacetId[0];
		String facetId = termIdxFacetId[1];
		
		SearchState searchState = sessionState.getDocumentRetrievalSearchState();
		UserInterfaceState uiState = sessionState.getDocumentRetrievalUiState();
//		UIFacet uiFacet = uiState.getUIFacet(facetId);
//		System.out.println("HIER");
//		System.out.println(facetId);
//		System.out.println(uiState.getUIFacets());
		
//		if (!(index < uiFacet.getLabelDisplayGroup().getNumberOfDisplayedObjects())) {
//			throw new IllegalStateException(
//					"Term with index " + index + " does not exist in this FacetBox component (there are only "
//							+ uiFacet.getLabelDisplayGroup().getNumberOfDisplayedObjects() + "). FacetConfiguration: "
//							+ uiFacet);
//		}

//		Label label = uiFacet.getLabelDisplayGroup().getDisplayedObjects().get(index);
		IConcept selectedConcept;
		// sessionState.getDocumentRetrievalSearchState().setSelectedTerm(label);
		if (!termService.isStringTermID(termId)) {
			selectedConcept = termService.getTerm(termId);
		} else {
			selectedConcept = termService.getTermObjectForStringTerm(termId, facetId);
		}
//		if (uiFacet.isInHierarchicViewMode()) {
//			Concept selectedTerm = (Concept) termService.getTerm(termId);
//			// if (label.hasChildHitsInFacet(uiFacet)) {
//			// uiFacet.appendNodeToCurrentPath(selectedTerm);
//			// }
//			if (selectedTerm.hasChildrenInFacet(uiFacet.getId())) {
//				uiFacet.appendNodeToCurrentPath(selectedTerm);
//			}
//		}
		searchState.addSelectedFacetConcept((Concept) selectedConcept);
	}
}
