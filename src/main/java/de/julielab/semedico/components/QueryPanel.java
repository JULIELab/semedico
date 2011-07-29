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
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchyNode;
import de.julielab.semedico.core.MultiHierarchy.IPath;
import de.julielab.semedico.core.services.ITermService;

public class QueryPanel {

	@Property
	@Parameter
	private Multimap<String, IMultiHierarchyNode> queryTerms;
	
	@Property
	@Parameter
	private Map<IMultiHierarchyNode, Facet> queryTermFacetMap;

	@Property
	@Parameter
	private Multimap<String, IMultiHierarchyNode> spellingCorrectedQueryTerms;

	@Parameter
	private SortCriterium sortCriterium;

	@Property
	@Parameter
	private Map<Facet, FacetConfiguration> facetConfigurations;

	@Property
	@Parameter
	private Multimap<String, String> spellingCorrections;

	@Property
	@Parameter
	private boolean reviewsFiltered;

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
	private IMultiHierarchyNode selectedTerm;

	@Property
	private IMultiHierarchyNode pathItem;

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

	// Notloesung solange die Facetten nicht gecounted werden; vllt. aber
	// ueberhaupt gar keine so schlechte Idee, wenn dann mal Facetten ohne
	// Treffer angezeigt werden. Dann aber in die Searchconfig einbauen evtl.
	@Property
	@Parameter
	private IMultiHierarchyNode noHitTerm;
	
	@Parameter
	private boolean newSearch;
	
	public void setupRender() {
		if (newSearch)
			termToDisambiguate = null;
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

		Collection<IMultiHierarchyNode> terms = queryTerms.get(queryTerm);
		if (terms.size() > 1)
			return true;
		else
			return false;
	}

	@Log
	public boolean isTermSelectedForDisambiguation() {
		return !newSearch && queryTerm != null && termToDisambiguate != null
				&& queryTerm.equals(termToDisambiguate);
	}

	public void onRefine(String queryTerm) {
		termToDisambiguate = queryTerm;
	}

	public void doQueryChanged(String queryTerm) throws Exception {
		if (queryTerm == null)
			return;

		queryTerms.removeAll(queryTerm);
	}

	public IMultiHierarchyNode getMappedTerm() {
		// TODO seems a bit arbitrary. Is it possible that there are multiple
		// FacetTerms for queryTerm? Should this be so? Is it an adequate
		// solution to just take the first?
		Collection<IMultiHierarchyNode> mappedTerms = queryTerms.get(queryTerm);
		if (mappedTerms.size() > 0)
			return mappedTerms.iterator().next();
		else
			return null;
	}

	public String getMappedTermClass() {
		IMultiHierarchyNode mappedTerm = getMappedTerm();
		if (mappedTerm != null)
			return getMappedTermFacet().getCssId() + "ColorA filterBox";
		else
			return null;
	}
	
	public Facet getMappedTermFacet() {
		IMultiHierarchyNode mappedTerm = getMappedTerm();
		return queryTermFacetMap.get(mappedTerm);
	}

	private Map<String, IMultiHierarchyNode> getUnambigousQueryTerms() {
		Map<String, IMultiHierarchyNode> unambigousTerms = new HashMap<String, IMultiHierarchyNode>();

		for (String queryTerm : queryTerms.keySet()) {
			Collection<IMultiHierarchyNode> terms = queryTerms.get(queryTerm);
			if (terms.size() == 1)
				unambigousTerms.put(queryTerm, terms.iterator().next());
		}

		return unambigousTerms;
	}

	public void onDrillUp(String queryTerm, int pathItemIndex) throws Exception {

		if (queryTerm == null)
			return;

		IMultiHierarchyNode searchTerm = queryTerms.get(queryTerm).iterator().next();

		if (searchTerm == null)
			return;

		IPath pathFromRoot = termService.getPathFromRoot(searchTerm);

		if (pathItemIndex < 0 || pathItemIndex > pathFromRoot.length() - 1)
			return;

		IMultiHierarchyNode parent = pathFromRoot.getNodeAt(pathItemIndex);

		FacetConfiguration configuration = facetConfigurations.get(searchTerm
				.getFirstFacet());
		IPath path = configuration.getCurrentPath();
		boolean termIsOnPath = path.containsNode(searchTerm);
		if (configuration.isHierarchicMode() && path.length() > 0
				&& termIsOnPath) {
			while (path.removeLastNode() != searchTerm)
				// That's all.
				;
		}

		Map<String, IMultiHierarchyNode> unambigousTerms = getUnambigousQueryTerms();

		for (String unambigousQueryTerm : unambigousTerms.keySet()) {
			IMultiHierarchyNode term = unambigousTerms.get(unambigousQueryTerm);

			if (termService.isAncestorOf(parent, term) && term != searchTerm) {
				queryTerms.removeAll(unambigousQueryTerm);
				return;
			}
		}

		Collection<IMultiHierarchyNode> parentCollection = new ArrayList<IMultiHierarchyNode>();
		parentCollection.add(parent);
		queryTerms.replaceValues(queryTerm, parentCollection);
	}

	public boolean showPathForTerm() {
		IMultiHierarchyNode mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null
				&& termService.getPathFromRoot(mappedTerm).length() > 1) {
			return facetConfiguration.isHierarchicMode();
		}
		else {
			return false;
		}
	}

	public boolean isFilterTerm() {
		IMultiHierarchyNode mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		if (facet.getType() == Facet.FILTER) {
			this.hasFilter = true;
			return true;
		}
		return false;
	}

	public Collection<IMultiHierarchyNode> getMappedTerms() {
		if (queryTerm == null)
			return Collections.EMPTY_LIST;

		// List<List<IMultiHierarchyNode>> = mappedTerm.getFacet().getId()

		List<IMultiHierarchyNode> mappedQueryTerms = new ArrayList<IMultiHierarchyNode>(
				queryTerms.get(queryTerm));

		return mappedQueryTerms;
	}

	public Multimap<Integer, IMultiHierarchyNode> getSortedTerms() {

		Collection<IMultiHierarchyNode> mappedQueryTerms = getMappedTerms();

		Multimap<Integer, IMultiHierarchyNode> sortedQueryTerms = HashMultimap.create();

		for (IMultiHierarchyNode currentTerm : mappedQueryTerms) {
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
		Collection<IMultiHierarchyNode> correctedTerms = spellingCorrectedQueryTerms
				.get(correctedTerm);
		queryTerms.putAll(correctedTerm, correctedTerms);
	}

	public void onRemoveTerm(String queryTerm) throws Exception {
		if (queryTerm == null)
			return;

		queryTerms.removeAll(queryTerm);
	}

	public void onEnableReviewFilter() {
		reviewsFiltered = true;
	}

	public void onDisableReviewFilter() {
		reviewsFiltered = false;
	}

	@Validate("required")
	public SortCriterium getSortCriterium() {
		return sortCriterium;
	}

	public void setSortCriterium(SortCriterium sortCriterium) {
		this.sortCriterium = sortCriterium;
	}

	public void onActionFromSortSelection() {

	}
	
	@Log
	public IPath getRootPath() {
		IMultiHierarchyNode mappedTerm = getMappedTerm();
		IPath rootPath = termService.getPathFromRoot(mappedTerm);
		// Don't return the very last element as all elements returned here get
		// a drillUp-ActionLink. The the name of the term itself is rendered
		// separately.
		return rootPath.subPath(0, rootPath.length() - 1);
	}

}
