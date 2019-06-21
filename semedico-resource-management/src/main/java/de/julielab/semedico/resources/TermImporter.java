package de.julielab.semedico.resources;

import de.julielab.bioportal.ontologies.BioPortalToolConstants;
import de.julielab.semedico.bioportal.BioPortalTermImporter;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import de.julielab.semedico.mesh.Tree;
import de.julielab.semedico.mesh.exchange.DataExporter;
import de.julielab.semedico.mesh.exchange.DataImporter;
import de.julielab.semedico.resources.terms.NCBIGeneImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TermImporter implements ITermImporter {

	private static final Logger logger = LoggerFactory.getLogger(TermImporter.class);
	private ITermDatabaseImportService termImportService;

	public TermImporter(ITermDatabaseImportService termImportService) {
		this.termImportService = termImportService;

	}

	public void importTerms(String[] sourceFiles) throws Exception {
		if (sourceFiles.length == 0)
			throw new IllegalArgumentException("You have to provide the source file(s) for terms to be imported.");
		String termFileString = sourceFiles[0];
		try {
			ImportSource termSource = determineTermSource(termFileString);
			switch (termSource) {
			case NCBI_GENE:
				if (sourceFiles.length < 6)
					throw new IllegalArgumentException(
							"For the import of the NCBI gene database you have to provide the files gene_info, organisms.tax, names.dmp, gene2summary, homologene.data and gene_group");
				String geneInfo = termFileString;
				String organisms = sourceFiles[1];
				String ncbiTaxNames = sourceFiles[2];
				String geneSummary = sourceFiles[3];
				String homologene = sourceFiles[4];
				String geneGroup = sourceFiles[5];
				NCBIGeneImporter ncbiGeneImporter = new NCBIGeneImporter(termImportService);
				ncbiGeneImporter.doImport(geneInfo, organisms, ncbiTaxNames, geneSummary, homologene, geneGroup);
				break;
			case BIO_PORTAL:
				if (sourceFiles.length < 2)
					throw new IllegalArgumentException(
							"For the import of BioPortal ontologies, the extracted ontology names in JSON format are required as well as the JSON ontology meta information directory. Both is created via the julielab-bioportal-ontology-tools.");
				BioPortalTermImporter bioPortalTermImporter = new BioPortalTermImporter(termImportService);
				File ontologiesInfoDir = new File(sourceFiles[1]);
				if (!ontologiesInfoDir.isDirectory()) {
					throw new IllegalArgumentException(
							"For the import of BioPortal ontologies, the extracted ontology names in JSON format are required as well as the JSON ontology meta information directory. Both is created via the julielab-bioportal-ontology-tools.");
				}
				Set<String> allowedAcronyms = Collections.emptySet();
				if (sourceFiles.length > 2) {
					allowedAcronyms = new HashSet<>();
					for (int i = 2; i < sourceFiles.length; ++i) {
						allowedAcronyms.add(sourceFiles[i]);
					}
				}
				bioPortalTermImporter.doImport(new File(termFileString), ontologiesInfoDir, allowedAcronyms);
				break;
			case SEMEDICO_XML_FILE:
			case SEMEDICO_XML_DIR: {
				String supplementariesFileString = sourceFiles.length < 2 ? null : sourceFiles[1];
				if (null == supplementariesFileString) {
					System.out.println(
							"Semedico-MeSH terms should be imported but the path to the MeSH Supplementary Concepts was not given. You should find the Supplementary Concepts XML file in a subdirectory under /data/data_resources/biology/MeSH in the JULIE Lab NFS. Do you really want to proceed? (y/n)");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String line;
					while (!(line = br.readLine()).equals("y") && !line.equals("n")) {
						System.out.println("Please specify 'y' for yes or 'n' for no.");
					}
					if (line.equals("n")) {
						logger.info("Aborting due to user request.");
						break;
					}
				}
				Tree semedicoMesh = new Tree("Semedico-MeSH");
				DataImporter.fromUserDefinedMeshXml(termFileString, semedicoMesh);
				if (null != supplementariesFileString) {
					semedicoMesh.renameDescriptorWithUi("D010472", "Perchlorates");
					DataImporter.fromSupplementaryConceptsXml(supplementariesFileString, semedicoMesh);
				}
				semedicoMesh.verifyIntegrity();

				// for debug: Print out facets and facet roots for a quick check
				// whether everything looks OK.
				// for (Descriptor d :
				// semedicoMesh.childDescriptorsOf(semedicoMesh.getRootDesc()))
				// {
				// System.out.println(d.getName());
				// for (Descriptor c : semedicoMesh.childDescriptorsOf(d))
				// System.out.println("\t" + c);
				// }

				DataExporter.toNeo4j(semedicoMesh, termSource, termImportService);
				break;
			}
			case SEMEDICO_IMMUNOLY_FILE:
			case SEMEDICO_IMMUNOLY_DIR: {
				Tree semedicoMesh = new Tree("Semedico-MeSH");
				DataImporter.fromUserDefinedMeshXml(termFileString, semedicoMesh);
				semedicoMesh.verifyIntegrity();

				DataExporter.toNeo4j(semedicoMesh, termSource, termImportService);
				break;
			}
			case MESH_XML:
				// NOT USED SINCE MESH IS INCLUDED IN BIOPORTAL
				File meshDir = new File(termFileString);
				String descriptorsPath = null;
				String supplementariesPath = null;
				File[] meshFiles = meshDir.listFiles();
				for (int i = 0; i < meshFiles.length; i++) {
					File meshFile = meshFiles[i];
					String meshFileName = meshFile.getName();
					if (meshFileName.startsWith("desc")
							&& (meshFileName.endsWith("xml") || meshFileName.endsWith("gz"))) {
						descriptorsPath = meshFile.getAbsolutePath();
					}
					if (meshFileName.startsWith("supp")
							&& (meshFileName.endsWith("xml") || meshFileName.endsWith("gz"))) {
						supplementariesPath = meshFile.getAbsolutePath();
					}
				}
				Tree mesh = new Tree("MeSH Descriptors and Supplementaries");
				DataImporter.fromOriginalMeshXml(descriptorsPath, mesh, true);
				DataImporter.fromSupplementaryConceptsXml(supplementariesPath, mesh);
				mesh.verifyIntegrity();
				DataExporter.toNeo4j(mesh, termSource, termImportService);
				break;
			case UNKNOWN:
				break;
			default:
				break;

			}
		} catch (IOException | SAXException e) {
			logger.error("SAXException or IOException: ", e);
		}
	}

	protected ImportSource determineTermSource(String termFileString) {
		ImportSource source = ImportSource.UNKNOWN;

		File termFile = new File(termFileString);

		// Is it NCBI gene (gene_info)?
		if (source == ImportSource.UNKNOWN && termFile.getName().contains("gene_info")) {
			source = ImportSource.NCBI_GENE;
		}
		// Is it BioPortal?
		if (source == ImportSource.UNKNOWN && termFile.isDirectory()) {
			String[] list = termFile.list((dir, name) ->
							name.contains(BioPortalToolConstants.CLASSES_EXT));

			if (list.length > 0)
				source = ImportSource.BIO_PORTAL;
		}
		// Is it the MESH in XML format?
		if (source == ImportSource.UNKNOWN && termFile.isDirectory()) {
			// descriptors
			boolean descFound = false;
			// supplementaries
			boolean suppFound = false;
			String[] list = termFile.list();
			for (int i = 0; i < list.length; i++) {
				String filename = list[i];
				if (filename.startsWith("desc"))
					descFound = true;
				if (filename.startsWith("supp"))
					suppFound = true;
			}
			if (descFound && suppFound)
				source = ImportSource.MESH_XML;
		}
		if (source == ImportSource.UNKNOWN && termFile.isDirectory()) {
			if (termFileString.contains("immunology")) {
				String[] list = termFile.list((dir, name) ->
						 name.endsWith("xml"));

				if (list.length > 0)
					source = ImportSource.SEMEDICO_IMMUNOLY_DIR;
			}
		}
		if (source == ImportSource.UNKNOWN && termFile.isFile()) {
			if (termFileString.contains("immunology")) {
				if (termFile.getName().endsWith("xml"))
					source = ImportSource.SEMEDICO_IMMUNOLY_FILE;
			}
		}
		// Is it a directory with XML files? Then we assume Semedico XML.
		if (source == ImportSource.UNKNOWN && termFile.isDirectory()) {
			String[] list = termFile.list((File dir, String name) ->
								name.endsWith("xml"));

			if (list.length > 0)
				source = ImportSource.SEMEDICO_XML_DIR;

		}
		// Is it a single XML file? Then this could be a single Semedico XML
		// file.
		if (source == ImportSource.UNKNOWN && termFile.isFile()) {
			if (termFile.getName().endsWith("xml"))
				source = ImportSource.SEMEDICO_XML_FILE;
		}
		logger.info("Determined source of term input: {}", source);
		return source;
	}

}
