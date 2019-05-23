package de.julielab.semedico.resources;

import java.io.File;

public interface IElementsAggregateIdMappingWriter {
	void writeMapping(String aggregateLabel, File outputFile);
}
