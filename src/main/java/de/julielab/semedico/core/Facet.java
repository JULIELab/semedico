package de.julielab.semedico.core;

import java.util.Collection;

import de.julielab.semedico.core.services.interfaces.ITermService;

public class Facet implements StructuralStateExposing, Comparable<Facet> {

	// public static abstract class SourceType {}
	//
	// public static abstract class FieldSource extends SourceType {}
	//
	// public static class FlatFieldSource extends FieldSource{}
	//
	// public static class HierarchicalFieldSource extends FieldSource {}
	//
	// public static final FieldSource FIELD_FLAT = new FlatFieldSource();
	// public static final FieldSource FIELD_HIERARCHICAL = new
	// HierarchicalFieldSource();

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
			public boolean isTaxonomic() {
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
			public boolean isTaxonomic() {
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
			public boolean isTaxonomic() {
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
			public boolean isTaxonomic() {
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
		public abstract boolean isTaxonomic();
	}

	// public static abstract class SourceType {
	// protected boolean flat;
	//
	// public boolean isFlat() {
	// return flat;
	// };
	//
	// public boolean isHierarchical() {
	// return !flat;
	// };
	//
	// public abstract boolean isSourceType(SourceLocation type);
	// };
	//
	// public static class FieldSource extends SourceType {
	// public FieldSource(boolean flat) {
	// this.flat = flat;
	// }
	//
	// public boolean isSourceType(SourceLocation type) {
	// return type == SourceLocation.FIELD;
	// }
	// }
	//
	// public static final FieldSource FIELD_FLAT = new FieldSource(true);
	// public static final FieldSource FIELD_HIERARCHICAL = new
	// FieldSource(false);

	// public enum SourceLocation {
	// FIELD
	// };

	public final static Facet KEYWORD_FACET = new Facet(0, "Keyword",
			"keywords");
	/**
	 * Name of this facet. This is also used for display.
	 */
	private String name;
	private String cssId;
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
	private int position;

	// The source of facet labels for this facet. This can be a field in the
	// index which contains (internally) hierarchical arranged terms. Another
	// field (e.g. for journals, authors...) could contain unordered facet
	// labels.
	private final Source source;

	// Moved to FacetService
	// public final static int BIO_MED = 0;
	// public final static int IMMUNOLOGY = 1;
	// public final static int BIBLIOGRAPHY = 2;
	// public final static int AGING = 3;
	// public final static int FILTER = 4;
	//
	// public static final int FIRST_AUTHOR_FACET_ID = 18;
	// public static final int LAST_AUTHOR_FACET_ID = 19;
	// public static final int PROTEIN_FACET_ID = 1;
	// public static final int CONCEPT_FACET_ID = 22;

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
		this.source = new Source(SourceType.KEYWORD, "keywords");
	}

	/**
	 * Only use for tests.
	 * 
	 * @param id
	 */
	public Facet(int id) {
		this.id = id;
		source = null;
	};

	public Facet(int id, String name, Collection<String> searchFieldNames,
			Collection<String> filterFieldName, int ordinal, String cssId,
			Source source) {
		this.id = id;
		this.name = name;
		this.searchFieldNames = searchFieldNames;
		this.filterFieldNames = filterFieldName;
		this.position = ordinal;
		this.cssId = cssId;
		this.source = source;
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

	public boolean isHierarchical() {
		return source.isHierarchical();
	}

	public boolean isFlat() {
		return source.isFlat();
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
			return !srcType.isTaxonomic();
		}

		public boolean isHierarchical() {
			return srcType.isTaxonomic();
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

	// /* (non-Javadoc)
	// * @see de.julielab.semedico.core.StructuralStateExposing#getSourceType()
	// */
	// @Override
	// public SourceType getStructureState() {
	// return source.getType();
	// }

}
