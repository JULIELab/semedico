package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.MappingProperties;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;
import de.julielab.semedico.elasticsearch.index.setup.ObjectPropertiesContainer;

public class DocumentProperties extends MappingProperties {
	public ObjectPropertiesContainer title;
	public ObjectPropertiesContainer abstracttext;
	public ObjectPropertiesContainer otherabstracttext;
	public MappingProperty documenttext;

	public DocumentProperties() {
		title = new ObjectPropertiesContainer(MappingTypes.object, new LikelihoodTextSpanProperties());
		abstracttext = new ObjectPropertiesContainer(MappingTypes.object, new TextSpanProperties());
		otherabstracttext = new ObjectPropertiesContainer(MappingTypes.object, new TextSpanProperties());
		documenttext = new TextSpanProperty();
	}
}
