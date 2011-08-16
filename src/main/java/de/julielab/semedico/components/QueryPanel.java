package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.Taxonomy.IPath;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.ITermService;

public class QueryPanel {

	@SessionState
	private SearchSessionState searchSessionState;
	
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
	@Persist
	private String termToDisambiguate;

	@Parameter
	@Property
	private IFacetTerm selectedTerm;

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

	@Inject
	private Logger logger;

	@Inject
	private ITermService termService;
	
	@Property
	private SearchState searchState;

	// Notloesung solange die Facetten nicht gecounted werden; vllt. aber
	// ueberhaupt gar keine so schlechte Idee, wenn dann mal Facetten ohne
	// Treffer angezeigt werden. Dann aber in die Searchconfig einbauen evtl.
	@Property
	@Parameter
	private IFacetTerm noHitTerm;

	private Multimap<String, IFacetTerm> queryTerms; 

	public void setupRender() {
		if (searchSessionState.getSearchState().isNewSearch())
			termToDisambiguate = null;
		searchState = searchSessionState.getSearchState();
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

		Collection<IFacetTerm> terms = searchState.getQueryTerms().get(queryTerm);
		if (terms.size() > 1)
			return true;
		else
			return false;
	}

	@Log
	public boolean isTermSelectedForDisambiguation() {
		return !searchState.isNewSearch() && queryTerm != null && termToDisambiguate != null
				&& queryTerm.equals(termToDisambiguate);
	}

	public void onRefine(String queryTerm) {
		termToDisambiguate = queryTerm;
	}

	public void doQueryChanged(String queryTerm) throws Exception {
		if (queryTerm == null)
			return;

		searchState.getQueryTerms().removeAll(queryTerm);
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
		if (mappedTerm != null)
			return getMappedTermFacet().getCssId() + "ColorA filterBox";
		else
			return null;
	}

	public Facet getMappedTermFacet() {
		IFacetTerm mappedTerm = getMappedTerm();
		return searchState.getQueryTermFacetMap().get(mappedTerm);
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

		Map<Facet, FacetConfiguration> facetConfigurations = searchSessionState.getUiState().getFacetConfigurations();
		IFacetTerm searchTerm = queryTerms.get(queryTerm).iterator().next();

		if (searchTerm == null)
			return;

		IPath pathFromRoot = termService.getPathFromRoot(searchTerm);

		if (pathItemIndex < 0 || pathItemIndex > pathFromRoot.length() - 1)
			return;

		IFacetTerm parent = pathFromRoot.getNodeAt(pathItemIndex);

		FacetConfiguration configuration = facetConfigurations.get(searchTerm
				.getFirstFacet());
		IPath path = configuration.getCurrentPath();
		boolean termIsOnPath = path.containsNode(searchTerm);
		if (configuration.isHierarchicMode() && path.length() > 0
				&& termIsOnPath) {
			while (path.removeLastNode() != searchTerm)
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
	}

	public boolean showPathForTerm() {
		Map<Facet, FacetConfiguration> facetConfigurations = searchSessionState.getUiState().getFacetConfigurations();
		IFacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null
				&& termService.getPathFromRoot(mappedTerm).length() > 1) {
			return facetConfiguration.isHierarchicMode();
		} else {
			return false;
		}
	}

	public boolean isFilterTerm() {
		IFacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		if (facet.getType() == FacetService.FILTER) {
			this.hasFilter = true;
			return true;
		}
		return false;
	}

	public Collection<IFacetTerm> getMappedTerms() {
		if (queryTerm == null)
			return Collections.EMPTY_LIST;

		// List<List<IMultiHierarchyNode>> = mappedTerm.getFacet().getId()

		List<IFacetTerm> mappedQueryTerms = new ArrayList<IFacetTerm>(queryTerms.get(queryTerm));

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

	public void onRemoveTerm(String queryTerm) throws Exception {
		if (queryTerm == null)
			return;

		queryTerms.removeAll(queryTerm);
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
	@Log
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

}
