package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.core.taxonomy.interfaces.IPath;

public class FacetBox extends AbstractFacetBox {

	@Inject
	private ISearchService searchService;
	
	@SessionState
	private SearchState searchState;
	
	public void onTermSelect(String termIndexAndFacetId) {
		super.onTermSelect(termIndexAndFacetId);

		Multimap<String, IFacetTerm> queryTerms = searchState.getQueryTerms();
		Label selectedLabel = searchState.getSelectedTerm();
		if (selectedLabel == null) {
			throw new IllegalStateException(
					"The IFacetTerm object reflecting the newly selected term is null.");
		}
		logger.debug("Name of newly selected label: {} (ID: {})",
				selectedLabel.getName(), selectedLabel.getId());
		// Get the FacetConfiguration associated with the selected term.
		Facet selectedFacet = facetConfiguration;

		IFacetTerm selectedTerm;
		boolean selectedTermIsAlreadyInQuery = false;
		Multimap<String, IFacetTerm> newQueryTerms = HashMultimap.create();

		if (selectedFacet.isHierarchic()) {
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
			selectedTerm = ((TermLabel)selectedLabel).getTerm();
			if (queryTerms.values().contains(selectedTerm))
				selectedTermIsAlreadyInQuery = true;
			else
				queryTerms.put(selectedTerm.getName(), selectedTerm);
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
			Map<Facet, UIFacet> facetConfigurations = uiState
					.getFacetConfigurations();
			UIFacet facetConfiguration = facetConfigurations
					.get(selectedFacet);
//			uiState.createLabelsForFacet(facetConfiguration);
		}
	}
	
	/**
	 * Updates the displayed labels in a facet, must be called e.g. after a
	 * drillUp.
	 */
	@Override
	protected void refreshFacetHit() {
		searchService.doFacetNavigationSearch(facetConfiguration, searchState.getSolrQueryString());
		// First of all: Check whether new terms will show up for which we don't
		// have collected frequency counts yet. If so, get the counts.
//		uiState.createLabelsForFacet(facetConfiguration);
		// sortLabelsIntoDisplayGroup();
	}

	/* (non-Javadoc)
	 * @see de.julielab.semedico.components.AbstractFacetBox#getTermCSSClasses()
	 */
	@Override
	public String getTermCSSClasses() {
		return "";
	}
}
