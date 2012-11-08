package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.exceptions.EmptySearchComplementException;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.core.taxonomy.interfaces.IPath;
import de.julielab.semedico.pages.BTermView;
import de.julielab.semedico.pages.Index;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.util.LazyDisplayGroup;

public class QueryPanel {

	@InjectPage
	private Index index;

	@InjectPage
	private BTermView bTermView;

	@SessionState
	@Property
	private SearchState searchState;

	@SessionState
	private UserInterfaceState uiState;

	@SuppressWarnings("unused")
	@Property
	@SessionState(create = false)
	private BTermUserInterfaceState bTermUIState;

	@Property
	@Parameter
	private Multimap<String, IFacetTerm> spellingCorrectedQueryTerms;

	@Property
	@Parameter
	private Multimap<String, String> spellingCorrections;

	@Property
	// Used to iterate over all mapped terms
	private String queryTerm;

	@Property
	private int queryTermIndex;

	@Property
	@Persist(PersistenceConstants.FLASH)
	private String termToDisambiguate;

	@Property
	private IFacetTerm pathItem;

	@Property
	private int pathItemIndex;

	@Property
	private String correctedTerm;

	@Property
	private int correctedTermIndex;

	@Property
	private boolean hasFilter = false;

	@Property
	@Persist
	private boolean showBTermPanel;

	@Inject
	private Logger logger;

	@Inject
	private ITermService termService;

	@Persist
	private Multimap<String, IFacetTerm> queryTerms;

	public void setupRender() {
		if (searchState.isNewSearch())
			termToDisambiguate = null;
		queryTerms = searchState.getQueryTerms();
	}

	public boolean isTermCorrected() {
		if (queryTerm == null || spellingCorrections == null)
			return false;

		return spellingCorrections.containsKey(queryTerm);
	}

	public Collection getCorrectedTerms() {
		if (queryTerm == null || spellingCorrections == null)
			return null;

		Collection correctedTerms = spellingCorrections.get(queryTerm);
		return correctedTerms;
	}

	public boolean isMultipleCorrectedTerms() {
		if (queryTerm == null || spellingCorrections == null)
			return false;

		return getCorrectedTerms().size() > 1;
	}

	public boolean isTermAmbigue() {
		if (queryTerm == null)
			return false;

		Collection<IFacetTerm> terms = searchState.getQueryTerms().get(
				queryTerm);
		if (terms.size() > 1)
			return true;
		else
			return false;
	}

	public boolean isTermSelectedForDisambiguation() {
		return queryTerm != null && termToDisambiguate != null
				&& queryTerm.equals(termToDisambiguate);
	}

	public void onRefine(String queryTerm) {
		termToDisambiguate = queryTerm;
	}

	public void doQueryChanged(String queryTerm) throws Exception {
		if (queryTerm == null)
			return;
		searchState.removeTerm(queryTerm);
	}

	/**
	 * Returns the first mapped term of the current query string which is
	 * iterated over in the template.
	 * 
	 * @return The mapped term of the current query string.
	 */
	public IFacetTerm getMappedTerm() {
		// TODO seems a bit arbitrary. Is it possible that there are multiple
		// FacetTerms for queryTerm? Should this be so? Is it an adequate
		// solution to just take the first?
		Collection<IFacetTerm> mappedTerms = queryTerms.get(queryTerm);
		if (mappedTerms.size() > 0)
			return mappedTerms.iterator().next();
		else
			return null;
	}

	public String getMappedTermClass() {
		IFacetTerm mappedTerm = getMappedTerm();
		if (mappedTerm != null) {
			Facet facet = getMappedTermFacet();
			String cssId = facet.getCssId();
			String termClass = cssId + " filterBox primaryFacetStyle";
			return termClass;
		} else
			return null;
	}

	public Facet getMappedTermFacet() {
		IFacetTerm mappedTerm = getMappedTerm();
		Map<IFacetTerm, Facet> queryTermFacetMap = searchState
				.getQueryTermFacetMap();
		Facet facet = queryTermFacetMap.get(mappedTerm);
		return facet;
	}

	private Map<String, IFacetTerm> getUnambigousQueryTerms() {
		Map<String, IFacetTerm> unambigousTerms = new HashMap<String, IFacetTerm>();

		for (String queryTerm : queryTerms.keySet()) {
			Collection<IFacetTerm> terms = queryTerms.get(queryTerm);
			if (terms.size() == 1)
				unambigousTerms.put(queryTerm, terms.iterator().next());
		}

		return unambigousTerms;
	}

	public void onDrillUp(String queryTerm, int pathItemIndex) throws Exception {

		if (queryTerm == null)
			return;

		Map<Facet, UIFacet> facetConfigurations = uiState
				.getFacetConfigurations();
		IFacetTerm searchTerm = queryTerms.get(queryTerm).iterator().next();

		if (searchTerm == null)
			return;

		IPath pathFromRoot = termService.getPathFromRoot(searchTerm);

		if (pathItemIndex < 0 || pathItemIndex > pathFromRoot.length() - 1)
			return;

		IFacetTerm parent = pathFromRoot.getNodeAt(pathItemIndex);

		UIFacet configuration = facetConfigurations.get(searchTerm
				.getFirstFacet());
		boolean termIsOnPath = configuration
				.containsCurrentPathNode(searchTerm);
		if (configuration.isHierarchic()
				&& configuration.getCurrentPathLength() > 0 && termIsOnPath) {
			while (configuration.removeLastNodeOfCurrentPath() != searchTerm)
				// That's all. We trust that selectedTerm IS on the path.
				;
		}

		Map<String, IFacetTerm> unambigousTerms = getUnambigousQueryTerms();

		for (String unambigousQueryTerm : unambigousTerms.keySet()) {
			IFacetTerm term = unambigousTerms.get(unambigousQueryTerm);

			if (termService.isAncestorOf(parent, term) && term != searchTerm) {
				queryTerms.removeAll(unambigousQueryTerm);
				return;
			}
		}

		Collection<IFacetTerm> parentCollection = new ArrayList<IFacetTerm>();
		parentCollection.add(parent);
		queryTerms.replaceValues(queryTerm, parentCollection);
		searchState.getQueryTermFacetMap().put(parent, configuration);
	}

	public boolean showPathForTerm() {
		Map<Facet, UIFacet> facetConfigurations = uiState
				.getFacetConfigurations();
		IFacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		UIFacet facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null
				&& termService.getPathFromRoot(mappedTerm).length() > 1) {
			return facetConfiguration.isHierarchic();
		} else {
			return false;
		}
	}

	public boolean isFilterTerm() {
		IFacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		// TODO magic number; we really need a separation of normal and
		// "special" facets (is there anything besides filter?)
		if (facet.getId() == 38) {
			this.hasFilter = true;
			return true;
		}
		return false;
	}

	public Collection<IFacetTerm> getMappedTerms() {
		if (queryTerm == null)
			return Collections.EMPTY_LIST;

		// List<List<IMultiHierarchyNode>> = mappedTerm.getFacet().getId()

		List<IFacetTerm> mappedQueryTerms = new ArrayList<IFacetTerm>(
				queryTerms.get(queryTerm));

		return mappedQueryTerms;
	}

	public Multimap<Integer, IFacetTerm> getSortedTerms() {

		Collection<IFacetTerm> mappedQueryTerms = getMappedTerms();

		Multimap<Integer, IFacetTerm> sortedQueryTerms = HashMultimap.create();

		for (IFacetTerm currentTerm : mappedQueryTerms) {
			sortedQueryTerms.put(currentTerm.getFirstFacet().getId(),
					currentTerm);
		}

		return sortedQueryTerms;
	}

	public Object[] getDrillUpContext() {
		return new Object[] { queryTerm, pathItemIndex };
	}

	public String[] getSpellingCorrection() {
		return new String[] { queryTerm, correctedTerm };
	}

	@Log
	public void onConfirmSpellingCorrection(String queryTerm,
			String correctedTerm) throws Exception {
		if (queryTerm == null || correctedTerm == null)
			return;

		queryTerms.removeAll(queryTerm);
		// logger.debug(spellingCorrection);
		Collection<IFacetTerm> correctedTerms = spellingCorrectedQueryTerms
				.get(correctedTerm);
		queryTerms.putAll(correctedTerm, correctedTerms);
	}

	public Index onRemoveTerm(String queryTerm) throws Exception {
		if (queryTerm == null)
			return null;

		queryTerms.removeAll(queryTerm);

		if (queryTerms.size() == 0)
			return index;

		return null;
	}

	public void onEnableReviewFilter() {
		searchState.setReviewsFiltered(true);
	}

	public void onDisableReviewFilter() {
		searchState.setReviewsFiltered(false);
	}

	@Validate("required")
	public SortCriterium getSortCriterium() {
		return searchState.getSortCriterium();
	}

	public void setSortCriterium(SortCriterium sortCriterium) {
		searchState.setSortCriterium(sortCriterium);
	}

	public void onActionFromSortSelection() {

	}

	/**
	 * Used by the template to get the path from a facet root to a particular
	 * query term. The elements are supplied with a link which causes a drill-up
	 * event. Thus, the last element of the path, the query term itself, is not
	 * returned.
	 * 
	 * @return The facet root path of the current term in exclusion of the term
	 *         itself.
	 */
	public IPath getRootPath() {
		// Get the term mapped to the currently referenced query string in the
		// iteration over all query terms.
		IFacetTerm mappedTerm = getMappedTerm();
		IPath rootPath = termService.getPathFromRoot(mappedTerm);
		// Don't return the very last element as all elements returned here get
		// a drillUp-ActionLink. The the name of the term itself is rendered
		// separately.
		return rootPath.subPath(0, rootPath.length() - 1);
	}

	public String onStartNewSearchNode() {
		logger.debug("New search node started. Current serach state:\n{}",
				searchState.toString());
		searchState.createNewSearchNode();
		return "Index";
	}

	@Inject
	private Messages messages;
	
	@SuppressWarnings("unused")
	@Property
	@Persist(PersistenceConstants.FLASH)
	private String searchNodeSubsumedMsg; 
	
	Object onfindIndirectNodeLinks() {
		try {
			bTermView.setSearchNodes(searchState.getSearchNodes());
		} catch (EmptySearchComplementException e) {
			searchNodeSubsumedMsg = messages.get("search_node_subsumed");
			return null;
		}
		return bTermView;
	}

	public String onClearSearchNodes() {
		logger.debug("Clearing search nodes.");
		searchState.clear();
		bTermUIState = null;
		bTermView.reset();
		return "Index";
	}

	@InjectComponent
	private Zone btermPanelZone;

	@Inject
	private Request request;

	@Inject
	@Path("context:images/ico_open.png")
	private Asset icoOpen;

	@Inject
	@Path("context:images/ico_closed.png")
	private Asset icoClosed;

	@Property
	private int searchNodeIndex;

	Object onToggleBTermPanel() {
		showBTermPanel = !showBTermPanel;
		boolean xhr = request.isXHR();
		if (xhr)
			return btermPanelZone.getBody();
		else
			return btermPanelZone;
	}

	public String getToggleBtermPanelMessage() {
		return showBTermPanel ? "hide B-Term panel" : "show B-Term panel";
	}

	public String getCloseOrOpenIco() {
		return showBTermPanel ? icoOpen.toClientURL() : icoClosed.toClientURL();
	}

	public boolean onSwitchToSearchNode(int searchNodeIndex) {
		if (searchState.getActiveSearchNodeIndex() == searchNodeIndex)
			return true;
		searchState.setActiveSearchNodeIndex(searchNodeIndex);
		return false;
	}

	public boolean isBTermAnalysisPossible() {
		return searchState.getSearchNodes().size() > 1
				&& !isSearchResultEmpty();
	}

	public boolean isCurrentSearchNodeBTermAnalysisEligible() {
		return !(isSearchResultEmpty() || isMaxNumberSearchNodesReached());
	}
	
	@Inject
	@Symbol(SemedicoSymbolConstants.MAX_NUMBER_SEARCH_NODES)
	private int maxNumberSearchNodes;

	@InjectPage
	private ResultList resultList;

	@Inject
	private PropertyAccess propertyAccess;

	public boolean isSearchResultEmpty() {
		@SuppressWarnings("unchecked")
		LazyDisplayGroup<DocumentHit> displayGroup = (LazyDisplayGroup<DocumentHit>) propertyAccess
				.get(resultList, "displayGroup");
		return displayGroup.getTotalSize() == 0;
	}

	public boolean isMaxNumberSearchNodesReached() {
		return searchState.getSearchNodes().size() >= maxNumberSearchNodes;
	}

	public String getAddSearchNodeTextClass() {
		if (isSearchResultEmpty() || isMaxNumberSearchNodesReached())
			return "greyedOutText";
		return "";
	}

	public String getBTermLinkTextClass() {
		return isBTermAnalysisPossible() ? "" : "greyedOutText";
	}

	public String getAddSearchNodeTooltipTitle() {
		if (isMaxNumberSearchNodesReached())
			return "Maxmimum number of search nodes has been reached.";
		else if (isSearchResultEmpty())
			return "The current document result list is empty.";
		else
			return "Save the current search and begin a new one.";
	}

	public String getAddSearchNodeTooltipFirstParagraph() {
		if (isMaxNumberSearchNodesReached())
			return "Start a B-Term analysis by hitting the next link or refine a search node by choosing it from below and then making the desired alterations.";
		else if (isSearchResultEmpty())
			return "A search can only be used for B-term analysis when it is non-empty.";
		return "Save the current document search results and begin a new search. Then, your two searches will be eligible for a B-Term analysis.";
	}

	public String getFindBTermsTooltip() {
		if (isSearchResultEmpty())
			return "In order to perform a B-Term analysis you have to specify two non-empty searches (search nodes). Please try another search term.";
		else if (isBTermAnalysisPossible())
			return "Begins an analysis of your current searches (search nodes). Both nodes will be analysed for terms, words and other textual expressions that are shared between them. The result will be an ordered list of terms which connect your chosen search nodes indirectly.";
		return "In order to perform a B-Term analysis you have to specify two non-empty searches (search nodes). Please add another search (link above) to do this.";
	}

	public String getQueryUIString() {
		Multimap<String, IFacetTerm> searchNode = searchState.getSearchNodes()
				.get(searchNodeIndex);
		StringBuilder sb = new StringBuilder(
				"<ul style=\"list-style:none;padding:0px;margin:0px\">");
		for (IFacetTerm term : searchNode.values()) {
			sb.append("<li class=\"list\">");
			sb.append(term.getName());
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

}
