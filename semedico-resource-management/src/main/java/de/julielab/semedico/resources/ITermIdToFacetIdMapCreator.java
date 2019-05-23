package de.julielab.semedico.resources;

import java.io.IOException;

public interface ITermIdToFacetIdMapCreator {
	void writeMapping(String outputFilePath, String label) throws IOException;
}
