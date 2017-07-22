package de.julielab.semedico.core.services;

import static de.julielab.neo4j.plugins.FacetManager.KEY_FACETS;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import de.julielab.neo4j.plugins.FacetManager;
import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.constants.semedico.FacetConstants;
import de.julielab.neo4j.plugins.constants.semedico.FacetGroupConstants;
import de.julielab.neo4j.plugins.constants.semedico.NodeConstants;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;
import de.julielab.neo4j.plugins.datarepresentation.PushTermsToSetCommand;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation.Type;
import de.julielab.semedico.core.facets.FacetGroupLabels;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;

public class Neo4jService implements ITermDatabaseService {

	public static class TransactionalStatement {
		public static class Statement {
			public String statement;
			public Map<String, Object> parameters;
			public String[] resultDataContents;

			public Statement(String cypherQuery) {
				statement = cypherQuery;
				resultDataContents = new String[] { Neo4jService.ROW };
			}

			/**
			 * Sets the result data contents, i.e. the format in which data is returned (graph, row or raw). Defaults to
			 * row.
			 * 
			 * @param resultDataContents
			 */
			public void setResultDataContent(String... resultDataContents) {
				this.resultDataContents = resultDataContents;
			}

			@Override
			public String toString() {
				return "Statement [statement=" + statement
						+ ", parameters="
						+ parameters
						+ ", resultDataContents="
						+ Arrays.toString(resultDataContents)
						+ "]";
			}
		}

		public List<Statement> statements;

		/**
		 * This constructor does nothing apart from instantiating an object.
		 */
		public TransactionalStatement() {
		}

		/**
		 * <p>
		 * A constructor that by default creates a single cypher statement and adds it to the list of statements in this
		 * transactional statement. This is a convenience shortcut to a manual creation of a single statement and adding
		 * it to the transactional statement.
		 * </p>
		 * <p>
		 * The default return format is {@link Neo4jService#ROW}
		 * </p>
		 * 
		 * @param cypherQuery
		 */
		public TransactionalStatement(String cypherQuery) {
			Statement stmt = new Statement(cypherQuery);
			addStatement(stmt);
		}

		/**
		 * <p>
		 * A constructor that by default creates a single cypher statement with the given parameters and adds it to the
		 * list of statements in this transactional statement. This is a convenience shortcut to a manual creation of a
		 * single statement and adding it to the transactional statement.
		 * </p>
		 * <p>
		 * The default return format is {@link Neo4jService#ROW}
		 * </p>
		 * 
		 * @param cypherQuery
		 */
		public TransactionalStatement(String cypherQuery, Map<String, Object> parameters) {
			Statement stmt = new Statement(cypherQuery);
			stmt.parameters = parameters;
			addStatement(stmt);
		}

		public void addStatement(Statement statement) {
			if (null == statements)
				statements = new ArrayList<>();
			statements.add(statement);
		}

		private static transient Gson gson = new Gson();

		public String toJson() {
			return gson.toJson(this);
		}

		@Override
		public String toString() {
			return "TransactionalStatement [statements=" + statements + "]";
		}

	}

	public enum MetaEdgeType {
		HAS_FACET_GROUP, HAS_FACET
	}

	public enum MetaLabel {
		ROOT
	}

	public enum FacetLabel {
		FACET
	}

	public static final String TERM_MANAGER_ENDPOINT = "db/data/ext/" + ConceptManager.class.getSimpleName() + "/graphdb/";
	public static final String FACET_MANAGER_ENDPOINT = "db/data/ext/" + FacetManager.class.getSimpleName()
			+ "/graphdb/";
	public static final String CYPHER_ENDPOINT = "db/data/cypher";

	/**
	 * The transactional endpoint allows to send multiple request that are served within a single transaction. However,
	 * we use it mostly (or even at all) because its return format for nodes is quite compact and thus very useful when
	 * we want to get full nodes and not just particular properties.
	 */
	public static final String TRANSACTIONAL_ENDPOINT = "db/data/transaction";

	/**
	 * <p>
	 * Only use from absolutely Neo4j-dependend places.
	 * </p>
	 * <p>
	 * Cypher response key "data", containing the result rows of the query.
	 * </p>
	 * 
	 */
	static final String DATA = "data";

	/**
	 * <p>
	 * Only use from absolutely Neo4j-dependend places.
	 * </p>
	 * <p>
	 * Cypher (from transactional endpoint) response key "results", containing the results of the query in the specified
	 * format.
	 * </p>
	 * 
	 */
	static final String RESULTS = "results";

	/**
	 * <p>
	 * Only use from absolutely Neo4j-dependend places.
	 * </p>
	 * <p>
	 * The name of the key containing the results in 'graph' format from the transactional endpoint.
	 * </p>
	 * 
	 */
	static final String GRAPH = "graph";

	/**
	 * <p>
	 * Only use from absolutely Neo4j-dependend places.
	 * </p>
	 * <p>
	 * The name of the key containing the results in 'row' format from the transactional endpoint.
	 * </p>
	 * 
	 */
	static final String ROW = "row";

	/**
	 * The to error messages in the results of a transactional cypher request.
	 */
	private static final String ERRORS = "errors";

	// private final DefaultHttpClient client;
	/**
	 * @deprecated Only used until the import is done correctly in its own project.
	 */
	@Deprecated
	private Map<String, Object> termsToInsert;
	/**
	 * @deprecated Only used until the import is done correctly in its own project.
	 */
	@Deprecated
	private boolean extraFacetsInserted = false;
	/**
	 * Indicates whether some basic server settings like automatic indexes have been created.
	 * 
	 * @deprecated Only used until the import is done correctly in its own project.
	 */
	@Deprecated
	private boolean neo4jServerIsSetUp = false;
	private final Logger log;
	private final String neo4jEndpoint;
	private Gson gson;
	private IHttpClientService httpClientService;

	public Neo4jService(Logger log, INeo4jHttpClientService httpClientService,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint) {
		this.log = log;
		this.httpClientService = httpClientService;
		this.neo4jEndpoint = neo4jEndpoint;
		log.info("Using Neo4j term database at {}", neo4jEndpoint);

		// // Largely copied from
		// // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e639
		// // We need the ConnectionManager for multi-threading purposes (pooling).
		// SchemeRegistry schemeRegistry = new SchemeRegistry();
		// schemeRegistry.register(new Scheme("http", 7474, PlainSocketFactory.getSocketFactory()));
		// ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		// client = new DefaultHttpClient(cm);

		gson = new Gson();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addTerm(Map<String, Object> termMap, String facetName) {
		// first of all: Basic server settings.
		if (!neo4jServerIsSetUp) {
			setupNeo4jServer();
		}

		if (null == termsToInsert)
			termsToInsert = new HashMap<String, Object>();
		Map<String, Object> termsMap = (Map<String, Object>) termsToInsert.get(facetName);
		List<Map<String, Object>> termsList = null;
		if (null == termsMap) {
			termsMap = new HashMap<String, Object>();
			termsMap.put("facet", getFacetMap(facetName));
			termsList = new ArrayList<Map<String, Object>>();
			termsMap.put("terms", termsList);
			termsToInsert.put(facetName, termsMap);
		} else {
			termsList = (List<Map<String, Object>>) termsMap.get("terms");
		}
		termsList.add(termMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void commitTerms() {
		if (!extraFacetsInserted)
			insertExtraFacets();

		HttpPost post = new HttpPost(neo4jEndpoint + "/" + TERM_MANAGER_ENDPOINT + ConceptManager.INSERT_TERMS);
		for (String facetName : termsToInsert.keySet()) {
			Map<String, Object> facetAndTerms = (Map<String, Object>) termsToInsert.get(facetName);
			org.neo4j.shell.util.json.JSONObject termsJson = new org.neo4j.shell.util.json.JSONObject(facetAndTerms);
			HashMap<String, Object> data = new HashMap<>();
			data.put("terms", termsJson.toString());
			data.put(ConceptManager.KEY_CREATE_HOLLOW_PARENTS, false);
			org.neo4j.shell.util.json.JSONObject dataJsonObj = new org.neo4j.shell.util.json.JSONObject(data);
			try {
				// post.setEntity(new StringEntity(dataJsonObj.toString()));
				// HttpResponse response = client.execute(post);
				try {
					HttpEntity entity = httpClientService.sendPostRequest(post, dataJsonObj.toString());
					// log.info("{}", response.getStatusLine());
					// HttpEntity entity = response.getEntity();
					if (entity != null)
						log.info("{}", EntityUtils.toString(entity));
					// do something useful with the response body
					// and ensure it is fully consumed
					EntityUtils.consume(entity);
				} finally {
					post.releaseConnection();
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		termsToInsert.clear();
	}

	@Override
	public List<String> getFacetIdsWithGeneralLabel(FacetLabels.General label) {
		String cypherQuery =
				String.format("MATCH (f:%s) " + "WHERE {label} in labels(f) " + "RETURN f.%s", FacetLabel.FACET,
						FacetConstants.PROP_ID);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("label", label);
		parameters.put("facetGroupsRootName", FacetConstants.NAME_FACET_GROUPS);
		String cypherResponse = sendCypherQuery(cypherQuery, parameters);
		JSONObject responseObject = new JSONObject(cypherResponse);
		JSONArray rows = responseObject.getJSONArray(DATA);

		List<String> facetIds = new ArrayList<>();
		for (int i = 0; i < rows.length(); i++) {
			// Our query asked exactly for the facet IDs, so this should be the
			// only value returned per row (i.e. per facet).
			JSONArray idCell = rows.getJSONArray(i);
			facetIds.add(idCell.getString(0));
		}
		return facetIds;
	}

	/**
	 * TODO: DELETE! Is gone to the new Semedico resource management project.
	 * 
	 * @param facetName
	 * @return
	 */
	@Deprecated
	private Object getFacetMap(String facetName) {
		String cssId = null;
		String facetGroupName = null;
		Integer position = null;
		String sourceType = FacetConstants.SRC_TYPE_HIERARCHICAL;
		List<FacetGroupLabels.General> facetGroupProperties = new ArrayList<>();
		List<FacetLabels.Unique> facetUniqueLabels = new ArrayList<>();
		List<FacetLabels.General> facetGeneralLabels = new ArrayList<>();

		Integer facetGroupPosition = null;

		Collection<String> searchFieldNames =
				Lists.newArrayList(IIndexInformationService.TITLE, IIndexInformationService.ABSTRACT,
						IIndexInformationService.MESH);

		facetGeneralLabels.add(FacetLabels.General.USE_FOR_SUGGESTIONS);
		facetGeneralLabels.add(FacetLabels.General.USE_FOR_QUERY_DICTIONARY);

		if ("Genes and Proteins".equals(facetName)) {
			cssId = "proteins";
			facetGroupName = "BioMed";
			position = 0;
		} else if ("Chemicals and Drugs".equals(facetName)) {
			cssId = "chemicals";
			facetGroupName = "BioMed";
			position = 1;
		} else if ("Diseases / Pathological Processes".equals(facetName)) {
			cssId = "diseases";
			facetGroupName = "BioMed";
			position = 2;
		} else if ("Organisms".equals(facetName)) {
			cssId = "organisms";
			facetGroupName = "BioMed";
			position = 3;
		} else if ("Cellular Processes".equals(facetName)) {
			cssId = "cellularProcesses";
			facetGroupName = "BioMed";
			position = 4;
		} else if ("Investigative Techniques".equals(facetName)) {
			cssId = "techniques";
			facetGroupName = "BioMed";
			position = 5;
		} else if ("Gene Expression".equals(facetName)) {
			cssId = "geneExression";
			facetGroupName = "BioMed";
			position = 6;
		} else if ("Signs and Symptoms".equals(facetName)) {
			cssId = "signs";
			facetGroupName = "BioMed";
			position = 7;
		} else if ("Therapies and Treatments".equals(facetName)) {
			cssId = "therapies";
			facetGroupName = "BioMed";
			position = 8;
		} else if ("Immunoglobulins and Antibodies".equals(facetName)) {
			cssId = "antibodies";
			facetGroupName = "Immunology";
			position = 1;
		} else if ("Minor Histocompatibility Antigens".equals(facetName)) {
			cssId = "mha";
			facetGroupName = "Immunology";
			position = 2;
		} else if ("Hematopoietic Progenitor Cells".equals(facetName)) {
			cssId = "progenitors";
			facetGroupName = "Immunology";
			position = 3;
		} else if ("Blood Cells".equals(facetName)) {
			cssId = "bloodCells";
			facetGroupName = "Immunology";
			position = 4;
		} else if ("Epitopes and Binding Sites".equals(facetName)) {
			cssId = "epitopes";
			facetGroupName = "Immunology";
			position = 5;
		} else if ("Immune Processes".equals(facetName)) {
			cssId = "immunity";
			facetGroupName = "Immunology";
			position = 6;
		} else if ("Transplantation".equals(facetName)) {
			cssId = "transplantation";
			facetGroupName = "Immunology";
			position = 7;
		} else if ("Concepts".equals(facetName)) {
			cssId = "concepts";
			facetGroupName = "NoFacet";
			facetUniqueLabels.add(FacetLabels.Unique.NO_FACET);
			position = 0;
		} else {

			throw new IllegalArgumentException("Facet with name \"" + facetName + "\" is unknown.");
		}

		if (facetGroupName.equals("BioMed")) {
			facetGroupPosition = 0;
			facetGroupProperties.add(FacetGroupLabels.General.SHOW_FOR_SEARCH);
			facetGroupProperties.add(FacetGroupLabels.General.SHOW_FOR_BTERMS);
		} else if (facetGroupName.equals("Immunology")) {
			facetGroupPosition = 1;
			facetGroupProperties.add(FacetGroupLabels.General.SHOW_FOR_SEARCH);
			facetGroupProperties.add(FacetGroupLabels.General.SHOW_FOR_BTERMS);
		} else {
			facetGroupPosition = -1;
		}

		Map<String, Object> facetMap = new HashMap<String, Object>();
		facetMap.put(FacetConstants.PROP_NAME, facetName);
		facetMap.put(FacetConstants.PROP_CSS_ID, cssId);
		facetMap.put(FacetConstants.PROP_SEARCH_FIELD_NAMES, searchFieldNames);
		facetMap.put(FacetConstants.PROP_SOURCE_TYPE, sourceType);
		facetMap.put(FacetConstants.PROP_POSITION, position);
		if (facetGeneralLabels.size() > 0)
			facetMap.put(NodeConstants.PROP_GENERAL_LABELS, facetGeneralLabels);
		if (facetUniqueLabels.size() > 0)
			facetMap.put(FacetConstants.PROP_UNIQUE_LABELS, facetUniqueLabels);
		Map<String, Object> facetGroupMap = new HashMap<>();
		facetGroupMap.put(NodeConstants.PROP_NAME, facetGroupName);
		facetGroupMap.put(FacetGroupConstants.PROP_POSITION, facetGroupPosition);
		if (facetGroupProperties.size() > 0)
			facetGroupMap.put(FacetGroupConstants.PROP_GENERAL_LABELS, facetGroupProperties);
		facetMap.put(FacetConstants.FACET_GROUP, facetGroupMap);
		return facetMap;
	}

	// from postgres
	// 20;2;"Journals";"journals";3;3;FALSE
	// 39;4;"Authors";"authors";3;0;FALSE
	// 21;3;"Years";"years";3;4;FALSE
	// 19;4;"Last Authors";"lastAuthors";3;2;FALSE
	// 18;4;"First Authors";"firstAuthors";3;1;FALSE

	@Override
	public JSONArray getFacetRootIDs(String facetId) {
		String cypherQuery =
				String.format(
						"MATCH (fgr:%s)-[:%s]->()-[:%s]->f-[:%s]->rt " + "WHERE fgr.%s = {facetGroupsName} AND f.%s = {facetId} AND NOT 'HOLLOW' IN labels(rt)"
								+ " RETURN COLLECT(rt.%s)", MetaLabel.ROOT, MetaEdgeType.HAS_FACET_GROUP,
						MetaEdgeType.HAS_FACET, Type.HAS_ROOT_TERM, FacetGroupConstants.PROP_NAME,
						FacetConstants.PROP_ID, ConceptConstants.PROP_ID);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("facetGroupsName", FacetConstants.NAME_FACET_GROUPS);
		parameters.put("facetId", facetId);
		String cypherResponse = sendCypherQuery(cypherQuery, parameters);
		JSONObject responseObject = new JSONObject(cypherResponse);
		if (responseObject.getJSONArray(DATA).length() > 0)
			// Return from the first row the first field, which is an array of
			// facet root term IDs.
			return responseObject.getJSONArray(DATA).getJSONArray(0).getJSONArray(0);
		return responseObject.getJSONArray(DATA);
	}

	@Override
	public JSONArray getFacetRootIDs(Iterable<? extends String> facetIds) {
		String cypherQuery =
				String.format(
						"MATCH (fgr:%s)-[:%s]->()-[:%s]->f-[:%s]->rt " + "WHERE fgr.%s = {facetGroupsName} AND f.%s IN {facetIds} AND NOT 'HOLLOW' IN labels(rt)"
								+ " RETURN f.id,COLLECT(rt.%s)", MetaLabel.ROOT, MetaEdgeType.HAS_FACET_GROUP,
						MetaEdgeType.HAS_FACET, Type.HAS_ROOT_TERM, FacetGroupConstants.PROP_NAME,
						FacetConstants.PROP_ID, ConceptConstants.PROP_ID);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("facetGroupsName", FacetConstants.NAME_FACET_GROUPS);
		parameters.put("facetIds", facetIds);
		String cypherResponse = sendCypherQuery(cypherQuery, parameters);
		JSONObject responseObject = new JSONObject(cypherResponse);
		if (responseObject.getJSONArray(DATA).length() > 0)
			// Return from the first row the first field, which is an array of
			// facet root term IDs.
			return responseObject.getJSONArray(DATA);
		return responseObject.getJSONArray(DATA);
	}

	@Override
	public JSONObject getFacetRootTerms(Iterable<? extends String> facetIds,
			Map<String, List<String>> requestedRootIds, int maxRoots) {
		HttpPost post = new HttpPost(neo4jEndpoint + "/" + TERM_MANAGER_ENDPOINT + ConceptManager.GET_FACET_ROOTS);

		Map<String, Object> params = new HashMap<>();
		params.put(ConceptManager.KEY_FACET_IDS, JsonSerializer.toJson(facetIds));
		if (null != requestedRootIds)
			params.put(ConceptManager.KEY_TERM_IDS, JsonSerializer.toJson(requestedRootIds));
		params.put(ConceptManager.KEY_MAX_ROOTS, maxRoots);
		try {
			HttpEntity entity = httpClientService.sendPostRequest(post, JsonSerializer.toJson(params));
			if (null != entity) {

				String facetRootsJsonString = EntityUtils.toString(entity);
				JSONObject facetRootsJson = new JSONObject(facetRootsJsonString);
				// The value from Neo4j is actually a Map
				// {"<facetId>":[...]} containing a list of facet roots.

				return facetRootsJson;
			}

			// HttpResponse response = client.execute(post);
			// HttpEntity entity = response.getEntity();
			// if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// if (null != entity) {
			//
			// String facetGroupsJsonString = EntityUtils.toString(entity);
			// JSONObject facetGroupsJson = new JSONObject(facetGroupsJsonString);
			// // The value from Neo4j is actually a Map
			// // {"facetGroups":[...]} containing a list of facet groups.
			// JSONArray facetGroups = facetGroupsJson.getJSONArray("facetGroups");
			// return facetGroups;
			// }
			// }
			// log.error("Error when getting facets from Neo4j Server: {}", null != entity ?
			// EntityUtils.toString(entity)
			// : response.getStatusLine());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		// String cypherQuery =
		// "MATCH (fgr:ROOT {name : {facetGroupsName}})-[:HAS_FACET_GROUP]->()-[:HAS_FACET]->f-[:HAS_ROOT_TERM]->n "
		// + "WHERE NOT 'HOLLOW' IN labels(n) AND f.id IN {facetIds} "
		// + "WITH f.id as fid,"
		// + "COLLECT(n) as rootNodes,"
		// + "COLLECT(labels(n)) as rootLabels,"
		// + "count(n) as numRoots"
		// + " WHERE numRoots < 200 RETURN fid,rootNodes,rootLabels";
		// Map<String, Object> parameters = new HashMap<>();
		// parameters.put("facetGroupsName", FacetConstants.NAME_FACET_GROUPS);
		// parameters.put("facetIds", facetIds);
		//
		// TransactionalStatement transactionStmt = new TransactionalStatement();
		// Statement stmt = new Statement(cypherQuery);
		// stmt.setResultDataContent("row");
		// transactionStmt.addStatement(stmt);
		// stmt.parameters = parameters;
		// String cypherResponse = null;
		// try {
		// cypherResponse = EntityUtils.toString(
		// httpClientService.sendPostRequest(neo4jEndpoint + "/" + TRANSACTIONAL_ENDPOINT,
		// transactionStmt.toJson()), "UTF-8");
		// JSONObject responseObject = new JSONObject(cypherResponse);
		// // Array of row objects
		// JSONArray dataArray = getDataOfTransactionalResponse(responseObject);
		// return dataArray;
		// } catch (ParseException | IOException e) {
		// e.printStackTrace();
		// }
		// return null;
	}

	@Override
	public JSONArray getFacets(boolean getHollowFacets) {
		HttpPost post = new HttpPost(neo4jEndpoint + "/" + FACET_MANAGER_ENDPOINT + FacetManager.GET_FACETS);
		Map<String, Object> params = new HashMap<>();
		params.put(FacetManager.PARAM_RETURN_HOLLOW_FACETS, getHollowFacets);
		try {
			HttpEntity entity = httpClientService.sendPostRequest(post, JsonSerializer.toJson(params));
			if (null != entity) {

				String facetGroupsJsonString = EntityUtils.toString(entity);
				JSONObject facetGroupsJson = new JSONObject(facetGroupsJsonString);
				// The value from Neo4j is actually a Map
				// {"facetGroups":[...]} containing a list of facet groups.
				JSONArray facetGroups = facetGroupsJson.getJSONArray("facetGroups");
				return facetGroups;
			}

			// HttpResponse response = client.execute(post);
			// HttpEntity entity = response.getEntity();
			// if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// if (null != entity) {
			//
			// String facetGroupsJsonString = EntityUtils.toString(entity);
			// JSONObject facetGroupsJson = new JSONObject(facetGroupsJsonString);
			// // The value from Neo4j is actually a Map
			// // {"facetGroups":[...]} containing a list of facet groups.
			// JSONArray facetGroups = facetGroupsJson.getJSONArray("facetGroups");
			// return facetGroups;
			// }
			// }
			// log.error("Error when getting facets from Neo4j Server: {}", null != entity ?
			// EntityUtils.toString(entity)
			// : response.getStatusLine());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// From Postgres:
	// id;default_index;name;cssId;type;facet_order;hidden
	// 1;6;"Genes and Proteins";"proteins";0;0;f
	// 3;6;"Chemicals and Drugs";"chemicals";0;2;f
	// 8;5;"Diseases / Pathological Processes";"diseases";0;3;f
	// 4;6;"Organisms";"organisms";0;4;f
	// 6;5;"Cellular Processes";"cellularProcesses";0;5;f
	// 7;5;"Investigative Techniques";"techniques";0;6;f
	// 5;5;"Gene Expression";"geneExression";0;7;f
	// 9;5;"Signs and Symptoms";"signs";0;8;f
	// 10;5;"Therapies and Treatments";"therapies";0;9;f

	// 11;6;"Immunoglobulins and Antibodies";"antibodies";1;0;f
	// 12;6;"Minor Histocompatibility Antigens";"mha";1;1;f
	// 13;6;"Hematopoietic Progenitor Cells";"progenitors";1;2;f
	// 14;6;"Blood Cells";"bloodCells";1;3;f
	// 15;5;"Epitopes and Binding Sites";"epitopes";1;4;f
	// 16;5;"Immune Processes";"immunity";1;5;f
	// 17;5;"Transplantation";"transplantation";1;6;f

	@Override
	public int getNumTerms() {
		// String cypherQuery = String.format("START n=node:%s(\"%s:*\") RETURN COUNT(n)",
		// FacetConceptConstants.INDEX_NAME,
		// FacetConceptConstants.PROP_ID);
		// String cypherQuery = String.format("MATCH (n:%s) RETURN COUNT(n)", TermLabels.General.TERM);
		// String response = sendCypherQuery(cypherQuery);
		// JSONObject jsonResponse = new JSONObject(response);
		// return jsonResponse.getJSONArray(DATA).getJSONArray(0).getInt(0);

		HttpEntity response =
				httpClientService.sendPostRequest(neo4jEndpoint + TERM_MANAGER_ENDPOINT + ConceptManager.GET_NUM_TERMS);
		int numTerms = -1;
		try {
			numTerms = Integer.parseInt(EntityUtils.toString(response));
		} catch (NumberFormatException | ParseException | IOException e) {
			e.printStackTrace();
		}
		return numTerms;
	}

	@Override
	public int getNumFacets() {
		String responseString = sendCypherQuery("MATCH (f:" + FacetLabels.General.FACET + ") RETURN count(f)");
		JSONObject response = new JSONObject(responseString);
		int numFacets = response.getJSONArray(DATA).getJSONArray(0).getInt(0);
		return numFacets;
	}

	@Override
	public JSONArray getShortestPathFromAnyRoot(String termId, String idType) {
		JSONArray pathsFromRoots = getAllPathsFromAnyRoots(termId, idType, true);
		if (pathsFromRoots.length() > 0)
			return pathsFromRoots.getJSONArray(0);
		return pathsFromRoots;
	}

	@Override
	public JSONArray getShortestPathFromAnyRoot(String termId) {
		return getShortestPathFromAnyRoot(termId, ConceptConstants.PROP_ID);
	}

	@Override
	public JSONArray getPathsFromRoots(Collection<String> termIds, String idType) {
		return getPathsFromRootsInFacet(termIds, idType, false, "");
	}

	@Override
	public JSONArray getPathsFromRootsInFacet(Collection<String> termIds, String idType, boolean sortByLength,
			String facetId) {
		Gson gson = new Gson();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(ConceptManager.KEY_TERM_IDS, gson.toJson(termIds));
		parameters.put(ConceptManager.KEY_ID_TYPE, idType);
		parameters.put(ConceptManager.KEY_SORT_RESULT, sortByLength);
		parameters.put(ConceptManager.KEY_FACET_ID, facetId);
		String requestString = JsonSerializer.toJson(parameters);
		HttpEntity response =
				httpClientService.sendPostRequest(neo4jEndpoint + "/"
						+ TERM_MANAGER_ENDPOINT
						+ ConceptManager.GET_PATHS_FROM_FACETROOTS, requestString);
		try {
			JSONObject pathsObject = new JSONObject(EntityUtils.toString(response));
			JSONArray paths = pathsObject.getJSONArray(ConceptManager.RET_KEY_PATHS);
			return paths;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public JSONArray getAllPathsFromAnyRoots(String termId, String idType, boolean sortByLength) {
		return getPathsFromRootsInFacet(Lists.newArrayList(termId), idType, sortByLength, "");
	}

	@Override
	public JSONArray getAllPathsFromAnyRoots(String termId, boolean sortByLength) {
		return getAllPathsFromAnyRoots(termId, ConceptConstants.PROP_ID, sortByLength);
	}

	@Override
	public JSONArray getTerm(String id) {
		return getTerms(Lists.newArrayList(id));
	}

	@Override
	public JSONArray getTermPath(String sourceId, String targetId, Type... types) {
		String cypherQuery =
				String.format(
						"START s=node:%s(%s={indexValue}), t=node:%s(%s={indexValue2})" + " MATCH p = shortestPath(s-[:%s*]->t)"
								+ " RETURN EXTRACT(n in NODES(p) | n.%s)", ConceptConstants.INDEX_NAME,
						ConceptConstants.PROP_ID, ConceptConstants.INDEX_NAME, ConceptConstants.PROP_ID,
						StringUtils.join(types, "|"), ConceptConstants.PROP_ID);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("indexValue", sourceId);
		parameters.put("indexValue2", targetId);
		String cypherResponse = sendCypherQuery(cypherQuery, parameters);
		JSONObject responseObject = new JSONObject(cypherResponse);
		JSONArray pathData = responseObject.getJSONArray(DATA);
		if (pathData.length() > 0)
			// The data is a single row - a JSONArray - holding an array with
			// the node IDs - in a JSONArray.
			return pathData.getJSONArray(0).getJSONArray(0);
		return null;
	}

	@Override
	public JSONArray getTerms(int limit) {
		String limitString = limit > 0 ? " LIMIT " + limit : "";
		TransactionalStatement statement =
				new TransactionalStatement(String.format(
						"MATCH (n:%s) WHERE NOT '%s' in labels(n) RETURN n,labels(n)" + limitString,
						TermLabels.GeneralLabel.TERM, TermLabels.GeneralLabel.HOLLOW));
		JSONObject responseObject = sendTransactionalCypherQuery(statement);
		return getDataOfTransactionalResponse(responseObject);
	}

	@Override
	public JSONArray getTerms(Iterable<String> ids) {
		// In the query we use the following abbreviations:
		// ir ~ incoming relation
		// or ~ outgoing relation
		String cypherQuery =
				String.format("START n=node:%s({indexQuery}) RETURN n,labels(n)", ConceptConstants.INDEX_NAME);
		Collection<String> searchTerms = new ArrayList<>();
		for (String id : ids) {
			String searchTerm = ConceptConstants.PROP_ID + ":" + id;
			searchTerms.add(searchTerm);
		}
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("indexQuery", StringUtils.join(searchTerms, " OR "));
		TransactionalStatement statement = new TransactionalStatement(cypherQuery, parameters);
		JSONObject responseObject = sendTransactionalCypherQuery(statement);
		return getDataOfTransactionalResponse(responseObject);
	}

	/**
	 * Returns the relevant data part of a tansactional Neo4j request. The result format also contains information about
	 * the transaction that can be discarded in many of our use cases.
	 * 
	 * @param responseObject
	 * @return
	 */
	private JSONArray getDataOfTransactionalResponse(JSONObject responseObject) {
		if (null == responseObject)
			return null;
		return responseObject.getJSONArray(RESULTS).getJSONObject(0).getJSONArray(DATA);
	}

	/**
	 * Moved to the new semedico resource management project.
	 */
	@Deprecated
	private void insertExtraFacets() {
		Map<String, Object> facetGroupBibliography = new HashMap<>();
		facetGroupBibliography.put(FacetGroupConstants.PROP_NAME, "Bibliography");
		facetGroupBibliography.put(FacetGroupConstants.PROP_POSITION, 2);
		facetGroupBibliography.put(FacetGroupConstants.PROP_GENERAL_LABELS,
				Lists.newArrayList(FacetGroupLabels.General.SHOW_FOR_SEARCH));

		List<Map<String, Object>> extraFacets = new ArrayList<>();
		Map<String, Object> authors = new HashMap<>();
		authors.put(FacetConstants.PROP_NAME, "Authors");
		authors.put(FacetConstants.PROP_CSS_ID, "authors");
		authors.put(FacetConstants.PROP_SEARCH_FIELD_NAMES, Lists.newArrayList(IIndexInformationService.AUTHORS));
		authors.put(FacetConstants.PROP_FILTER_FIELD_NAMES, Lists.newArrayList(IIndexInformationService.FACET_AUTHORS));
		authors.put(FacetConstants.PROP_SOURCE_TYPE, FacetConstants.SRC_TYPE_STRINGS);
		authors.put(FacetConstants.PROP_GENERAL_LABELS, Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS));
		authors.put(FacetConstants.PROP_GENERAL_LABELS,
				Lists.newArrayList(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
		authors.put(FacetConstants.PROP_UNIQUE_LABELS, Lists.newArrayList(FacetLabels.Unique.AUTHORS));
		authors.put(FacetConstants.PROP_POSITION, 0);
		authors.put(FacetConstants.FACET_GROUP, facetGroupBibliography);
		extraFacets.add(authors);

		Map<String, Object> lastAuthors = new HashMap<>();
		lastAuthors.put(FacetConstants.PROP_NAME, "Last Authors");
		lastAuthors.put(FacetConstants.PROP_CSS_ID, "lastAuthors");
		lastAuthors.put(FacetConstants.PROP_SEARCH_FIELD_NAMES,
				Lists.newArrayList(IIndexInformationService.LAST_AUTHORS));
		lastAuthors.put(FacetConstants.PROP_FILTER_FIELD_NAMES,
				Lists.newArrayList(IIndexInformationService.FACET_LAST_AUTHORS));
		lastAuthors.put(FacetConstants.PROP_SOURCE_TYPE, FacetConstants.SRC_TYPE_STRINGS);
		lastAuthors
				.put(FacetConstants.PROP_GENERAL_LABELS, Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS));
		lastAuthors.put(FacetConstants.PROP_GENERAL_LABELS,
				Lists.newArrayList(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
		lastAuthors.put(FacetConstants.PROP_UNIQUE_LABELS, Lists.newArrayList(FacetLabels.Unique.LAST_AUTHORS));
		lastAuthors.put(FacetConstants.PROP_POSITION, 1);
		lastAuthors.put(FacetConstants.FACET_GROUP, facetGroupBibliography);
		extraFacets.add(lastAuthors);

		Map<String, Object> firstAuthors = new HashMap<>();
		firstAuthors.put(FacetConstants.PROP_NAME, "First Authors");
		firstAuthors.put(FacetConstants.PROP_CSS_ID, "firstAuthors");
		firstAuthors.put(FacetConstants.PROP_SEARCH_FIELD_NAMES,
				Lists.newArrayList(IIndexInformationService.FIRST_AUTHORS));
		firstAuthors.put(FacetConstants.PROP_FILTER_FIELD_NAMES,
				Lists.newArrayList(IIndexInformationService.FACET_FIRST_AUTHORS));
		firstAuthors.put(FacetConstants.PROP_SOURCE_TYPE, FacetConstants.SRC_TYPE_STRINGS);
		firstAuthors.put(FacetConstants.PROP_GENERAL_LABELS,
				Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS));
		firstAuthors.put(FacetConstants.PROP_GENERAL_LABELS,
				Lists.newArrayList(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
		firstAuthors.put(FacetConstants.PROP_UNIQUE_LABELS, Lists.newArrayList(FacetLabels.Unique.FIRST_AUTHORS));
		firstAuthors.put(FacetConstants.PROP_POSITION, 2);
		firstAuthors.put(FacetConstants.FACET_GROUP, facetGroupBibliography);
		extraFacets.add(firstAuthors);

		Map<String, Object> journals = new HashMap<>();
		journals.put(FacetConstants.PROP_NAME, "Journals");
		journals.put(FacetConstants.PROP_CSS_ID, "journals");
		journals.put(FacetConstants.PROP_SEARCH_FIELD_NAMES, Lists.newArrayList(IIndexInformationService.JOURNAL));
		journals.put(FacetConstants.PROP_FILTER_FIELD_NAMES,
				Lists.newArrayList(IIndexInformationService.FACET_JOURNALS));
		journals.put(FacetConstants.PROP_SOURCE_TYPE, FacetConstants.SRC_TYPE_STRINGS);
		journals.put(FacetConstants.PROP_GENERAL_LABELS, Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS));
		journals.put(FacetConstants.PROP_GENERAL_LABELS,
				Lists.newArrayList(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
		journals.put(FacetConstants.PROP_UNIQUE_LABELS, Lists.newArrayList(FacetLabels.Unique.JOURNALS));
		journals.put(FacetConstants.PROP_POSITION, 3);
		journals.put(FacetConstants.FACET_GROUP, facetGroupBibliography);
		extraFacets.add(journals);

		Map<String, Object> years = new HashMap<>();
		years.put(FacetConstants.PROP_NAME, "Years");
		years.put(FacetConstants.PROP_CSS_ID, "years");
		years.put(FacetConstants.PROP_SEARCH_FIELD_NAMES, Lists.newArrayList(IIndexInformationService.YEAR));
		years.put(FacetConstants.PROP_FILTER_FIELD_NAMES, Lists.newArrayList(IIndexInformationService.FACET_YEARS));
		years.put(FacetConstants.PROP_SOURCE_TYPE, FacetConstants.SRC_TYPE_STRINGS);
		years.put(FacetConstants.PROP_GENERAL_LABELS, Lists.newArrayList(FacetLabels.General.USE_FOR_SUGGESTIONS));
		years.put(FacetConstants.PROP_GENERAL_LABELS, Lists.newArrayList(FacetLabels.General.USE_FOR_QUERY_DICTIONARY));
		years.put(FacetConstants.PROP_UNIQUE_LABELS, Lists.newArrayList(FacetLabels.Unique.YEARS));
		years.put(FacetConstants.PROP_POSITION, 3);
		years.put(FacetConstants.FACET_GROUP, facetGroupBibliography);
		extraFacets.add(years);

		insertFacets(extraFacets);
		extraFacetsInserted = true;
	}

	/**
	 * @deprecated Only used until the import is done correctly in its own project.
	 */
	@Deprecated
	private void insertFacets(List<Map<String, Object>> extraFacets) {
		HttpPost post = new HttpPost(neo4jEndpoint + "/" + FACET_MANAGER_ENDPOINT + FacetManager.INSERT_FACETS);
		org.neo4j.shell.util.json.JSONArray facetsJson = new org.neo4j.shell.util.json.JSONArray(extraFacets);
		HashMap<String, String> data = new HashMap<String, String>();
		data.put(KEY_FACETS, facetsJson.toString());
		org.neo4j.shell.util.json.JSONObject dataJsonObj = new org.neo4j.shell.util.json.JSONObject(data);
		try {
			// post.setEntity(new StringEntity(dataJsonObj.toString()));
			// HttpResponse response = client.execute(post);
			try {
				HttpEntity entity = httpClientService.sendPostRequest(post, dataJsonObj.toString());
				// log.info("{}", response.getStatusLine());
				// HttpEntity entity = response.getEntity();
				if (entity != null)
					log.info("{}", EntityUtils.toString(entity));
				// do something useful with the response body
				// and ensure it is fully consumed
				EntityUtils.consume(entity);
			} finally {
				post.releaseConnection();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public JSONArray popTermsFromSet(String label, int amount) {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(ConceptManager.KEY_LABEL, label);
		parameterMap.put(ConceptManager.KEY_AMOUNT, amount);
		org.neo4j.shell.util.json.JSONObject jsonMap = new org.neo4j.shell.util.json.JSONObject(parameterMap);

		HttpEntity response =
				httpClientService.sendPostRequest(neo4jEndpoint + TERM_MANAGER_ENDPOINT
						+ ConceptManager.POP_TERMS_FROM_SET, jsonMap.toString());
		try {
			JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response));
			JSONArray jsonTerms = jsonResponse.getJSONArray(ConceptManager.RET_KEY_TERMS);
			return jsonTerms;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// @Override
	// public long pushAllTermsToSet(String label, String facetPropertyKey, String facetPropertyValue,
	// String termPropertyKey, String termPropertyValue) {
	// PushTermsToSetCommand cmd = new PushTermsToSetCommand(label);
	// cmd.eligibleTermDefinition = cmd.new TermSelectionDefinition(facetPropertyKey, facetPropertyValue,
	// termPropertyKey, termPropertyValue);
	// Map<String, Object> parameterMap = new HashMap<>();
	// parameterMap.put(ConceptManager.KEY_TERM_PUSH_CMD, JsonSerializer.toJson(cmd));
	// org.neo4j.shell.util.json.JSONObject jsonMap = new org.neo4j.shell.util.json.JSONObject(parameterMap);
	//
	// HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + TERM_MANAGER_ENDPOINT
	// + ConceptManager.PUSH_ALL_TERMS_TO_SET, jsonMap.toString());
	// try {
	// // We don't expect a response, just consume it for safety.
	// String numberOfPushedTermsStr = EntityUtils.toString(response);
	// return Long.parseLong(numberOfPushedTermsStr);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return -1;
	// }

	@Override
	public long pushTermsToSet(PushTermsToSetCommand cmd, int amount) {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put(ConceptManager.KEY_TERM_PUSH_CMD, JsonSerializer.toJson(cmd));
		if (amount > 0)
			parameterMap.put(ConceptManager.KEY_AMOUNT, amount);
		org.neo4j.shell.util.json.JSONObject jsonMap = new org.neo4j.shell.util.json.JSONObject(parameterMap);

		HttpEntity response =
				httpClientService.sendPostRequest(
						neo4jEndpoint + TERM_MANAGER_ENDPOINT + ConceptManager.PUSH_TERMS_TO_SET, jsonMap.toString());
		try {
			// We don't expect a response, just consume it for safety.
			String numberOfPushedTermsStr = EntityUtils.toString(response);
			return Long.parseLong(numberOfPushedTermsStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Only use from absolutely Neo4j-dependent classes.
	 * 
	 * @param cypherQuery
	 * @return
	 */
	public String sendCypherQuery(String cypherQuery) {
		return sendCypherQuery(cypherQuery, null);
	}

	/**
	 * Only use from absolutely Neo4j-dependent classes.
	 * 
	 * @param cypherQuery
	 * @return
	 */
	public JSONObject sendTransactionalCypherQuery(TransactionalStatement statement) {
		try {
			HttpEntity request = httpClientService.sendPostRequest(neo4jEndpoint + "/" + TRANSACTIONAL_ENDPOINT,
					statement.toJson());
			if (null == request) {
				log.warn("Could not connect to Neo4j");
				return null;
			}
			String cypherResponse =
					EntityUtils.toString(
							request, "UTF-8");
			JSONObject responseObject = new JSONObject(cypherResponse);
			if (responseObject.getJSONArray(Neo4jService.ERRORS).length() > 0) {
				JSONArray errors = responseObject.getJSONArray(Neo4jService.ERRORS);
				log.error("The transactional Cypher endpoint reported (an) error(s): {}", errors);
				log.error("Request was: {}", statement);
			} else {
				return responseObject;
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Only use from absolutely Neo4j-dependent classes.
	 * 
	 * @param cypherQuery
	 * @return
	 */
	public String sendCypherQuery(String cypherQuery, Map<String, Object> parameters) {
		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("query", cypherQuery);
		if (null != parameters)
			queryMap.put("params", parameters);
		org.neo4j.shell.util.json.JSONObject jsonQueryObject = new org.neo4j.shell.util.json.JSONObject(queryMap);

		HttpEntity response =
				httpClientService.sendPostRequest(neo4jEndpoint + "/" + CYPHER_ENDPOINT, jsonQueryObject.toString());
		try {
			return EntityUtils.toString(response, StandardCharsets.UTF_8);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// private HttpEntity sendPostRequest(HttpPost reusablePost, String request) {
	// try {
	// if (!StringUtils.isBlank(request))
	// reusablePost.setEntity(new StringEntity(request));
	// HttpResponse response = client.execute(reusablePost);
	// HttpEntity entity = response.getEntity();
	// // We take all 200 values with us, because 204 is not really an
	// // error. To get specific return codes, see HttpStatus
	// // constants.
	// if (response.getStatusLine().getStatusCode() < 300) {
	// return entity;
	// }
	// String responseString = EntityUtils.toString(entity);
	// log.error("Error when posting a request to Neo4j Server: {}",
	// null != entity && !StringUtils.isBlank(responseString) ? responseString : response.getStatusLine());
	// log.error("The request was: {}", request);
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }
	//
	// private HttpEntity sendPostRequest(String address, String request) {
	// HttpPost post = new HttpPost(address);
	// post.setHeader("Content-type", "application/json");
	// return sendPostRequest(post, request);
	// }
	//
	// private HttpEntity sendPostRequest(String address) {
	// HttpPost post = new HttpPost(address);
	// return sendPostRequest(post, null);
	// }

	/**
	 * Creates automatic indexes
	 */
	private void setupNeo4jServer() {
		log.info("NEO4J SERVER IS SET UP; THIS SHOULD ONLY HAPPEN ONCE");
		log.info("Creating automatic index on label " + TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS);
		String cypherQuery =
				"CREATE INDEX ON :" + TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS
						+ "("
						+ ConceptConstants.PROP_ID
						+ ")";
		sendCypherQuery(cypherQuery);

		log.info("Creating automatic index on label " + TermLabels.GeneralLabel.PENDING_FOR_QUERY_DICTIONARY);
		cypherQuery =
				"CREATE INDEX ON :" + TermLabels.GeneralLabel.PENDING_FOR_QUERY_DICTIONARY
						+ "("
						+ ConceptConstants.PROP_ID
						+ ")";
		sendCypherQuery(cypherQuery);

		log.info("Creating automatic index on label " + TermLabels.GeneralLabel.TERM);
		cypherQuery = "CREATE INDEX ON :" + TermLabels.GeneralLabel.TERM + "(" + ConceptConstants.PROP_ID + ")";
		sendCypherQuery(cypherQuery);
		// log.info("Creating automatic index on label UNIQUE");
		// cypherQuery = "CREATE INDEX ON :UNIQUE(" + NodeConstants.PROP_NAME +
		// ")";
		// sendCypherQuery(cypherQuery);
		neo4jServerIsSetUp = true;
	}

	@Override
	public boolean termPathExists(String sourceId, String targetId, Type... types) {
		JSONArray termPath = getTermPath(sourceId, targetId, types);
		return null != termPath;
	}

	@Override
	public JSONObject getTermChildren(Iterable<? extends String> termIds, String label) {
		Map<String, Object> parameter = new HashMap<>();
		parameter.put(ConceptManager.KEY_TERM_IDS, gson.toJson(termIds));
		parameter.put(ConceptManager.KEY_LABEL, label);
		HttpEntity response =
				httpClientService.sendPostRequest(neo4jEndpoint + "/"
						+ TERM_MANAGER_ENDPOINT
						+ ConceptManager.GET_CHILDREN_OF_TERMS, gson.toJson(parameter));
		try {
			String responseString = EntityUtils.toString(response, "UTF-8");
			return new JSONObject(responseString);
		} catch (ParseException | IOException e) {
			log.error("Error while requesting children of terms with IDs {}: {}", termIds, e);
		}
		return null;
	}

	@Override
	public JSONArray getShortestRootPathInFacet(String termId, String facetId) {
		Map<String, Object> params = new HashMap<>();
		params.put("termId", termId);
		params.put("facetId", facetId);
		String response =
				sendCypherQuery(
						"match (n:TERM {id:{termId}}),(f:FACET {id:{facetId}}), p = shortestPath(f-[:HAS_ROOT_TERM|IS_BROADER_THAN*..]->n) return TAIL(EXTRACT(t in nodes(p) | t.id))",
						params);
		// response format is like this:
		// {
		// "columns" : [ "TAIL(EXTRACT(t in nodes(p) | t.id))" ],
		// "data" : [ [ [ "tid162", "tid81" ] ] ]
		// }
		JSONObject responseObject = new JSONObject(response);
		JSONArray data = responseObject.getJSONArray(DATA);
		// Check whether there is no path
		if (data.length() == 0)
			return data;
		// If there is a path, return it;
		// we need: "of the returned data the first column and from that the array we expect there"
		JSONArray pathArray = data.getJSONArray(0).getJSONArray(0);
		return pathArray;
		// Not yet really needed, but if we do, this is the way it works:
		// match (n:TERM {id:"tid5115066"}),(f:FACET {id:"fid344"}), p =
		// shortestPath(f-[:HAS_ROOT_TERM|IS_BROADER_THAN]->n) return TAIL(EXTRACT(t in nodes(p) | t.id));
		// throw new RuntimeException("Not implemented");
	}

	@Override
	public JSONArray getTermIdsByLabel(String label) {
		String response = sendCypherQuery("MATCH (t:" + label + ") RETURN COLLECT(t.id)");
		JSONObject responseObject = new JSONObject(response);
		JSONArray data = responseObject.getJSONArray(DATA);
		// Check whether there are no terms
		if (data.length() == 0)
			return data;
		// Return the first column of the first row
		return data.getJSONArray(0).getJSONArray(0);
	}
}
