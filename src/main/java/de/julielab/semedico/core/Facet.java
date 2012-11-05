package de.julielab.semedico.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.slf4j.Logger;

import de.julielab.semedico.core.exceptions.IncompatibleStructureException;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

public class Facet implements StructuralStateExposing, Comparable<Facet> {

	public static Facet KEYWORD_FACET = new Facet(0, "Keyword", "keywords");
	/**
	 * Name of this facet. This is also used for display.
	 */
	protected String name;
	protected String cssId;
	/**
	 * Identifier number of this facet.
	 */
	private Integer id;
	private Collection<String> searchFieldNames;
	private Collection<String> filterFieldNames;
	/**
	 * The position of this facet when it comes to ordering for display. This
	 * only delivers a default-order within a facet group. The order could be
	 * changed by the user, which will <em>not</em> reflect in another position
	 * number (this class is part of the object model rather then of the session
	 * state!). The actual display order of facets will be determined by their
	 * position in the session state object copy of FacetGroup in the
	 * searchConfiguration.
	 */
	protected int position;

	// The source of facet labels for this facet. This can be a field in the
	// index which contains (internally) hierarchical arranged terms. Another
	// field (e.g. for journals, authors...) could contain unordered facet
	// labels.
	protected final Source source;

	protected Collection<IFacetTerm> facetRoots;

	/**
	 * Exclusively used to generate {@link #KEYWORD_FACET}.
	 * 
	 * @param name
	 * @param cssId
	 */
	private Facet(int id, String name, String cssId) {
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
				throw new IncompatibleStructureException(
						"This is the keyword facet; it is neither flat nor hierarchical since it does not contain any special terms.");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see de.julielab.semedico.core.Facet.Source#isHierarchical()
			 */
			@Override
			public boolean isHierarchic() {
				throw new IncompatibleStructureException(
						"This is the keyword facet; it is neither flat nor hierarchical since it does not contain any special terms.");
			}

		};
		facetRoots = Collections.emptyList();
	}

	/**
	 * Only use for tests.
	 * 
	 * @param id
	 */
	public Facet(int id) {
		this.id = id;
		source = null;
		facetRoots = Collections.emptyList();
	};

	public Facet(int id, String name, Collection<String> searchFieldNames,
			Collection<String> filterFieldName, int position, String cssId,
			Source source) {
		this.id = id;
		this.name = name;
		this.searchFieldNames = searchFieldNames;
		this.filterFieldNames = filterFieldName;
		this.position = position;
		this.cssId = cssId;
		this.source = source;
	}

	public void addFacetRoot(IFacetTerm rootTerm) {
		if (facetRoots == null)
			facetRoots = new HashSet<IFacetTerm>();
		facetRoots.add(rootTerm);
	}

	public void setFacetRoots(Collection<IFacetTerm> rootTerms) {
		this.facetRoots = rootTerms;
	}

	public String getName() {
		return name;
	}

	public String getCssId() {
		return cssId;
	}

	public Integer getId() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Facet [name=" + name + ", id=" + id + ", source=" + source
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
	 * @return the facetRoots
	 */
	public Collection<IFacetTerm> getFacetRoots() {
		return facetRoots;
	}

	public boolean isHierarchic() {
		return source.isHierarchic();
	}

	public boolean isFlat() {
		return source.isFlat();
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
		return this.id == otherFacet.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}

	public UIFacet getUiFacetCopy(Logger logger) {
		UIFacet facet = new UIFacet(logger, id, name, searchFieldNames,
				filterFieldNames, facetRoots, position, cssId, source);
		return facet;
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
		
		public boolean isTermSource() {
			return srcType.isTermSource();
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
			public boolean isTermSource() {
				return false;
			}

			@Override
			public boolean isStringTermSource() {
				return true;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}
		},
		FIELD_TAXONOMIC_TERMS {

			@Override
			public boolean isTermSource() {
				return true;
			}

			@Override
			public boolean isStringTermSource() {
				return true;
			}

			@Override
			public boolean isHierarchic() {
				return true;
			}
		},
		FIELD_FLAT_TERMS {

			@Override
			public boolean isTermSource() {
				return true;
			}

			@Override
			public boolean isStringTermSource() {
				return true;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}

		},
		KEYWORD {

			@Override
			public boolean isTermSource() {
				return false;
			}

			@Override
			public boolean isStringTermSource() {
				return false;
			}

			@Override
			public boolean isHierarchic() {
				return false;
			}

		};
		/**
		 * Determines whether this facet source contains IDs of 'real' terms.
		 * Meant with that are terms which have known synonyms, writing variants
		 * etc. This is in contrast to author names, for example, where we don't
		 * know anything but the author name string itself.
		 * 
		 * @return <code>true</code> iff this source contains term IDs for terms
		 *         which are managed by the {@link ITermService}.
		 */
		public abstract boolean isTermSource();

		/**
		 * Determines whether this facet source contains terms which are defined
		 * by the exact <em>Lucene</em> terms in an index field. This is the
		 * case for authors or for years, for example. These
		 * <em>string terms</em> are defined completely by their string
		 * appearance and have no known synonyms or writing variants. That is,
		 * two different author name strings could, in the real world, refer to
		 * the same person, but we don't know about it because we have no way to
		 * find out.
		 * 
		 * @return <code>true</code> iff this source contains string terms
		 *         rather then IDs for full-defined terms.
		 */
		public abstract boolean isStringTermSource();

		/**
		 * Determines whether the terms in this facet form a taxonomy.
		 * 
		 * @return <code>true</code> iff the terms contained in this source have
		 *         a taxonomic structure.
		 */
		public abstract boolean isHierarchic();
	}

}
