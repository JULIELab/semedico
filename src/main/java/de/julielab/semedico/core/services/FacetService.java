package de.julielab.semedico.core.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import de.julielab.db.IDBConnectionService;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.util.JavaScriptUtils;

public class FacetService implements IFacetService {

	private static Logger logger = LoggerFactory.getLogger(FacetService.class);
	private final String selectFacets = "select t1.name, t1.css_identifier, t1.facet_id, t1.type, t1.facet_order, t2.name as index "
			+ " from facet t1, index t2 where t1.default_index_id = t2.index_id and t1.hidden = 'false' order by facet_order ";

	private final String selectFacetsWithId = "select t1.name, t1.css_identifier, t1.facet_id, t1.type, t2.name as index "
			+ " from facet t1, index t2 where t1.default_index_id = t2.index_id and facet_id = ";

	private Connection connection;
	private List<Facet> facets;
	private Map<Integer, Facet> facetsById;
	private List<FacetGroup<Facet>> facetGroupsSearch;
	private Map<Integer, FacetGroup<Facet>> facetGroupsByType;
	private Set<Facet> stringTermFacets;
	private ArrayList<FacetGroup<Facet>> facetGroupsBTerms;
	private final ITermService termService;

	// TODO are the connections ever returned to the pool (i.e. closed)??
	public FacetService(IDBConnectionService connectionService,
			ITermService termService) throws SQLException {
		this.termService = termService;
		facetsById = new HashMap<Integer, Facet>();
		facets = new ArrayList<Facet>();
		facetGroupsSearch = new ArrayList<FacetGroup<Facet>>();
		facetGroupsBTerms = new ArrayList<FacetGroup<Facet>>();
		facetGroupsByType = new HashMap<Integer, FacetGroup<Facet>>();
		stringTermFacets = new HashSet<Facet>();
		this.connection = connectionService.getConnection();

		getFacets();
		doConsistencyChecks();
		this.connection.close();
	}

	public List<Facet> getFacets() {
		if (facets != null && facets.size() > 0)
			return facets;

		try {
			ResultSet rs = connection.createStatement().executeQuery(
					selectFacets);

			while (rs.next()) {
				Facet facet = createFacet(rs);

				// if (facet.getId() == KEYWORD_FACET_ID)
				// facetsById.put(facet.getId(), Facet.KEYWORD_FACET);
				// else {
				facets.add(facet);
				facetsById.put(facet.getId(), facet);
				// Concepts have type -1�

				// }

				logger.info(facet + " loaded.");
			}

			Collections.sort(facetGroupsSearch);
			Collections.sort(facetGroupsBTerms);
		} catch (SQLException e) {
			logger.error("SQL exception: ", e);
		}

		return facets;
	}

	public Facet readFacetWithId(Integer id) throws SQLException {

		ResultSet rs = connection.createStatement().executeQuery(
				selectFacetsWithId + id);
		Facet facet = null;
		while (rs.next()) {
			facet = createFacet(rs);
			facets.add(facet);
			facetsById.put(facet.getId(), facet);
		}

		return facet;
	}

	private Facet createFacet(ResultSet rs) throws SQLException {
		int facetType = rs.getInt("type");
		int facetId = rs.getInt("facet_id");
		Set<String> searchFieldNames = new HashSet<String>();
		Set<String> filterFieldNames = new HashSet<String>();
		Facet.SourceType srcType = null;
		String srcName = null;
		boolean isStringTermFacet = false;
		switch (facetType) {
		case BIO_MED:
		case IMMUNOLOGY:
		case AGEING:
			srcType = Facet.SourceType.FIELD_TAXONOMIC_TERMS;
			srcName = IIndexInformationService.FACET_TERMS + facetId;
			Collections.addAll(searchFieldNames,
					IIndexInformationService.TITLE,
					IIndexInformationService.ABSTRACT,
					IIndexInformationService.MESH);
			break;
		case BIBLIOGRAPHY:
			isStringTermFacet = true;
			srcType = Facet.SourceType.FIELD_STRINGS;
			Collections.addAll(searchFieldNames,
					IIndexInformationService.TITLE,
					IIndexInformationService.ABSTRACT);
			if (facetId == 18) {
				srcName = IIndexInformationService.FACET_FIRST_AUTHORS;
				filterFieldNames
						.add(IIndexInformationService.FACET_FIRST_AUTHORS);
				searchFieldNames
						.add(IIndexInformationService.FACET_FIRST_AUTHORS);
			} else if (facetId == 19) {
				srcName = IIndexInformationService.FACET_LAST_AUTHORS;
				filterFieldNames
						.add(IIndexInformationService.FACET_LAST_AUTHORS);
				searchFieldNames
						.add(IIndexInformationService.FACET_LAST_AUTHORS);
			} else if (facetId == 20) {
				srcName = IIndexInformationService.FACET_JOURNALS;
				filterFieldNames.add(IIndexInformationService.FACET_JOURNALS);
				searchFieldNames.add(IIndexInformationService.JOURNAL);
			} else if (facetId == 21) {
				srcName = IIndexInformationService.FACET_YEARS;
				filterFieldNames.add(IIndexInformationService.FACET_YEARS);
			} else if (facetId == 39) {
				srcName = IIndexInformationService.FACET_AUTHORS;
				filterFieldNames.add(IIndexInformationService.FACET_AUTHORS);
				searchFieldNames.add(IIndexInformationService.FACET_AUTHORS);
			}
			break;
		case FILTER:
			srcType = Facet.SourceType.FIELD_STRINGS;
			srcName = IIndexInformationService.FILTER_DOCUMENT_CLASSES;
			filterFieldNames
					.add(IIndexInformationService.FILTER_DOCUMENT_CLASSES);
			break;
		case BTERMS:
			// Not completely adequate; perhaps a source type refinement is
			// required.
			srcType = Facet.SourceType.FIELD_STRINGS;
			// Kind of a hack: These fields will only be used for terms in the
			// B-term facet box which are not already terms which know their
			// search fields, i.e. simple strings. These can be normal words,
			// synonyms, hypernyms or specialist lexicon entries. Since all
			// these will always come from title and text, we just use these
			// fields here.
			Collections.addAll(filterFieldNames,
					IIndexInformationService.ABSTRACT,
					IIndexInformationService.TITLE);
		}
		if (facetId == FACET_ID_CONCEPTS)
			srcType = Facet.SourceType.FIELD_FLAT_TERMS;

		Facet.Source facetSource = new Facet.Source(srcType, srcName);
		Facet facet = new Facet(facetId, rs.getString("name"),
				searchFieldNames, filterFieldNames, rs.getInt("facet_order"),
				rs.getString("css_identifier"), facetSource);

		if (isStringTermFacet)
			stringTermFacets.add(facet);

		if (facetType >= 0) {
			FacetGroup<Facet> group = facetGroupsByType.get(facetType);
			boolean showForSearch = false;
			boolean showForBTerms = false;
			int position = facetType;
			if (group == null) {
				String name = "";
				if (facetType == BIO_MED) {
					name = "BioMed";
					showForSearch = true;
					showForBTerms = true;
				} else if (facetType == IMMUNOLOGY) {
					name = "Immunology";
					showForSearch = true;
					showForBTerms = true;
				} else if (facetType == AGEING) {
					name = "Ageing";
					showForSearch = true;
					showForBTerms = true;
				} else if (facetType == BIBLIOGRAPHY) {
					name = "Bibliography";
					showForSearch = true;
				} else if (facetType == FILTER) {
					name = "Filter";
					showForSearch = true;
				} else if (facetType == BTERMS) {
					name = "B-Terms";
					showForBTerms = true;
					position = -1;
				}
				group = new FacetGroup<Facet>(name, position, showForBTerms);
				facetGroupsByType.put(facetType, group);
				if (showForSearch)
					facetGroupsSearch.add(group);
				if (showForBTerms)
					facetGroupsBTerms.add(group);
			}
			group.add(facet);
		}

		return facet;
	}

	@Override
	public Facet getFacetByName(String facetName) {
		if (facets == null || facets.size() == 0)
			getFacets();

		for (Facet facet : facets)
			if (facet.getName().equals(facetName))
				return facet;

		return null;
	}

	public Facet getFacetById(Integer id) {
		Facet facet = facetsById.get(id);
		if (facet == null) {
			try {
				facet = readFacetWithId(id);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
		return facet;
	}

	public Facet getFacetByIndexFieldName(String indexName) {
		for (Facet facet : facetsById.values()) {
			if (facet.getSearchFieldNames() != null
					&& facet.getSearchFieldNames().size() > 0) {
				for (String field : facet.getSearchFieldNames()) {
					if (field.equals(indexName))
						return facet;
				}
			}
			if (facet.getFilterFieldNames() != null
					&& facet.getFilterFieldNames().size() > 0) {
				for (String field : facet.getFilterFieldNames()) {
					if (field.equals(indexName))
						return facet;
				}
			}
		}
		return null;
	}

	// public List<Facet> getFacetsWithType(int type) {
	// List<Facet> facets = getFacets();
	// List<Facet> facetsWithType = new ArrayList<Facet>();
	//
	// for (Facet facet : facets)
	// if (facet.getType() == type && !facetsWithType.contains(facet))
	// facetsWithType.add(facet);
	//
	// return facetsWithType;
	// }

	public Facet getKeywordFacet() {
		return Facet.KEYWORD_FACET;
	}

	@Override
	public List<FacetGroup<Facet>> getFacetGroupsSearch() {
		return facetGroupsSearch;
	}

	@Override
	public List<FacetGroup<Facet>> getFacetGroupsBTerms() {
		return facetGroupsBTerms;
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
					"Consistency checks must be made AFTER the facets have been initialized.");

		boolean error = false;

		// Check for duplicates in the CSS-IDs. This is not only a design
		// problem since the CSS-IDs are also used as JavaScript object name for
		// the corresponding FacetBoxes (Ajax will break if we have multiple
		// boxes with the same ID).
		Set<String> cssIds = new HashSet<String>(facets.size());
		for (Facet facet : facets) {
			if (cssIds.contains(facet.getCssId())) {
				logger.error(
						"The facet CSS-ID '{}' has been assigned multiple times. It should be unique.",
						facet.getCssId());
				error = true;
			}
			cssIds.add(facet.getCssId());
		}

		// Check whether the CSS-IDs are also valid JavaScript identifiers
		// (because, as explained above, the IDs are used as JavaScript variable
		// names).
		for (String cssId : cssIds) {
			boolean isJSIdentifier = JavaScriptUtils
					.isJavascriptIdentifier(cssId);
			if (!isJSIdentifier) {
				logger.error(
						"The CSS-ID '{}' is no valid JavaScript identifier. The facet's CSS-IDs"
								+ " are required to be valid JavaScript identifiers since the IDs are used"
								+ " for both CSS and for JavaScript variable names.",
						cssId);
				error = true;
			}
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
		return facetsById.get(FACET_ID_AUTHORS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IFacetService#getFirstAuthorFacet()
	 */
	@Override
	public Facet getFirstAuthorFacet() {
		return facetsById.get(FACET_ID_FIRST_AUTHORS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IFacetService#getLastAuthorFacet()
	 */
	@Override
	public Facet getLastAuthorFacet() {
		return facetsById.get(FACET_ID_LAST_AUTHORS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IFacetService#isAnyAuthorFacetId(java
	 * .lang.Integer)
	 */
	@Override
	public boolean isAnyAuthorFacetId(Integer id) {
		return id == FACET_ID_AUTHORS || id == FACET_ID_FIRST_AUTHORS
				|| id == FACET_ID_LAST_AUTHORS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IFacetService#isAnyAuthorFacet(de.
	 * julielab.semedico.core.Facet)
	 */
	@Override
	public boolean isAnyAuthorFacet(Facet facet) {
		return isAnyAuthorFacetId(facet.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.interfaces.IFacetService#isBTermFacet
	 * (de.julielab.semedico.core.Facet)
	 */
	@Override
	public boolean isBTermFacet(Facet facet) {
		return facet.getId() == FACET_ID_BTERMS;
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
		Collection<Facet> hierarchicalFacets = Collections2.filter(facets,
				new Predicate<Facet>() {

					@Override
					public boolean apply(Facet input) {
						return input.getSource().isTermSource();
					}

				});
		return hierarchicalFacets;
	}
}