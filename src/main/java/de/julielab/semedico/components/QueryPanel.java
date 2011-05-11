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
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.SortCriterium;

public class QueryPanel {

	@Property
	@Parameter
	private Multimap<String, FacetTerm> queryTerms;

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

	// Notloesung solange die Facetten nicht gecounted werden; vllt. aber
	// ueberhaupt gar keine so schlechte Idee, wenn dann mal Facetten ohne
	// Treffer angezeigt werden. Dann aber in die Searchconfig einbauen evtl.
	@Property
	@Parameter
	private boolean nothingFound;

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
		Collection<FacetTerm> mappedTerms = queryTerms.get(queryTerm);
		if (mappedTerms.size() > 0)
			return mappedTerms.iterator().next();
		else
			return null;
	}

	public String getMappedTermClass() {
		FacetTerm mappedTerm = getMappedTerm();
		if (mappedTerm != null)
			return mappedTerm.getFacet().getCssId() + "ColorA filterBox";
		else
			return null;
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

		if (pathItemIndex < 0
				|| pathItemIndex > searchTerm.getAllParents().size() - 1)
			return;

		FacetTerm parent = searchTerm.getAllParents().get(pathItemIndex);

		FacetConfiguration configuration = facetConfigurations.get(searchTerm
				.getFacet());
		List<FacetTerm> path = configuration.getCurrentPath();
		if (configuration.isHierarchicMode() && path.size() > 0
				&& searchTerm.isOnPath(path)) {
			path.clear();
			path.addAll(parent.getAllParents());
			path.add(parent);
		}

		Map<String, FacetTerm> unambigousTerms = getUnambigousQueryTerms();

		for (String unambigousQueryTerm : unambigousTerms.keySet()) {
			FacetTerm term = unambigousTerms.get(unambigousQueryTerm);

			if (term.isParentTerm(parent) && term != searchTerm) {
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
		Facet facet = mappedTerm.getFacet();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null)
			return facetConfiguration.isHierarchicMode();
		else
			return false;
	}

	public boolean isFilterTerm() {
		FacetTerm mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFacet();
		if (facet.getType() == Facet.FILTER) {
			this.hasFilter = true;
			return true;
		}
		return false;
	}

	public Collection<FacetTerm> getMappedTerms() {
		if (queryTerm == null)
			return Collections.EMPTY_LIST;

		List<FacetTerm> mappedQueryTerms = new ArrayList<FacetTerm>(
				queryTerms.get(queryTerm));

		return mappedQueryTerms;
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

}
