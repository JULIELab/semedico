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
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.services.ITermService;

public class QueryPanel {

	@Property
	@Parameter
	private Multimap<String, FacetTerm> queryTerms;
	
	@Property
	@Parameter
	private Map<FacetTerm, Facet> queryTermFacetMap;

	@Property
	@Parameter
	private Multimap<String, FacetTerm> spellingCorrectedQueryTerms;

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

	@Property
	@Persist
	private FacetTerm selectedTerm;

	@Property
	private FacetTerm pathItem;

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
	private FacetTerm noHitTerm;

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

		Collection<FacetTerm> terms = queryTerms.get(queryTerm);
		if (terms.size() > 1)
			return true;
		else
			return false;
	}

	@Log
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

		queryTerms.removeAll(queryTerm);
	}

	public FacetTerm getMappedTerm() {
		// TODO seems a bit arbitrary. Is it possible that there are multiple
		// FacetTerms for queryTerm? Should this be so? Is it an adequate
		// solution to just take the first?
		Collection<FacetTerm> mappedTerms = queryTerms.get(queryTerm);
		if (mappedTerms.size() > 0)
			return mappedTerms.iterator().next();
		else
			return null;
	}

	public String getMappedTermClass() {
		FacetTerm mappedTerm = getMappedTerm();
		if (mappedTerm != null)
			return getMappedTermFacet().getCssId() + "ColorA filterBox";
		else
			return null;
	}
	
	public Facet getMappedTermFacet() {
		FacetTerm mappedTerm = getMappedTerm();
		return queryTermFacetMap.get(mappedTerm);
	}

	private Map<String, FacetTerm> getUnambigousQueryTerms() {
		Map<String, FacetTerm> unambigousTerms = new HashMap<String, FacetTerm>();

		for (String queryTerm : queryTerms.keySet()) {
			Collection<FacetTerm> terms = queryTerms.get(queryTerm);
			if (terms.size() == 1)
				unambigousTerms.put(queryTerm, terms.iterator().next());
		}

		return unambigousTerms;
	}

	public void onDrillUp(String queryTerm, int pathItemIndex) throws Exception {

		if (queryTerm == null)
			return;

		FacetTerm searchTerm = queryTerms.get(queryTerm).iterator().next();

		if (searchTerm == null)
			return;

		List<FacetTerm> pathFromRoot = termService.getPathFromRoot(searchTerm);

		if (pathItemIndex < 0 || pathItemIndex > pathFromRoot.size() - 1)
			return;

		FacetTerm parent = pathFromRoot.get(pathItemIndex);

		FacetConfiguration configuration = facetConfigurations.get(searchTerm
				.getFirstFacet());
		List<FacetTerm> path = configuration.getCurrentPath();
		int termIndexOnPath = path.indexOf(searchTerm);
		if (configuration.isHierarchicMode() && path.size() > 0
				&& termIndexOnPath != -1) {
			for (int i = path.size() - 1; i > termIndexOnPath; --i)
				path.remove(i);
		}

		Map<String, FacetTerm> unambigousTerms = getUnambigousQueryTerms();

		for (String unambigousQueryTerm : unambigousTerms.keySet()) {
			FacetTerm term = unambigousTerms.get(unambigousQueryTerm);

			if (termService.isAncestorOf(parent, term) && term != searchTerm) {
				queryTerms.removeAll(unambigousQueryTerm);
				return;
			}
		}

		Collection<FacetTerm> parentCollection = new ArrayList<FacetTerm>();
		parentCollection.add(parent);
		queryTerms.replaceValues(queryTerm, parentCollection);
	}

	public boolean showPathForTerm() {
		FacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null
				&& termService.getPathFromRoot(mappedTerm).size() > 1)
			return facetConfiguration.isHierarchicMode();
		else
			return false;
	}

	public boolean isFilterTerm() {
		FacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFirstFacet();
		if (facet.getType() == Facet.FILTER) {
			this.hasFilter = true;
			return true;
		}
		return false;
	}

	public Collection<FacetTerm> getMappedTerms() {
		if (queryTerm == null)
			return Collections.EMPTY_LIST;

		// List<List<FacetTerm>> = mappedTerm.getFacet().getId()

		List<FacetTerm> mappedQueryTerms = new ArrayList<FacetTerm>(
				queryTerms.get(queryTerm));

		return mappedQueryTerms;
	}

	public Multimap<Integer, FacetTerm> getSortedTerms() {

		Collection<FacetTerm> mappedQueryTerms = getMappedTerms();

		Multimap<Integer, FacetTerm> sortedQueryTerms = HashMultimap.create();

		for (FacetTerm currentTerm : mappedQueryTerms) {
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
		Collection<FacetTerm> correctedTerms = spellingCorrectedQueryTerms
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

	public List<FacetTerm> getRootPath() {
		FacetTerm mappedTerm = getMappedTerm();
		List<FacetTerm> rootPath = termService.getPathFromRoot(mappedTerm);
		// Don't return the very last element as all elements returned here get
		// a drillUp-ActionLink. The the name of the term itself is rendered
		// seperately.
		return rootPath.subList(0, rootPath.size() - 1);
	}

}
