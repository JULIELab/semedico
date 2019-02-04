package de.julielab.semedico.core.services;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.commons.concepts.FacetLabels.General;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.services.interfaces.IConceptDatabaseService;
import de.julielab.semedico.core.util.JavaScriptUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class FacetNeo4jService extends CoreFacetService {

	private Map<FacetLabels.Unique, Facet> facetsByLabel;
	private Set<Facet> stringTermFacets;
	private final IConceptDatabaseService neo4jService;


	private Boolean getHollowfacets;
	private boolean facetsFromNeo4jHaveBeenLoaded;

	public FacetNeo4jService(Logger log, @Symbol(SemedicoSymbolConstants.FACETS_LOAD_AT_START) Boolean loadFacets,
			@Symbol(SemedicoSymbolConstants.GET_HOLLOW_FACETS) Boolean getHollowfacets,
			IConceptDatabaseService neo4jService) {
		super(log);
		this.getHollowfacets = getHollowfacets;
		this.neo4jService = neo4jService;
		facetsByLabel = new HashMap<>();
		stringTermFacets = new HashSet<Facet>();

		if (loadFacets) {
			getFacets();
			doConsistencyChecks();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Facet> getFacets() {
		if (facetsFromNeo4jHaveBeenLoaded)
			return facets;

        List<FacetGroup<Facet>> facetGroups = neo4jService.getFacetGroups(getHollowfacets).collect(toList());
			// we currently just don't show facets
//        facetGroupsSearch = facetGroups.stream().filter(fg -> fg.getLabels().contains(FacetGroupLabels.General.SHOW_FOR_SEARCH)).sorted().collect(toList());
  //      facetGroupsBTerms = facetGroups.stream().filter(fg -> fg.getLabels().contains(FacetGroupLabels.General.SHOW_FOR_BTERMS)).sorted().collect(toList());
        facetsByLabel = new HashMap<>();
        facetGroups.stream().flatMap(FacetGroup::stream).filter(f -> f.getUniqueLabels() != null).forEach(f -> f.getUniqueLabels().forEach(l -> facetsByLabel.put(l, f)));
        facetsById = facetGroups.stream().flatMap(FacetGroup::stream).collect(Collectors.toMap(Facet::getId, Function.identity()));
        facetsByLabel.put(FacetLabels.Unique.KEYWORDS, Facet.KEYWORD_FACET);

        if (log.isInfoEnabled())
            facetsById.values().forEach(f -> log.debug("Facet loaded: {}", f));
        facetsFromNeo4jHaveBeenLoaded = true;

		return this.facets;
	}

	@Override
	public Facet getFacetByIndexFieldName(String indexName) {
		for (Facet facet : facetsByLabel.values()) {
			if (facet.getSearchFieldNames() != null && facet.getSearchFieldNames().size() > 0) {
				for (String field : facet.getSearchFieldNames()) {
					if (field.equals(indexName))
						return facet;
				}
			}
			if (facet.getFilterFieldNames() != null && facet.getFilterFieldNames().size() > 0) {
				for (String field : facet.getFilterFieldNames()) {
					if (field.equals(indexName))
						return facet;
				}
			}
		}
		return null;
	}

	public Facet getKeywordFacet() {
		return Facet.KEYWORD_FACET;
	}

	public Set<Facet> getStringTermFacets() {
		return stringTermFacets;
	}

	/**
	 * 
	 */
	private void doConsistencyChecks() {
		if (facets == null || facets.size() == 0)
			throw new IllegalStateException(
					"Consistency checks must be made AFTER the facets have been initialized. However, there are no facets available. Perhaps there was a loading problem?");

		boolean error = false;

		// Check for duplicates in the CSS-IDs. This is not only a design
		// problem since the CSS-IDs are also used as JavaScript object name for
		// the corresponding FacetBoxes (Ajax will break if we have multiple
		// boxes with the same ID).
		Set<String> cssIds = new HashSet<String>(facets.size());
		for (Facet facet : facets) {
			if (cssIds.contains(facet.getCssId())) {
				log.error(
						"The facet CSS-ID '{}' has been assigned multiple times. It must be unique since it serves as JavaScript variable name.",
						facet.getCssId());
				error = true;
			}
			cssIds.add(facet.getCssId());
		}

		// Check whether the CSS-IDs are also valid JavaScript identifiers
		// (because, as explained above, the IDs are used as JavaScript variable
		// names).
		for (String cssId : cssIds) {
			boolean isJSIdentifier = JavaScriptUtils.isJavascriptIdentifier(cssId);
			if (!isJSIdentifier) {
				log.error("The CSS-ID '{}' is no valid JavaScript identifier. The facet's CSS-IDs"
						+ " are required to be valid JavaScript identifiers since the IDs are used"
						+ " for both CSS and for JavaScript variable names.", cssId);
				error = true;
			}
		}

		if (null == facetGroupsSearch || 0 == facetGroupsSearch.size()) {
			log.error(
					"There is no facet group to be included for the search-view on the frontend. Thus, the user wouldn't see any facets when searching.");
			// error = true;
		}

//		if (null == facetGroupsBTerms || 0 == facetGroupsBTerms.size()) {
//			log.error(
//					"There is no facet group to be included for the BTerm-view on the frontend. Thus, the user wouldn't see any facets when exploring indirect links.");
//			// error = true;
//		}

		if (error)
			throw new IllegalStateException(
					"There were problems while loading the facets. See the logs for more information.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IFacetService#getAuthorFacet()
	 */
	@Override
	public Facet getAuthorFacet() {
		return facetsByLabel.get(FacetLabels.Unique.AUTHORS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IFacetService#getFirstAuthorFacet()
	 */
	@Override
	public Facet getFirstAuthorFacet() {
		return facetsByLabel.get(FacetLabels.Unique.FIRST_AUTHORS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IFacetService#getLastAuthorFacet()
	 */
	@Override
	public Facet getLastAuthorFacet() {
		return facetsByLabel.get(FacetLabels.Unique.LAST_AUTHORS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IFacetService#
	 * isTotalFacetCountField(java.lang.String)
	 */
	@Override
	public boolean isTotalFacetCountField(String facetFieldName) {
//		return facetFieldName.equals(IIndexInformationService.FACETS);
		// TODO
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IFacetService#
	 * getHierarchicalFacets()
	 */
	@Override
	public Collection<Facet> getTermSourceFacets() {
		Collection<Facet> hierarchicalFacets = Collections2.filter(facets, new Predicate<Facet>() {

			@Override
			public boolean apply(Facet input) {
				boolean isTermSource;
				try {
					isTermSource = input.getSource().isDatabaseTermSource();
				} catch (NullPointerException e) {
					log.error("Error when trying to determine type of facetSource of facet {}.", input.getName());
					throw e;
				}
				return isTermSource;

			}

		});
		return hierarchicalFacets;
	}

	@Override
	public List<Facet> getSuggestionFacets() {
		// TODO remove the label in the database
//		List<String> suggestionFacetIds = neo4jService
//				.getFacetIdsWithGeneralLabel(FacetLabels.General.USE_FOR_SUGGESTIONS);
//		List<Facet> suggestionFacets = new ArrayList<>(suggestionFacetIds.size());
//		for (String facetId : suggestionFacetIds) {
//			suggestionFacets.add(facetsById.get(facetId));
//		}
//
//		return suggestionFacets;
		return Arrays.asList(Facet.MOST_FREQUENT_CONCEPTS_FACET);
	}

	@Override
	public Facet getFacetByLabel(FacetLabels.Unique label) {
		return facetsByLabel.get(label);
	}

	@Override
	public List<Facet> getFacetsByLabel(General label) {
		List<Facet> ret = new ArrayList<>();
		for (Facet facet : facets) {
			if (facet.hasGeneralLabel(label))
				ret.add(facet);
		}
		return ret;
	}

	@Override
	public List<Facet> getFacetsByLabels(Set<General> labels) {
		log.debug("Returning facets with labels {}", labels);
		List<Facet> ret = new ArrayList<>();
		for (Facet facet : facets) {
			if (facet.getLabels() == null)
				log.debug("Facet {} does not have any general labels.", facet);
			for (General label : facet.getLabels()) {
				if (labels.contains(label))
					ret.add(facet);
			}
		}
		return ret;
	}

	@Override
	public Facet getInducedFacet(String termId, General facetLabel) {
		List<Facet> facets;
		if (null != facetLabel) {
			facets = getFacetsByLabel(facetLabel);
		} else
			facets = getFacets();
		for (Facet facet : facets) {
			String inducingTermId = facet.getInducingTermId();
			if (null != inducingTermId && inducingTermId.equals(termId))
				return facet;
		}
		return null;
	}
}