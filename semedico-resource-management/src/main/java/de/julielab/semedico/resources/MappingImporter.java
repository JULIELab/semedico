package de.julielab.semedico.resources;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.julielab.neo4j.plugins.datarepresentation.ImportMapping;
import de.julielab.semedico.bioportal.OntologyClassMapping;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class MappingImporter implements IMappingImporter {

	private static final String MAPPING_DATA_EXTENSION = ".map.json";

	private ITermDatabaseImportService importService;
	private Logger log;

	public MappingImporter(Logger log, ITermDatabaseImportService importService) {
		this.log = log;
		this.importService = importService;

	}

	@Override
	public void importMappings(String pathToMappings, Set<String> allowedAcronyms) throws IOException {
		log.info("Importing mappings from {}{}", pathToMappings, allowedAcronyms != null && !allowedAcronyms.isEmpty() ? " for acronyms " + allowedAcronyms : "");
		File file = new File(pathToMappings);
		if (file.isFile()) {
			log.info("Found mapping file \"{}\", processing...", file.getAbsolutePath());
			importMappingFile(file);
		} else if (file.isDirectory()) {
			File[] files = file.listFiles((dir, name) ->
						name.endsWith(MAPPING_DATA_EXTENSION) || 
						name.endsWith(MAPPING_DATA_EXTENSION + ".gz"));

			log.info("Got directory with {} mapping files.", files != null ? files.length : 0	);
			for (File mappingFile : files) {
				String name = mappingFile.getName();
				String acronym = name.substring(0, name.indexOf('.'));
				if (null != allowedAcronyms && !allowedAcronyms.isEmpty() && !allowedAcronyms.contains(acronym)) {
					log.debug(
							"Skipping mappings for ontology with acronym {} because it is not contained in the set of allowed acronyms.",
							acronym);
					continue;
				}
				log.info("Processing mapping file {}", mappingFile.getAbsolutePath());
				importMappingFile(mappingFile);
			}
		} else {
			log.warn("{} is neither a file nor a directory", pathToMappings);
		}
	}

	private void importMappingFile(File file) throws IOException {
		Gson gson = new Gson();
		Reader reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), "UTF-8");
		Type mappingListType = new TypeToken<List<OntologyClassMapping>>() {//
				}.getType();
		List<OntologyClassMapping> mappings = gson.fromJson(reader, mappingListType);

		List<ImportMapping> importMappings = new ArrayList<>(mappings.size());
		for (OntologyClassMapping mapping : mappings) {
			String type = mapping.source;
			// TODO this means ignoring user-delivered mappings! We just do this for the moment to lower data load since
			// we don't know how to deal with those currently
			if (mapping.process != null) {
				continue;
			}
			if (null == type) {
				throw new IllegalArgumentException("Could not find the mapping type for mapping " + mapping);
			}
			if (type.equals("SAME_URI")) {
				// This kind of "mapping" is handled automatically by the ConceptManager plugin.
				continue;
			}

			if (mapping.classes.size() != 2) {
				throw new IllegalArgumentException("Mapping occurred that does not map exactly two classes " + mapping);
			}
			
			String id1 = mapping.classes.get(0).id;
			String id2 = mapping.classes.get(1).id;

			ImportMapping importMapping = new ImportMapping(id1, id2, type);
			importMappings.add(importMapping);
		}

		log.info(
				"Importing {} mappings after filtering unwanted mapping types like SAME_URI and user-defined mappings (for the time being).",
				importMappings.size());
		String response = importService.importMappings(importMappings);

		log.info(
				"Server responded that {} mappings have been inserted successfully (duplicates are not inserted again).",
				response);
	}

}
