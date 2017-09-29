package de.julielab.semedico.elasticsearch.index.setup;

public abstract class Filter {
	public String type;

	public Filter(String type) {
		super();
		this.type = type;
	}
	
}
