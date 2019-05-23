package de.julielab.semedico.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

public interface IMappingImporter {
	/**
	 * 
	 * @param pathToMappings
	 *            A mapping file or a directory containing mapping files. Directory contents are filtered for files
	 *            ending with <tt>.dat</tt>.
	 * @param allowedAcronyms 
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	void importMappings(String pathToMappings, Set<String> allowedAcronyms) throws FileNotFoundException, IOException;
}
