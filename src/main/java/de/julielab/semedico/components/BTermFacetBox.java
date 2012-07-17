package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import de.julielab.semedico.core.SearchState;


public class BTermFacetBox extends AbstractFacetBox {

	@SessionState
	private SearchState searchState;
	
	@Inject
	private Logger logger;
	
	public void onTermSelect(String termIndexAndFacetId) {
		super.onTermSelect(termIndexAndFacetId);
		logger.debug("Selected B-Term: '{}'", searchState.getSelectedTerm().getName());
	}

	/* (non-Javadoc)
	 * @see de.julielab.semedico.components.AbstractFacetBox#refreshFacetHit()
	 */
	@Override
	protected void refreshFacetHit() {
		// TODO Auto-generated method stub
		
	}
}
