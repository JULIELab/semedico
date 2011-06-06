package de.julielab.semedico.components;

import java.text.Format;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.semedico.base.FacetInterface;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;
import de.julielab.semedico.search.IFacetHitCollectorService;
import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.IClientIdentificationService;
import de.julielab.semedico.util.AbbreviationFormatter;
import de.julielab.semedico.util.DisplayGroup;
import de.julielab.semedico.util.LabelFilter;

public class FacetBox implements FacetInterface {

	@Property
	@Parameter
	private FacetHit facetHit;

	@Property
	@Parameter
	private FacetConfiguration facetConfiguration;

	@SuppressWarnings("unused")
	@Property
	@Parameter("true")
	private boolean showLabelCount;

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

	@SuppressWarnings("unused")
	@Property
	private int labelIndex;

	@Parameter
	private FacetTerm selectedTerm;

	@SuppressWarnings("unused")
	@Property
	@Parameter("true")
	private boolean viewModeSwitchable;

	// @Parameter
	// private OpenBitSet documents;

	@Property
	private FacetTerm pathItem;

	@Property
	private int pathItemIndex;

	@ApplicationState
	private Client client;

	private static String INIT_JS = "var %s = new FacetBox(\"%s\", \"%s\", %s, %s, %s);";

	@Inject
	@Path("facetbox.js")
	private Asset facetBoxJS;

	@Inject
	private Request request;

	@Inject
	private IFacetHitCollectorService facetHitCollectorService;

	@Inject
	private Logger logger;

	@Inject
	private ComponentResources resources;

	@Environmental
	private RenderSupport renderSupport;

	@BeginRender
	public void initialize() {
		if (abbreviationFormatter == null)
			abbreviationFormatter = new AbbreviationFormatter(
					MAX_PATH_ENTRY_LENGTH);
		if (displayGroup == null) {
			displayGroup = new DisplayGroup<Label>();
			displayGroup.setBatchSize(3);
			displayGroup.setFilter(new LabelFilter());
		}

		totalFacetCount = facetHit.getTotalFacetCount(facetConfiguration
				.getFacet());
		facetConfiguration.setHidden(false);
		if (totalFacetCount == 0)
			facetConfiguration.setHidden(true);
		LabelMultiHierarchy labelHierarchy = facetHit.getLabelHierarchy();

		if (facetConfiguration.containsSelectedTerms()) {
			FacetTerm lastPathTerm = facetConfiguration.getLastPathElement();
			Label lastPathLabel = labelHierarchy.getNode(lastPathTerm.getId());
			displayGroup.setAllObjects(labelHierarchy
					.getHitChildren(lastPathLabel));
		} else {
			displayGroup.setAllObjects(labelHierarchy
					.getHitFacetRoots(facetConfiguration.getFacet()));
		}
		displayGroup.displayBatch(1);
	}

	@AfterRender
	void addJavaScript(MarkupWriter markupWriter) {
		renderSupport.addScriptLink(facetBoxJS);
		Link link = resources.createEventLink(EVENT_NAME);
		String id = getClientId();
		if (id != null)
			renderSupport.addScript(INIT_JS, id, id, link.toAbsoluteURI(),
					facetConfiguration.isExpanded(),
					facetConfiguration.isCollapsed(),
					facetConfiguration.isHierarchicMode());
		// renderSupport.addScript("alert('ok');");

	}

	public void onTermSelect(int index) {
		if (!(index < displayGroup.getNumberOfDisplayedObjects()))
			throw new IllegalStateException(
					"Term with index "
							+ index
							+ " does not exist in this FacetBox component (there are only "
							+ displayGroup.getNumberOfDisplayedObjects()
							+ "). FacetConfiguration: " + facetConfiguration);

		Label label = displayGroup.getDisplayedObjects().get(index);
		selectedTerm = label.getTerm();
		if (facetConfiguration.isHierarchicMode()) {
			if (label.hasChildHits())
				facetConfiguration.getCurrentPath().add(selectedTerm);
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

		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		filter.setFilterToken(null);
		displayGroup.setFilter(filter);
	}

	private void changeCollapsation(boolean collapsed) {
		if (collapsed) {
			facetConfiguration.setCollapsed(true);
			facetConfiguration.setExpanded(false);
		} else {
			facetConfiguration.setCollapsed(false);
			facetConfiguration.setExpanded(false);
		}

		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		filter.setFilterToken(null);
		displayGroup.setFilter(filter);

		displayGroup.displayBatch(1);
		displayGroup.setBatchSize(3);
	}

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
			LabelFilter filter = (LabelFilter) displayGroup.getFilter();
			filter.setFilterToken(filterToken);
			displayGroup.setFilter(filter);
		}

		if (clearFilter != null) {
			LabelFilter filter = (LabelFilter) displayGroup.getFilter();
			filter.setFilterToken(null);
			displayGroup.setFilter(filter);
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

	public void drillUp(int index) {
		List<FacetTerm> path = facetConfiguration.getCurrentPath();

		if (index < 0 || index >= path.size())
			return;

		for (Iterator<FacetTerm> iterator = path.iterator(); iterator.hasNext();) {
			FacetTerm term = iterator.next();
			if (path.indexOf(term) > index)
				iterator.remove();
		}

		refreshFacetHit();
	}

	private void refreshFacetHit() {
		System.err
				.println("Refresh triggered, but there is no implementation!");
		// Iterator<FacetHit> hitsIterator = facetHitCollectorService
		// .collectFacetHits(Lists.newArrayList(facetConfiguration))
		// .iterator();
		//
		// if (hitsIterator.hasNext()) {
		// facetHit = hitsIterator.next();
		// displayGroup.setAllObjects(facetHit);
		// }

	}

	public void drillToTop() {
		List<FacetTerm> path = facetConfiguration.getCurrentPath();
		path.clear();

		refreshFacetHit();
	}

	public void switchViewMode() {

		if (facetConfiguration.isHierarchicMode())
			facetConfiguration.getCurrentPath().clear();

		facetConfiguration.setHierarchicMode(!facetConfiguration
				.isHierarchicMode());

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
		return facetConfiguration.isHierarchicMode() ? "modeSwitchLinkList"
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
		if (!facetConfiguration.isHierarchicMode())
			return "list";
		else if (labelItem.hasChildHits())
			return "tree";
		else
			return "list";
	}

	public boolean showFilter() {
		return (facetConfiguration.isExpanded() && displayGroup
				.hasMultipleBatches()) || isFiltered();
	}

	public boolean showMore() {
		return displayGroup.getAllObjects().size() > 3;
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
		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		return filter.getFilterToken() != null
				&& !filter.getFilterToken().equals("");
	}

	public String getFilterValue() {
		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		if (filter.getFilterToken() == null
				|| filter.getFilterToken().trim().equals(""))
			return "type to filter";
		else
			return filter.getFilterToken();
	}

	public int getListMargin() {
		return getPathMargin() + 7;
	}

	public String getPathItemDescription() {
		String description = "";

		if (pathItem.getShortDescription() != null
				&& !pathItem.getShortDescription().equals("")) {
			description = "Synonyms: " + pathItem.getShortDescription()
					+ "<br/><br/>";
			description = description.replace(';', ',');
		}
		description += pathItem.getDescription();

		return description;
	}

	public String getLabelDescription() {
		String description = "";

		FacetTerm term = labelItem.getTerm();
		if (term.getShortDescription() != null
				&& !term.getShortDescription().equals("")) {
			description = "Synonyms: " + term.getShortDescription()
					+ "<br/><br/>";
			description = description.replace(';', ',');
		}
		description += term.getDescription();

		return description;
	}

	public boolean getIsHidden() {
		if (facetHit == null)
			return true;

		if (facetConfiguration != null && facetConfiguration.isHidden())
			return true;

		return false;
	}

	public String getClientId() {
		if (facetHit != null)
			return facetConfiguration.getFacet().getCssId();
		else
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

}
