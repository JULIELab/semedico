package de.julielab.semedico.core.facets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.facets.Facet.Source;
import de.julielab.semedico.core.facets.Facet.SourceType;
import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class Facet implements Comparable<Facet> {

	public static Facet KEYWORD_FACET = new Facet(NodeIDPrefixConstants.FACET + "-1", "Keyword", "keywords");
//	static {
//		KEYWORD_FACET.searchFieldNames = Arrays.asList(IIndexInformationService.MEDLINE_SEARCH_FIELDS);
//	}

	public static Facet CORE_TERMS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-2", "Special Terms", "specialterms");
//	static {
//		CORE_TERMS_FACET.searchFieldNames = Arrays.asList(IIndexInformationService.TITLE, IIndexInformationService.ABSTRACT);
//	}
	public static Facet MOST_INFORMATIVE_CONCEPTS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-3", "Most Informative Concepts", "mostinformative");
	public static Facet MOST_FREQUENT_CONCEPTS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-4", "Most Frequent Concepts", "mostfrequent");
	static {
		Source defaultSource = new Facet.Source(SourceType.FIELD_FLAT_TERMS, IIndexInformationService.GeneralIndexStructure.conceptlist);
		MOST_INFORMATIVE_CONCEPTS_FACET.setSource(defaultSource);
		MOST_FREQUENT_CONCEPTS_FACET.setSource(defaultSource);
	}
	
	public static Facet BOOLEAN_OPERATORS_FACET = new Facet(NodeIDPrefixConstants.FACET + "-3", "Boolean Operators", "booleanoperators");

	/**
	 * Name of this facet. This is also used for display.
	 */
	protected String name;
	protected String cssId;
	/**
	 * Identifier number of this facet.
	 */
	private String id;
	private Collection<String> searchFieldNames;
	private Collection<String> filterFieldNames;
	/**
	 * A set of labels to mark particular facets, e.g. to be "The Authors facet" or "the BTerm Facet" and such.
	 */
	private Set<FacetLabels.Unique> uniqueLabels;
	private Set<FacetLabels.General> generalLabels;
	/**
	 * The position of this facet when it comes to ordering for display. This only delivers a default-order within a
	 * facet group. The order could be changed by the user, which will <em>not</em> reflect in another position number
	 * (this class is part of the object model rather then of the session state!). The actual display order of facets
	 * will be determined by their position in the session state object copy of FacetGroup in the searchConfiguration.
	 */
	protected int position;
	
	private String inducingTermId;
	
	public Set<FacetLabels.Unique> getUniqueLabels() {
		return uniqueLabels;
	}

	public Set<FacetLabels.General> getGeneralLabels() {
		return generalLabels;
	}

	// The source of facet labels for this facet. This can be a field in the
	// index which contains (internally) hierarchical arranged terms. Another
	// field (e.g. for journals, authors...) could contain unordered facet
	// labels.
	protected Source source;

	public void setSource(Source source) {
		this.source = source;
	}

	/**
	 * The number of root terms this facet has. This value is precomputed within the term database and then just loaded
	 * from there. Only hierarchical facets have roots at all, so for flat facets this is <tt>null</tt>.
	 */
	private int numRoots;

	private ITermService termService;
	private Set<General> aggregationLabels;
	private List<String> aggregationFields;

	private String shortName;

	/**
	 * Exclusively used to generate {@link #KEYWORD_FACET}.
	 * 
	 * @param name
	 * @param cssId
	 */
	private Facet(String id, String name, String cssId) {
		this.id = id;
		this.name = name;
		this.cssId = cssId;
		// A special source which is of no source type, not hierarchical and not
		// flat. This source type should not occur anywhere else.
		this.source = new Source(SourceType.KEYWORD, "keywords") {

			/*
			 * (non-Javadoc)
			 * 
			 * @see de.julielab.semedico.core.Facet.Source#isFlat()
			 */
			@Override
			public boolean isFlat() {
				return true;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see de.julielab.semedico.core.Facet.Source#isHierarchical()
			 */
			@Override
			public boolean isHierarchic() {
				return false;
			}

		};
		// facetRoots = Collections.emptyList();
		uniqueLabels = new HashSet<>();
		uniqueLabels.add(FacetLabels.Unique.KEYWORDS);
		generalLabels = new HashSet<>();
		setSearchFieldNames(Lists.newArrayList(IIndexInformationService.MEDLINE_SEARCH_FIELDS));
		setFilterFieldNames(Lists.newArrayList(IIndexInformationService.MEDLINE_SEARCH_FIELDS));
	}

	/**
	 * Only use for tests.
	 * 
	 * @param id
	 */
	public Facet(String id) {
		this.id = id;
		source = null;
		// facetRoots = Collections.emptyList();
	};

	public Facet(String id, String name) {
		this.id = id;
		this.name = name;
		this.source = null;
		this.searchFieldNames = Collections.emptyList();
		this.filterFieldNames = Collections.emptyList();
		this.uniqueLabels = Collections.emptySet();
	}

	public Facet(String id, String name, Collection<String> searchFieldNames, Collection<String> filterFieldName,
			Set<FacetLabels.General> generalLabels, Set<FacetLabels.Unique> uniqueLabels, int position, String cssId,
			Source source, ITermService termService) {
		this.id = id;
		this.name = name;
		
		this.searchFieldNames = searchFieldNames;
		this.filterFieldNames = filterFieldName;
		this.generalLabels = generalLabels;
		this.position = position;
		this.cssId = cssId;
		this.source = source;
		this.uniqueLabels = uniqueLabels;
		this.termService = termService;
	}

	public Facet(Facet template) {
		this.id = template.id;
		this.name = template.name;
		this.searchFieldNames = template.searchFieldNames;
		this.filterFieldNames = template.filterFieldNames;
		this.generalLabels = template.generalLabels;
		this.position = template.position;
		this.cssId = template.cssId;
		this.source = template.source;
		this.uniqueLabels = template.uniqueLabels;
		this.termService = template.termService;
		this.aggregationLabels = template.aggregationLabels;
		this.aggregationFields = template.aggregationFields;
		this.numRoots = template.numRoots;
	}

	public String getName() {
		return name;
	}

	public String getCssId() {
		return cssId;
	}

	public String getId() {
		return id;
	}

	public Collection<String> getSearchFieldNames() {
		return searchFieldNames;
	}

	public void setSearchFieldNames(Collection<String> searchFieldNames) {
		this.searchFieldNames = searchFieldNames;
	}

	/**
	 * @return the filterFieldName
	 */
	public Collection<String> getFilterFieldNames() {
		return filterFieldNames;
	}

	/**
	 * @param filterFieldNames
	 *            the filterFieldName to set
	 */
	public void setFilterFieldNames(Collection<String> filterFieldNames) {
		this.filterFieldNames = filterFieldNames;
	}

	@Override
	public String toString() {
		return "Facet [name=" + name
				+ ", id="
				+ id
				+ ", uniqueLabels="
				+ uniqueLabels
				+ ", generalLabels="
				+ generalLabels
				+ ", source="
				+ source
				+ "]";
	}

	public int compareTo(Facet otherFacet) {
		return this.position - otherFacet.getPosition();
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the source
	 */
	public Facet.Source getSource() {
		return source;
	}

	/**
	 * Returns the root terms of this facet. For most facets, the returned value will be all root terms in the database.
	 * For facets with an extreme amount of roots, the result could be empty or considerably smaller than the number of
	 * roots as stored in the database. If the result is empty, there are too many roots in the database and no roots
	 * have yet been loaded individually. If the number of returned roots is not 0 but smaller than the amount of roots
	 * in the database, the up to now individually loaded concept are returned.
	 * 
	 * @return The roots of this facet, either all existing database roots or - for facets with large amount of roots -
	 *         those roots loaded until now.
	 */
	public List<Concept> getFacetRoots() {
		if (isFlat())
			return Collections.emptyList();
		return termService.getFacetRoots(this);
	}

	/**
	 * Assures that at least all facet roots whose IDs are among <tt>termIds</tt> are loaded for this facet and then
	 * returns all loaded facet roots.
	 * 
	 * @param termIds
	 * @return
	 */
	public List<Concept> getFacetRoots(List<String> termIds) {
		if (isFlat())
			return Collections.emptyList();
		if (allDBRootsLoaded())
			return getFacetRoots();
		Map<Facet, List<String>> requestedRootIds = new HashMap<>();
		requestedRootIds.put(this, termIds);
		termService.assureFacetRootsLoaded(requestedRootIds);
		return getFacetRoots();
	}

	public Collection<String> getFacetRootIds() {
		Set<String> termIds = new HashSet<>();
		for (Concept term : getFacetRoots()) {
			String termId = term.getId();
			termIds.add(termId);
		}
		return termIds;
	}

	public boolean isHierarchic() {
		return source.isHierarchic();
	}

	public boolean isFlat() {
		return source.isFlat();
	}

	public boolean isAnyAuthorFacet() {
		return uniqueLabels.contains(FacetLabels.Unique.AUTHORS) || uniqueLabels
				.contains(FacetLabels.Unique.FIRST_AUTHORS) || uniqueLabels.contains(FacetLabels.Unique.LAST_AUTHORS);
	}

	/**
	 * Returns the exact author-related label for this facet, if this is any author facet. Returns <tt>null</tt>
	 * otherwise.
	 * 
	 * @return
	 */
	public FacetLabels.Unique getAuthorLabel() {
		for (FacetLabels.Unique label : uniqueLabels) {
			if (label.equals(FacetLabels.Unique.AUTHORS) || uniqueLabels.contains(FacetLabels.Unique.FIRST_AUTHORS)
					|| uniqueLabels.contains(FacetLabels.Unique.LAST_AUTHORS))
				return label;
		}
		return null;
	}

	public boolean hasUniqueLabel(FacetLabels.Unique label) {
		return uniqueLabels.contains(label);
	}

	public boolean hasGeneralLabel(FacetLabels.General label) {
		return generalLabels.contains(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof Facet))
			return false;
		Facet otherFacet = (Facet) arg0;
		return this.id.equals(otherFacet.id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public UIFacet getUiFacetCopy(Logger logger) {
		UIFacet facet = new UIFacet(logger, this);
		return facet;
	}

	/**
	 * The number of root terms this facet has. This value is precomputed within the term database and then just loaded
	 * from there. Only hierarchical facets have roots at all, so for flat facets this is <tt>null</tt>.
	 */
	public int getNumRootsInDB() {
		return numRoots;
	}

	public int getNumRootsLoaded() {
		return termService.getNumLoadedRoots(id);
	}

	public void setNumRoots(Integer numRoots) {
		if (numRoots != null && numRoots > 0 && isFlat())
			throw new IllegalArgumentException("Flat facets do not have root terms, but the flat facet with ID " + id
					+ " should be set to have "
					+ numRoots
					+ " root terms.");
		if (null == numRoots)
			this.numRoots = 0;
		else
			this.numRoots = numRoots;
	}

	public static class Source {

		private final SourceType srcType;
		private final String srcName;

		public Source(SourceType srcType, String srcName) {
			this.srcType = srcType;
			this.srcName = srcName;
		}

		/**
		 * @return the name of the index field for which terms should be counted
		 */
		public String getName() {
			return srcName;
		}

		/**
		 * @return the type
		 */
		public SourceType getType() {
			return srcType;
		}

		public boolean isFlat() {
			return !srcType.isHierarchic();
		}

		public boolean isHierarchic() {
			return srcType.isHierarchic();
		}

		public boolean isDatabaseTermSource() {
			return srcType.isDatabaseTermSource();
		}

		
		public boolean isStringTermSource() {
			return srcType.isStringTermSource();
		}
		public boolean isAggregation() {
			return srcType.isAggregation();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Source [srcType=" + srcType + ", srcName=" + srcName + "]";
		}
	}

	public static enum SourceType {
		FIELD_STRINGS {

			@Override
			public boolean isDatabaseTermSource() {
				return false;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}

			@Override
			public boolean isAggregation() {
				return false;
			}
		},
		FIELD_TAXONOMIC_TERMS {

			@Override
			public boolean isDatabaseTermSource() {
				return true;
			}

			@Override
			public boolean isHierarchic() {
				return true;
			}

			@Override
			public boolean isAggregation() {
				return false;
			}
		},
		FIELD_FLAT_TERMS {

			@Override
			public boolean isDatabaseTermSource() {
				return true;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}

			@Override
			public boolean isAggregation() {
				return false;
			}

		},
		FACET_AGGREGATION {

			@Override
			public boolean isDatabaseTermSource() {
				return true;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}

			@Override
			public boolean isAggregation() {
				return true;
			}

		},
		KEYWORD {

			@Override
			public boolean isDatabaseTermSource() {
				return false;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}

			@Override
			public boolean isAggregation() {
				return false;
			}

		};
		/**
		 * Determines whether this facet source contains IDs of 'real' terms. Meant with that are terms which have known
		 * synonyms, writing variants etc. This is in contrast to author names, for example, where we don't know
		 * anything but the author name string itself.
		 * 
		 * @return <code>true</code> iff this source contains term IDs for terms which are managed by the
		 *         {@link ITermService}.
		 */
		public abstract boolean isDatabaseTermSource();

		/**
		 * Determines whether this facet source contains terms which are defined by the exact <em>Lucene</em> terms in
		 * an index field. This is the case for authors or for years, for example. These <em>string terms</em> are
		 * defined completely by their string appearance and have no known synonyms or writing variants. That is, two
		 * different author name strings could, in the real world, refer to the same person, but we don't know about it
		 * because we have no way to find out.
		 * 
		 * @return <code>true</code> iff this source contains string terms rather then IDs for full-defined terms.
		 */
		public boolean isStringTermSource() {
			return !isDatabaseTermSource();
		}

		/**
		 * Determines whether the terms in this facet form a taxonomy.
		 * 
		 * @return <code>true</code> iff the terms contained in this source have a taxonomic structure.
		 */
		public abstract boolean isHierarchic();

		/**
		 * Determines whether this facet is just an aggregation of other facets.
		 * 
		 * @return <code>true</code> iff the term contents of this source is actually an agglomeration of terms from
		 *         other facet sources.
		 */
		public abstract boolean isAggregation();
	}

	public void setAggregationLabels(Set<General> aggregationLabels) {
		this.aggregationLabels = aggregationLabels;
	}

	public Set<General> getAggregationLabels() {
		return this.aggregationLabels;
	}

	public boolean isAggregationFacet() {
		return source.isAggregation();
	}

	public void setAggregationFields(List<String> facetAggregationFields) {
		this.aggregationFields = facetAggregationFields;
	}

	public List<String> getAggregationFields() {
		return aggregationFields;
	}

	public boolean allDBRootsLoaded() {
		return getNumRootsLoaded() - getNumRootsInDB() >= 0;
	}

	public String getInducingTermId() {
		return inducingTermId;
	}

	public void setInducingTermId(String inducingTermId) {
		this.inducingTermId = inducingTermId;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

}
