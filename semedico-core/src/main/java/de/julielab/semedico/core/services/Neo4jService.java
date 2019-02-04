package de.julielab.semedico.core.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.FacetManager;
import de.julielab.neo4j.plugins.datarepresentation.PushConceptsToSetCommand;
import de.julielab.neo4j.plugins.datarepresentation.constants.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.constants.FacetConstants;
import de.julielab.neo4j.plugins.datarepresentation.constants.FacetGroupConstants;
import de.julielab.semedico.commons.concepts.FacetGroupLabels;
import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.ConceptDescription;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetGroup;
import de.julielab.semedico.core.services.interfaces.IConceptDatabaseService;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import de.julielab.semedico.core.util.ConceptLoadingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Neo4jService implements IConceptDatabaseService {

    public static final String CONCEPT_MANAGER_ENDPOINT = "db/data/ext/" + ConceptManager.class.getSimpleName() + "/graphdb/";
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
     */
    static final String RESULTS = "results";
    /**
     * <p>
     * Only use from absolutely Neo4j-dependend places.
     * </p>
     * <p>
     * The name of the key containing the results in 'graph' format from the transactional endpoint.
     * </p>
     */
    static final String GRAPH = "graph";
    /**
     * <p>
     * Only use from absolutely Neo4j-dependend places.
     * </p>
     * <p>
     * The name of the key containing the results in 'row' format from the transactional endpoint.
     * </p>
     */
    static final String ROW = "row";
    /**
     * The to error messages in the results of a transactional cypher request.
     */
    private static final String ERRORS = "errors";
    private final static String[] EMPTY_STRING_ARRAY = new String[0];
    private final Logger log;
    private final String neo4jHttpUrl;
    private Gson gson;
    private IHttpClientService httpClientService;
    private Driver driver;

    public Neo4jService(Logger log, @INeo4jHttpClientService.Neo4jHttpClient IHttpClientService httpClientService,
                        @Symbol(SemedicoSymbolConstants.NEO4J_HOST) String neo4jHost, @Symbol(SemedicoSymbolConstants.NEO4J_HTTP_PORT) int neo4jHttpPort, Driver driver) {
        this.log = log;
        this.httpClientService = httpClientService;
        this.neo4jHttpUrl = "http://" + neo4jHost + ":" + neo4jHttpPort + "/";
        this.driver = driver;
        log.info("Connected HTTP Neo4j client for the concept database to {}", neo4jHttpUrl);
        gson = new Gson();
    }


    @Override
    public List<String> getFacetIdsWithGeneralLabel(FacetLabels.General label) {
        String cypherQuery =
                String.format("MATCH (f:%s) WHERE {label} in labels(f) RETURN f.%s", FacetLabel.FACET,
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

    @Override
    public String[] getFacetRootIDs(String facetId) {
        String cypherQuery =
                String.format(
                        "MATCH (fgr:%s)-[:%s]->()-[:%s]->f-[:%s]->rt "
                                + "WHERE fgr.%s = {facetGroupsName} AND f.%s = {facetId} AND NOT 'HOLLOW' IN labels(rt)"
                                + " RETURN COLLECT(rt.%s)", MetaLabel.ROOT, MetaEdgeType.HAS_FACET_GROUP,
                        MetaEdgeType.HAS_FACET, IConceptRelation.Type.HAS_ROOT_CONCEPT, FacetGroupConstants.PROP_NAME,
                        FacetConstants.PROP_ID, ConceptConstants.PROP_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("facetGroupsName", FacetConstants.NAME_FACET_GROUPS);
        parameters.put("facetId", facetId);
        String cypherResponse = sendCypherQuery(cypherQuery, parameters);
        JSONObject responseObject = new JSONObject(cypherResponse);
        if (responseObject.getJSONArray(DATA).length() > 0) {
            // Return from the first row the first field, which is an array of
            // facet root term IDs.
            JSONArray pathArray = responseObject.getJSONArray(DATA).getJSONArray(0).getJSONArray(0);
            String[] ret = new String[pathArray.length()];
            for (int i = 0; i < pathArray.length(); i++)
                ret[i] = pathArray.getString(i);
        }
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public String[] getFacetRootIDs(Iterable<? extends String> facetIds) {
        String cypherQuery =
                String.format(
                        "MATCH (fgr:%s)-[:%s]->()-[:%s]->f-[:%s]->rt "
                                + "WHERE fgr.%s = {facetGroupsName} AND f.%s IN {facetIds} AND NOT 'HOLLOW' IN labels(rt)"
                                + " RETURN f.id,COLLECT(rt.%s)", MetaLabel.ROOT, MetaEdgeType.HAS_FACET_GROUP,
                        MetaEdgeType.HAS_FACET, IConceptRelation.Type.HAS_ROOT_CONCEPT, FacetGroupConstants.PROP_NAME,
                        FacetConstants.PROP_ID, ConceptConstants.PROP_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("facetGroupsName", FacetConstants.NAME_FACET_GROUPS);
        parameters.put("facetIds", facetIds);
        String cypherResponse = sendCypherQuery(cypherQuery, parameters);
        JSONObject responseObject = new JSONObject(cypherResponse);
        if (responseObject.getJSONArray(DATA).length() > 0) {
            // Return from the first row the first field, which is an array of
            // facet root term IDs.
            JSONArray pathArray = responseObject.getJSONArray(DATA).getJSONArray(0).getJSONArray(0);
            String[] ret = new String[pathArray.length()];
            for (int i = 0; i < pathArray.length(); i++)
                ret[i] = pathArray.getString(i);
        }
        return EMPTY_STRING_ARRAY;
    }

    @Override
    public Multimap<String, ConceptDescription> getFacetRootConcepts(Iterable<? extends String> facetIds,
                                                                     Map<String, List<String>> requestedRootIds, int maxRoots) throws ConceptLoadingException {
        HttpPost post = new HttpPost(neo4jHttpUrl + "/" + CONCEPT_MANAGER_ENDPOINT + ConceptManager.GET_FACET_ROOTS);

        Map<String, Object> params = new HashMap<>();
        params.put(ConceptManager.KEY_FACET_IDS, gson.toJson(facetIds));
        if (null != requestedRootIds && !requestedRootIds.isEmpty())
            params.put(ConceptManager.KEY_CONCEPT_IDS, gson.toJson(requestedRootIds));
        params.put(ConceptManager.KEY_MAX_ROOTS, maxRoots);
        try {
            HttpEntity entity = httpClientService.sendPostRequest(post, gson.toJson(params));
            if (null != entity) {
                ObjectMapper om = new ObjectMapper();
                ObjectReader objectReader = om.readerFor(ConceptDescription.class);
                String facetRootsJsonString = EntityUtils.toString(entity);
                JsonNode facetRootsJson = om.readTree(facetRootsJsonString);
                // The value from Neo4j is actually a Map
                // {"<facetId>":[...]} containing a list of facet roots.
                Multimap<String, ConceptDescription> roots = HashMultimap.create();
                for (Map.Entry<String, JsonNode> field : (Iterable<Map.Entry<String, JsonNode>>) () -> facetRootsJson.fields()) {
                    String facetId = field.getKey();
                    JsonNode conceptList = field.getValue();
                    for (int i = 0; i < conceptList.size(); i++) {
                        JsonNode conceptNode = conceptList.get(i);
                        ConceptDescription conceptDescription = objectReader.readValue(conceptNode);
                        roots.put(facetId, conceptDescription);
                    }
                }

                return roots;
            }
        } catch (IOException e) {
            throw new ConceptLoadingException(e);
        }
        return null;
    }

    @Override
    public Stream<FacetGroup<Facet>> getFacetGroups(boolean getHollowFacets) {
        HttpPost post = new HttpPost(neo4jHttpUrl + "/" + FACET_MANAGER_ENDPOINT + FacetManager.GET_FACETS);
        Map<String, Object> params = new HashMap<>();
        params.put(FacetManager.PARAM_RETURN_HOLLOW_FACETS, getHollowFacets);
        try {
            HttpEntity entity = httpClientService.sendPostRequest(post, gson.toJson(params));
            if (null != entity) {

                String facetGroupsJsonString = EntityUtils.toString(entity);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonTree = mapper.readTree(facetGroupsJsonString);
                return StreamSupport.stream(jsonTree.withArray("facetGroups").spliterator(), true).map(fgNode -> {
                    JsonNode facetsNode = fgNode.findValue("facets");
                    FacetGroup<Facet> fg = new FacetGroup<>(fgNode.get(FacetGroupConstants.PROP_NAME).asText(), fgNode.get(FacetGroupConstants.PROP_POSITION).asInt());
                    Optional.ofNullable(fgNode.get(FacetGroupConstants.PROP_TYPE)).ifPresent(typeString -> fg.setType(FacetGroupLabels.Type.valueOf(typeString.asText())));
                    for (int i = 0; i < facetsNode.size(); i++) {
                        JsonNode facetNode = facetsNode.get(i);
                        try {
                            fg.add(mapper.treeToValue(facetNode, Facet.class));
                        } catch (JsonProcessingException e) {
                            log.error("Error when deserializing facet with json {}:", facetNode, e);
                        }
                    }
                    return fg;
                });
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getNumConcepts() {
        HttpEntity response =
                httpClientService.sendPostRequest(neo4jHttpUrl + CONCEPT_MANAGER_ENDPOINT + ConceptManager.GET_NUM_CONCEPTS);
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
    public String[] getShortestPathFromAnyRoot(String conceptId, String idType) {
        return getAllPathsFromAnyRoots(conceptId, idType, true)[0];
    }

    @Override
    public String[] getShortestPathFromAnyRoot(String conceptId) {
        return getShortestPathFromAnyRoot(conceptId, ConceptConstants.PROP_ID);
    }

    @Override
    public String[][] getPathsFromRoots(Collection<String> termIds, String idType) {
        return getPathsFromRootsInFacet(termIds, idType, false, "");
    }


    @Override
    public String[][] getPathsFromRootsInFacet(Collection<String> termIds, String idType, boolean sortByLength,
                                               String facetId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ConceptManager.KEY_CONCEPT_IDS, gson.toJson(termIds));
        parameters.put(ConceptManager.KEY_ID_TYPE, idType);
        parameters.put(ConceptManager.KEY_SORT_RESULT, sortByLength);
        parameters.put(ConceptManager.KEY_FACET_ID, facetId);
        String requestString = gson.toJson(parameters);
        HttpEntity response =
                httpClientService.sendPostRequest(neo4jHttpUrl + "/"
                        + CONCEPT_MANAGER_ENDPOINT
                        + ConceptManager.GET_PATHS_FROM_FACETROOTS, requestString);
        try {
            JSONObject pathsObject = new JSONObject(EntityUtils.toString(response));
            JSONArray paths = pathsObject.getJSONArray(ConceptManager.RET_KEY_PATHS);
            String[][] ret = new String[paths.length()][];
            for (int i = 0; i < paths.length(); i++) {
                JSONArray jsonPath = paths.getJSONArray(i);
                ret[i] = new String[jsonPath.length()];
                for (int j = 0; j < jsonPath.length(); j++) {
                    ret[i][j] = jsonPath.getString(j);
                }
            }
            return ret;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public String[][] getAllPathsFromAnyRoots(String termId, String idType, boolean sortByLength) {
        return getPathsFromRootsInFacet(Lists.newArrayList(termId), idType, sortByLength, "");
    }

    @Override
    public String[][] getAllPathsFromAnyRoots(String conceptId, boolean sortByLength) {
        return getAllPathsFromAnyRoots(conceptId, ConceptConstants.PROP_ID, sortByLength);
    }

    @Override
    public Optional<ConceptDescription> getConcept(String id) {
        return getConcepts(Collections.singletonList(id)).findAny();
    }

    @Override
    public String[] getConceptPath(String sourceId, String targetId, IConceptRelation.Type... types) {
        // Neo4j does not allow it and unless we really need we just forbid it
        if (sourceId.equals(targetId))
            throw new IllegalArgumentException("Getting the shortest path between a concept and itself is not supported." +
                    " Requested source and target ID was " + sourceId + ", allowed relationship types were " + Arrays.toString(types));
        String cypherQuery = String.format(
                "MATCH (c:CONCEPT {id:$indexValue}), (b:CONCEPT {id:$indexValue2}), p=shortestPath((c)-[%s*1..]-(b)) RETURN EXTRACT(n in NODES(p) | n.%s)",
                StringUtils.join(types, "|"),
                ConceptConstants.PROP_ID);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("indexValue", sourceId);
        parameters.put("indexValue2", targetId);
        String[] result;
        try (Session session = driver.session()) {
            StatementResult queryResult = session.readTransaction(tx -> tx.run(cypherQuery, parameters));
            if (queryResult.hasNext()) {
                List<String> path = queryResult.single().get(0).asList(Value::asString);
                result = path.toArray(EMPTY_STRING_ARRAY);
            } else {
                result = EMPTY_STRING_ARRAY;
            }
        }
        return result;
    }

    @Override
    public Stream<ConceptDescription> getConcepts(int limit) {
        String limitString = limit > 0 ? " LIMIT " + limit : "";
        TransactionalStatement statement =
                new TransactionalStatement(String.format(
                        "MATCH (n:%s) WHERE NOT '%s' in labels(n) RETURN n,labels(n)" + limitString,
                        TermLabels.GeneralLabel.TERM, TermLabels.GeneralLabel.HOLLOW));
        JsonNode responseObject = sendTransactionalCypherQuery(statement);
        return getDataOfTransactionalResponse(responseObject);
    }

    @Override
    public Stream<ConceptDescription> getConcepts(Iterable<String> ids) {
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
        JsonNode responseObject = sendTransactionalCypherQuery(statement);
        return getDataOfTransactionalResponse(responseObject);
    }

    /**
     * Returns the relevant data part of a tansactional Neo4j request. The result format also contains information about
     * the transaction that can be discarded in many of our use cases.
     *
     * @param responseObject
     * @return
     */
    private Stream<ConceptDescription> getDataOfTransactionalResponse(JsonNode responseObject) {
        if (null == responseObject)
            return null;
        // Format:
        // [
        //	{
        //	"row": [
        //		<concept as JSON object>,
        //   [
        //		"CONCEPT"
        //   ]
        //  ],
        //	"meta": [
        //	{
        //		"id": 33,
        //		"type": "node",
        //		"deleted": false
        //	},
        //		null
        //   ]
        //	},
        // {
        //    "row": []
        // }
        // Thus each row is an object holding one "row" object. The row is an array of columns. We are interested
        // in the first column as this is the concept and the second columns which holds the labels of the concept.
        List<JsonNode> rows = responseObject.findValues("row");
        ObjectMapper om = new ObjectMapper();
        ObjectReader or = om.readerFor(ConceptDescription.class);
        return rows.stream().
                map(row -> {
                    JsonNode conceptJson = row.get(0);
                    JsonNode labels = row.get(1);
                    ConceptDescription description = null;
                    try {
                        description = or.readValue(conceptJson);
                        String[] labelArray = StreamSupport.stream(labels.spliterator(), false).map(JsonNode::asText).toArray(String[]::new);
                        description.setLabels(labelArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return description;
                }).filter(Objects::nonNull);
    }

    @Override
    public List<ConceptDescription> popTermsFromSet(String label, int amount) throws ConceptLoadingException {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put(ConceptManager.KEY_LABEL, label);
        parameterMap.put(ConceptManager.KEY_AMOUNT, amount);
        org.neo4j.shell.util.json.JSONObject jsonMap = new org.neo4j.shell.util.json.JSONObject(parameterMap);

        HttpEntity response =
                httpClientService.sendPostRequest(neo4jHttpUrl + CONCEPT_MANAGER_ENDPOINT
                        + ConceptManager.POP_CONCEPTS_FROM_SET, jsonMap.toString());

        List<ConceptDescription> concepts = new ArrayList<>();
        try {
            ObjectMapper om = new ObjectMapper();
            ObjectReader objectReader = om.readerFor(ConceptDescription.class);
            String responseString = EntityUtils.toString(response);
            JsonNode conceptsReturnMap = om.readTree(responseString);
            // The value from Neo4j is actually a Map
            // {"concepts":[...]} containing a list of popped concept objects.
            JsonNode conceptList = conceptsReturnMap.get(ConceptManager.RET_KEY_CONCEPTS);
            for (int i = 0; i < conceptList.size(); i++) {
                JsonNode conceptNode = conceptList.get(i);
                ConceptDescription conceptDescription = objectReader.readValue(conceptNode);
                concepts.add(conceptDescription);
            }
        } catch (IOException e) {
            throw new ConceptLoadingException(e);
        }
        return concepts;
    }

    @Override
    public long pushTermsToSet(PushConceptsToSetCommand cmd, int amount) {
        Map<String, Object> parameterMap = new HashMap<>();
        Gson gson = new Gson();
        parameterMap.put(ConceptManager.KEY_CONCEPT_PUSH_CMD, gson.toJson(cmd));
        if (amount > 0)
            parameterMap.put(ConceptManager.KEY_AMOUNT, amount);
        org.neo4j.shell.util.json.JSONObject jsonMap = new org.neo4j.shell.util.json.JSONObject(parameterMap);

        HttpEntity response =
                httpClientService.sendPostRequest(
                        neo4jHttpUrl + CONCEPT_MANAGER_ENDPOINT + ConceptManager.PUSH_CONCEPTS_TO_SET, jsonMap.toString());
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

    public JsonNode sendTransactionalCypherQuery(TransactionalStatement statement) {
        try {
            HttpEntity request = httpClientService.sendPostRequest(neo4jHttpUrl + "/" + TRANSACTIONAL_ENDPOINT,
                    statement.toJson());
            if (null == request) {
                log.warn("Could not connect to Neo4j");
                return null;
            }
            String cypherResponse =
                    EntityUtils.toString(
                            request, "UTF-8");
            ObjectMapper m = new ObjectMapper();
            JsonNode jsonTree = m.readTree(cypherResponse);
            if (jsonTree.withArray("errors").size() > 0) {
                log.error("The transactional Cypher endpoint reported (an) error(s): {}", jsonTree.get("errors"));
                log.error("Request was: {}", statement);
            } else {
                return jsonTree;
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
                httpClientService.sendPostRequest(neo4jHttpUrl + "/" + CYPHER_ENDPOINT, jsonQueryObject.toString());
        try {
            return EntityUtils.toString(response, StandardCharsets.UTF_8);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public StatementResult sendCypherQueryViaBolt(String query, Map<String, Object> parameters) {
        try (Session session = driver.session()) {
            StatementResult result = session.run(query, parameters);
            return result;
        }
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
    // HttpEntity response = httpClientService.sendPostRequest(neo4jHttpUrl + CONCEPT_MANAGER_ENDPOINT
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
    public boolean termPathExists(String sourceId, String targetId, IConceptRelation.Type... types) {
        String[] termPath = getConceptPath(sourceId, targetId, types);
        return termPath.length > 0;
    }

    @Override
    public JSONObject getTermChildren(Iterable<? extends String> termIds, String label) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put(ConceptManager.KEY_CONCEPT_IDS, gson.toJson(termIds));
        parameter.put(ConceptManager.KEY_LABEL, label);
        HttpEntity response =
                httpClientService.sendPostRequest(neo4jHttpUrl + "/"
                        + CONCEPT_MANAGER_ENDPOINT
                        + ConceptManager.GET_CHILDREN_OF_CONCEPTS, gson.toJson(parameter));
        try {
            String responseString = EntityUtils.toString(response, "UTF-8");
            return new JSONObject(responseString);
        } catch (ParseException | IOException e) {
            log.error("Error while requesting children of terms with IDs {}: {}", termIds, e);
        }
        return null;
    }

    @Override
    public String[] getShortestRootPathInFacet(String conceptId, String facetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("conceptId", conceptId);
        params.put("facetId", facetId);
        String response =
                sendCypherQuery(
                        String.format(
                                "MATCH (n:%s {id:{conceptId}})," +
                                        "(f:FACET {id:{facetId}})," +
                                        " p = shortestPath((f)-[:%s|%s*..]->(n))" +
                                        " RETURN TAIL(EXTRACT(t IN NODES(p) | t.id))",
                                ConceptManager.ConceptLabel.CONCEPT,
                                ConceptManager.EdgeTypes.HAS_ROOT_CONCEPT,
                                ConceptManager.EdgeTypes.IS_BROADER_THAN),
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
            return EMPTY_STRING_ARRAY;
        // If there is a path, return it;
        // we need: "of the returned data the first column and from that the array we expect there"
        JSONArray pathArray = data.getJSONArray(0).getJSONArray(0);
        String[] ret = new String[pathArray.length()];
        for (int i = 0; i < pathArray.length(); i++) {
            ret[i] = pathArray.getString(i);
        }
        return ret;
    }

    @Override
    public String[] getTermIdsByLabel(String label) {
        String response = sendCypherQuery("MATCH (t:" + label + ") RETURN COLLECT(t.id)");
        JSONObject responseObject = new JSONObject(response);
        JSONArray data = responseObject.getJSONArray(DATA);
        // Check whether there are no terms
        if (data.length() == 0)
            return EMPTY_STRING_ARRAY;
        // Return the first column of the first row
        JSONArray ids = data.getJSONArray(0).getJSONArray(0);
        String[] ret = new String[ids.length()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = ids.getString(i);
        return ret;
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

    public static class FacetGroupsResponse {
        public List<FacetGroup<Facet>> facetGroups;
    }

    public static class TransactionalStatement {
        private static transient Gson gson = new Gson();
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

        public String toJson() {
            return gson.toJson(this);
        }

        @Override
        public String toString() {
            return "TransactionalStatement [statements=" + statements + "]";
        }

        public static class Statement {
            public String statement;
            public Map<String, Object> parameters;
            public String[] resultDataContents;

            public Statement(String cypherQuery) {
                statement = cypherQuery;
                resultDataContents = new String[]{Neo4jService.ROW};
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

    }
}
