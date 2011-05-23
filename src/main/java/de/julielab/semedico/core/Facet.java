package de.julielab.semedico.core;


public class Facet implements Comparable<Facet>{

	private String name;
	private String cssId;
	private Integer id;
	private String defaultIndexName;
	private int type;
	private int index;
	private int position;
	public final static int BIO_MED = 0;
	public final static int IMMUNOLOGY = 1;
	public final static int BIBLIOGRAPHY = 2;
	public final static int AGING = 3;	
	public final static int FILTER = 4;
	

	public static final int FIRST_AUTHOR_FACET_ID = 18;
	public static final int LAST_AUTHOR_FACET_ID = 19;
	public static final int PROTEIN_FACET_ID = 1;
	public static final int CONCEPT_FACET_ID = 22;
	
	public Facet(Integer id) {
		super();
		this.id = id;
		type = -1;
	}

	public Facet(String name) {
		super();
		this.name = name;
		type = -1;
	}
	
	public Facet(String name, String cssId) {
		super();
		this.name = name;
		this.cssId = cssId;
		this.id = 0;
		type = -1;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCssId() {
		return cssId;
	}
	
	public void setCssId(String id) {
		this.cssId = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
		return "{ name: "+name + "; cssId: "+cssId+ " index: " + index+ ";}";
	}

	public int compareTo(Facet facet1) {
		return id - facet1.getId();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

}
