package de.julielab.semedico.core;


public class Facet implements Comparable<Facet> {

	public enum SourceType { FIELD_HIERARCHIC, FIELD_FLAT };
	
	public final static Facet KEYWORD_FACET = new Facet(0, "Keyword", "keywords");
	/**
	 * Name of this facet. This is also used for display.
	 */
	private String name;
	private String cssId;
	/**
	 * Identifier number of this facet.
	 */
	private Integer id;
	private String defaultIndexName;
	private int type;
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
	// Moved to FacetService
//	public final static int BIO_MED = 0;
//	public final static int IMMUNOLOGY = 1;
//	public final static int BIBLIOGRAPHY = 2;
//	public final static int AGING = 3;
//	public final static int FILTER = 4;
//
//	public static final int FIRST_AUTHOR_FACET_ID = 18;
//	public static final int LAST_AUTHOR_FACET_ID = 19;
//	public static final int PROTEIN_FACET_ID = 1;
//	public static final int CONCEPT_FACET_ID = 22;

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
	}

	public Facet(int id, String name, String defaultIndexName, int type,
			int ordinal, String cssId) {
		this.id = id;
		this.name = name;
		this.defaultIndexName = defaultIndexName;
		this.type = type;
		this.position = ordinal;
		this.cssId = cssId;
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

	public String getDefaultIndexName() {
		return defaultIndexName;
	}

	public void setDefaultIndexName(String defaultIndexName) {
		this.defaultIndexName = defaultIndexName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "{ name: " + name + "; cssId: " + cssId + ";}";
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
	
	public class Source {
		private final SourceType srcType;
		private final String srcName;

		public Source(SourceType srcType, String srcName) {
			this.srcType = srcType;
			this.srcName = srcName;
			
		}

		/**
		 * @return the name
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
	}

}
