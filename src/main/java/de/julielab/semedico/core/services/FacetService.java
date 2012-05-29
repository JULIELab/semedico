package de.julielab.semedico.core.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.db.IDBConnectionService;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.util.JavaScriptUtils;

public class FacetService implements IFacetService {

	private static Logger logger = LoggerFactory.getLogger(FacetService.class);
	private final String selectFacets = "select t1.name, t1.css_identifier, t1.facet_id, t1.type, t1.facet_order, t2.name as index "
			+ " from facet t1, index t2 where t1.default_index_id = t2.index_id and t1.hidden = 'false' order by facet_order ";

	private final String selectFacetsWithId = "select t1.name, t1.css_identifier, t1.facet_id, t1.type, t2.name as index "
			+ " from facet t1, index t2 where t1.default_index_id = t2.index_id and facet_id = ";

	private final int KEYWORD_FACET_ID = 0;
	private Connection connection;
	private List<Facet> facets;
	private Map<Integer, Facet> facetsById;
	private List<FacetGroup<Facet>> facetGroups;
	private Map<Integer, FacetGroup<Facet>> facetGroupsByType;
	private Set<Facet> stringTermFacets;

	// TODO are the connections ever returned to the pool (i.e. closed)??
	public FacetService(IDBConnectionService connectionService)
			throws SQLException {
		facetsById = new HashMap<Integer, Facet>();
		facets = new ArrayList<Facet>();
		facetGroups = new ArrayList<FacetGroup<Facet>>();
		facetGroupsByType = new HashMap<Integer, FacetGroup<Facet>>();
		stringTermFacets = new HashSet<Facet>();
		this.connection = connectionService.getConnection();

		getFacets();
		doConsistencyChecks();
	}

	public List<Facet> getFacets() {
		if (facets != null && facets.size() > 0)
			return facets;

		try {
			ResultSet rs = connection.createStatement().executeQuery(
					selectFacets);

			while (rs.next()) {
				Facet facet = createFacet(rs);

				if (facet.getId() == KEYWORD_FACET_ID)
					facetsById.put(facet.getId(), Facet.KEYWORD_FACET);
				else {
					facets.add(facet);
					facetsById.put(facet.getId(), facet);
					// Concepts have type -1â

				}

				logger.info(facet + " loaded.");
			}

			facetGroups.addAll(facetGroupsByType.values());
			Collections.sort(facetGroups);
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
			srcName = IndexFieldNames.FACET_TERMS + facetId;
			Collections.addAll(searchFieldNames, IndexFieldNames.TITLE,
					IndexFieldNames.ABSTRACT, IndexFieldNames.MESH);
			break;
		case BIBLIOGRAPHY:
			isStringTermFacet = true;
			srcType = Facet.SourceType.FIELD_STRINGS;
			Collections.addAll(searchFieldNames, IndexFieldNames.TITLE,
					IndexFieldNames.ABSTRACT);
			if (facetId == 18) {
				srcName = IndexFieldNames.FACET_FIRST_AUTHORS;
				searchFieldNames.add(IndexFieldNames.FACET_FIRST_AUTHORS);
			} else if (facetId == 19) {
				srcName = IndexFieldNames.FACET_LAST_AUTHORS;
				searchFieldNames.add(IndexFieldNames.FACET_LAST_AUTHORS);
			} else if (facetId == 20) {
				srcName = IndexFieldNames.FACET_JOURNALS;
				filterFieldNames.add(IndexFieldNames.FACET_JOURNALS);
				searchFieldNames.add(IndexFieldNames.JOURNAL);
			} else if (facetId == 21) {
				srcName = IndexFieldNames.FACET_YEARS;
				filterFieldNames.add(IndexFieldNames.FACET_YEARS);
			} else if (facetId == 39) {
				srcName = IndexFieldNames.FACET_AUTHORS;
				searchFieldNames.add(IndexFieldNames.FACET_AUTHORS);
			}
			break;
		case FILTER:
			srcType = Facet.SourceType.FIELD_STRINGS;
			srcName = IndexFieldNames.FILTER_DOCUMENT_CLASSES;
			filterFieldNames.add(IndexFieldNames.FILTER_DOCUMENT_CLASSES);
			break;
		}

		Facet.Source facetSource = new Facet.Source(srcType, srcName);
		Facet facet = new Facet(facetId, rs.getString("name"),
				searchFieldNames, filterFieldNames, rs.getInt("facet_order"),
				rs.getString("css_identifier"), facetSource);

		if (isStringTermFacet)
			stringTermFacets.add(facet);

		if (facetType >= 0) {
			FacetGroup<Facet> group = facetGroupsByType.get(facetType);
			if (group == null) {
				String name = "";
				if (facetType == BIO_MED)
					name = "BioMed";
				else if (facetType == IMMUNOLOGY)
					name = "Immunology";
				else if (facetType == AGEING)
					name = "Ageing";
				else if (facetType == BIBLIOGRAPHY)
					name = "Bibliography";
				else if (facetType == FILTER)
					name = "Filter";
				group = new FacetGroup<Facet>(name, facetType);
				facetGroupsByType.put(facetType, group);
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

	public List<FacetGroup<Facet>> getFacetGroups() {
		return facetGroups;
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
			boolean isJSIdentifier = JavaScriptUtils.isJavascriptIdentifier(cssId);
			if (!isJSIdentifier) {
				logger.error(
						"The CSS-ID '{}' is no valid JavaScript identifier. The facet's CSS-IDs" +
						" are required to be valid JavaScript identifiers since the IDs are used" +
						" for both CSS and for JavaScript variable names.",
						cssId);
				error = true;
			}
		}

		if (error)
			throw new IllegalStateException(
					"There were problems while loading the facets. See the logs for more information.");
	}
}