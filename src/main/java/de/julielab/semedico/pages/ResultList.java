package de.julielab.semedico.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.components.FacetedSearchLayout;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.core.taxonomy.interfaces.IPath;
import de.julielab.semedico.search.components.SemedicoSearchResult;
import de.julielab.util.LazyDisplayGroup;

public class ResultList {

	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;

	@InjectPage
	private Index index;

	@InjectComponent("FacetedSearchLayout")
	private FacetedSearchLayout searchLayout;

	@SessionState(create = false)
	@Property
	private SearchState searchState;

	// Only used to be passed to the FacetedSearchLayout component.
	@SuppressWarnings("unused")
	@SessionState
	@Property
	private UserInterfaceState uiState;

	@Inject
	private ISearchService searchService;

	@Property
	@Persist
	private LazyDisplayGroup<DocumentHit> displayGroup;

	@SuppressWarnings("unused")
	@Property
	@Persist
	// Used for display only.
	private long elapsedTime;

	@Inject
	private ITermService termService;

	/**
	 * <p>
	 * Event handler which is executed before beginning page rendering.
	 * </p>
	 * <p>
	 * The main page will check whether there is a search whose search results
	 * could be displayed. If not, the user is redirected to the Index page.
	 * </p>
	 * 
	 * @return The Index page if there is no search to display. Otherwise, null
	 *         will be returned to signal the page rendering.
	 * @see http://tapestry.apache.org/page-navigation.html
	 */
	public Object onActivate() {
		if (searchState == null)
			return index;
		return null;
	}

	@OnEvent(value = "switchToSearchNode")
	public Object onActionFromQueryPanel() throws IOException {
		// FacetedSearchResult searchResult = searchService.search(searchState
		// .getQueryTerms(), IFacetedSearchService.DO_FACET);
		// setSearchResult(searchResult);
		// if (true)
		// throw new NotImplementedException();
		SemedicoSearchResult searchResult = searchService
				.doSearchNodeSwitchSearch(searchState.getSolrQueryString(),
						searchState.getQueryTerms());
		setSearchResult(searchResult);
		return this;
	}

	public ResultList onDisambiguateTerm() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onRemoveTerm() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onDrillUp() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onDisableReviewFilter() throws IOException {
		return searchLayout.performSubSearch();
	}

	public ResultList onEnableReviewFilter() throws IOException {
		return searchLayout.performSubSearch();
	}

	/**
	 * @param result
	 */
	public void setSearchResult(SemedicoSearchResult searchResult) {

		elapsedTime = searchResult.elapsedTime;
		displayGroup = searchResult.documentHits;

//		collapseAllFacets();
		
		// expand menu were query was found and collapse all other
		// for(FacetGroup<UIFacet> f : uiState.getFacetGroups()){
		// for(UIFacet a:f){
		// a.setCollapsed(true);
		// }
		// }

		// for(IFacetTerm term : searchState.getQueryTerms().values()){
		// IPath currentPath = termService.getPathFromRoot(term);
		//
		// for(Facet facet : term.getFacets()){
		//
		// if(facet.isHierarchic()){
		// uiState.getFacetConfigurations().get(facet).setCurrentPath(currentPath.copyPath());
		//
		// for(FacetGroup<UIFacet> f : uiState.getFacetGroups()){
		//
		// if(f.contains(facet)){
		//
		// for(UIFacet a:f){
		// if(a.equals(facet)){
		//
		// }
		// else{
		// a.setCollapsed(true);
		// }
		// }
		// }
		// }
		// }
		// }
		// }
	}

	// /**
	// * @param documentHits
	// * @param elapsedTime
	// */
	// public void setDocumentHits(LazyDisplayGroup<DocumentHit> documentHits,
	// long elapsedTime) {
	// displayGroup = documentHits;
	// this.elapsedTime = elapsedTime;
	// }
	
	/*
	 * expand menu were query was found and collapse all other
	 */
	private void collapseAllFacets(){
		for(FacetGroup<UIFacet> f : uiState.getFacetGroups()){
						
			for(UIFacet a : f){
				a.setCollapsed(true);
			}
			for(UIFacet b : getTermList()){
				if(f.contains(b)){
					expandQueryTerms(f, b);
				}
			}
		}
	}
	
	/*
	 * will expand the facet if it contains a query term
	 */
	private void expandQueryTerms(FacetGroup<UIFacet> group, UIFacet uifacet) {
		for (IFacetTerm term : searchState.getQueryTerms().values()) {
			IPath currentPath = termService.getPathFromRoot(term);

			if (uifacet.isHierarchic()) {
				uifacet.setCurrentPath(currentPath.copyPath());
				uifacet.setCollapsed(false);
				uiState.setFirstFacet(group, uifacet);
			}
		}
	}

	/*
	 * returns a list of all query terms
	 */
	private List<UIFacet> getTermList() {
		List<UIFacet> queryterms = new ArrayList<UIFacet>();
		for (IFacetTerm term : searchState.getQueryTerms().values()) {

			for (Facet facet : term.getFacets()) {
				queryterms.add(uiState.getFacetConfigurations().get(facet));
			}
		}
		return queryterms;
	}

}
