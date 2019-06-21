package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.ConceptCoordinates;
import de.julielab.neo4j.plugins.datarepresentation.ImportConcept;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.neo4j.plugins.datarepresentation.ImportOptions;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TermLabelAdder implements ITermLabelAdder {

	private Logger log;
	private ITermDatabaseImportService termImportService;

	public TermLabelAdder(Logger log, ITermDatabaseImportService termImportService) {
		this.log = log;
		this.termImportService = termImportService;

	}

	@Override
	public void addTermLabels(String termIdFile, String idProperty, String originalSource, String... labels) {
		log.info(
				"Adding labels {} to terms identified via the ID property {} (original source: {}), using ID file {}.",
				new Object[] { Arrays.toString(labels), idProperty, originalSource, termIdFile });
		try {
			log.info("Reading term ID file from {}.", termIdFile);
			LineIterator lines = FileUtils.lineIterator(new File(termIdFile), "UTF-8");
			List<ImportConcept> terms = new ArrayList<>();
			while (lines.hasNext()) {
				String id = lines.nextLine().trim();
				if (id.startsWith("#"))
					continue;
				ImportConcept term = new ImportConcept();
				term.coordinates = new ConceptCoordinates();
				if (idProperty.equals(ConceptConstants.PROP_SRC_IDS))
					term.coordinates.sourceId = id;
				else if (idProperty.equals(ConceptConstants.PROP_ORG_ID)) {
					term.coordinates.originalId = id;
					if (StringUtils.isBlank(originalSource))
						throw new IllegalArgumentException(
								"The original term ID was specified to identify terms, however the name of the original source was omitted which is necessary for original ID identification.");
					term.coordinates.originalSource = originalSource;
				}
				term.generalLabels = Arrays.asList(labels);
				terms.add(term);
			}
			log.info("Performing the import into the database.");
			ImportConceptAndFacet importTermAndFacet = new ImportConceptAndFacet(terms, null);
			ImportOptions importOptions = new ImportOptions();
			// We only want to add labels to existing terms. If a term is not existing in the database, no new term
			// shall be created.
			importOptions.merge = true;
			importTermAndFacet.importOptions = importOptions;
			termImportService.importTerms(importTermAndFacet);
		} catch (IOException e) {
			log.error("IOException: ", e);
		}
	}

}
