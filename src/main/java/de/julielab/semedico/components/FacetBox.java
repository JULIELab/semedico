package de.julielab.semedico.components;

import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.IPath;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.internal.FacetInterface;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.search.IFacetedSearchService;
import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.IClientIdentificationService;
import de.julielab.semedico.util.AbbreviationFormatter;
import de.julielab.util.DisplayGroup;

public class FacetBox implements FacetInterface {

	@InjectPage
	private ResultList resultList;
	
	@SessionState
	private SearchState searchState;
	
	@SessionState
	private UserInterfaceState uiState;
	
	@Inject
	private ITermService termService;
	
	@Inject
	private IFacetedSearchService searchService;
	
	@Property
	@Parameter
	private FacetConfiguration facetConfiguration;

	@Property
	@Parameter("true")
	private boolean showLabelCountForFacets;

	@Property
	@Parameter("true")
	private boolean showLabelCountForTerms;
	
	@Property
	private long totalFacetCount;

	@Property
	@Persist
	private Format abbreviationFormatter;

	@Property
	@Persist
	private DisplayGroup<Label> displayGroup;

	@Property
	private Label labelItem;

	@Property
	private int labelIndex;

	// @SuppressWarnings("unused")
	// @Property
	// @Parameter("true")
	// private boolean viewModeSwitchable;

	// @Parameter
	// private OpenBitSet documents;

	@Property
	private IFacetTerm pathItem;

	@Property
	private int pathItemIndex;

	@SessionState
	private Client client;

	private static String INIT_JS = "var %s = new FacetBox(\"%s\", \"%s\", %s, %s, %s);";

	@Inject
	@Path("facetbox.js")
	private Asset facetBoxJS;

	@Inject
	private Request request;

	@Inject
	private Logger logger;

	@Inject
	private ComponentResources resources;
	
	@Environmental
	private JavaScriptSupport javaScriptSupport;

	
	// TODO inject default label number to display

	@SetupRender
	public boolean initialize() {
		if (facetConfiguration == null)
			return false;

		FacetHit facetHit = uiState.getFacetHit();

		if (abbreviationFormatter == null)
			abbreviationFormatter = new AbbreviationFormatter(
					MAX_PATH_ENTRY_LENGTH);
		// if (displayGroup == null) {
		// displayGroup = new DisplayGroup<Label>();
		// displayGroup.setBatchSize(3);
		// displayGroup.setFilter(new LabelFilter());
		// }

		try {
			displayGroup = facetConfiguration.getLabelDisplayGroup();

			totalFacetCount = facetHit.getTotalFacetCount(facetConfiguration
					.getFacet());
			facetConfiguration.setHidden(false);
			if (!displayGroup.hasObjects())
				facetConfiguration.setHidden(true);

			// sortLabelsIntoDisplayGroup();
			// displayGroup.displayBatch(1);

			return true;
		} catch (IllegalStateException e) {
			logger.warn(e.getMessage());
		}
		facetConfiguration.setHidden(true);
		return false;
	}

	/**
	 * Sets the correct labels for this FacetBox' facet into the
	 * <code>displayGroup</code>.
	 * <p>
	 * This method differentiates between flat and hierarchical facet mode and
	 * put the appropriate label set into the <code>displayGroup</code>.
	 * </p>
	 */
	// private void sortLabelsIntoDisplayGroup() {
	// FacetHit facetHit = uiState.getFacetHit();
	// if (facetConfiguration.isHierarchical()) {
	// displayGroup.setAllObjects(facetHit
	// .getLabelsForFacetOnCurrentLevel(facetConfiguration));
	// // if (facetConfiguration.isDrilledDown()) {
	// // IFacetTerm lastPathTerm = facetConfiguration
	// // .getLastPathElement();
	// // displayGroup.setAllObjects(facetHit.getLabelsForHitChildren(
	// // lastPathTerm, thisFacet));
	// // } else {
	// // displayGroup.setAllObjects(facetHit
	// // .getLabelsForHitFacetRoots(thisFacet));
	// // }
	// } else {
	// displayGroup.setAllObjects(facetHit
	// .getFlatLabels(facetConfiguration));
	// }
	//
	// }

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
		if (facetConfiguration.isHierarchical()) {
			IFacetTerm selectedTerm = ((TermLabel) label).getTerm();
			if (label.hasChildHitsInFacet(facetConfiguration.getFacet())) {
				facetConfiguration.appendNodeToCurrentPath(selectedTerm);
			}
		}
		
		
		
		Multimap<String, IFacetTerm> queryTerms = searchState.getQueryTerms();
		Label selectedLabel = searchState.getSelectedTerm();
		if (selectedLabel == null) {
			throw new IllegalStateException(
					"The IFacetTerm object reflecting the newly selected term is null.");
		}
		logger.debug("Name of newly selected label: {} (ID: {})",
				selectedLabel.getName(), selectedLabel.getId());
		// Get the FacetConfiguration associated with the selected term.
		Facet selectedFacet = facetConfiguration.getFacet();

		IFacetTerm selectedTerm;
		boolean selectedTermIsAlreadyInQuery = false;
		Multimap<String, IFacetTerm> newQueryTerms = HashMultimap.create();

		if (selectedLabel instanceof TermLabel) {
			selectedTerm = ((TermLabel) selectedLabel).getTerm();
			logger.debug(
					"Searching for ancestors of {} in the query for refinement...",
					selectedTerm.getName());
			// We have to take caution when refining a term. Only the
			// deepest term of each root-node-path in the hierarchy may be
			// included in our queryTerms map.
			// Reason 1: The root-node-path of _each_ term in queryTerms is
			// computed automatically in the QueryPanel
			// currently.
			// Reason 2: We associate refined terms with the (user) query string
			// of the original term. Multiple terms per string -> disambiguation
			// triggers.
			IPath rootPath = termService.getPathFromRoot(selectedTerm);
			String refinedQueryStr = null;
			// Build a new queryTerms map with all not-refined terms.
			// The copying is done because in rare cases writing on the
			// queryTokens map while iterating over it can lead to a
			// ConcurrentModificationException.
			for (Map.Entry<String, IFacetTerm> entry : queryTerms.entries()) {
				String queryToken = entry.getKey();
				IFacetTerm term = entry.getValue();

				IPath potentialAncestorRootPath = termService
						.getPathFromRoot(term);

				if (!rootPath.containsNode(term)
						&& !potentialAncestorRootPath
								.containsNode(selectedTerm))
					newQueryTerms.put(queryToken, term);
				else {
					// If there IS a term in queryTerms which lies on the root
					// path, just memorize its key. Except its the exact term
					// which
					// has been selected. This can happen when a facet has been
					// drilled up and the same term is selected again.
					if (term.equals(selectedTerm))
						selectedTermIsAlreadyInQuery = true;
					refinedQueryStr = queryToken;
					logger.debug(
							"Found ancestor of {} in current search query: {}",
							selectedTerm.getName(), term.getName());
				}
			}
			if (!selectedTermIsAlreadyInQuery) {
				// If there was an ancestor of the selected term in queryTerms,
				// now
				// associate the new term with its ancestor's query string.
				if (refinedQueryStr != null) {
					logger.debug("Ancestor found, refining the query.");
					newQueryTerms.put(refinedQueryStr, selectedTerm);
				} else {
					// Otherwise, add a new mapping.
					logger.debug("No ancestor found, add the term into the current search query.");

					// Associate the new term with its ID as query string.
					newQueryTerms.put(selectedTerm.getId(), selectedTerm);
					// Append the new term to the raw query
				}
			}
		} else {
			logger.debug("String label (with no associated term) selected. Creating special FacetTerm object.");
			// What about a FacetTermFactory? It could cache these things and
			// offer proper methods for terms with a facet vs. key terms.
			// TODO: Now we have kind of a factory, what about caching? Could
			// happen right inside of StringTermService
			selectedTerm = termService.getTermObjectForStringTerm(
					selectedLabel.getName(), selectedFacet);
			// selectedTerm = new FacetTerm("\"" + selectedLabel.getId() + "\"",
			// selectedLabel.getName());
			// selectedTerm.addFacet(selectedFacet);
			// selectedTerm.setIndexNames(selectedFacet.getFilterFieldNames());
			if (queryTerms.values().contains(selectedTerm))
				selectedTermIsAlreadyInQuery = true;
			else
				queryTerms.put(selectedTerm.getId(), selectedTerm);
			newQueryTerms = queryTerms;
		}

		if (!selectedTermIsAlreadyInQuery) {
			List<String> allTerms = new ArrayList<String>();
			for (String name : newQueryTerms.keySet())
				for (IFacetTerm term : newQueryTerms.get(name))
					allTerms.add(name + ": " + term.getName());
			logger.info("New term added to query. Current queryTerms content: '"
					+ StringUtils.join(allTerms, "', '") + "'");

			searchState.setDisambiguatedQuery(newQueryTerms);
			searchState.getQueryTermFacetMap().put(selectedTerm, selectedFacet);
			
		} else {
			logger.debug("Selected term is already contained in the query. No changes made.");
			Map<Facet, FacetConfiguration> facetConfigurations = uiState
					.getFacetConfigurations();
			FacetConfiguration facetConfiguration = facetConfigurations
					.get(selectedFacet);
			uiState.createLabelsForFacet(facetConfiguration);
		}
	}

	private void changeExpansion(boolean expanded) {
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

	private void changeCollapsation(boolean collapsed) {
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
	 * Updates the displayed labels in a facet, must be called e.g. after a
	 * drillUp.
	 */
	private void refreshFacetHit() {
		// First of all: Check whether new terms will show up for which we don't
		// have collected frequency counts yet. If so, get the counts.
		uiState.createLabelsForFacet(facetConfiguration);
		// sortLabelsIntoDisplayGroup();

	}

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
