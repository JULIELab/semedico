package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.neo4j.plugins.datarepresentation.ImportMapping;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;

public interface ITermDatabaseImportService {
	String importTerms(ImportConceptAndFacet termsAndFacet);
	String importMappings(List<ImportMapping> mappings);

	String getDBHost();
}
