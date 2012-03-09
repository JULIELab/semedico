package de.julielab.semedico.core.services;

import static de.julielab.xml.XMLConstants.NAME;
import static de.julielab.xml.XMLConstants.RETURN_ARRAY;
import static de.julielab.xml.XMLConstants.RETURN_XML_FRAGMENT;
import static de.julielab.xml.XMLConstants.XPATH;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.TermFileEntry;
import de.julielab.xml.Utils;

public class TermFileReaderService implements Enumeration<TermFileEntry>,
		ITermFileReaderService {

	private Facet currentFacet;
	private IFacetService facetService;
	private Iterator<Map<String, Object>> termIterator = null;
	private Logger logger;

	public TermFileReaderService(Logger logger, IFacetService facetService)
			throws IOException {
		super();
		this.facetService = facetService;
		this.logger = logger;
	}

	@Override
	public void reset(String filePath) throws IOException {
		try {
			// Construct the record schema we want to retrieve.
			// Later (see at the end of the method) we will define
			// that we iterate over all /terms/term elements.
			// The following field definitions declare, which information
			// we'd like to extract from each of these term elements and where
			// to find it (by XPath).
			// We give a name to each field, thus declaring something like a
			// column of a data record. Thus, in our view a term is one date
			// record consisting of the columns declared below.
			List<Map<String, String>> fields = new ArrayList<Map<String, String>>();

			Map<String, String> field = new HashMap<String, String>();
			field.put(NAME, "id");
			field.put(XPATH, "@id");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "parent-id");
			field.put(XPATH, "@parent-id");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "canonic");
			field.put(XPATH, "canonic");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "shortDescription");
			field.put(XPATH, "shortDescription");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "description");
			field.put(XPATH, "description");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "type");
			field.put(XPATH, "type");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "synonyms");
			field.put(XPATH, "synonyms/synonym");
			field.put(RETURN_ARRAY, "true");
			fields.add(field);

			field = new HashMap<String, String>();
			field.put(NAME, "variations");
			field.put(XPATH, "variations/variation");
			field.put(RETURN_XML_FRAGMENT, "true");
			field.put(RETURN_ARRAY, "true");
			fields.add(field);

			// Determine facet name of the terms in this file.
			VTDGen vg = new VTDGen();
			vg.parseFile(filePath, false);
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/terms/@facet");
			String facetName = ap.evalXPathToString();
			ap.selectXPath("/terms/@facet-id");
			String facetId = ap.evalXPathToString();
			if (facetName.equals("null"))
				currentFacet = Facet.KEYWORD_FACET;
			else if (facetId != null && !facetId.equals(""))
				currentFacet = facetService.getFacetWithId(Integer
						.parseInt(facetId));
			else
				currentFacet = facetService.getFacetWithName(facetName);

			// if (currentFacet == null)
			// throw new IllegalStateException("Facet " + facetName
			// + " not found!");

			termIterator = Utils.constructRowIterator(filePath, 1000,
					"/terms/term", fields, false);
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}

	}

	// @Override
	// public void reset(String filePath) throws IOException {
	// try {
	// // Create a builder factory
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// factory.setValidating(false);
	//
	// // Create the builder and parse the file
	// Document termFile = factory.newDocumentBuilder().parse(new
	// File(filePath));
	// termElements =
	// termFile.getDocumentElement().getElementsByTagName("term");
	// String facetName =
	// termFile.getDocumentElement().getAttributes().getNamedItem("facet").getNodeValue();
	// if( facetName.equals("null") )
	// currentFacet = FacetService.KEYWORD_FACET;
	// else
	// currentFacet = facetService.getFacetWithName(facetName);
	//
	// if( currentFacet == null )
	// throw new IllegalStateException("Facet " + facetName + " not found!");
	//
	// currentIndex = 0;
	// } catch (Exception e) {
	// throw new IOException(e.getMessage());
	// }
	//
	// }

	public boolean hasMoreElements() {
		if (termIterator == null)
			throw new IllegalStateException(
					"Please call 'reset(filePath)' before starting to iterate.");
		return termIterator.hasNext();
	}

	public TermFileEntry nextElement() {
		if (termIterator == null)
			throw new IllegalStateException(
					"Please call 'reset(filePath)' before starting to iterate.");

		Map<String, Object> row = termIterator.next();

		TermFileEntry term = new TermFileEntry();

		String id = (String) row.get("id");
		String parentId = (String) row.get("parent-id");
		String canonic = (String) row.get("canonic");
		String type = (String) row.get("type");
		String shortDescription = (String) row.get("shortDescription");
		String description = (String) row.get("description");
		String[] synonyms = (String[]) row.get("synonyms");
		String[] variations = (String[]) row.get("variations");

		// System.out.println((String) row.get("id"));
		// System.out.println(parentId);
		// System.out.println(canonic);
		// System.out.println(type);

		// TODO wird row in einen sinnvollen String aufgelöst?
		if (id == null)
			throw new IllegalStateException("Term without ID encountered: "
					+ row);

		term.setFacet(currentFacet);
		term.setId(id);
		if (parentId != null)
			term.setParentId(parentId);
		if (canonic != null)
			term.setCanonic(canonic);
		if (type != null)
			term.setType(type);
		if (shortDescription != null)
			term.setShortDescription(shortDescription);
		if (description != null)
			term.setDescription(description);
		if (synonyms != null)
			term.setSynonyms(Arrays.asList(synonyms));
		else
			term.setSynonyms(Collections.<String> emptyList());
		if (variations != null) {
			List<List<String>> varList = new ArrayList<List<String>>();
			VTDGen vg = new VTDGen();
			AutoPilot ap = new AutoPilot();
			for (String variationXML : variations) {
				try {
					List<String> tokenList = new ArrayList<String>();
					vg.setDoc(variationXML.getBytes("UTF-8"));
					vg.parse(false);
					VTDNav vn = vg.getNav();
					ap.bind(vn);
					ap.selectXPath("token");
					while (ap.evalXPath() != -1)
						tokenList.add(Utils.getElementText(vn));
					ap.resetXPath();
				} catch (VTDException e) {
					e.printStackTrace();
					logger.error(
							"Exception while parsing variation tokens from:\n{}\n Exception: {}",
							variationXML, e);
					System.exit(1);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			term.setVariations(varList);
		} else
			term.setVariations(Collections.<List<String>> emptyList());

		return term;
	}

	// public TermFileEntry nextElements() {
	// if (currentIndex == -1)
	// throw new IllegalStateException(
	// "Please call 'reset(filePath)' before starting to iterate.");
	//
	// Node termNode = termElements.item(currentIndex);
	// TermFileEntry term = new TermFileEntry();
	//
	// term.setFacet(currentFacet);
	// term.setId(termNode.getAttributes().getNamedItem("id").getNodeValue());
	// if (termNode.getAttributes().getNamedItem("parent-id") != null)
	// term.setParentId(termNode.getAttributes().getNamedItem("parent-id")
	// .getNodeValue());
	//
	// NodeList termChilds = termNode.getChildNodes();
	// for (int i = 0; i < termChilds.getLength(); i++) {
	// Node termChild = termChilds.item(i);
	//
	// if (termChild.getNodeName().equals("canonic"))
	// term.setCanonic(termChild.getTextContent());
	// else if (termChild.getNodeName().equals("type"))
	// term.setType(termChild.getTextContent());
	// else if (termChild.getNodeName().equals("shortDescription"))
	// term.setShortDescription(termChild.getTextContent());
	// else if (termChild.getNodeName().equals("description"))
	// term.setDescription(termChild.getTextContent());
	// else if (termChild.getNodeName().equals("synonyms")) {
	// NodeList synonymNodes = termChild.getChildNodes();
	// List<String> synonyms = new ArrayList<String>(
	// synonymNodes.getLength());
	// for (int j = 0; j < synonymNodes.getLength(); j++) {
	// Node synonymNode = synonymNodes.item(j);
	// if (synonymNode.getNodeName().equals("synonym"))
	// synonyms.add(synonymNode.getTextContent());
	// }
	// term.setSynonyms(synonyms);
	// } else if (termChild.getNodeName().equals("variations")) {
	// NodeList variationNodes = termChild.getChildNodes();
	// List<List<String>> variations = new ArrayList<List<String>>(
	// variationNodes.getLength());
	// for (int j = 0; j < variationNodes.getLength(); j++) {
	// Node variationNode = variationNodes.item(j);
	// if (variationNode.getNodeName().equals("variation")) {
	// NodeList tokenNodes = variationNode.getChildNodes();
	//
	// List<String> variation = new ArrayList<String>(
	// tokenNodes.getLength());
	// for (int k = 0; k < tokenNodes.getLength(); k++) {
	// Node tokenNode = tokenNodes.item(k);
	// if (tokenNode.getNodeName().equals("token"))
	// variation.add(tokenNode.getTextContent());
	// }
	// variations.add(variation);
	// }
	// }
	// term.setVariations(variations);
	// }
	// }
	//
	// currentIndex++;
	// return term;
	// }

	public List<TermFileEntry> sortTopDown(List<TermFileEntry> terms) {

		List<TermFileEntry> roots = new ArrayList<TermFileEntry>();
		List<TermFileEntry> orderedTermFileEntrys = new ArrayList<TermFileEntry>();

		for (TermFileEntry term : terms)
			if (term.getParent() == null)
				roots.add(term);

		for (TermFileEntry term : roots) {
			_collectTreeNodesTopDown(orderedTermFileEntrys, term);
		}

		return orderedTermFileEntrys;
	}

	private void _collectTreeNodesTopDown(List<TermFileEntry> list,
			TermFileEntry term) {
		list.add(term);
		for (TermFileEntry child : term.getChildren())
			_collectTreeNodesTopDown(list, child);
	}

	public void resolveRelationships(List<TermFileEntry> terms) {
		Map<String, TermFileEntry> termsWithId = new HashMap<String, TermFileEntry>();
		for (TermFileEntry term : terms)
			termsWithId.put(term.getId(), term);

		for (TermFileEntry term : terms) {
			String parentId = term.getParentId();
			if (parentId != null) {
				TermFileEntry parent = termsWithId.get(parentId);

				if (parent != null) {
					parent.getChildren().add(term);
					term.setParent(parent);
				}
			}
		}
	}

}
