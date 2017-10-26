package de.julielab.semedico.elasticsearch.index.setup;

public abstract class Index {
	public Settings settings;
	public Mappings mappings;

	public Index(Settings settings, Mappings mappings) {
		super();
		this.settings = settings;
		this.mappings = mappings;
	}

}
