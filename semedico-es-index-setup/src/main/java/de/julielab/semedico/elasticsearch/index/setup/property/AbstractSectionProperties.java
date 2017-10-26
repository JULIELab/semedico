package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;

public class AbstractSectionProperties extends LikelihoodTextSpanProperties {
	public final MappingProperty label;
	public final MappingProperty nlmcategory;

	public AbstractSectionProperties() {
		super();
		label = new MappingProperty(MappingTypes.keyword, true);
		nlmcategory = new MappingProperty(MappingTypes.keyword, true);
	}
}
