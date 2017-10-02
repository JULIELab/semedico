package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.MappingProperties;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;
import de.julielab.semedico.elasticsearch.index.setup.ObjectPropertiesContainer;

public class DocumentProperties extends MappingProperties {
	public ObjectPropertiesContainer title;
	public MappingProperty abstracttext;
	public MappingProperty documenttext;

	public DocumentProperties() {
		title = new ObjectPropertiesContainer(MappingTypes.object, new LikelihoodTextSpanProperties());
		abstracttext = new TextSpanProperty();
		documenttext = new TextSpanProperty();
	}
}
