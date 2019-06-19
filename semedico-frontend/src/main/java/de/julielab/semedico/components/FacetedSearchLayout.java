package de.julielab.semedico.components;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.util.LazyDisplayGroup;
import de.julielab.semedico.pages.ResultList;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

/**
 * Central starting point of the whole of Semedico. While the index page may be
 * the entry point, all searching logic, facet configuration, facet expanding
 * etc. has its origin in this page.
 * 
 * @author faessler
 * 
 */
@Import(
	stylesheet =
	{
		"context:js/jquery-ui/jquery-ui.min.css",
		"context:css/facets.css",
		"context:css/semedico-bootstrap.css",
		"context:css/semedico-facetedsearchlayout.css"
	},
	library =
	{
		"context:js/jquery.min.js"
	})

public class FacetedSearchLayout extends Search
{
	@InjectPage
	private ResultList resultList;

	@Inject
	private IFacetService facetService;

	@Inject
	private IConceptService termService;

	@Inject
	private Logger logger;

	// Just taken from the embedding page and passed further to the Tabs
	// component. This
	// way, the Tabs component "knows" which user interface to render, e.g.
	// search interface or B-Term-Viewing interface.
	@Parameter(required = true)
	@Property
	private UserInterfaceState uiState;

	@Parameter(required = true)
	@Property
	private ParseTree query;

	@Persist
	@Property
	private int selectedFacetType;

	@Property
	@Persist
	private LabelStore currentFacetHit;

	@Persist
	@Property
	private LazyDisplayGroup<HighlightedSemedicoDocument> displayGroup;

	@Persist
	@Property
	private String originalQueryString;

	@Property
	@Parameter
	int indexOfFirstArticle;
	@Property
	@Parameter
	int indexOfLastArticle;
	@Property
	@Parameter
	long elapsedTime;
	@Property
	@Parameter
	long totalHits;

	public Object onTermSelect() {
		return performSubSearch();
	}

	public Object onActionFromFilterPanel() {
		return performSubSearch();
	}

	public Object onRemoveFilterConcept() {
		return performSubSearch();
	}

	@Log
	public void onDownloadPmids() {
	}

	// we load the base styles at last so they're are not overwritten (e.g.
	// semedico-base.css included styles for jquery-ui which shouldn't be
	// overwritten by the jquery-ui CSS)
	@Import(stylesheet = "context:css/semedico-base.css")
	@CleanupRender
	public void cleanUpRender() {
		sessionState.getDocumentRetrievalSearchState().setNewSearch(false);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	public String getGoogleFontStyle() {
		return "https://fonts.googleapis.com/css?family=Open+Sans:400,300&subset=latin,greek,greek-ext,vietnamese,cyrillic-ext,cyrillic,latin-ext";
	}

}
