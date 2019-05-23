package de.julielab.semedico.resources;

import java.io.File;
import java.util.Set;

public interface IBioCPMCDevDocExtractor {
	public enum IdSource {
		PMC, PUBMED
	}
	void extractSemedicoDevDocuments(File pmcBiocUnicodeDir, File outputFile, Set<String> docIds, IdSource source);
}
