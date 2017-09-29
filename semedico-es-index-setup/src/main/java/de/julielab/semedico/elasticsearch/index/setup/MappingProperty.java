package de.julielab.semedico.elasticsearch.index.setup;

public class MappingProperty {

	public enum TermVector {
		with_positions_offsets
	}

	public MappingTypes type;
	public boolean store;
	public String analyzer;
	public TermVector term_vector;

	public MappingProperty(MappingTypes type) {
		super();
		this.type = type;
	}

	public MappingProperty(MappingTypes type, boolean store) {
		this(type);
		this.store = store;
	}

	public MappingProperty(MappingTypes type, boolean store, String analyzer) {
		this(type, store);
		this.analyzer = analyzer;
	}

	public MappingProperty(MappingTypes type, boolean store, String analyzer, TermVector term_vector) {
		this(type, store, analyzer);
		this.term_vector = term_vector;
	}

}
