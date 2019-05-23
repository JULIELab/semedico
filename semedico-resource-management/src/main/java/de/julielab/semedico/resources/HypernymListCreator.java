package de.julielab.semedico.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.slf4j.Logger;

import de.julielab.neo4j.plugins.Export;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.semedico.Utils;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;

public class HypernymListCreator implements IHypernymListCreator {

	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	private Logger log;

	public HypernymListCreator(Logger log, @Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint,
			INeo4jHttpClientService httpClientService) {
		this.log = log;
		this.neo4jEndpoint = neo4jEndpoint;
		this.httpClientService = httpClientService;

	}

	public void writeHypernymList(String outputFilePath, String termLabel, String... facetLabels) {
		Map<String, Object> params = new HashMap<>();
		if (facetLabels.length > 0)
			params.put(Export.PARAM_LABELS, JsonSerializer.toJson(facetLabels));
		if (!StringUtils.isBlank(termLabel))
			params.put(Export.PARAM_LABEL, termLabel);
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + Export.EXPORT_ENDPOINT
				+ Export.HYPERNYMS, JsonSerializer.toJson(params));
		try {
			JSONArray jsonArray = new JSONArray(EntityUtils.toString(response));
			log.info("Retrieved {} bytes of hypernym file data.", jsonArray.length());
			Utils.writeByteJsonArrayToStringFile(jsonArray, outputFilePath, true);
		} catch (ParseException | IOException e) {
			log.error("ParseException or IOException: ", e);
		}
	}

	// private static class HypernymsCacheLoader extends CacheLoader<Node, Set<String>> {
	//
	// private LoadingCache<Node, Set<String>> hypernymCache;
	// private Set<Node> visitedNodes;
	//
	// public HypernymsCacheLoader() {
	// this.visitedNodes = new HashSet<>();
	// }
	//
	// public void setHypernymCache(LoadingCache<Node, Set<String>> cache) {
	// this.hypernymCache = cache;
	//
	// }
	//
	// @Override
	// public Set<String> load(Node n) throws Exception {
	// visitedNodes.add(n);
	// Set<String> hypernyms = new HashSet<>();
	// for (Relationship rel : n.getRelationships(Direction.INCOMING, EdgeTypes.IS_BROADER_THAN)) {
	// Node directHypernym = rel.getStartNode();
	// boolean isHollow = false;
	// for (Label l : directHypernym.getLabels())
	// if (l.equals(TermLabel.HOLLOW))
	// isHollow = true;
	// if (isHollow)
	// continue;
	// if (visitedNodes.contains(directHypernym))
	// continue;
	// String directHypernymId = ((String) directHypernym.getProperty(TermConstants.PROP_ID)).intern();
	// hypernyms.add(directHypernymId);
	// hypernyms.addAll(hypernymCache.get(directHypernym));
	// }
	// visitedNodes.remove(n);
	// return hypernyms;
	// }
	//
	// }
	//
	// private static final Logger log = LoggerFactory.getLogger(HypernymListCreator.class);
	//
	// public static void writeHypernymList(GraphDatabaseService graphDb, int cacheSize, String outputFilePath) throws
	// IOException {
	//
	// HypernymsCacheLoader cacheLoader = new HypernymsCacheLoader();
	// LoadingCache<Node, Set<String>> hypernymIdSets = CacheBuilder.newBuilder().maximumSize(cacheSize)
	// .build(cacheLoader);
	// cacheLoader.setHypernymCache(hypernymIdSets);
	//
	// try (Transaction tx = graphDb.beginTx()) {
	// ResourceIterable<Node> facets = GlobalGraphOperations.at(graphDb).getAllNodesWithLabel(
	// FacetManager.FacetLabel.FACET);
	// try {
	// File hypernymsFile = new File(outputFilePath);
	// if (hypernymsFile.exists())
	// hypernymsFile.delete();
	// log.info("Writing hypernym list to {}.", outputFilePath);
	// Set<Node> visitedNodes = new HashSet<>();
	// for (Node facet : facets) {
	// Iterable<Relationship> rels = facet.getRelationships(Direction.OUTGOING, EdgeTypes.HAS_ROOT_TERM);
	// for (Relationship rel : rels) {
	// writeHypernyms(rel.getEndNode(), visitedNodes, hypernymIdSets, hypernymsFile);
	// }
	// }
	// log.info("Hypernym file has successfully been written to {}.", outputFilePath);
	// } catch (ExecutionException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	//
	// private static void writeHypernyms(Node n, Set<Node> visitedNodes, LoadingCache<Node, Set<String>>
	// hypernymIdSets,
	// File hypernymsFile) throws ExecutionException, IOException {
	// if (visitedNodes.contains(n))
	// return;
	// visitedNodes.add(n);
	// boolean isHollow = false;
	// for (Label l : n.getLabels())
	// if (l.equals(TermLabel.HOLLOW))
	// isHollow = true;
	// if (isHollow)
	// return;
	// Set<String> hypernyms = hypernymIdSets.get(n);
	// if (hypernyms.size() > 0)
	// FileUtils.write(hypernymsFile,
	// n.getProperty(TermConstants.PROP_ID) + "=" + StringUtils.join(hypernyms, "|") + "\n", "UTF-8",
	// true);
	// for (Relationship rel : n.getRelationships(Direction.OUTGOING, EdgeTypes.IS_BROADER_THAN)) {
	// writeHypernyms(rel.getEndNode(), visitedNodes, hypernymIdSets, hypernymsFile);
	// }
	// if (visitedNodes.size() % 100000 == 0)
	// log.info("Finished {}.", visitedNodes.size());
	// }
}
