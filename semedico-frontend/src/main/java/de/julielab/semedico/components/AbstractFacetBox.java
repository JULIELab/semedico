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

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.components.data.MessageLabel;
import de.julielab.semedico.core.search.components.data.TermLabel;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.util.DisplayGroup;
import de.julielab.semedico.internal.FacetInterface;
import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.IClientIdentificationService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.util.AbbreviationFormatter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

/**
 * @author faessler
 * 
 */
@Import(stylesheet="context:css/facetbox.css")
public abstract class AbstractFacetBox implements FacetInterface
{
	// for subclasses
	@SessionState(create = false)
	protected SemedicoSessionState sessionState;

	/**
	 * Has to be delivered by subclass in the 'setupRender' phase.
	 */
	protected AbstractUserInterfaceState uiState;

	@Inject
	protected IConceptService termService;

	@Parameter(name = "uiFacet")
	private UIFacet uiFacetParameter;

	/**
	 * Has the same value as {@link #uiFacetParameter} (see
	 * {@link #initialize()}). The value is just given to this persist property
	 * to that the original parameter is not accessed so often. Because on each
	 * access, the containing component has to return this component again to
	 * allow for access of the parameter (because pages and components are
	 * pooled and do not belong to a particular user). This may cause an
	 * abundance of confusing logging messages we'd like to avoid.
	 */
	@Property
	@Persist
	protected UIFacet uiFacet;

	@Parameter
	protected LabelStore labelStore;

	@Property
	protected long totalFacetCount;

	@Property
	@Parameter("true")
	protected boolean showLabelCountForFacets;

	// @Property
	// @Parameter("true")
	// protected boolean showLabelCountForTerms;

	@Property
	@Persist
	protected Format abbreviationFormatter;

	@Property
	@Persist
	protected DisplayGroup<Label> displayGroup;

	@Property
	@Parameter
	protected Concept selectedConcept;

	@Property
	protected Label labelItem;

	@Property
	protected int labelIndex;

	@Property
	protected Concept pathItem;

	@Property
	protected int pathItemIndex;

	@Property
	protected int currentIndentDepth;

	@SessionState
	protected Client client;

	protected static String INIT_JS = "var %s = new FacetBox(\"%s\", \"%s\", %s, %s, %s);";

	@Inject
	@Path("facetbox.js")
	protected Asset facetBoxJS;

	@Inject
	protected Request request;

	@Inject
	protected Logger log;

	@Inject
	protected ComponentResources resources;

	@Environmental
	protected JavaScriptSupport javaScriptSupport;

	@Symbol(SymbolConstants.PRODUCTION_MODE)
	private boolean productionMode;

	// TODO inject default label number to display

	@SetupRender
	public boolean initialize()
	{
		if (uiFacetParameter == null)
		{
			return false;
		}

		uiFacet = uiFacetParameter;

		log.debug("Creating FacetBox for facet {} (FacetBox: {}; UIFacet: {}).",
				new Object[] { uiFacet.getName(), this, System.identityHashCode(uiFacet) });

		if (abbreviationFormatter == null)
		{
			abbreviationFormatter = new AbbreviationFormatter(MAX_PATH_ENTRY_LENGTH);
		}
		try
		{
			displayGroup = uiFacet.getLabelDisplayGroup();

			totalFacetCount = labelStore.getTotalFacetCount(uiFacet);

			if (!displayGroup.hasObjects())
			{
				log.debug("Facet {} has no terms to display and thus is collapsed.", uiFacet);
				// uiFacet.setCollapsed(true);
				// uiFacet.setHidden(true);
			}

			return true;
		}
		catch (IllegalStateException e)
		{
			log.warn(e.getMessage());
		}
		uiFacet.setHidden(true);
		return false;
	}

	@Inject
	private AjaxResponseRenderer ajaxResponseRenderer;

	@AfterRender
	void afterRender()
	{
		javaScriptSupport.importJavaScriptLibrary(facetBoxJS);
		Link link = resources.createEventLink(EVENT_NAME);
		String id = getClientId();
		if (id != null)
		{
			// INIT_JS="var %s = new FacetBox(\"%s\", \"%s\", %s, %s, %s)"
			javaScriptSupport.addScript(
				INIT_JS, id, id,
				link.toAbsoluteURI(),
				uiFacet.isExpanded(),
				uiFacet.isCollapsed(),
				uiFacet.isInHierarchicViewMode());
		}
	}

	/**
	 * Sets {@link #selectedConcept} to the selected concept and, if the facet
	 * is hierarchical, drills down the hierarchy to the selected node to then
	 * display its children. The <tt>selectedConcept</tt> property may be used
	 * by subclasses.
	 * 
	 * @param termIndexAndFacetId
	 */
	public void onTermSelect(String termIndexAndFacetId)
	{
		String[] termIdxFacetId = termIndexAndFacetId.split("_");
		int index = Integer.parseInt(termIdxFacetId[0]);
		String facetId = termIdxFacetId[1];
		
		if (!(index < displayGroup.getNumberOfDisplayedObjects()))
		{
			throw new IllegalStateException(
					"Term with index " + index + " does not exist in this FacetBox component (there are only "
							+ displayGroup.getNumberOfDisplayedObjects() + "). FacetConfiguration: " + uiFacet);
		}
		
		Label label = displayGroup.getDisplayedObjects().get(index);
		// sessionState.getDocumentRetrievalSearchState().setSelectedTerm(label);
		if (label instanceof TermLabel)
		{
			selectedConcept = ((TermLabel) label).getTerm();
		}
		else
		{
			selectedConcept = termService.getTermObjectForStringTerm(label.getName(), facetId);
		}
		if (uiFacet.isInHierarchicViewMode())
		{
			Concept selectedTerm = ((TermLabel) label).getTerm();
			// if (label.hasChildHitsInFacet(uiFacet)) {
			// uiFacet.appendNodeToCurrentPath(selectedTerm);
			// }
			if (selectedTerm.hasChildrenInFacet(uiFacet.getId()))
			{
				uiFacet.appendNodeToCurrentPath(selectedTerm);
			}
		}

	}

	protected void changeExpansion(boolean expanded)
	{
		log.debug("Setting expansion of facet {} to {}.", uiFacet.getName(), expanded);
		
		if (expanded)
		{
			displayGroup.displayBatch(1);
			displayGroup.setBatchSize(20);

			uiFacet.setExpanded(true);
			uiFacet.setCollapsed(false);
		}
		else
		{
			displayGroup.displayBatch(1);
			displayGroup.setBatchSize(3);
			uiFacet.setExpanded(false);
			uiFacet.setCollapsed(false);
		}

		displayGroup.resetFilter();

		refreshFacetHit();
	}

	protected void changeCollapsation(boolean collapsed)
	{
		log.debug("Setting collapsation of facet {} to {}.", uiFacet.getName(), collapsed);
		
		if (collapsed)
		{
			uiFacet.setCollapsed(true);
			uiFacet.setExpanded(false);
		}
		else
		{
			uiFacet.setCollapsed(false);
			uiFacet.setExpanded(false);
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
	public Object onAction()
	{
		log.debug("Action event is called for facet {}.", uiFacet.getName());
		String isExpanded = request.getParameter(EXPAND_LIST_PARAM);
		String collapse = request.getParameter(COLLAPSE_PARAM);
		String pager = request.getParameter(PAGER_PARAM);
		String filterToken = request.getParameter(FILTER_TOKEN_PARAM);
		String clearFilter = request.getParameter(CLEAR_FILTER_PARAM);
		String hide = request.getParameter(HIDE_PARAM);
		String hierarchicMode = request.getParameter(HIERARCHIC_MODE_PARAM);
		String drillUp = request.getParameter(DRILL_UP_PARAM);
		String drillToTop = request.getParameter(DRILL_TO_TOP_PARAM);

		log.info(
			"trigger() isExpanded: " + isExpanded
			+ " collapse: " + collapse
			+ " pager " + pager
			+ " filterToken " + filterToken
			+ " clearFilter " + clearFilter
			+ " hide " + hide
			+ " hierarchicMode " + hierarchicMode
			+ " drillUp " + drillUp
			+ " drillToTop " + drillToTop);

		if (isExpanded != null)
		{
			changeExpansion(Boolean.parseBoolean(isExpanded));
		}

		if (pager != null)
		{
			if (pager.equals("next"))
			{
				displayGroup.displayNextBatch();
			}
			else
			{
				displayGroup.displayPreviousBatch();
			}
		}

		if (collapse != null)
		{
			changeCollapsation(Boolean.parseBoolean(collapse));
		}

		if (filterToken != null)
		{
			displayGroup.setFilterToken(filterToken);
		}

		if (clearFilter != null)
		{
			displayGroup.setFilterToken(null);
		}

		if (hide != null && hide.equals("true"))
		{
			uiFacet.setHidden(true);
		}

		if (hierarchicMode != null)
		{
			switchViewMode();
		}

		if (drillUp != null)
		{
			int index = Integer.parseInt(drillUp);
			drillUp(index);
		}

		if (drillToTop != null)
		{
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
	public void drillUp(int index)
	{

		if (index < 0 || index >= uiFacet.getCurrentPathLength())
		{
			return;
		}

		Concept selectedTerm = uiFacet.getNodeOnCurrentPathAt(index);

		while (uiFacet.removeLastNodeOfCurrentPath() != selectedTerm)
			// That's all. We trust that selectedTerm IS on the path.
			;

		refreshFacetHit();
	}

	public void drillToTop()
	{
		uiFacet.clearCurrentPath();

		refreshFacetHit();
	}

	public void switchViewMode()
	{

		// TODO why? Wouldn't it be nicer to remember the path?
		// if (facetConfiguration.isHierarchical())
		// facetConfiguration.getCurrentPath().clear();

		uiFacet.switchViewMode();
		// TODO trigger the collection of flat facet counts if necessary (i.e.
		// when not already done).
		refreshFacetHit();

	}

	public String getCollapseLinkId()
	{
		return getClientId() + "CollapseLink";
	}

	public String getModeSwitchLinkId()
	{
		return getClientId() + "ModeSwitchLink";
	}

	public String getCloseLinkId()
	{
		return getClientId() + "CloseLink";
	}

	public String getFacetBoxHeaderPathStyle()
	{
		return "display:" + (uiFacet.isCollapsed() ? "none" : "block;");
	}

	public String getPathEntryStyle()
	{
		return "margin-left:" + getPathMargin(false) + "px";
	}

	public String getPathLinkId()
	{
		return getClientId() + "pathLink" + pathItemIndex;
	}

	public String getModeSwitchLinkClass()
	{
		String modeSwitchClass = uiFacet.isInHierarchicViewMode() ? "modeSwitchLinkList" : "modeSwitchLinkTree";
		return modeSwitchClass + " tooltip";
	}

	public String getLinkId()
	{
		return getClientId() + "Link";
	}

	public String getTopLinkId()
	{
		return getClientId() + "TopLink";
	}

	public String getPagerPreviousLinkId()
	{
		return getClientId() + "PagerPreviousLink";
	}

	public String getPagerNextLinkId()
	{
		return getClientId() + "PagerNextLink";
	}

	@Deprecated
	public String getLabelFilterStyle()
	{
		return "margin-left:" + getElementMargin() + "px;";
	}

	public String getLabelStyle()
	{
		return "margin-left:" + getListMargin() + "px;";
	}

	public String getBoxFooterLeftStyle()
	{
		return "margin-left:" + getFooterMargin() + "px;";
	}

	public String getBoxFooterLeftMaximizedStyle()
	{
		return "margin-left:" + getFooterMargin() + "px;";
	}

	public String getStructureRelatedClass()
	{
		if (labelItem instanceof TermLabel)
		{
			TermLabel tl = (TermLabel) labelItem;
			if (tl.getTerm().hasChildrenInFacet(uiFacet.getId()) && uiFacet.isHierarchic())
			{
				return "tree";
			}
		}
		return "list";
	}

//	public String getSelectionRelatedClass() {
//		if (labelItem instanceof TermLabel) {
//			Concept currentConcept = ((TermLabel) labelItem).getTerm();
//			SearchState searchState = sessionState.getDocumentRetrievalSearchState();
//			if (searchState.isSelectedFacetConcept(currentConcept))
//				return "selected";
//			return "";
//		} else {
//			throw new IllegalArgumentException(
//					"Only termlabels are currently supported, this method has to be extended for label class "
//							+ labelItem.getClass());
//		}
//	}

	public String getFirstPathItemClass()
	{
		if (pathItemIndex == 0)
		{
			return "first";
		}
		return "";
	}

	public boolean showFilter()
	{
		return (uiFacet.isExpanded() && displayGroup.hasMultipleBatches()) || isFiltered();
	}

	public boolean isList()
	{
		if (getStructureRelatedClass() != "tree")
		{
			return true;
		}
		return false;
	}

	public int getPathMargin(boolean isList)
	{
		if (pathItemIndex == 0)
		{
			currentIndentDepth = 11;
		}
		else
		{
			if (!isList)
			{
				currentIndentDepth = currentIndentDepth + 17;
			}
		}
		return currentIndentDepth;
	}

	public int getElementMargin()
	{

		if (client.getName().equals(IClientIdentificationService.IEXPLORER))
		{
			return getPathMargin(false) + 10;
		}
		else
		{
			return getPathMargin(false) +10;
		}
	}

	public int getFooterMargin()
	{
		if (client.equals(Client.IEXPLORER6))
		{
			return getPathMargin(false) + 0;
		}
		else
		{
			return getPathMargin(false) + 10;
		}
	}

	public boolean isFiltered()
	{
		return displayGroup.isFiltering();
	}

	public String getFilterValue()
	{
		if (!displayGroup.isFiltering())
		{
			return "type to filter";
		}
		else
		{
			return displayGroup.getFilterToken();
		}
	}

	public int getListMargin()
	{
		if (pathItemIndex == 0)
		{
			return getPathMargin(true);
		}
		return getPathMargin(true) + 17;
	}

	public String getPathItemDescription()
	{
		String description = "";

		if (pathItem.getSynonyms() != null && pathItem.getSynonyms().size() > 0)
		{
			description = "<b>Synonyms</b>: " + StringUtils.join(pathItem.getSynonyms(), ", ") + "<br/><br/>";
		}
		
		description += "<b>Term description:</b><br/>" + StringUtils.join(pathItem.getDescriptions(), "<br/>");
		
		if (!productionMode)
		{
			description += "<br/><br/><b>Term ID</b>: " + pathItem.getId();
		}

		return description;
	}

	@Deprecated
	public String getLabelDescription()
	{
		String description = "";

		Concept term = null;
		
		if (labelItem instanceof TermLabel)
		{
			term = ((TermLabel) labelItem).getTerm();

			List<String> synonyms = new ArrayList<>();
			List<String> descriptions = new ArrayList<>();

			for (String synonym : term.getSynonyms())
			{
				synonyms.add(StringEscapeUtils.escapeJava(synonym));
			}
			
			for (String descriptionItem : term.getDescriptions())
			{
				descriptions.add(StringEscapeUtils.escapeJava(descriptionItem));
			}

			List<String> descriptionParts = new ArrayList<>();
			if (null != term.getSynonyms() && term.getSynonyms().size() > 0)
			{
				descriptionParts.add("<b>Synonyms:</b> " + StringUtils.join(synonyms, ", "));
			}
			if (null != term.getDescriptions() && term.getDescriptions().size() > 0)
			{
				descriptionParts.add("<b>Description:</b><br/>" + StringUtils.join(descriptions, "<br/>"));
			}
			if (!productionMode)
			{
				descriptionParts.add("<b>Term ID</b>: " + term.getId());
			}
			description = StringUtils.join(descriptionParts, "<br/><br/>");
		}

		return description;
	}
	
	public JSONArray getLabelSynonyms()
	{
		JSONArray synonyms = new JSONArray();
		if (labelItem instanceof TermLabel)
		{
			Concept term = ((TermLabel) labelItem).getTerm();
			for (String synonym : term.getSynonyms())
			{
				synonyms.put(synonym);
			}
		}
		return synonyms;
	}
	
	public JSONArray getLabelDescriptions()
	{
		JSONArray descriptions = new JSONArray();
		if (labelItem instanceof TermLabel)
		{
			Concept term = ((TermLabel) labelItem).getTerm();
			for (String description : term.getDescriptions())
			{			
				descriptions.put(description);
			}
		}
		return descriptions;
	}
	
	public String getLabelFacetName()
	{
		if (labelItem instanceof TermLabel)
		{
			Concept term = ((TermLabel) labelItem).getTerm();
			String facetname = term.getFirstFacet().getName();
		
			if (facetname.length() > 30 && term.getFirstFacet().getShortName() != null)
			{
				facetname = term.getFirstFacet().getShortName();
			}
			
			return facetname;
		}
		return "";
	}

	public String getFacetIdSubtitle()
	{
		if (!productionMode)
		{
			return "<b>Facet ID</b>: " + uiFacet.getId();
		}
		return "";
	}

	public boolean getIsHidden()
	{
		if (uiFacet != null && uiFacet.isHidden())
		{
			return true;
		}

		return false;
	}

	private String clientId;

	public String getClientId()
	{
		if (StringUtils.isBlank(clientId))
		{
			if (uiFacet != null)
			{
				return uiFacet.getCssId();
			}
			// clientId =
			// javaScriptSupport.allocateClientId(uiFacet.getCssId());
			return clientId;
		}
		return clientId;
	}

	public String getBoxId()
	{
		return getClientId() + "Box";
	}

	public String getPanelId()
	{
		return getClientId() + "Panel";
	}

	public String getListId()
	{
		return getClientId() + "List";
	}

	public String getPanelStyle()
	{
		return "display:" + (uiFacet.isCollapsed() ? "none" : "block;");
	}

	public boolean getShowLabelCountFacets()
	{
		// return showLabelCountForFacets
		// // TODO Evil magic string!!
		// && !uiState.getSelectedFacetGroup().getName().equals("Filters");
		// TOOD filters are handled differently nowadays, this should probably
		// be removed
		return false;
	}

	public boolean getViewModeSwitchable()
	{
		return uiFacet.isHierarchic();
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
	public String getTermIndexAndFacetId()
	{
		return labelIndex + "_" + uiFacet.getId();
	}

	public abstract String getTermCSSClasses();

	public String getLabelItemMessage()
	{
		if (!(labelItem instanceof MessageLabel))
		{
			log.error("The label \"{}\" is no MessageLabel and thus has no description.");
			return labelItem.getName();
		}
		MessageLabel mLabel = (MessageLabel) labelItem;
		return mLabel.getLongMessage();
	}
}
