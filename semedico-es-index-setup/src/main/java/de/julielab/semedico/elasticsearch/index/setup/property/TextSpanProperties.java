package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.MappingProperties;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty;
import de.julielab.semedico.elasticsearch.index.setup.MappingTypes;

/**
 * A template for a "properties" element for each index (type) that represents a
 * span of text like a sentence or a paragraph. It basically defines a field
 * "text" that is preanalyzed, indexed, uses the semedico_text analyzer and has
 * positions and offsets. It is not stored.
 * 
 * @author faessler
 *
 */
public class TextSpanProperties extends MappingProperties {
	public final MappingProperty text;
	public final MappingProperty begin;
	public final MappingProperty end;

	public TextSpanProperties() {
		text = new TextSpanProperty();
		begin = new MappingProperty(MappingTypes.integer, true, false);
		end = new MappingProperty(MappingTypes.integer, true, false);
	}
}
