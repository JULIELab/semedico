package de.julielab.semedico.components;

import java.util.Map;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.Path;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.state.SemedicoSessionState;

public class Term {

	@SessionState
	private SemedicoSessionState sessionState;

	@Inject
	private IConceptService termService;

	@Property
	private Concept pathItem;

	@Property
	private int pathItemIndex;

	@Parameter(required = true)
	@Property
	private IConcept term;

	@Parameter(required = true)
	@Property
	private Facet facet;

	@Parameter(value = "false")
	private boolean showPath;

	@Parameter(value = "-1")
	private int conceptNodeId;
	private AbstractUserInterfaceState uiState;
	void setupRender() {
		uiState =  sessionState.getDocumentRetrievalUiState();
	}
	
	public boolean showPathForTerm() {
		if (!showPath)
			return false;
		Map<Facet, UIFacet> facetConfigurations = uiState.getUIFacets();
		UIFacet uiFacet = facetConfigurations.get(facet);
		if (facet != null && getRootPath().length() > 0) {
			return uiFacet.isHierarchic();
		} else {
			return false;
		}
	}

	public Object[] getDrillUpContext() {
		return new Object[] { conceptNodeId, pathItemIndex };
	}

	public String getMappedTermClass() {
		String cssId = facet.getCssId();
		String termClass = cssId + " filterBox primaryFacetStyle";
		return termClass;
	}

	public boolean isDrillUpDisabled() {
		return conceptNodeId == -1;
	}

	/**
	 * Used by the template to get the path from a facet root to a particular query term. The elements are supplied with
	 * a link which causes a drill-up event. Thus, the last element of the path, the query term itself, is not returned.
	 * 
	 * @return The facet root path of the current term in exclusion of the term itself.
	 */
	public IPath getRootPath() {
		if (term.getConceptType() == ConceptType.KEYWORD)
			return Path.EMPTY_PATH;

		IPath rootPath = null;
		UIFacet uiFacet = uiState.getUIFacets().get(facet);
		IPath facetPath = Path.EMPTY_PATH;
		if (null != uiFacet)
			facetPath = uiFacet.getCurrentPath();
		if (null != facetPath.getLastNode() && facetPath.getLastNode().equals(term))
			rootPath = facetPath;

		if (null == rootPath)
			rootPath = termService.getShortestPathFromAnyRoot(term);

		if (rootPath.isEmpty())
			return rootPath;
		// Don't return the very last element as all elements returned here get
		// a drillUp-ActionLink. The the name of the term itself is rendered
		// separately.
		return rootPath.subPath(0, rootPath.length() - 1);
	}

}
