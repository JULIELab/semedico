package de.julielab.semedico.resources;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bioc.BioCCollection;
import bioc.BioCDocument;
import bioc.BioCPassage;
import bioc.io.BioCCollectionWriter;
import bioc.io.BioCFactory;

public class BioCPMCDevDocExtractor implements IBioCPMCDevDocExtractor {

	private static final Logger log = LoggerFactory.getLogger(BioCPMCDevDocExtractor.class);

//	public BioCPMCDevDocExtractor(Logger log) {
//		this.log = log;
//	}

	public static void extractSemedicoDevDocuments2(File pmcBiocUnicodeDir, File outputFile, Set<String> docIds,
			IdSource source) {
		// String forEachXpath = "/collection/document";
		// List<Map<String, String>> fields = new ArrayList<>();
		// Map<String, String> field;
		//
		// field = new HashMap<>();
		// field.put(JulieXMLConstants.NAME, "docId");
		// switch (source) {
		// case PMC:
		// field.put(JulieXMLConstants.XPATH,
		// "passage/infon[@key=\"article-id_pmc\"]");
		// break;
		// case PUBMED:
		// field.put(JulieXMLConstants.XPATH,
		// "passage/infon[@key=\"article-id_pmid\"]");
		// break;
		// }
		// fields.add(field);
		//
		// field = new HashMap<>();
		// field.put(JulieXMLConstants.NAME, "xml");
		// field.put(JulieXMLConstants.XPATH, ".");
		// field.put(JulieXMLConstants.RETURN_XML_FRAGMENT, "true");
		// fields.add(field);

		File[] biocXmlFiles = pmcBiocUnicodeDir.listFiles((dir, name) -> name.endsWith(".xml"));

		log.info("Found {} XML files in directory {}", biocXmlFiles.length, pmcBiocUnicodeDir.getAbsolutePath());
		BioCFactory biocfactory = BioCFactory.newFactory(BioCFactory.STANDARD);
		BioCCollection selectedCollection = new BioCCollection();
		try {
			// try (OutputStream os = new FileOutputStream(outputFile)) {
			// IOUtils.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			// "<!DOCTYPE collection SYSTEM \"BioC.dtd\">\n" + "<collection>",
			// os, "UTF-8");
			int numFound = 0;
			for (int i = 0; i < biocXmlFiles.length; i++) {
				File file = biocXmlFiles[i];
				Iterator<BioCDocument> it = biocfactory.createBioCDocumentReader(new FileReader(file)).iterator();
				// Iterator<Map<String, Object>> it =
				// JulieXMLTools.constructRowIterator(file.getAbsolutePath(),
				// 1024, forEachXpath, fields, false);
				while (it.hasNext()) {
					BioCDocument doc = it.next();
					// Map<String, Object> row = (Map<String, Object>)
					// it.next();
					// String docId = (String) row.get("docId");
					String pmcId = doc.getID();
					String pmid = determinePmid(doc);

					String docId = source == IdSource.PMC ? pmcId : pmid;
					// in case of PMIDs it might happen that the current
					// document
					// does not have an ID; as such, it can't be a candidate for
					// selection by PMID
					if (StringUtils.isBlank(docId))
						continue;
					if (docIds.contains(docId)) {
						// String xml = (String) row.get("xml");
						// IOUtils.write(xml, os, "UTF-8");
						selectedCollection.addDocument(doc);
						++numFound;
					}
					if (numFound == docIds.size())
						break;
				}
				if (numFound == docIds.size())
					break;
				System.out.print(".");
			}
			try (BioCCollectionWriter biocwriter = biocfactory.createBioCCollectionWriter(new FileWriter(outputFile))) {
				biocwriter.writeCollection(selectedCollection);
			} catch (XMLStreamException e) {
				log.error("XMLStreamException: ", e);
			}
			log.info("Done.");
			// IOUtils.write("</collection>", os, "UTF-8");
			log.info("Wrote {} selected documents out of {} specified IDs to {}",
					new Object[] { numFound, docIds.size(), outputFile.getAbsolutePath() });
		} catch (IOException e) {
			log.error("IOException: ", e);
		}
	}

	private static String determinePmid(BioCDocument doc) {
		List<BioCPassage> passages = doc.getPassages();
		for (BioCPassage passage : passages) {
			String pmcid = passage.getInfon("article-id_pmc");
			if (null != pmcid) {
				String pmid = passage.getInfon("article-id_pmid");
				return pmid;
			}
		}
		return null;
	}

	@Override
	public void extractSemedicoDevDocuments(File pmcBiocUnicodeDir, File outputFile, Set<String> docIds,
			IdSource source) {
		// TODO Auto-generated method stub
		
	}
}
