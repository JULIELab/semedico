package de.julielab.semedico.elasticsearch.index.setup;

public class ObjectPropertiesContainer extends PropertiesContainer {

	public MappingTypes type;

	public ObjectPropertiesContainer(MappingTypes type, MappingProperties properties) {
		super(properties);
		this.type = type;
	}

}
