package de.julielab.semedico.bioportal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.bioportal.ontologies.BioPortalToolConstants;
import de.julielab.bioportal.ontologies.data.OntologyClass;
import de.julielab.bioportal.ontologies.data.OntologyClassParents;
import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacetGroup;
import de.julielab.neo4j.plugins.datarepresentation.ImportOptions;
import de.julielab.neo4j.plugins.datarepresentation.ConceptCoordinates;
import de.julielab.neo4j.plugins.datarepresentation.ImportConcept;
import de.julielab.neo4j.plugins.datarepresentation.ImportConceptAndFacet;
import de.julielab.semedico.core.facets.FacetGroupLabels;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import de.julielab.semedico.resources.ResourceTermLabels;
import jline.internal.InputStreamReader;

public class BioPortalTermImporter {
	
	private class OntologyClassSynonymComparator implements Comparator<String> {
		private RuleBasedCollator collator = BioPortalTermImporter.createComparisonCollator();
		@Override
		public int compare(String o1, String o2) {
			return collator.compare(o1, o2);
		}
		
	}

	public static final String BIO_PORTAL_CSS_PREFIX = "bioportal_";
	public static final String BIO_PORTAL_FACET_GROUP_NAME = "BioPortal";
	private static final Logger log = LoggerFactory.getLogger(BioPortalTermImporter.class);
	private Gson gson = new Gson();
	private OntologyClassFilter classFilter = new OntologyClassFilter();
	private int facetPosition = 0;
	private Comparator<String> synonymComparator = new OntologyClassSynonymComparator();
	private ITermDatabaseImportService termImportService;

	public BioPortalTermImporter(ITermDatabaseImportService termImportService) {
		this.termImportService = termImportService;
	}

	public void doImport(File downloadedOntologyTermsDir, File downloadedOntologyInfoDir, Set<String> allowedAcronyms)
			throws IOException {
		boolean skipUnallowed = null != allowedAcronyms && !allowedAcronyms.isEmpty();
		if (skipUnallowed)
			log.info("Restricting imported ontologies to {}", allowedAcronyms);
		List<OntologyMetaData> ontologyMetaData = loadOntologyList(downloadedOntologyInfoDir);
		log.info("Found meta data for {} ontologies at {}", ontologyMetaData.size(), downloadedOntologyInfoDir);
		for (OntologyMetaData metaData : ontologyMetaData) {
			if (skipUnallowed && !allowedAcronyms.contains(metaData.acronym)) {
				log.debug("Skipping ontology {} because it is not contained in the set of allowed ontologies.",
						metaData.acronym);
				continue;
			}
			log.info("Loading ontology classes for ontology {}", metaData.acronym);
			try {
				List<OntologyClass> ontologyClasses = loadOntologyClasses(downloadedOntologyTermsDir, metaData);
				log.info("Importing ontology {} into Neo4j ({} classes).", metaData.acronym, ontologyClasses.size());

				log.info("Got {} ontology classes overall.", ontologyClasses.size());
				classFilter.filter(ontologyClasses);
				log.info("After filtering obsolete classes and applying the class filter, {} classes remain.", ontologyClasses.size());
				if (ontologyClasses.isEmpty()) {
					log.info("Skipping ontology because it doesn't have any classes after filtering.");
					continue;
				}
				log.info("Normalizing whitespaces of synonyms and filtering out too short synonyms...");
				normalizeSynonyms(ontologyClasses);
				log.info("Setting preferred labels where missing...");
				setPrefLabelWhereMissing(ontologyClasses);

				log.info("Doing the actual import...");
				for (int i = 0; i < ontologyClasses.size(); i += 1000) {
					List<OntologyClass> batchClasses = (i + 1000 >= ontologyClasses.size())
							?ontologyClasses.subList(i, ontologyClasses.size())
							:ontologyClasses.subList(i, i+1000);
							
					ImportConceptAndFacet dataMap = createTermsDataMap(batchClasses, metaData);
					log.info("Classes {} to {}", i+1, i+batchClasses.size());
					termImportService.importTerms(dataMap);
				}
			} catch (Exception e) {
				log.error("Could not load classes of ontology " + metaData.acronym + " due to error: ", e);
			}
		}
	}

	private void setPrefLabelWhereMissing(List<OntologyClass> ontologyClasses) {
		for (OntologyClass ontoClass : ontologyClasses) {
			if (StringUtils.isBlank(ontoClass.prefLabel)) {
				String prefLabel = getPrefLabel(ontoClass);
				ontoClass.prefLabel = prefLabel;
			}
			ontoClass.prefLabel = StringUtils.normalizeSpace(ontoClass.prefLabel);
		}
	}

	/**
	 * Normalized spaces on synonyms and removes synonyms that equal the preferred name.
	 * @param ontologyClasses The ontology classes meant for import.
	 */
	private void normalizeSynonyms(List<OntologyClass> ontologyClasses) {
		for (OntologyClass ontoClass : ontologyClasses) {
			List<String> synonyms = ontoClass.synonym.synonyms;
			if (null != synonyms) {
				List<String> newSynonyms = new ArrayList<>(synonyms.size());
				Collections.sort(synonyms, synonymComparator);
				for (int i = 0; i < synonyms.size(); i++) {
					String newSynonym = StringUtils.normalizeSpace(synonyms.get(i));
					if (newSynonym.length() > 1 && !newSynonym.equals(ontoClass.prefLabel))
						newSynonyms.add(newSynonym);
				}
				ontoClass.synonym.synonyms = newSynonyms;
			}
		}
	}

	/**
	 * Get the preferred label of the class <tt>ontoClass</tt>. If it does not
	 * exist - or is empty - the ID part of the URI is taken as the preferred
	 * name. If, however, this part does too much look like an ID and not a
	 * name, the shortest description is used as preferred term. <br/>
	 * TODO: Why don't look at synonyms...? This should be done before the
	 * descriptions!
	 * 
	 * @param ontoClass
	 * @return
	 */
	private static String getPrefLabel(OntologyClass ontoClass) {
		String prefLabel = ontoClass.prefLabel;
		if (StringUtils.isBlank(prefLabel)) {
			prefLabel = ontoClass.id.substring(ontoClass.id.lastIndexOf('#') + 1);
			if (ontoClass.definition != null && !ontoClass.definition.isEmpty()) {
				int minlengthIndex = 0;
				int minlength = Integer.MAX_VALUE;
				for (int i = 0; i < ontoClass.definition.size(); i++) {
					String definition = ontoClass.definition.get(i);
					if (definition.length() < minlength) {
						minlength = definition.length();
						minlengthIndex = i;
					}
				}
				String minlengthDef = ontoClass.definition.get(minlengthIndex);
				// The preferred label should be replaced by the shortest
				// description if the current prefLabel - i.e.
				// the URI fragment - contains an underscore "_" and the second
				// part contains numbers. We assume that strings of this form
				// are mostly some kind of ID and thus not a
				// good preferred Label. There are only 35 cases where there is
				// no preferred label defined, so this part is not too
				// important.
				if (prefLabel.contains("_")) {
					String[] split = prefLabel.split("_");
					if (split[1].matches(".*[0-9].*"))
						prefLabel = minlengthDef;
				}
			}
		}
		return StringUtils.trim(StringUtils.normalizeSpace(prefLabel));
	}

	private ImportConceptAndFacet createTermsDataMap(Collection<OntologyClass> terms, OntologyMetaData meta) {
		List<ImportConcept> importTerms = new ArrayList<>();
		ImportConceptAndFacet termAndFacet;

		switch (meta.acronym) {
		case "MESH":
			for (OntologyClass term : terms) {
				ImportConcept importTerm = createImportTerm(term, meta);
				String[] uriParts = importTerm.coordinates.sourceId.split("/");
				String ui = uriParts[uriParts.length - 1];
				if (ui.startsWith("D") || ui.startsWith("C")) {
					importTerm.coordinates.originalId = ui;
					// TODO This is wrong of course! As soon we want to disclose
					// the (original) source(s) of terms, this
					// would be a problem.
					importTerm.coordinates.originalSource = meta.acronym;
				}
				importTerm.addGeneralLabel(ResourceTermLabels.IdMapping.ID_MAP_MESH.toString());
				importTerms.add(importTerm);
			}

			termAndFacet = createImportTermAndFacet(meta, "MeSH", importTerms);
			termAndFacet.facet.searchFieldNames.add(IIndexInformationService.GeneralIndexStructure.mesh);
			break;
		case "NCBITAXON":
			for (OntologyClass term : terms) {
				ImportConcept importTerm = createImportTerm(term, meta);
				// IDs look like this:
				// http://purl.bioontology.org/ontology/NCBITAXON/131567
				String[] uriParts = importTerm.coordinates.sourceId.split("/");
				String ui = uriParts[uriParts.length - 1];
				importTerm.coordinates.originalId = ui;
				// importTerm.addGeneralLabel(ResourceTermLabels.IdMapping.ID_MAP_NCBI_TAXONOMY.toString());
				// TODO This is wrong of course! As soon we want to dispose the
				// (original) source(s) of terms, this
				// would be a problem.
				importTerm.coordinates.originalSource = meta.acronym;
				importTerms.add(importTerm);
			}

			termAndFacet = createImportTermAndFacet(meta, "NCBI Taxonomy", importTerms);
			break;
		default:
			for (OntologyClass term : terms) {
				ImportConcept importTerm = createImportTerm(term, meta);
				importTerms.add(importTerm);
			}
			termAndFacet = createImportTermAndFacet(meta, null, importTerms);
		}

		return termAndFacet;
	}

	protected ImportConcept createImportTerm(OntologyClass term, OntologyMetaData meta) {
		List<String> parents = term.parents.parents;
		List<ConceptCoordinates> parentCoords = new ArrayList<>();
		if (null == parents) {
			parents = Collections.emptyList();
		}
		for (String parent : parents) {
			parentCoords.add(new ConceptCoordinates(parent, "BioPortal", true));
		}
		ImportConcept importTerm = new ImportConcept(term.prefLabel, term.definition, term.synonym.synonyms,
				new ConceptCoordinates(term.id ,meta.acronym, true),
				parentCoords);
		importTerm.addGeneralLabel(ResourceTermLabels.Gazetteer.GAZETTEER_BIOPORTAL.toString());
		return importTerm;
	}

	protected ImportConceptAndFacet createImportTermAndFacet(OntologyMetaData meta, String shortFacetName, List<ImportConcept> importTerms) {
		List<String> searchFieldNames = Lists.newArrayList(IIndexInformationService.GeneralIndexStructure.title,
				IIndexInformationService.GeneralIndexStructure.abstracttext);
		List<String> facetGeneralLabels = Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS.toString(),
				FacetLabels.General.USE_FOR_QUERY_DICTIONARY.toString(),
				FacetLabels.General.FACET_BIO_PORTAL.toString(), FacetLabels.General.USE_FOR_BTERMS.toString());
		// We can't use dashes because the cssId is also used as a JavaScript
		// identifier in Semedico where dashes in
		// variable names are not allowed (just as in Java).
		String cssId = BIO_PORTAL_CSS_PREFIX + meta.acronym.replaceAll("-", "_");
		Integer position = facetPosition++;
		String sourceType = FacetConstants.SRC_TYPE_HIERARCHICAL;
		String facetName = meta.name;
		String effectiveShortFacetName = null != shortFacetName ? shortFacetName : meta.acronym;

		// String facetGroupName = BIO_PORTAL_FACET_GROUP_NAME;
		// TODO for the moment we just integrate used BioPortal ontologies in
		// our BioMed facet group...
		String facetGroupName = "BioMed";
		Integer facetGroupPosition = 4;
		List<String> facetGroupLabels = new ArrayList<>();
		facetGroupLabels.add(FacetGroupLabels.General.SHOW_FOR_SEARCH.toString());
		// Not for the moment
		// facetGroupLabels.add(FacetGroupLabels.General.SHOW_FOR_BTERMS.toString());

		ImportFacetGroup importFacetGroup = new ImportFacetGroup(facetGroupName, facetGroupPosition, facetGroupLabels);
		importFacetGroup.type = FacetGroupLabels.Type.BIO_PORTAL.name();

		BioPortalImportFacet importFacet = new BioPortalImportFacet(facetName, cssId, sourceType, searchFieldNames,
				null, position, facetGeneralLabels, importFacetGroup);
		importFacet.acronym = meta.acronym;
		importFacet.iri = meta.id;
		importFacet.noFacet = meta.acronym.equals("GAZ");
		importFacet.shortName = effectiveShortFacetName;

		ImportOptions importOptions = new ImportOptions();
		importOptions.cutParents = Lists.newArrayList("http://www.w3.org/2002/07/owl#Thing",
				"http://www.onto-med.de/ontologies/gfo.owl#Entity");
//		importOptions.noFacetCmd = new AddToNonFacetGroupCommand();
//		importOptions.noFacetCmd.addParentCriterium(AddToNonFacetGroupCommand.ParentCriterium.NO_PARENT);
		// importOptions.noFacetCmd.addGeneralFacetPropertiesCriterium(importOptions.noFacetCmd.new
		// FacetPropertyValueCriterium(BioPortal.acronym.toString(), "GAZ"));
		// For external classes referenced from BioPortal ontologies, we allow
		// for "hollow" terms, i.e. terms that are
		// referenced as a parent but are not defined in an ontology itself.
		// importOptions.createHollowParents = true;

		ImportConceptAndFacet termAndFacet = new ImportConceptAndFacet(importTerms, importFacet, importOptions);
		return termAndFacet;
	}

	private List<OntologyClass> loadOntologyClasses(File downloadedOntologyTermsDir, OntologyMetaData metaData)
			throws IOException {
		List<OntologyClass> classes = new ArrayList<>();
		File[] matches = downloadedOntologyTermsDir.listFiles((dir, name) -> name.startsWith(metaData.acronym + BioPortalToolConstants.CLASSES_EXT));
		if (matches.length == 0) {
			log.warn(
					"No classes for ontology {} could be found, because the file {} does not exist (ontology not found on BioPortal servers).",
					metaData.acronym, downloadedOntologyTermsDir.getAbsolutePath() + File.separator + metaData.acronym + BioPortalToolConstants.CLASSES_EXT);
			return Collections.emptyList();
		}
		File ontologyClassesFile = matches[0];
		if (ontologyClassesFile.length() == 0) {
			log.warn(
					"No classes for ontology {} could be found, because it is empty (sometimes an ontology is found at BioPortal but just has no classes to download).",
					metaData.acronym);
			return Collections.emptyList();
		}

		return loadOntologyClassesFromFile(classes, ontologyClassesFile);
	}

	private List<OntologyClass> loadOntologyClassesFromFile(List<OntologyClass> classes, File ontologyClassesFile)
			throws IOException {
		InputStream is = new FileInputStream(ontologyClassesFile);
		if (ontologyClassesFile.getName().endsWith("gz")) {
			is = new GZIPInputStream(is);
		}
		LineIterator lineIt = IOUtils.lineIterator(is, "UTF-8");
		while (lineIt.hasNext()) {
			String ontologyClassLine = lineIt.nextLine();
			OntologyClass ontologyClass = gson.fromJson(ontologyClassLine, OntologyClass.class);
			if (null == ontologyClass.parents) {
				ontologyClass.parents = OntologyClassParents.EMPTY_PARENTS;
				ontologyClass.parents.parents = Collections.emptyList();
			}
			classes.add(ontologyClass);
		}

		return classes;
	}

	private List<OntologyMetaData> loadOntologyList(File ontologyInfoDir)
			throws IOException {
		List<OntologyMetaData> ontologyMetaData = new ArrayList<>();
		
		File[] metaFiles = ontologyInfoDir.listFiles((dir, name) -> 
				name.contains(BioPortalToolConstants.METADATA_EXT));

		for (int i = 0; i < metaFiles.length; i++) {
			File metaFile = metaFiles[i];
			InputStream is = new FileInputStream(metaFile); 
				if (metaFile.getName().endsWith(".gz"))
					is = new GZIPInputStream(is);
			OntologyMetaData metaData = gson.fromJson(new InputStreamReader(is), OntologyMetaData.class);
			ontologyMetaData.add(metaData);
		}
		
		return ontologyMetaData;
	}

	public static RuleBasedCollator createComparisonCollator() {
		try {
			// default rules
			RuleBasedCollator collator = new RuleBasedCollator("");
			// only primary differences matter, i.e. we don't care for
			// differences in case or accents.
			collator.setStrength(Collator.PRIMARY);
			collator.freeze();
			return collator;
		} catch (Exception e) {
			log.error("Error: ", e);
		}
		return null;
	}

}
