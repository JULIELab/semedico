package de.julielab.semedico.components;

import java.util.Collection;
import java.util.Map;

import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.LazyDisplayGroup;
import de.julielab.semedico.pages.ResultList;

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
	private ITermService termService;

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

	/**
	 * Uses {@link UIFacet#getCurrentPath()} to add all ancestors of the terms
	 * in <code>terms</code> to the current paths of the corresponding facet
	 * configurations. If a term in <code>terms</code> has no parent term, i.e.
	 * it is a root, the term itself is added to the current path of its facet
	 * configuration.
	 * <p>
	 * The {@link FacetBox} component associated with a particular facet
	 * configuration will then show the facet category drilled down to children
	 * of the last element of a path. The path itself is reflected on the
	 * {@link QueryPanel} component.
	 * </p>
	 * <p>
	 * If there are several terms of the same facet category in
	 * <code>terms</code>, the first term encountered will determine the set
	 * path. Following terms will not be reflected. (This is my understanding at
	 * least - EF).
	 * </p>
	 * <p>
	 * The facet configurations in <code>facetConfigurations</code> should be
	 * resetted before calling this method.
	 * </p>
	 * 
	 * @param terms
	 *            The term to which the different facet categories are currently
	 *            drilled down to.
	 * @param facetConfigurations
	 *            The facet configurations to set the current path to the
	 *            associated term in <code>terms</code.>
	 */
	protected void drillDownFacetConfigurations(Collection<IFacetTerm> terms, Map<Facet, UIFacet> facetConfigurations)
	{

		for (IFacetTerm searchTerm : terms)
		{
			if (!searchTerm.hasChildren())
				continue;

			UIFacet configuration = facetConfigurations.get(searchTerm.getFirstFacet());

			if (configuration.isInHierarchicViewMode() && configuration.getCurrentPathLength() == 0)
			{
				configuration.setCurrentPath(termService.getShortestPathFromAnyRoot(searchTerm).copyPath());
			}
		}
	}

	@Log
	public void onDownloadPmids() {
		// try {
		// searchResult = searchService.search(originalQueryString,
		// searchState.getQueryTerms().size(),
		// searchState.getSortCriterium(),
		// searchState.isReviewsFiltered());
		// Collection<String> pmids = searchService.getPmidsForSearch(
		// originalQueryString, searchState);
		// for (String pmid : pmids)
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	// we load the base styles at last so there are not overwritten (e.g.
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
