package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.constants.semedico.FacetGroupConstants;
import de.julielab.semedico.core.facets.BioPortalFacet;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.Facet.Source;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.facets.FacetGroupLabels;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.facets.FacetProperties.BioPortal;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.JSON;
import de.julielab.semedico.core.util.JavaScriptUtils;

public class FacetNeo4jService extends CoreFacetService {

	private Map<FacetLabels.Unique, Facet> facetsByLabel;
	private Set<Facet> stringTermFacets;
	private final ITermDatabaseService neo4jService;

	private ITermService termService;

	private Boolean getHollowfacets;
	private boolean facetsFromNeo4jHaveBeenLoaded;

	public FacetNeo4jService(Logger log, @Symbol(SemedicoSymbolConstants.FACETS_LOAD_AT_START) Boolean loadFacets,
			@Symbol(SemedicoSymbolConstants.GET_HOLLOW_FACETS) Boolean getHollowfacets,
			ITermDatabaseService neo4jService, ITermService termService) {
		super(log);
		this.getHollowfacets = getHollowfacets;
		this.neo4jService = neo4jService;
		this.termService = termService;
		facetsByLabel = new HashMap<>();
		stringTermFacets = new HashSet<>();

		if (loadFacets) {
			getFacets();
			doConsistencyChecks();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Facet> getFacets() {
		if (facetsFromNeo4jHaveBeenLoaded)
			return facets;
		facetsFromNeo4jHaveBeenLoaded = true;

		JSONArray jsonFacetGroups = neo4jService.getFacets(getHollowfacets);
		for (int i = 0; null != jsonFacetGroups && i < jsonFacetGroups.length(); i++) {
			JSONObject jsonFacetGroup = jsonFacetGroups.getJSONObject(i);
			String facetGroupName = jsonFacetGroup.getString(FacetGroupConstants.PROP_NAME);
			FacetGroupLabels.Type facetGroupType = null;
			if (jsonFacetGroup.has(FacetGroupConstants.PROP_TYPE))
				facetGroupType = FacetGroupLabels.Type.valueOf(jsonFacetGroup.getString(FacetGroupConstants.PROP_TYPE));
			Integer facetGroupPosition = jsonFacetGroup.getInt(FacetGroupConstants.PROP_POSITION);

			FacetGroup<Facet> facetGroup = new FacetGroup<>(facetGroupName, facetGroupPosition);
			facetGroup.setType(facetGroupType);

			JSONArray jsonFacets = jsonFacetGroup.getJSONArray("facets");
			for (int j = 0; j < jsonFacets.length(); j++) {
				JSONObject jsonFacet = jsonFacets.getJSONObject(j);
				String id = jsonFacet.getString(FacetConstants.PROP_ID);
				String name = jsonFacet.getString(FacetConstants.PROP_NAME);
				String shortName = JSON.getString(jsonFacet, FacetConstants.PROP_SHORT_NAME);
				String cssId = jsonFacet.getString(FacetConstants.PROP_CSS_ID);
				int position = jsonFacet.getInt(FacetConstants.PROP_POSITION);
				Integer numRootTerms = JSON.getInt(jsonFacet, FacetConstants.PROP_NUM_ROOT_TERMS);
				List<String> searchFieldNames = JSON.jsonArrayProperty2List(jsonFacet,
						FacetConstants.PROP_SEARCH_FIELD_NAMES);
				List<String> filterFieldNames = JSON.jsonArrayProperty2List(jsonFacet,
						FacetConstants.PROP_FILTER_FIELD_NAMES);
				String sourceName = jsonFacet.getString(FacetConstants.PROP_SOURCE_NAME);
				String sourceTypeString = jsonFacet.getString(FacetConstants.PROP_SOURCE_TYPE);
				List<String> facetAggregationLabels = JSON.jsonArrayProperty2List(jsonFacet,
						FacetConstants.AGGREGATION_LABELS, Collections.<String> emptyList());
				List<String> facetAggregationFields = JSON.jsonArrayProperty2List(jsonFacet,
						FacetConstants.PROP_AGGREGATION_FIELDS, Collections.<String> emptyList());
				List<String> labelStrings = JSON.jsonArrayProperty2List(jsonFacet, FacetConstants.KEY_LABELS,
						Collections.<String> emptyList());
				String inducingTermId = JSON.getString(jsonFacet, FacetConstants.PROP_INDUCING_TERM);
				Boolean active = JSON.getBoolean(jsonFacet, FacetConstants.PROP_ACTIVE);

				if (null != active && !active) {
					continue;
				}

				Set<FacetLabels.Unique> uniqueLabels = FacetLabels.uniqueStringLabels2EnumLabels(labelStrings,
						HashSet.class);
				Set<FacetLabels.General> generalLabels = FacetLabels.generalStringLabels2EnumLabels(labelStrings,
						HashSet.class);
				Set<FacetLabels.General> aggregationLabels = FacetLabels
						.generalStringLabels2EnumLabels(facetAggregationLabels, HashSet.class);

				Facet.SourceType sourceType;
				switch (sourceTypeString) {
				case FacetConstants.SRC_TYPE_FLAT:
					sourceType = Facet.SourceType.FIELD_FLAT_TERMS;
					break;
				case FacetConstants.SRC_TYPE_HIERARCHICAL:
					sourceType = Facet.SourceType.FIELD_TAXONOMIC_TERMS;
					break;
				case FacetConstants.SRC_TYPE_FACET_AGGREGATION:
					sourceType = Facet.SourceType.FACET_AGGREGATION;
					if (facetAggregationLabels.isEmpty() && facetAggregationFields.isEmpty())
						throw new IllegalStateException("Facet with ID " + id + " has source type " + sourceTypeString
								+ " but does not define labels to identify facets to be part of the aggregation or aggregation fields.");
					break;
				default:
					throw new IllegalArgumentException("Unknown facet source type \"" + sourceTypeString + "\".");
				}

				Source facetSource = new Facet.Source(sourceType, sourceName);

				Facet facet;
				if (generalLabels.contains(FacetLabels.General.FACET_BIO_PORTAL)) {
					String acronym = jsonFacet.getString(BioPortal.acronym);
					String iri = jsonFacet.getString(BioPortal.IRI);
					facet = new BioPortalFacet(id, name, searchFieldNames, filterFieldNames, generalLabels,
							uniqueLabels, position, cssId, facetSource, termService, acronym, iri);
				} else {
					facet = new Facet(id, name, searchFieldNames, filterFieldNames, generalLabels, uniqueLabels,
							position, cssId, facetSource, termService);
				}
				facet.setAggregationLabels(aggregationLabels);
				facet.setAggregationFields(facetAggregationFields);
				facet.setInducingTermId(inducingTermId);
				facet.setShortName(shortName);

				if (null != numRootTerms) {
					facet.setNumRoots(numRootTerms);
				}

				for (FacetLabels.Unique label : uniqueLabels) {
					facetsByLabel.put(label, facet);
				}

				facetGroup.add(facet);
				facets.add(facet);
				facetsById.put(facet.getId(), facet);
				log.info("{} loaded.", facet);
			}

		}
		facetsByLabel.put(FacetLabels.Unique.KEYWORDS, Facet.KEYWORD_FACET);
		Collections.sort(facetGroupsSearch);
		for (FacetGroup<Facet> fg : facetGroupsSearch) {
			Collections.sort(fg);
		}
		
		return facets;
	}

	@Override
	public Facet getFacetByIndexFieldName(String indexName) {
		for (Facet facet : facetsByLabel.values()) {
			if (facet.getSearchFieldNames() != null && !facet.getSearchFieldNames().isEmpty()) {
				for (String field : facet.getSearchFieldNames()) {
					if (field.equals(indexName))
						return facet;
				}
			}
			if (facet.getFilterFieldNames() != null && !facet.getFilterFieldNames().isEmpty()) {
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
		if (facets == null || facets.isEmpty()) {
			throw new IllegalStateException(
					"Consistency checks must be made AFTER the facets have been initialized. However, there are no facets available. Perhaps there was a loading problem?");
		}
		boolean error = false;

		// Check for duplicates in the CSS-IDs. This is not only a design
		// problem since the CSS-IDs are also used as JavaScript object name for
		// the corresponding FacetBoxes (Ajax will break if we have multiple
		// boxes with the same ID).
		Set<String> cssIds = new HashSet<>(facets.size());
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

		if (null == facetGroupsSearch || facetGroupsSearch.isEmpty()) {
			log.error(
					"There is no facet group to be included for the search-view on the frontend. Thus, the user wouldn't see any facets when searching.");
		}

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
		return facetFieldName.equals(IIndexInformationService.FACETS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IFacetService#
	 * getHierarchicalFacets()
	 */
	@Override
	public Collection<Facet> getTermSourceFacets() {
		return Collections2.filter(facets, new Predicate<Facet>() {

			@Override
			public boolean apply(Facet input) {
				boolean isTermSource;
				try {
					isTermSource = input.getSource().isDatabaseTermSource();
				} catch (NullPointerException e) {
					log.error("Error when trying to determine type of source of facet {}.", input.getName());
					throw e;
				}
				return isTermSource;

			}

		});
	}

	@Override
	public List<Facet> getSuggestionFacets() {
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
			if (facet.getGeneralLabels() == null)
				log.debug("Facet {} does not have any general labels.", facet);
			for (General label : facet.getGeneralLabels()) {
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
		} else {
			facets = getFacets();
		}
		for (Facet facet : facets) {
			String inducingTermId = facet.getInducingTermId();
			if (null != inducingTermId && inducingTermId.equals(termId))
				return facet;
		}
		return null;
	}
}