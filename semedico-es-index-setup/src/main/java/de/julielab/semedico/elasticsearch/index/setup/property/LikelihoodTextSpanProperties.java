package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;

/**
 * Just adds a numerical 'likelihood' field to the preanalyzed text field of
 * {@link TextSpanProperties}. The likelihood is the epistemic modality
 * value for the represented span of document text.
 * 
 * @author faessler
 *
 */
public class LikelihoodTextSpanProperties extends TextSpanProperties {
	public MappingProperty likelihood;

	public LikelihoodTextSpanProperties() {
		super();
		likelihood = new MappingProperty(MappingTypes.integer, true);
	}
}
