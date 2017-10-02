package de.julielab.semedico.elasticsearch.index.setup;

import de.julielab.semedico.elasticsearch.index.setup.mapping.DefaultMapping;

public class Mappings {
	DefaultMapping _default_;
	PropertiesContainer items;

	public Mappings(DefaultMapping _default_, MappingProperties properties) {
		super();
		this._default_ = _default_;
		this.items = new PropertiesContainer(properties);
	}
	
}
