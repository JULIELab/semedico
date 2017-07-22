package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.neo4j.plugins.datarepresentation.ConceptInsertionResponse;
import de.julielab.neo4j.plugins.datarepresentation.ImportMapping;
import de.julielab.neo4j.plugins.datarepresentation.ImportTermAndFacet;

public interface ITermDatabaseImportService {
	ConceptInsertionResponse importTerms(ImportTermAndFacet termsAndFacet);
	String importMappings(List<ImportMapping> mappings);

	void createDefaultFacets();
	String getDBHost();
}
