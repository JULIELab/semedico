package de.julielab.semedico.components;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.services.Request;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.pages.ResultList;
import de.julielab.util.LazyDisplayGroup;

public class BTermPanel {
	@SuppressWarnings("unused")
	@Property
	@SessionState(create = false)
	private BTermUserInterfaceState bTermUIState;
	
	@SuppressWarnings("unused")
	@Property
	@Persist(PersistenceConstants.FLASH)
	private String searchNodeSubsumedMsg;
	
	@Property
	@Persist
	private boolean showBTermPanel;
	
	@SessionState
	@Property
	private SearchState searchState;
	
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

	// We'll probably need to move this logic. Would the JS be an okayish place?
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
