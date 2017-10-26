package de.julielab.semedico.elasticsearch.index.setup;

public class MappingProperty {

	public enum TermVector {
		with_positions_offsets
	}

	public MappingTypes type;
	public boolean index;
	public boolean store;
	public AnalyzerTypes analyzer;
	public TermVector term_vector;
	public Norms norms;

	public MappingProperty(MappingTypes type) {
		super();
		this.type = type;
	}
	
	public MappingProperty(MappingTypes type, boolean store) {
		this(type);
		this.store = store;
	}

	public MappingProperty(MappingTypes type, boolean store, boolean index) {
		this(type, store);
		this.index = index;
	}

	public MappingProperty(MappingTypes type, boolean store, boolean index, AnalyzerTypes analyzer) {
		this(type, store, index);
		this.analyzer = analyzer;
	}

	public MappingProperty(MappingTypes type, boolean store, boolean index, AnalyzerTypes analyzer, TermVector term_vector) {
		this(type, store, index, analyzer);
		this.term_vector = term_vector;
	}

	public MappingProperty(MappingTypes type, boolean store, boolean index, AnalyzerTypes analyzer, TermVector term_vector,
			boolean enableNorms) {
		this(type, store, index, analyzer, term_vector);
		this.norms = new Norms(enableNorms);
	}

	public static class Norms {
		public boolean enabled;

		public Norms(boolean enabled) {
			super();
			this.enabled = enabled;
		}

	}

}
