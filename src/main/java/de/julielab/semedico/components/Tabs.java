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

import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.FacetService;

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

	@Property
	@SessionState
	private SearchSessionState searchSessionState;

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
	private FacetGroup<FacetConfiguration> facetGroupLoopItem;

	/**
	 * Determines whether the frequency count of terms is shown. When set to
	 * "true", the count is shown in parenthesis after the term's name.
	 */
	@SuppressWarnings("unused")
	@Property
	@Parameter("true")
	private boolean showLabelCount;

	/**
	 * This is just passed to the FacetBox components so they can render the hit
	 * terms.
	 */
	@SuppressWarnings("unused")
	@Property
	@Parameter
	private FacetHit facetHit;

	@Inject
	private ComponentResources resources;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private Request request;
	
	private UserInterfaceState uiState = searchSessionState.getUiState();
	
	@Inject
	private Logger logger;
	

	// TODO Rather give the FacetGroup class a type attribute.
	public boolean isFilter() {
		return uiState.getSelectedFacetGroupIndex() == FacetService.FILTER;
	}

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
	public FacetConfiguration getFacetConfiguration(int facet_nr) {
		FacetGroup<FacetConfiguration> currentFacetGroup = uiState.getSelectedFacetGroup();
		if (facet_nr < currentFacetGroup.size()) {
			return currentFacetGroup.get(facet_nr);
		}
		return null;
	}

	public FacetConfiguration getFacetConfiguration1() {
		return getFacetConfiguration(0);
	}

	public FacetConfiguration getFacetConfiguration2() {
		return getFacetConfiguration(1);
	}

	public FacetConfiguration getFacetConfiguration3() {
		return getFacetConfiguration(2);
	}

	public FacetConfiguration getFacetConfiguration4() {
		return getFacetConfiguration(3);
	}

	public FacetConfiguration getFacetConfiguration5() {
		return getFacetConfiguration(4);
	}

	public FacetConfiguration getFacetConfiguration6() {
		return getFacetConfiguration(5);
	}

	public FacetConfiguration getFacetConfiguration7() {
		return getFacetConfiguration(6);
	}

	public FacetConfiguration getFacetConfiguration8() {
		return getFacetConfiguration(7);
	}

	public FacetConfiguration getFacetConfiguration9() {
		return getFacetConfiguration(8);
	}

	public FacetConfiguration getFacetConfiguration10() {
		return getFacetConfiguration(9);
	}

	public FacetConfiguration getFacetConfiguration11() {
		return getFacetConfiguration(10);
	}

	public FacetConfiguration getFacetConfiguration12() {
		return getFacetConfiguration(11);
	}

	public FacetConfiguration getFacetConfiguration13() {
		return getFacetConfiguration(12);
	}

	public FacetConfiguration getFacetConfiguration14() {
		return getFacetConfiguration(13);
	}

	public FacetConfiguration getFacetConfiguration15() {
		return getFacetConfiguration(14);
	}

	public Object onTabSelect() {
		String selectedTab = request.getParameter(SELECTED_TAB_PARAMETER);
		// The returned parameter value is the index of the selected
		// facet group. Thus we only have to parse this integer
		// and we're ready to go.
		uiState.setSelectedFacetGroupIndex(Integer.parseInt(selectedTab));
		
		logger.debug("Creating labels to display for selected facet group.");
		uiState.createLabelsForSelectedFacetGroup();
		logger.debug("Preparing child terms of displayed terms.");
		if (!uiState.prepareLabelsForSelectedFacetGroup())
			logger.debug("No children to prepare.");
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
