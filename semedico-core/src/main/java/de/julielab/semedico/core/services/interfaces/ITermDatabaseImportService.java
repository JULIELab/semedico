package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.neo4j.plugins.datarepresentation.ConceptInsertionResponse;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportMapping;

public interface ITermDatabaseImportService {
	List<ConceptInsertionResponse> importTerms(ImportConceptAndFacet termsAndFacet);
	String importMappings(List<ImportMapping> mappings);

	void createDefaultFacets();
	String getDBHost();
}
