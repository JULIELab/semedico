package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import de.julielab.semedico.base.FacetDefinitions;
import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.services.interfaces.IFacetService;

public class BTermFacetBox extends AbstractFacetBox {

	@SessionState
	private SearchState searchState;

	@SessionState
	private BTermUserInterfaceState uiState;
	
	@Inject
	private IFacetService facetService;

	@Inject
	private Logger logger;

	public void onTermSelect(String termIndexAndFacetId) {
		super.onTermSelect(termIndexAndFacetId);
		logger.debug("Selected B-Term: '{}'", searchState.getSelectedTerm()
				.getName());
		refreshFacetHit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.components.AbstractFacetBox#refreshFacetHit()
	 */
	@Override
	protected void refreshFacetHit() {
		uiState.createLabelsForFacet(facetConfiguration);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.components.AbstractFacetBox#getTermCSSClasses()
	 */
	@Override
	public String getTermCSSClasses() {
		String cssClasses = "";
		if (!(labelItem instanceof TermLabel))
			return cssClasses;
		String cssId = ((TermLabel) labelItem).getTerm().getFirstFacet()
				.getCssId();

		// For the special B-Term-Facet we also want the terms to have their
		// original facet color. But it looks better when the primary color is
		// used, opposed to when shown in their facet's facet boxes, then only
		// the heading is of the primary color and the term's background is of
		// secondary color. Thus, here we distinguish between the B-Term-Facet
		// box and other facet boxes.
		if (facetService.isBTermFacet(facetConfiguration.getFacet()))
			cssClasses = cssId + " " + FacetDefinitions.PRIMARY_STYLE;
		else
			cssClasses = cssId + " " + FacetDefinitions.SECONDARY_STYLE;
		return cssClasses;
	}
}
