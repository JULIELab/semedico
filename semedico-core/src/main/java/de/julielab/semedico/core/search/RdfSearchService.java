package de.julielab.semedico.core.search;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;

public class RdfSearchService implements IRdfSearchService {
	private static final String DRIVER = "virtuoso.jdbc4.Driver";
	private static final String GRAPH = "http://test301B";
	private static final String VIRTUOSO_PW = "haekel.schnecke";
	private static final String VIRTUOSO_USER = "dba";
	private static final String VIRTUOSO_HOST = "jdbc:virtuoso://localhost:1111";
	private static final String BACK_TO_SEMEDICO = "/semedico-frontend-1.7-SNAPSHOT/Main:ShowArticle/";

	// XGMML
	private static final String CONFIDENCE = "confidence";
	private static final String ATT = "att";
	private static final String BOOLEAN = "boolean";
	private static final String EDGE = "edge";
	private static final String NODE = "node";
	private static final String LABEL = "label";
	private static final String SENTENCE = "sentence";
	private static final String STRING = "string";
	private static final String LINK = "link";
	private static final String ID = "id";
	private static final String CENTER = "center";

	// RDF
	private static final String INTERACTION = "http://placeholder/ppi/";
	private static final String UNIPROT = "http://purl.uniprot.org/uniprot/";
	private static final String BINDING = "Binding";
	private static final String HAS_PMID = "http://placeholder/has-pmid";
	private static final String PMID = "http://www.ncbi.nlm.nih.gov/pubmed/";
	private static final String HAS_SENTENCE = "http://placeholder/has-text/";
	private static final String NIL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String VALUE = "value";
	private static final String NUMBER = "number";
	private static final String FULL_NAME = "fullName";
	private static final String TRUE = "true";
	private static final String GRAPHICS = "graphics";
	private static final String ELLIPSE = "ellipse";
	private static final int MINIMAL_LIMIT = 2;
	private static final String PLEASE = "Please click a node to load details.";
	private static String SPARQL_TEMPLATE = "SPARQL SELECT ?s ?p ?o ?pmid ?sentence "
			+ "FROM <%s>  WHERE {{<"
			+ UNIPROT
			+ "%s> ?p ?o. ?x rdf:subject <"
			+ UNIPROT
			+ "%s>. ?x rdf:object ?o. "
			+ "?p rdfs:subClassOf <"
			+ INTERACTION
			+ ">.. } UNION {?s ?p <"
			+ UNIPROT
			+ "%s>. ?x rdf:object <"
			+ UNIPROT
			+ "%s>. ?x rdf:subject ?s. "
			+ "?p rdfs:subClassOf <"
			+ INTERACTION
			+ ">. } "
			+ "?x rdf:predicate ?p. "
			+ "?x rdf:type rdf:Statement. "
			+ "?x <"
			+ HAS_PMID + "> ?pmid. ?x <" + HAS_SENTENCE + "> ?sentence. } ";
	private static String SPARQL_TEMPLATE_WITHOUT_NIL = "SPARQL SELECT ?s ?p ?o ?pmid ?sentence "
			+ "FROM <%s>  WHERE {{<"
			+ UNIPROT
			+ "%s> ?p ?o. ?x rdf:subject <"
			+ UNIPROT
			+ "%s>. ?x rdf:object ?o. "
			+ "filter(?o != rdf:nil)."
			+ "?p rdfs:subClassOf <"
			+ INTERACTION
			+ ">. } UNION {?s ?p <"
			+ UNIPROT
			+ "%s>. ?x rdf:object <"
			+ UNIPROT
			+ "%s>. ?x rdf:subject ?s. filter(?s != rdf:nil)."
			+ "?p rdfs:subClassOf <"
			+ INTERACTION
			+ ">. } "
			+ "?x rdf:predicate ?p. "
			+ "?x rdf:type rdf:Statement. "
			+ "?x <"
			+ HAS_PMID + "> ?pmid. ?x <" + HAS_SENTENCE + "> ?sentence. } ";

	private static String SPARQL_TEMPLATE_MINIMALIST = "SPARQL SELECT distinct ?s ?p ?o"
			+ "FROM <%s>  WHERE {{<"
			+ UNIPROT
			+ "%s> ?p ?o. ?x rdf:subject <"
			+ UNIPROT
			+ "%s>. ?x rdf:object ?o. "
			+ "filter(?o != rdf:nil). "
			+ "?p rdfs:subClassOf <"
			+ INTERACTION
			+ ">. } UNION {?s ?p <"
			+ UNIPROT
			+ "%s>. ?x rdf:object <"
			+ UNIPROT
			+ "%s>. ?x rdf:subject ?s. filter(?s != rdf:nil)."
			+ "?p rdfs:subClassOf <"
			+ INTERACTION
			+ ">. } "
			+ "?x rdf:predicate ?p. " + "?x rdf:type rdf:Statement.} ";

	/**
	 * This class provides methods to query a RDF-store. The RDF must contain:
	 * s-p-o with s,o being proteins and p a ppi a reification of the above a
	 * sentence and a pmid attached to the reification
	 * 
	 * @throws ClassNotFoundException
	 *             If problems occur loading the driver.
	 */
	public RdfSearchService() throws ClassNotFoundException {
		Class.forName(DRIVER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.query.IRdfQueryService#getSubgraph(int,
	 * java.lang.String)
	 */
	@Override
	public String getSubgraph(int pathLength, String... center)
			throws Exception {
		return getSubgraph(false, pathLength, center);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.query.IRdfQueryService#getSubgraph(java.lang.String)
	 */
	@Override
	public String getSubgraph(String... center) throws Exception {
		return getSubgraph(false, 1, center);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.query.IRdfQueryService#getSubgraph(boolean,
	 * java.lang.String)
	 */
	@Override
	public String getSubgraph(boolean withNil, String... center)
			throws Exception {
		return getSubgraph(withNil, 1, center);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.query.IRdfQueryService#getSubgraph(boolean,
	 * int, java.lang.String[])
	 */
	@Override
	public String getSubgraph(boolean withNil, int pathLength, String[] center)
			throws Exception {
		if (pathLength < 1)
			throw new IllegalArgumentException("Invalid pathlength "
					+ pathLength + "!");
		// Convention for submitting multiple parameters from JS
		if (center.length == 1)
			center = center[0].split("--");
		// used to display bigger networks without details
		boolean minimal = (MINIMAL_LIMIT > 0 && center.length > MINIMAL_LIMIT) ? true
				: false;

		Connection conn = DriverManager.getConnection(VIRTUOSO_HOST,
				VIRTUOSO_USER, VIRTUOSO_PW);
		String graph = GRAPH;

		Set<String> nodes = new HashSet<>();
		Map<String, Object[]> edges = new HashMap<>();
		Set<String> allreadyQueriedAround = new HashSet<>();
		Set<String> toQuery = new HashSet<>();

		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		StringWriter stringWriter = new StringWriter();
		XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);

		startXGMML(writer, center, pathLength);
		for (int i = 0; i < pathLength; ++i) {
			if (i == 0)
				toQuery.addAll(Arrays.asList(center));
			else {
				toQuery.clear();
				toQuery.addAll(nodes);
				toQuery.removeAll(allreadyQueriedAround);
			}
			for (String searched : toQuery) {
				ResultSet rs = subgraphAround(graph, searched, conn, withNil,
						minimal);
				while (rs.next()) {
					String subject = rs.getString(1);
					// subject and object are null if they are the node which is
					// searched
					subject = (subject == null) ? searched : subject
							.replaceAll(UNIPROT, "");
					String predicate = (minimal) ? PLEASE : rs.getString(2)
							.replaceAll(INTERACTION, "");
					String object = rs.getString(3);
					object = (object == null) ? searched : object.replaceAll(
							UNIPROT, "");
					if (subject.equals(NIL))
						subject = object;
					if (object.equals(NIL))
						object = subject;
					String pmid = (minimal) ? "" : rs.getString(4);
					String sentence = (minimal) ? "" : rs.getString(5)
							.replaceAll("\"", "\\\"").replaceAll("\'", "\\\'");
					float confidence = 0.5f;// TODO: change here and in
											// TEMPLATEs if
											// confidence gets added to RDF
					String edge = subject + object + predicate + pmid
							+ sentence + confidence;
					if (!edges.containsKey(edge)) {
						edges.put(edge, new Object[] { subject, object,
								predicate, pmid, sentence, confidence });
						nodes.add(subject);
						nodes.add(object);
					}
				}
			}
		}
		for (String node : nodes)
			addNode(node, center, writer);
		for (Object[] edge : edges.values())
			addEdge(edge, writer);
		endXGMML(writer, stringWriter);
		return stringWriter.toString();
	}

	/**
	 * Finishes the GraphhML element with the appropriate tags and
	 * closes/flushes all streams
	 */
	private void endXGMML(XMLStreamWriter writer, StringWriter stringWriter)
			throws Exception {
		writer.writeEndElement(); // graph
		writer.writeEndDocument();

		writer.flush();
		writer.close();
		stringWriter.flush();
		stringWriter.close();
	}

	/**
	 * Starts a GraphhML element
	 * 
	 * @param pathlength
	 */
	private void startXGMML(XMLStreamWriter writer, String[] center,
			int pathlength) throws XMLStreamException {
		writer.writeStartDocument();
		writer.writeStartElement("graph");
		writer.writeAttribute(LABEL,
				"Graph around " + StringUtils.join(center, " and ")
						+ " with pathlength " + pathlength);
		writer.writeAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
		writer.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
		writer.writeAttribute("xmlns:cy", "http://www.cytoscape.org");
		writer.writeAttribute("xmlns", "http://www.cs.rpi.edu/XGMML");
		writer.writeAttribute("directed", "false");
		addElement(ATT, writer, new String[][] { { NAME, "__layoutAlgorithm" },
				{ VALUE, "force-directed" }, { TYPE, STRING },
				{ "cy:hidden", TRUE } });
		addATT("__layoutAlgorithm", "force-directed", STRING, writer);
	}

	/**
	 * Adds an edge into the graph
	 * 
	 * @param source
	 *            Source node
	 * @param target
	 *            Target node
	 * @param cytoscapeId
	 * @param label
	 *            Kind of PPI
	 * @param pmid
	 *            PMID of the text containing this interaction
	 * @param sentence
	 *            Text containing this interaction
	 * @param confidence
	 *            Confidence for the extraction of this interaction
	 * @param writer
	 *            Used to write the XGMML
	 * @throws XMLStreamException
	 */
	private void addEdge(Object[] edge, XMLStreamWriter writer)
			throws XMLStreamException {
		String source = (String) edge[0];
		String target = (String) edge[1];
		String label = (String) edge[2];
		String pmid = (String) edge[3];
		String sentence = (String) edge[4];
		Float confidence = (Float) edge[5];

		writer.writeStartElement(EDGE);
		writer.writeAttribute("label", "(" + label + ")");
		writer.writeAttribute("source", source);
		writer.writeAttribute("target", target);
		if (!(label.equals(BINDING) || label.equals(PLEASE)))
			writer.writeAttribute("directed", "true");

		// sub elements, containing meta data
		addATT(LINK, pmid.replace(PMID,
				BACK_TO_SEMEDICO), STRING, writer);
		addATT(SENTENCE, sentence, STRING, writer);
		addATT(CONFIDENCE, confidence.toString(), NUMBER, writer);
		addATT("interaction", label.replaceAll("_", " "), STRING, writer);
		addATT("canonicalName", source + " (" + label + ") " + target, STRING,
				writer);
		// for desktop cytoscape
		addElement(
				GRAPHICS,
				writer,
				new String[][] {
						{ "width", String.valueOf((int) (10 * confidence)) },
						{ "fill", "#2b4b60" }, { "cy:edgeLabel", label },
						{ "cy:sourceArrow", "0" }, { "cy:targetArrow", "0" },
						{ "cy:edgeLineType", "SOLID" },
						{ "cy:curved", "STRAIGHT_LINES" },
						{ "cy:sourceArrowColor", "#2b4b60" },
						{ "cy:targetArrowColor", "#2b4b60" }, });
		writer.writeEndElement(); // edge
	}

	/**
	 * Adds a node into the graph
	 * 
	 * @param node
	 *            Node to add
	 * @param center
	 *            Center of this graph, used to decide if this node is displayed
	 *            bigger
	 * @param writer
	 *            Used to write the XGMML
	 * @throws XMLStreamException
	 */
	private void addNode(String node, String[] center, XMLStreamWriter writer)
			throws XMLStreamException {
		String shortName = node.split("_")[0];
		writer.writeStartElement(NODE);
		writer.writeAttribute(ID, node);
		writer.writeAttribute(LABEL, shortName); // gets rid of species

		// sub elements, containing meta data
		addATT(FULL_NAME, node, STRING, writer);
		addATT(LINK, UNIPROT + node, STRING, writer);
		for (String toEmph : center) {
			if (node.endsWith(toEmph)) {
				addATT(CENTER, TRUE, BOOLEAN, writer);
				// for desktop cytoscape
				addElement(GRAPHICS, writer, new String[][] {
						{ "cy:nodeLabel", shortName }, { TYPE, ELLIPSE },
						{ "h", "60" }, { "w", "60" }, { "fill", "#e3e3e3" },
						{ "width", "1" } });
			} else
				addElement(GRAPHICS, writer, new String[][] {
						{ "cy:nodeLabel", shortName }, { TYPE, ELLIPSE },
						{ "h", "40" }, { "w", "40" }, { "fill", "#e3e3e3" },
						{ "width", "1" } });

		}

		writer.writeEndElement(); // node
	}

	/**
	 * Adds a ATT-element with several attributes into the graph
	 * 
	 * @param name
	 *            Name of the element
	 * @param value
	 *            Value of the element
	 * @param type
	 *            Type of the element
	 * @param writer
	 *            Used to write the XGMML
	 * @throws XMLStreamException
	 */
	private void addATT(String name, String value, String type,
			XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement(ATT);
		writer.writeAttribute(NAME, name);
		writer.writeAttribute(VALUE, value);
		writer.writeAttribute(TYPE, type);
		writer.writeEndElement();
	}

	/**
	 * Adds a generic XML-element into the graph
	 * 
	 * @param name
	 *            Name of the element
	 * @param writer
	 *            Used to write the XGMML
	 * @param attributeAndValueTuples
	 *            2D Array, containing 1D Arrays used as attribute-name /
	 *            attribute-value tuples
	 * @throws XMLStreamException
	 */
	private void addElement(String name, XMLStreamWriter writer,
			String[][] attributeAndValueTuples) throws XMLStreamException {
		writer.writeStartElement(name);
		for (String[] tuple : attributeAndValueTuples) {
			writer.writeAttribute(tuple[0], tuple[1]);
		}
		writer.writeEndElement();
	}

	// /**
	// * Returns the in-going and out-going PPIs around a node.
	// *
	// * @param graph
	// * RDF graph to retrieve triples from, no <> needed.
	// * @param center
	// * Node to build the subgraph around, no <> or prefix needed.
	// * @param limit
	// * Maximum number of retrieved triples.
	// * @param conn
	// * Connection to virtuoso server
	// * @return ResultSet, containing a subject at 1, predicate at 2, object at
	// * 3, PMID at 4 and sentence at 5. Subject xor object will be empty,
	// * they are the center node in this case. (Don't try to use a query
	// * with unbound s/o and filters, to slow!).
	// * @throws SQLException
	// */
	// private ResultSet subgraphAround(String graph, String center, int limit,
	// Connection conn) throws SQLException {
	// return conn.createStatement().executeQuery(
	// String.format(SPARQL_TEMPLATE, graph, center, center, center,
	// center) + " limit " + limit);
	// }

	/**
	 * Returns the in-going and out-going PPIs around a node.
	 * 
	 * @param graph
	 *            RDF graph to retrieve triples from, no <> needed.
	 * @param center
	 *            Node to build the subgraph around, no <> or prefix needed.
	 * @param limit
	 *            Maximum number of retrieved triples.
	 * @param conn
	 *            Connection to virtuoso server
	 * @param withNil
	 *            Flag for filtering Interactions with rdf:nil
	 * @param minimal
	 *            Flag for retrieving only distinct results
	 * @return ResultSet, containing a subject at 1, predicate at 2, object at
	 *         3, PMID at 4 and sentence at 5. Subject xor object will be empty,
	 *         they are the center node in this case. (Don't try to use a query
	 *         with unbound s/o and filters, to slow!).
	 * @throws SQLException
	 */
	private ResultSet subgraphAround(String graph, String center,
			Connection conn, boolean withNil, boolean minimal)
			throws SQLException {
		if (minimal)
			return conn.createStatement().executeQuery(
					String.format(SPARQL_TEMPLATE_MINIMALIST, graph, center,
							center, center, center));
		else if (withNil)
			return conn.createStatement().executeQuery(
					String.format(SPARQL_TEMPLATE, graph, center, center,
							center, center));
		else
			return conn.createStatement().executeQuery(
					String.format(SPARQL_TEMPLATE_WITHOUT_NIL, graph, center,
							center, center, center));
	}
}
