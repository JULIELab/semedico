package de.julielab.semedico.components;

import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.state.SemedicoSessionState;

/**
 * 
 * @author faessler
 * @deprecated We no longer filter on concepts but add them to the query, as we
 *             did in the past
 */
@Deprecated
@Import(stylesheet = "context:css/facetconceptfilter.css")
public class FacetConceptFilterPanel {

	@SessionState(create = false)
	protected SemedicoSessionState sessionState;

	@Property
	private Concept filterConceptItem;

	@Property
	@Persist
	private SearchState searchState;

	public boolean setupRender() {
		if (null != sessionState) {
			this.searchState = sessionState.getDocumentRetrievalSearchState();
			if (searchState.getSelectedFacetConcepts() != null && !searchState.getSelectedFacetConcepts().isEmpty())
				// don't render the component if there are no filter concepts
				return true;
		}
		return false;
	}

	public void onRemoveFilterConcept(String conceptId) {
		List<Concept> selectedFacetConcepts = searchState.getSelectedFacetConcepts();
		// if (selectedFacetConcepts != null &&
		// !selectedFacetConcepts.isEmpty()) {
		Iterator<Concept> it = selectedFacetConcepts.iterator();
		while (it.hasNext()) {
			Concept concept = (Concept) it.next();
			if (concept.getId().equals(conceptId))
				it.remove();
		}
		// }
	}
}
