package de.julielab.semedico.components;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.ISearchService;

/**
 * This component is responsible for rendering the facet group tabs (BioMed,
 * Immunology, ...) and creating the FacetBox components for the currently
 * selected facet group.
 * <p>
 * The facet group tabs are rendered by a loop in the template which iterates
 * over all facet groups. The facet groups are given in the searchConfiguration
 * which determines the state of a particular search (query terms, facet
 * order...).<br>
 * A facet group is identified by its index (0, 1, 2, ...) which is stored in
 * the private attribute {@link #selectedFacetGroupIndex}.
 * </p>
 * 
 * @author faessler
 * 
 */
public class Tabs {
	// The name the event should have which is triggered when selecting a facet
	// group.
	private static final String EVENT_NAME = "tabselect";
	// The HTML element ID of the facet bar in the template.
	private static final String FACET_BAR_ID = "facetBar";
	// The JavaScript variable declaration for the JavaScript Tabs object.
	// This JavaScript object adds onClick event listeners to the HTML elements
	// representing the facet groups and determines the correct CSS class for
	// rendering a facet group tab active (foreground) or inactive (background).
	private static final String INIT_JS = "var %s = new Tabs(\"%s\", \"%s\");";
	// When a facet group is select, an Ajax request is sent. This request
	// includes the facet group which has been selected in the form of
	// the group's index, given as a parameter. E.g.: selectedTab="0" for
	// the first facet group.
	private static final String SELECTED_TAB_PARAMETER = "selectedTab";

	@SessionState
	private SearchState searchState;

	@Parameter
	@Property
	private UserInterfaceState uiState;

	@Inject
	private ISearchService searchService;
	
	@Inject
	@Path("tabs.js")
	private Asset tabsJS;

	@SuppressWarnings("unused")
	@Property
	@Parameter
	private int facet_nr;

	@SuppressWarnings("unused")
	@Property
	private int facetGroupLoopIndex;

	@SuppressWarnings("unused")
	@Property
	private FacetGroup<UIFacet> facetGroupLoopItem;

	/**
	 * Determines whether the frequency count of terms is shown. When set to
	 * "true", the count is shown in parenthesis after the term's name.
	 */
	@SuppressWarnings("unused")
	@Property
	@Parameter("true")
	private boolean showLabelCount;

	@Property
	private UIFacet facetConfigurationLoopItem;
	
	@Inject
	private ComponentResources resources;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private Request request;

	@Inject
	private Logger logger;

	/**
	 * This method is used by the loop in the template which creates the
	 * required FacetBox components.
	 * <p>
	 * facet_nr is the index variable used by this loop. In every iteration the
	 * facet_nr-th facet configuration is required for construction of the
	 * corresponding FacetBox.
	 * </p>
	 * 
	 * @param facet_nr
	 *            Passed by tapestry as loop iterator index over facet groups.
	 * @return The facet configuration for the facet with position
	 *         <code>facet_nr</code> in the currently selected facet group.
	 */
	public UIFacet getFacetConfiguration(int facet_nr) {
		FacetGroup<UIFacet> currentFacetGroup = uiState
				.getSelectedFacetGroup();
		if (facet_nr < currentFacetGroup.size()) {
			return currentFacetGroup.get(facet_nr);
		}
		return null;
	}

	public UIFacet getFacetConfiguration1() {
		return getFacetConfiguration(0);
	}

	public UIFacet getFacetConfiguration2() {
		return getFacetConfiguration(1);
	}

	public UIFacet getFacetConfiguration3() {
		return getFacetConfiguration(2);
	}

	public UIFacet getFacetConfiguration4() {
		return getFacetConfiguration(3);
	}

	public UIFacet getFacetConfiguration5() {
		return getFacetConfiguration(4);
	}

	public UIFacet getFacetConfiguration6() {
		return getFacetConfiguration(5);
	}

	public UIFacet getFacetConfiguration7() {
		return getFacetConfiguration(6);
	}

	public UIFacet getFacetConfiguration8() {
		return getFacetConfiguration(7);
	}

	public UIFacet getFacetConfiguration9() {
		return getFacetConfiguration(8);
	}

	public UIFacet getFacetConfiguration10() {
		return getFacetConfiguration(9);
	}

	public UIFacet getFacetConfiguration11() {
		return getFacetConfiguration(10);
	}

	public UIFacet getFacetConfiguration12() {
		return getFacetConfiguration(11);
	}

	public UIFacet getFacetConfiguration13() {
		return getFacetConfiguration(12);
	}

	public UIFacet getFacetConfiguration14() {
		return getFacetConfiguration(13);
	}

	public UIFacet getFacetConfiguration15() {
		return getFacetConfiguration(14);
	}

	public Object onTabSelect() {
		String selectedTab = request.getParameter(SELECTED_TAB_PARAMETER);
		// The returned parameter value is the index of the selected
		// facet group. Thus we only have to parse this integer
		// and we're ready to go.
		uiState.setSelectedFacetGroupIndex(Integer.parseInt(selectedTab));

		
		// This happens when the user just opens a URL to the main page without
		// giving a query. Don't get any label counts then.
		if (searchState.getUserQueryString() == null) {
			logger.debug("User query string is null.");
			return this;
		}

		searchService.doTabSelectSearch(searchState.getSolrQueryString());
//		logger.debug(
//				"Creating labels to display for selected facet group \"{}\".",
//				uiState.getSelectedFacetGroup().getName());
//		uiState.createLabelsForSelectedFacetGroup();
//		logger.debug("Preparing child terms of displayed terms.");
//		if (!uiState.prepareLabelsForSelectedFacetGroup())
//			logger.debug("No children to prepare.");
		// Re-render the component with the new facet group selected.
		return this;
	}

	@AfterRender
	void addJavaScript(MarkupWriter markupWriter) {
		javaScriptSupport.importJavaScriptLibrary(tabsJS);
		Link link = resources.createEventLink(EVENT_NAME);

		int selectedFacetGroupIndex = uiState.getSelectedFacetGroupIndex();
		javaScriptSupport.addScript(INIT_JS, FACET_BAR_ID,
				selectedFacetGroupIndex, link.toAbsoluteURI());
	}
}
