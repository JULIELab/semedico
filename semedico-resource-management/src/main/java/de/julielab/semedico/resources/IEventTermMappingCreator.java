package de.julielab.semedico.resources;

import java.io.File;

public interface IEventTermMappingCreator {
	/**
	 * Writes the mapping from event <tt>specificType</tt> - as output e.g. by the JReX UIMA component - to Semedico
	 * term IDs. This requires the terms have been defined to be event terms before via the {@link IEventTermDefiner}.
	 * 
	 * @param outputFile
	 */
	void writeEventTermMapping(File outputFile);
}
