package de.julielab.semedico.resources;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface IIndexDocumentIdsWriter {
	void writeDocumentIdsInIndex() throws FileNotFoundException, IOException;
}
