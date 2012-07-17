/**
 * AbstractFacetBox.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 02.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.components;

import java.text.Format;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.internal.FacetInterface;
import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.IClientIdentificationService;
import de.julielab.semedico.util.AbbreviationFormatter;
import de.julielab.util.DisplayGroup;

/**
 * @author faessler
 * 
 */
public abstract class AbstractFacetBox implements FacetInterface {
	@SessionState(create = false)
	protected SearchState searchState;

	@SessionState(create = false)
	protected UserInterfaceState uiState;

	@Inject
	protected ITermService termService;

	@Property
	@Parameter
	protected FacetConfiguration facetConfiguration;

	@Parameter
	protected LabelStore labelStore;

	@Property
	protected long totalFacetCount;

	@Property
	@Parameter("true")
	protected boolean showLabelCountForFacets;

	@Property
	@Parameter("true")
	protected boolean showLabelCountForTerms;

	@Property
	@Persist
	protected Format abbreviationFormatter;

	@Property
	@Persist
	protected DisplayGroup<Label> displayGroup;

	@Property
	@Parameter
	protected IFacetTerm selectedTerm;

	@Property
	protected Label labelItem;

	@Property
	protected int labelIndex;

	@Property
	protected IFacetTerm pathItem;

	@Property
	protected int pathItemIndex;

	@SessionState
	protected Client client;

	protected static String INIT_JS = "var %s = new FacetBox(\"%s\", \"%s\", %s, %s, %s);";

	@Inject
	@Path("facetbox.js")
	protected Asset facetBoxJS;

	@Inject
	protected Request request;

	@Inject
	protected Logger logger;

	@Inject
	protected ComponentResources resources;

	@Environmental
	protected JavaScriptSupport javaScriptSupport;

	// TODO inject default label number to display

	@SetupRender
	public boolean initialize() {
		if (facetConfiguration == null)
			return false;

		if (abbreviationFormatter == null)
			abbreviationFormatter = new AbbreviationFormatter(
					MAX_PATH_ENTRY_LENGTH);

		try {
			displayGroup = facetConfiguration.getLabelDisplayGroup();

			totalFacetCount = labelStore.getTotalFacetCount(facetConfiguration
					.getFacet());

			facetConfiguration.setHidden(false);
			if (!displayGroup.hasObjects())
				facetConfiguration.setHidden(true);

			return true;
		} catch (IllegalStateException e) {
			logger.warn(e.getMessage());
		}
		facetConfiguration.setHidden(true);
		return false;
	}

	@AfterRender
	void afterRender(MarkupWriter markupWriter) {
		javaScriptSupport.importJavaScriptLibrary(facetBoxJS);
		Link link = resources.createEventLink(EVENT_NAME);
		String id = getClientId();
		if (id != null)
			javaScriptSupport.addScript(INIT_JS, id, id, link.toAbsoluteURI(),
					facetConfiguration.isExpanded(),
					facetConfiguration.isCollapsed(),
					facetConfiguration.isHierarchical());
	}

	public void onTermSelect(String termIndexAndFacetId) {
		int index = Integer.parseInt(termIndexAndFacetId.split("_")[0]);
		if (!(index < displayGroup.getNumberOfDisplayedObjects()))
			throw new IllegalStateException(
					"Term with index "
							+ index
							+ " does not exist in this FacetBox component (there are only "
							+ displayGroup.getNumberOfDisplayedObjects()
							+ "). FacetConfiguration: " + facetConfiguration);

		Label label = displayGroup.getDisplayedObjects().get(index);
		searchState.setSelectedTerm(label);
		if (label instanceof TermLabel) {
			selectedTerm = ((TermLabel) label).getTerm();
		} else {
			selectedTerm = termService.getTermObjectForStringTerm(
					label.getName(), IFacetService.BTERMS_FACET);
		}
		if (facetConfiguration.isHierarchical()) {
			IFacetTerm selectedTerm = ((TermLabel) label).getTerm();
			if (label.hasChildHitsInFacet(facetConfiguration.getFacet())) {
				facetConfiguration.appendNodeToCurrentPath(selectedTerm);
			}
		}

	}

	protected void changeExpansion(boolean expanded) {
		if (expanded) {
			displayGroup.displayBatch(1);
			displayGroup.setBatchSize(20);

			facetConfiguration.setExpanded(true);
			facetConfiguration.setCollapsed(false);
		} else {
			displayGroup.displayBatch(1);
			displayGroup.setBatchSize(3);
			facetConfiguration.setExpanded(false);
			facetConfiguration.setCollapsed(false);
		}

		displayGroup.resetFilter();

		refreshFacetHit();
	}

	protected void changeCollapsation(boolean collapsed) {
		if (collapsed) {
			facetConfiguration.setCollapsed(true);
			facetConfiguration.setExpanded(false);
		} else {
			facetConfiguration.setCollapsed(false);
			facetConfiguration.setExpanded(false);
		}

		displayGroup.resetFilter();

		displayGroup.displayBatch(1);
		displayGroup.setBatchSize(3);

		refreshFacetHit();
	}

	/**
	 * Called after each event which changes the contents of the FacetBox.
	 */
	protected abstract void refreshFacetHit();

	/**
	 * Performs all dynamic HTML/Ajax requests sent by the facetbox.js scripts.
	 * Thus, all dynamic changes to the facet box happen here.
	 * 
	 * @return The FacetBox component itself to trigger its re-rendering.
	 */
	public Object onAction() {
		String isExpanded = request.getParameter(EXPAND_LIST_PARAM);
		String collapse = request.getParameter(COLLAPSE_PARAM);
		String pager = request.getParameter(PAGER_PARAM);
		String filterToken = request.getParameter(FILTER_TOKEN_PARAM);
		String clearFilter = request.getParameter(CLEAR_FILTER_PARAM);
		String hide = request.getParameter(HIDE_PARAM);
		String hierarchicMode = request.getParameter(HIERARCHIC_MODE_PARAM);
		String drillUp = request.getParameter(DRILL_UP_PARAM);
		String drillToTop = request.getParameter(DRILL_TO_TOP_PARAM);

		logger.info("trigger() isExpanded: " + isExpanded + " collapse: "
				+ collapse + " pager " + pager + " filterToken " + filterToken
				+ " clearFilter " + clearFilter + " hide " + hide
				+ " hierarchicMode " + hierarchicMode + " drillUp " + drillUp
				+ " drillToTop " + drillToTop);

		if (isExpanded != null)
			changeExpansion(Boolean.parseBoolean(isExpanded));

		if (pager != null) {
			if (pager.equals("next")) {
				displayGroup.displayNextBatch();
			} else {
				displayGroup.displayPreviousBatch();
			}
		}

		if (collapse != null)
			changeCollapsation(Boolean.parseBoolean(collapse));

		if (filterToken != null) {
			displayGroup.setFilter(filterToken);
		}

		if (clearFilter != null) {
			displayGroup.setFilter(null);
		}

		if (hide != null && hide.equals("true")) {
			facetConfiguration.setHidden(true);
		}

		if (hierarchicMode != null) {
			switchViewMode();
		}

		if (drillUp != null) {
			int index = Integer.parseInt(drillUp);
			drillUp(index);
		}

		if (drillToTop != null) {
			drillToTop();
		}

		return this;
	}

	/**
	 * Removes all successors of the term at index <code>index</code> which was
	 * selected.
	 * 
	 * @param index
	 */
	public void drillUp(int index) {

		if (index < 0 || index >= facetConfiguration.getCurrentPathLength())
			return;

		IFacetTerm selectedTerm = facetConfiguration
				.getNodeOnCurrentPathAt(index);

		while (facetConfiguration.removeLastNodeOfCurrentPath() != selectedTerm)
			// That's all. We trust that selectedTerm IS on the path.
			;

		refreshFacetHit();
	}

	public void drillToTop() {
		facetConfiguration.clearCurrentPath();

		refreshFacetHit();
	}

	public void switchViewMode() {

		// TODO why? Wouldn't it be nicer to remember the path?
		// if (facetConfiguration.isHierarchical())
		// facetConfiguration.getCurrentPath().clear();

		facetConfiguration.switchStructureMode();
		// TODO trigger the collection of flat facet counts if necessary (i.e.
		// when not already done).
		refreshFacetHit();

	}

	public String getCollapseLinkId() {
		return getClientId() + "CollapseLink";
	}

	public String getModeSwitchLinkId() {
		return getClientId() + "ModeSwitchLink";
	}

	public String getCloseLinkId() {
		return getClientId() + "CloseLink";
	}

	public String getFacetBoxHeaderPathStyle() {
		return "display:"
				+ (facetConfiguration.isCollapsed() ? "none" : "block;");
	}

	public String getPathEntryStyle() {
		return "margin-left:" + getPathMargin() + "px";
	}

	public String getPathLinkId() {
		return getClientId() + "pathLink" + pathItemIndex;
	}

	public String getModeSwitchLinkClass() {
		return facetConfiguration.isHierarchical() ? "modeSwitchLinkList"
				: "modeSwitchLinkTree";
	}

	public String getLinkId() {
		return getClientId() + "Link";
	}

	public String getTopLinkId() {
		return getClientId() + "TopLink";
	}

	public String getPagerPreviousLinkId() {
		return getClientId() + "PagerPreviousLink";
	}

	public String getPagerNextLinkId() {
		return getClientId() + "PagerNextLink";
	}

	public String getLabelFilterStyle() {
		return "margin-left:" + getElementMargin() + "px;";
	}

	public String getLabelStyle() {
		return "margin-left:" + getListMargin() + "px;";
	}

	public String getBoxFooterLeftStyle() {
		return "margin-left:" + getFooterMargin() + "px;";
	}

	public String getBoxFooterLeftMaximizedStyle() {
		return "margin-left:" + getFooterMargin() + "px;";
	}

	public String getLabelClass() {
		if (facetConfiguration.isFlat())
			return "list";
		else if (labelItem.hasChildHitsInFacet(facetConfiguration.getFacet()))
			return "tree";
		else
			return "list";
	}

	public boolean showFilter() {
		return (facetConfiguration.isExpanded() && displayGroup
				.hasMultipleBatches()) || isFiltered();
	}

	public int getPathMargin() {
		return (pathItemIndex + 1) * 7;
	}

	public int getElementMargin() {

		if (client.getName().equals(IClientIdentificationService.IEXPLORER))
			return getPathMargin() + 5;
		else
			return getPathMargin() + 15;
	}

	public int getFooterMargin() {
		if (client.equals(Client.IEXPLORER6))
			return getPathMargin() + 5;
		else
			return getPathMargin() + 15;
	}

	public boolean isFiltered() {
		return displayGroup.isFiltered();
	}

	public String getFilterValue() {
		if (!displayGroup.isFiltered())
			return "type to filter";
		else
			return displayGroup.getFilter();
	}

	public int getListMargin() {
		return getPathMargin() + 7;
	}

	public String getPathItemDescription() {
		String description = "";

		if (pathItem.getSynonyms() != null
				&& !pathItem.getSynonyms().equals("")) {
			description = "Synonyms: " + pathItem.getSynonyms() + "<br/><br/>";
			description = description.replace(';', ',');
		}
		description += pathItem.getDescription();

		return description;
	}

	public String getLabelDescription() {
		String description = "";

		IFacetTerm term = null;
		if (labelItem instanceof TermLabel) {
			term = ((TermLabel) labelItem).getTerm();
			if (term.getSynonyms() != null && !term.getSynonyms().equals("")) {
				description = "Synonyms: " + term.getSynonyms() + "<br/><br/>";
				description = description.replace(';', ',');
			}
			description += term.getDescription();
		}

		return description;
	}

	public boolean getIsHidden() {
		if (facetConfiguration != null && facetConfiguration.isHidden())
			return true;

		return false;
	}

	public String getClientId() {
		if (facetConfiguration != null)
			return facetConfiguration.getFacet().getCssId();
		return null;
	}

	public String getBoxId() {
		return getClientId() + "Box";
	}

	public String getPanelId() {
		return getClientId() + "Panel";
	}

	public String getListId() {
		return getClientId() + "List";
	}

	public String getPanelStyle() {
		return "display:"
				+ (facetConfiguration.isCollapsed() ? "none" : "block;");
	}

	public boolean getShowLabelCountFacets() {
		return showLabelCountForFacets
				&& uiState.getSelectedFacetGroupIndex() != FacetService.FILTER;
	}

	public boolean getViewModeSwitchable() {
		return facetConfiguration.getFacet().isHierarchical();
	}

	/**
	 * Returns a string which consists of the current term name index rendered
	 * in the facet box and the facet id.
	 * <p>
	 * This method is intended for use in the component's template. When a term
	 * is clicked on, {@link #onTermSelect(String)} is called (in this component
	 * and containing components/pages). To uniquely identify the term, its
	 * position and the facet it is in is returned.
	 * </p>
	 * <p>
	 * The format is <termIndex>_<facetId>
	 * </p>
	 * 
	 * @return A string identifying the term which has been clicked in terms of
	 *         currently displayed term names and the facet in which the term
	 *         has been selected.
	 */
	public String getTermIndexAndFacetId() {
		return labelIndex + "_" + facetConfiguration.getFacet().getId();
	}
}
