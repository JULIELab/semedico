package de.julielab.semedico.elasticsearch.index.setup.property;

import de.julielab.semedico.elasticsearch.index.setup.*;
import de.julielab.semedico.elasticsearch.index.setup.MappingProperty.TermVector;

public class AllTypesProperties extends MappingProperties {
	// Properties for all text structures
	public final MappingProperty text;
	// For relations, begin and end will be arrays; of no consequence here but
	// important after retrieval and reading the fields.
	public final MappingProperty begin;
	public final MappingProperty end;

	// For all text sections small enough to make sense of epistemic modality.
	public final MappingProperty likelihood;

	// Abstract Sections
	public final MappingProperty label;
	public final MappingProperty nlmcategory;

	// Base documents
	public final ObjectPropertiesContainer title;
	public final ObjectPropertiesContainer abstracttext;
	public final ObjectPropertiesContainer otherabstracttext;
	public final MappingProperty documenttext;

	// Relations
	public final MappingProperty arguments;
	public final MappingProperty argumentwords;
	public final MappingProperty types;
	public final MappingProperty sentence;
	public final MappingProperty sentencebegin;
	public final MappingProperty sentenceend;
	public final MappingProperty numarguments;
	public final MappingProperty numdistinctarguments;
	public final MappingProperty source;
	
	// The scope label (chunk, sentence, ...)
	public final MappingProperty scope;

	public AllTypesProperties() {
		// For all text-based structures
		text = new TextSpanProperty();
		begin = new MappingProperty(MappingTypes.integer, true, false);
		end = new MappingProperty(MappingTypes.integer, true, false);

		likelihood = new MappingProperty(MappingTypes.integer, true, true);
		
		// Abstract Sections
		label = new MappingProperty(MappingTypes.keyword, true);
		nlmcategory = new MappingProperty(MappingTypes.keyword, true);

		// Full documents
		title = new ObjectPropertiesContainer(MappingTypes.object, new LikelihoodTextSpanProperties());
		abstracttext = new ObjectPropertiesContainer(MappingTypes.object, new TextSpanProperties());
		otherabstracttext = new ObjectPropertiesContainer(MappingTypes.object, new TextSpanProperties());
		documenttext = new MappingProperty(MappingTypes.preanalyzed, true, true, AnalyzerTypes.semedico_text,
				TermVector.with_positions_offsets);

		// Relations
		arguments = new MappingProperty(MappingTypes.keyword, false, true);
		argumentwords = new MappingProperty(MappingTypes.text, false, true);
		types = new MappingProperty(MappingTypes.keyword, false, true);
		sentence = new MappingProperty(MappingTypes.preanalyzed, false, true, AnalyzerTypes.semedico_text,
				TermVector.with_positions_offsets, false);
		sentencebegin = new MappingProperty(MappingTypes.integer, true);
		sentenceend = new MappingProperty(MappingTypes.integer, true);
		numarguments = new MappingProperty(MappingTypes.integer, false, true);
		numdistinctarguments = new MappingProperty(MappingTypes.integer, false, true);
		source = new MappingProperty(MappingTypes.text, true, true);
		
		// Which type of document this is
		scope = new MappingProperty(MappingTypes.keyword, true, true);
	}
}
