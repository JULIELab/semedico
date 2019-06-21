package de.julielab.semedico.mesh.exchange;

import com.google.common.collect.LinkedHashMultimap;
import de.julielab.neo4j.plugins.datarepresentation.*;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import de.julielab.semedico.mesh.FacetsProvider;
import de.julielab.semedico.mesh.Tree;
import de.julielab.semedico.mesh.components.*;
import de.julielab.semedico.mesh.modifications.DescAdditions;
import de.julielab.semedico.mesh.tools.ProgressCounter;
import de.julielab.semedico.resources.ImportSource;
import de.julielab.semedico.resources.ResourceTermLabels;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;

/**
 * This class deals with exporting a <code>Tree</code> object into various
 * formats and/or targets.
 * 
 * @author Philipp Lucas
 */
public class DataExporter {

	private static Logger logger = org.slf4j.LoggerFactory.getLogger(DataExporter.class);

	/**
	 * Exports tree into a file at file path in the DOT format.
	 * 
	 * @param tree
	 *            Tree to export.
	 * @param filepath
	 *            File path to save the exported data at.
	 */
	public static void toDOT(Tree tree, String filepath) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Exports tree into easy to understand file format.
	 * 
	 * @param tree
	 *            Tree to export.
	 * @param filepath
	 *            File path to save the exported data at.
	 */
	public static void toOwnTxt(Tree tree, String filepath) {
		logger.info("# Exporting data to own simple file format '{}' ... ", filepath);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {

			// get all descriptors
			Collection<Descriptor> allDesc = tree.getAllDescriptors();
			ProgressCounter counter = new ProgressCounter(allDesc.size(), 10, "descriptor");
			counter.startMsg();

			for (Descriptor desc : allDesc) {
				writer.write(desc.tofullString(tree));
				writer.write("\n");
				counter.inc();
			}

		} catch (IOException e) {
			logger.error("Error writing to file: ", e);
		}

		logger.info("# ... done.");
	}

	/**
	 * <p>
	 * Exports the descriptors <code>descs</code> to file <code>filename</code>.
	 * Format will be "OwnXML" format.
	 * </p>
	 * 
	 * <p>
	 * The locations provided by <code>desc2locations</code> will be used
	 * instead of any other possible real locations of the descriptors, in case
	 * the descriptors are part of a tree. Note that there is no need for a
	 * descriptor to be part of a tree.
	 * </p>
	 * 
	 * @param locations
	 *            The locations for each descriptor.
	 * @param descs
	 *            A set of descriptors of <code>tree</code>
	 * @param filename
	 *            A filename to export the descriptors to.
	 */
	public static void toOwnXml(DescAdditions desc2locations, String filename) {
		File ownXmlFile = new File(filename);
		File parentDir = ownXmlFile.getParentFile();
		if (!parentDir.isDirectory() && !parentDir.mkdirs()) {
			logger.error("Can't create directory {} to store {}", parentDir, filename);
			return;
		}

		try (OutputStream bufferedOut = new BufferedOutputStream(new FileOutputStream(filename))) {
			Set<Descriptor> descsSortedList = desc2locations.keySet();
			// sort collection by descriptor name
			// List<Descriptor> descsSortedList = new
			// ArrayList<Descriptor>(desc2locations.keySet());
			// Collections.sort(descsSortedList, new
			// DescriptorNameComparator());

			logger.info("# Exporting " + desc2locations.size() + " descriptors to ownXML file '" + filename + "' ... ");

			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			// factory.setProperty(OutputKeys.INDENT, "yes"); // doesn't work
			XMLStreamWriter writer = factory.createXMLStreamWriter(bufferedOut, "UTF-8");

			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("DescriptorRecordSet");

			ProgressCounter counter = new ProgressCounter(descsSortedList.size(), 10, "descriptor");
			for (Descriptor desc : descsSortedList) {
				writeDescToOwnXml(desc2locations.get(desc), desc, writer);
				counter.inc();
			}

			writer.writeEndElement();
			writer.writeEndDocument();

			writer.flush();
			writer.close();

			logger.info("# ... done.");
		} catch (XMLStreamException e) {
			System.err.println(e.getStackTrace());
			System.err.println(e.getMessage());
		} catch (FileNotFoundException e) {
			System.err.println("File '" + filename + "' not found.");
		} catch (IOException e) {
			System.err.println("general IO Exception: " + e.getMessage());
		}
	}

	/**
	 * Exports all descriptors in <code>data</code> to file
	 * <code>filename</code>. Format will be "OwnXML".
	 * 
	 * @param data
	 *            A <code>Tree</code> object.
	 * @param filename
	 *            A filename to export the descriptors to.
	 */
	public static void toOwnXml(Tree data, String filename) {
		DescAdditions descAdds = new DescAdditions();
		for (Descriptor desc : data.getAllDescriptors()) {
			VertexLocations locs = new VertexLocations();
			descAdds.put(desc, locs);
			for (TreeVertex v : desc.getTreeVertices()) {
				TreeVertex parent = data.parentVertexOf(v);

				if (parent == null) {
					locs.put(v.getName(), null);
				} else {
					locs.put(v.getName(), parent.getName());
				}
			}
		}
		toOwnXml(descAdds, filename);
	}

	/**
	 * Writes out the given descriptor to the writer...
	 * 
	 * @param data
	 * @param desc
	 * @param writer
	 * @throws XMLStreamException
	 */
	private static void writeDescToOwnXml(VertexLocations locations, Descriptor desc, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("DescriptorRecord");
		writer.writeCharacters("\n");

		// DescriptorUI
		writer.writeStartElement("DescriptorUI");
		writer.writeCharacters(desc.getUI());
		writer.writeEndElement();
		writer.writeCharacters("\n");

		// Locations
		writer.writeStartElement("LocationList");
		writer.writeCharacters("\n");
		for (String vertexName : locations.getVertexNameSet()) {
			writer.writeStartElement("Location");
			writer.writeCharacters("\n");

			writer.writeStartElement("VertexName");
			writer.writeCharacters(vertexName);
			writer.writeEndElement();
			writer.writeCharacters("\n");

			String parentName = locations.get(vertexName);
			if (parentName != null && !parentName.isEmpty()) {
				writer.writeStartElement("ParentVertexName");
				writer.writeCharacters(parentName);
				writer.writeEndElement();
				writer.writeCharacters("\n");
			}

			writer.writeEndElement();
			writer.writeCharacters("\n");
		}
		writer.writeEndElement();
		writer.writeCharacters("\n");

		// Concepts & Terms
		writer.writeStartElement("ConceptList");
		writer.writeCharacters("\n");
		for (Concept concept : desc.getConcepts()) {
			writer.writeStartElement("Concept");
			writer.writeAttribute("PreferredConceptYN", (concept.isPreferred() ? "Y" : "N"));
			writer.writeCharacters("\n");

			writer.writeStartElement("ScopeNote");
			writer.writeCharacters(desc.getScopeNote());
			writer.writeEndElement();
			writer.writeCharacters("\n");

			writer.writeStartElement("TermList");
			writer.writeCharacters("\n");
			for (Term term : concept.getTerms()) {
				writer.writeStartElement("Term");
				writer.writeAttribute("ConceptPreferredTermYN", (term.isPreferred() ? "Y" : "N"));
				writer.writeCharacters("\n");

				// writer.writeStartElement("TermUI");
				// writer.writeCharacters(term.getID());
				// writer.writeEndElement(); writer.writeCharacters("\n");

				writer.writeStartElement("String");
				writer.writeCharacters(term.getName());
				writer.writeEndElement();
				writer.writeCharacters("\n");

				writer.writeEndElement();
				writer.writeCharacters("\n");
			}
			writer.writeEndElement();
			writer.writeCharacters("\n");

			writer.writeEndElement();
			writer.writeCharacters("\n");
		}
		writer.writeEndElement();
		writer.writeCharacters("\n");

		writer.writeEndElement();
		writer.writeCharacters("\n");
	}

	/**
	 * 
	 * @param tree
	 *            data tree to be imported into Neo4j
	 * @param importSource
	 * @param termImportService
	 *            HTTP address to the Neo4j server, e.g.
	 *            <tt>http://myhost:7474/</tt>.
	 */
	public static void toNeo4j(final Tree tree, ImportSource importSource,
			ITermDatabaseImportService termImportService) {

		// Sanity check.
		List<Descriptor> rootChildren2 = tree.childDescriptorsOf(tree.getRootDesc());
		for (Descriptor facet : rootChildren2) {
			if (facet.getName().startsWith("Facet"))
				;// System.out.println(facet.getUI() + " is facet");
			else
				throw new IllegalStateException(
						"There is at least one top-level node - i.e. a node directly under the technical root node - that is no facet: "
								+ facet);
		}

		FacetsProvider facetsProvider = new FacetsProvider(tree);

		// get all descriptors sorted by it's heights
		List<Descriptor> allDesc = tree.getAllDescriptorsByHeight();

		// root node should be the first one, so just delete it
		if (allDesc.get(0).equals(tree.getRootDesc())) {
			allDesc.remove(0);
		} else {
			logger.error(
					"ERROR: implementation of getAllDescriptorsByHeight has changed. Cannot remove root descriptor anymore.");
		}

		logger.info("Organizing descriptors by facet, i.e. each descriptor's MeSH root term...");
		ProgressCounter counter = new ProgressCounter(allDesc.size(), 10000, "descriptor");
		counter.startMsg();
		// Use a LINKEDHashMultimap to keep the import order constant over
		// multiple imports
		LinkedHashMultimap<String, Descriptor> facet2Desc = LinkedHashMultimap.create();
		List<Descriptor> facetDescriptors = tree.childDescriptorsOf(tree.getRootDesc());
		for (Descriptor desc : allDesc) {
			Set<String> facets = facetsProvider.getFacets(desc);

			// Is this perhaps a facet itself?
			if (facetDescriptors.contains(desc))
				continue;

			for (String facet : facets) {
				facet2Desc.put(facet, desc);
			}
			counter.inc();
		}
		counter.finishMsg();

		// only using linked hash maps does not seem to keep import order
		// constant. So just sort everything before
		// importing to somehow reach a constant import order
		List<String> facetNames = new ArrayList<>(facet2Desc.keySet());
		Collections.sort(facetNames);

		for (String facetName : facetNames) {
			Collection<Descriptor> descriptorsInFacet = facet2Desc.get(facetName);
			List<Descriptor> sortedDescriptorsInFacet = new ArrayList<>(descriptorsInFacet);
//			Collections.sort(sortedDescriptorsInFacet, new DescriptorHeightComparator(tree));
			counter = new ProgressCounter(sortedDescriptorsInFacet.size(), 1000, "Semedico term");
			counter.startMsg();
			logger.info("Converting descriptors for facet {} to Semedico terms...", facetName);
			ImportFacet importFacet;
			switch (importSource) {
			case MESH_XML:
				importFacet = FacetsProvider.createMeshImportFacet(facetName);
				break;
			case SEMEDICO_IMMUNOLY_DIR:
			case SEMEDICO_IMMUNOLY_FILE:
			case SEMEDICO_XML_DIR:
			case SEMEDICO_XML_FILE:
				importFacet = FacetsProvider.createSemedicoImportFacet(facetName);
				break;
			default:
				throw new IllegalArgumentException(
						"This term import algorithm is not applicable to import source " + importSource);
			}
			List<ImportConcept> importTerms = new ArrayList<>();
			ImportConceptAndFacet importTermAndFacet = new ImportConceptAndFacet(importTerms, importFacet);
			// Allow hollow parents because some parents are distributed into
			// other facets.
			importTermAndFacet.importOptions = new ImportOptions(true);
			for (Descriptor desc : sortedDescriptorsInFacet) {
				String termId = desc.getUI();
				String preferredName = desc.getPrefConcept().getPrefTerm().getName();
				List<String> synonyms = desc.getSynonymNames();
				String description = desc.getScopeNote();
				List<ConceptCoordinates> parents = new ArrayList<>();

				// Determine parents.
				List<Descriptor> parentDescriptors = tree.parentDescriptorsOf(desc);
				for (Descriptor parentDescriptor : parentDescriptors) {
					// Exclude the facet nodes, they are no terms.
					if (!facetDescriptors.contains(parentDescriptor)) {
						if (importSource == ImportSource.SEMEDICO_IMMUNOLY_DIR ||
						importSource == ImportSource.SEMEDICO_IMMUNOLY_FILE) {
							parents.add(new ConceptCoordinates(parentDescriptor.getUI(), "Semedico Immunology Terms", parentDescriptor.getUI(), "MESH"));
						} else {
							parents.add(new ConceptCoordinates(parentDescriptor.getUI(), "MESH", parentDescriptor.getUI(), "MESH"));
						}
					}
				}
				ImportConcept term = new ImportConcept(preferredName, synonyms, description, new ConceptCoordinates(), parents);
				term.coordinates.sourceId = termId;
				if (term.coordinates.sourceId.matches("(D|C)[0-9]+")) {
					term.addGeneralLabel(ResourceTermLabels.IdMapping.ID_MAP_MESH.toString());
					// TODO This should be some kind of constant or identifier
					// to a "Source Node" in the database having
					// all
					// information about the actual source so it could be
					// displayed in Semedico.
					// Also, this value has to match the originalSource given to
					// MESH terms from BioPortal in
					// BioPortalTermImport for proper merging of the terms in
					// the Neo4j plugin.
					term.coordinates.originalSource = "MESH";
					if (importSource == ImportSource.MESH_XML) {
						term.coordinates.source = "MeSH";
					} else if (importSource == ImportSource.SEMEDICO_IMMUNOLY_DIR ||
							importSource == ImportSource.SEMEDICO_IMMUNOLY_FILE) {
						term.coordinates.source = "Semedico Immunology Terms";
					} else {
						term.coordinates.source = "Semedico Default Terms";
					}
				} else if (FacetsProvider.isImmunologyFacet(facetName)) {
					term.addGeneralLabel(ResourceTermLabels.IdMapping.ID_MAP_IMMUNOLOGY.toString());
					// TODO This should be some kind of constant or identifier
					// to a "Source Node" in the database having
					// all
					// information about the actual source so it could be
					// displayed in Semedico.
					term.coordinates.source = "Semedico Immunology Terms";
					if (!term.coordinates.sourceId.matches("(D|C)[0-9]+"))
						term.coordinates.originalSource = "Semedico Immunology Terms";
				} else {
					logger.warn("No ID map for term {} of source {}", termId, importSource);
					// the warning will apply to:
//					11:19:21 [main] WARN  d.j.s.mesh.exchange.DataExporter - No ID map for term dog of source SEMEDICO_XML_DIR
//					11:19:21 [main] WARN  d.j.s.mesh.exchange.DataExporter - No ID map for term human of source SEMEDICO_XML_DIR
//					11:19:21 [main] WARN  d.j.s.mesh.exchange.DataExporter - No ID map for term mouse of source SEMEDICO_XML_DIR
//					11:19:21 [main] WARN  d.j.s.mesh.exchange.DataExporter - No ID map for term rat of source SEMEDICO_XML_DIR
					// which should be okay since we use the NCBI Taxonomy for organisms
					// TODO This should be some kind of constant or identifier
					// to a "Source Node" in the database having
					// all
					// information about the actual source so it could be
					// displayed in Semedico.
					term.coordinates.source = "Semedico Default Terms";
					if (!term.coordinates.sourceId.matches("(D|C)[0-9]+"))
						term.coordinates.originalSource = "Semedico Default Terms";
				}
				// All Semedico terms use original IDs in their source files
				term.coordinates.originalId = termId;

				importTerms.add(term);
				counter.inc();
			}
			counter.finishMsg();

			logger.info("Importing facet {} and its {} terms into Neo4j on host {}.",
					new Object[] { facetName, importTerms.size(), termImportService.getDBHost() });
			// HttpEntity response =
			// neo4jAdapter.sendPostRequest(termImportService + "/" +
			// Neo4jService.TERM_MANAGER_ENDPOINT
			// + TermManager.INSERT_TERMS,
			// importTermAndFacet.toNeo4jRestRequest());
			String response = termImportService.importTerms(importTermAndFacet);
			logger.info("Server responded: {}", response);
		}
	}

	/**
	 * Exports tree into a file at file path in the SIF format.
	 * 
	 * @param tree
	 *            Tree to export.
	 * @param filepath
	 *            File path to save the exported data at.
	 */
	public static void toSIF(Tree tree, String filepath) {

		logger.info("# Exporting data to SIF file '{}' ... ", filepath);

		try (Writer fw = new BufferedWriter(new FileWriter(filepath))) {
			ProgressCounter counter = new ProgressCounter(tree.vertexSet().size(), 10, "tree vertex");
			counter.startMsg();

			// Breadth first traversion of tree
			TreeSet<String> sortSet = new TreeSet<>();
			BreadthFirstIterator<TreeVertex, DefaultEdge> iter = new BreadthFirstIterator<>(
					tree);
			while (iter.hasNext()) {
				TreeVertex v = iter.next();
				// TreeNumber treeNr = tree.treeNumberOf(v);
				// get all children and save edges with these in SIF file
				Set<DefaultEdge> outEdges = tree.outgoingEdgesOf(v);
				for (DefaultEdge e : outEdges) {
					TreeVertex child = tree.getEdgeTarget(e);
					// /TreeNumber childNr = tree.treeNumberOf(child);
					// write to file
					sortSet.add(v.getName() + " pointsAt " + child.getName() + "\n");
				}
				counter.inc();
			}

			// write out (thanks to TreeSet it is sorted!)
			for (String str : sortSet) {
				fw.write(str);
			}
		} catch (IOException e) {
			logger.error("Error writing to SIF file: " + e.getMessage());
		}
		logger.info("# ... done.");
	}

}