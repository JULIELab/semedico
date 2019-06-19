package de.julielab.semedico.components;

import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroup;
import de.julielab.semedico.core.facets.UIFacetGroupSection;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.state.SemedicoSessionState;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

/**
 * This component is responsible for rendering the facet group tabs (BioMed, Immunology, ...) and creating the FacetBox
 * components for the currently selected facet group.
 * <p>
 * The facet group tabs are rendered by a loop in the template which iterates over all facet groups. The facet groups
 * are given in the searchConfiguration which determines the state of a particular search (query terms, facet order...).
 * <br>
 * A facet group is identified by its index (0, 1, 2, ...) which is stored in the private attribute
 * {@link #selectedFacetGroupIndex}.
 * </p>
 * 
 * @author faessler
 * 
 */

@Import(library = { "facetSection.js", "termlist-tooltips.js" })
public class Tabs {

	@InjectComponent
	private Zone facetSectionListZone;

	@InjectComponent
	private Zone tabHeaderZone;

	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;

	public String getHeaderClass() {
		return facetGroupLoopIndex == uiState.getSelectedFacetGroupIndex() ? "tabActive" : "tabInActive";
	}

	@Inject
	private Block dialogBlock;

	public Object onOpenDialog() {
		return dialogBlock;
	}

	@SessionState
	@Property
	private SemedicoSessionState sessionState;

	@Parameter(required=true)
	@Property
	private ParseTree query;

	@Parameter
	@Property
	private UserInterfaceState uiState;

	@Inject
	private ISearchService searchService;

	@Property
	private int facetGroupLoopIndex;

	@Property
	private FacetGroup<UIFacet> facetGroupLoopItem;

	@Property
	private UIFacet uiFacetLoopItem;

	@Inject
	private ComponentResources resources;

	 @Environmental
	 private JavaScriptSupport javaScriptSupport;

	@Inject
	private Request request;

	@Inject
	private Logger logger;

	public String getLoopItemFacetName() {
		return uiFacetLoopItem.getName();
	}

	public UIFacetGroupSection getUIFacetGroupSection(int sectionNr) {
		if (null != uiState) {
			UIFacetGroup currentFacetGroup = uiState.getSelectedFacetGroup();
			if (sectionNr < currentFacetGroup.numSections()) {
				UIFacetGroupSection section = currentFacetGroup.getSection(sectionNr);
				logger.debug("Displaying facet section nr {}: {}.", sectionNr, section.getName());
				return section;
			}
		}
		return null;
	}

	public UIFacetGroupSection getSection1() {
		return getUIFacetGroupSection(0);
	}

	public UIFacetGroupSection getSection2() {
		return getUIFacetGroupSection(1);
	}

	public UIFacetGroupSection getSection3() {
		return getUIFacetGroupSection(2);
	}

	public UIFacetGroupSection getSection4() {
		return getUIFacetGroupSection(3);
	}

	public UIFacetGroupSection getSection5() {
		return getUIFacetGroupSection(4);
	}

	public UIFacetGroupSection getSection6() {
		return getUIFacetGroupSection(5);
	}

	/**
	 * This method is used by the loop in the template which creates the required FacetBox components.
	 * <p>
	 * facet_nr is the index variable used by this loop. In every iteration the facet_nr-th facet configuration is
	 * required for construction of the corresponding FacetBox.
	 * </p>
	 * 
	 * @param facetNr
	 *            Passed by tapestry as loop iterator index over facet groups.
	 * @return The facet configuration for the facet with position <code>facet_nr</code> in the currently selected facet
	 *         group.
	 */
	public UIFacet getUIFacet(int facetNr) {
		FacetGroup<UIFacet> currentFacetGroup = uiState.getSelectedFacetGroup();
		if (facetNr < currentFacetGroup.size()) {
			return currentFacetGroup.get(facetNr);
		}
		return null;
	}

	public Object onTabSelect(int facetGroupIndex) {
		// The returned parameter value is the index of the selected
		// facet group.
		uiState.setSelectedFacetGroupIndex(facetGroupIndex);
		//searchService.doTabSelectSearch(sessionState.getDocumentRetrievalSearchState().getSolrQueryString());

		// This happens when the user just opens a URL to the main page without
		// giving a query. Don't get any label counts then.
		if (sessionState.getDocumentRetrievalSearchState().getUserQueryString() == null) {
			logger.debug("User query string is null.");
			return this;
		}

		if (request.isXHR()) {
			ajaxResponseRenderer.addRender("facetSectionList", facetSectionListZone).addRender("tabHeader",
					tabHeaderZone);
			return null;
		}
		return this;

	}
	@AfterRender
	void afterRender() {
		javaScriptSupport.addInitializerCall("initializeFacetTermTooltips", new JSONArray());
	}

	// TODO: facetDialogJS
	// @AfterRender
	// void addJavaScript(MarkupWriter markupWriter) {
	// javaScriptSupport.importJavaScriptLibrary(tabsJS);
	// Link link = resources.createEventLink(EVENT_NAME);
	//
	// int selectedFacetGroupIndex = uiState.getSelectedFacetGroupIndex();
	// javaScriptSupport.addScript(INIT_JS, FACET_BAR_ID,
	// selectedFacetGroupIndex, link.toAbsoluteURI());
	// }
}
