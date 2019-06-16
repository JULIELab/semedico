package de.julielab.semedico.core.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonSyntaxException;
import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.neo4j.plugins.datarepresentation.PushConceptsToSetCommand;
import de.julielab.neo4j.plugins.datarepresentation.constants.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.commons.concepts.FacetLabels;
import de.julielab.semedico.core.entities.ConceptRelation;
import de.julielab.semedico.core.entities.ConceptRelationKey;
import de.julielab.semedico.core.entities.TermFacetKey;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.*;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation.Type;
import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.*;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.util.ConceptCreationException;
import de.julielab.semedico.core.util.ConceptLoadingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class ConceptNeo4jService extends BaseConceptService {
    private IConceptDatabaseService neo4jService;
    private LoadingCache<String, List<Concept>> facetRootCache;
    private LoadingCache<ConceptRelationKey, IConceptRelation> relationshipCache;
    private LoadingCache<TermFacetKey, IPath> shortestRootPathInFacetCache;
    private LoadingCache<String, IPath> shortestRootPathCache;
    private LoadingCache<Pair<String, String>, Collection<IPath>> allRootPathsInFacetCache;

    public ConceptNeo4jService(Logger logger, ICacheService cacheService,
                               IConceptDatabaseService neo4jService, IConceptCreator conceptCreator) {
        super(logger, conceptCreator, null, cacheService != null ?  cacheService
                .getCache(Region.TERM) : null);
        this.neo4jService = neo4jService;
        // In production, the cacheService should never be null. This is only for tests.
        if (cacheService != null) {
            this.facetRootCache = cacheService.getCache(Region.FACET_ROOTS);
            this.shortestRootPathCache = cacheService.getCache(Region.ROOT_PATHS);
            this.allRootPathsInFacetCache = cacheService.getCache(Region.ROOT_PATHS_IN_FACET);
            this.shortestRootPathInFacetCache = cacheService
                    .getCache(Region.SHORTEST_ROOT_PATH_IN_FACET);
            this.relationshipCache = cacheService.getCache(Region.RELATIONSHIP);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.julielab.semedico.core.services.IConceptService#getFacetRoots(de.julielab
     * .semedico.core.Facet)
     */
    @Override
    public List<Concept> getFacetRoots(Facet facet) {
        return facetRootCache.getUnchecked(facet.getId());
    }

    @Override
    public Map<String, List<Concept>> assureFacetRootsLoaded(Map<Facet, List<String>> requestRootIds) throws ConceptLoadingException, ConceptCreationException {
        Map<String, List<String>> missingPotentialRoots = new HashMap<>();
        for (Entry<Facet, List<String>> facetRootRequest : requestRootIds.entrySet()) {
            Facet facet = facetRootRequest.getKey();
            if (facet.allDBRootsLoaded()) {
                log.debug("Skipping facet {} because it already has all its roots loaded.",
                        facet.getName());
                // It makes no sense to look for more roots when all have been
                // loaded already
                continue;
            }
            String facetId = facet.getId();
            List<String> requestedPotentialRootIds = facetRootRequest.getValue();
            List<String> missingPotentialRootIdList = new ArrayList<>();
            // get already loaded facet roots, we don't need to load those again
            List<Concept> loadedRootList = facetRootCache.getIfPresent(facetId);
            Set<String> loadedRootIds = new HashSet<>();
            if (null != loadedRootList && !loadedRootList.isEmpty()) {
                for (Concept loadedRoot : loadedRootList)
                    loadedRootIds.add(loadedRoot.getId());
            }
            for (String potentialRootId : requestedPotentialRootIds) {
                if (!loadedRootIds.contains(potentialRootId)) {
                    missingPotentialRootIdList.add(potentialRootId);
                }
            }
            if (!missingPotentialRootIdList.isEmpty()) {
                missingPotentialRoots.put(facetId, missingPotentialRootIdList);
                log.debug("Loading {} missing potential roots for facet {} (ID: {})",
                        missingPotentialRootIdList.size(), facet.getName(), facet.getId());
            } else {
                log.debug(
                        "Skipping {} because all terms in the input list have already been loaded or are not root terms.",
                        facet.getName());
            }
        }

        Multimap<String, ConceptDescription> loadedRootConceptDescriptions = neo4jService.getFacetRootConcepts(
                missingPotentialRoots.keySet(), missingPotentialRoots, 0);
        Map<String, List<Concept>> loadedRootTermsMap = FacetRootCacheLoader
                .createFacetRootsFromConceptDescriptions(missingPotentialRoots.keySet(), loadedRootConceptDescriptions, log,
                        conceptCache, conceptCreator);
        for (String facetId : missingPotentialRoots.keySet()) {
            List<Concept> newlyLoadedRoots = loadedRootTermsMap.get(facetId);
            if (null != newlyLoadedRoots && newlyLoadedRoots.size() > 0) {
                // merge newly loaded roots in already loaded roots
                List<Concept> loadedRoots = facetRootCache.getIfPresent(facetId);
                if (null != loadedRoots)
                    newlyLoadedRoots.addAll(loadedRoots);
                facetRootCache.put(facetId, newlyLoadedRoots);
            }
        }
        return loadedRootTermsMap;
    }

    @Override
    public int getNumLoadedRoots(String facetId) {
        List<Concept> roots = facetRootCache.getIfPresent(facetId);
        if (null == roots)
            return 0;
        return roots.size();
    }

    @Override
    public Iterator<IConcept> getTerms() {
        return getTerms(-1);
    }

    @Override
    public Iterator<IConcept> getTerms(int limit) {
        int breakPoint = 1000000;
        int numTerms = neo4jService.getNumConcepts();
        if (numTerms <= breakPoint || (limit > 0 && limit < breakPoint)) {
            // TODO
            final JSONArray termsArray = null;//neo4jService.getConcepts(limit > 0 ? limit : breakPoint);
            // final IConceptService conceptService = this;
            return new Iterator<IConcept>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < termsArray.length();
                }

                @Override
                public IHierarchicalConcept next() {
                    IHierarchicalConcept term = null;
                    if (hasNext()) {
                        JSONArray termRow = termsArray.getJSONObject(i).getJSONArray(
                                Neo4jService.ROW);
                        JSONObject termJsonOBject = termRow.getJSONObject(0);
                        JSONArray termLabels = termRow.getJSONArray(1);
                        // String termId = termArray.getString(0);
                        // term = new FacetTerm(termId, conceptService);
                        // TODO
                        term = null;//conceptCreator.createConceptFromJson(
                        //termJsonOBject.toCompactString(), termLabels);
                        // FacetTermCacheLoader.convertTermJSONObject(term,
                        // termJsonOBject, facetService);

                        i++;
                    }
                    return term;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            // We have too many terms to return them all at once. Thus we fall
            // back to the queue approach.
            String setName = "TEMP_QUEUE_" + System.currentTimeMillis();
            pushAllTermsIntoQueue(setName);
            return getTermsInQueue(setName);
        }
    }

    @Override
    public IPath getShortestPathFromAnyRoot(IConcept node) {
        return shortestRootPathCache.getUnchecked(node.getId()).copyPath();
    }

    @Override
    public boolean isAncestorOf(IConcept candidate, IConcept term) {
        return neo4jService.termPathExists(candidate.getId(), term.getId(), Type.IS_BROADER_THAN);
    }

    public long pushTermsIntoQueue(String queueName, int amount) {
        PushConceptsToSetCommand cmd = new PushConceptsToSetCommand(queueName);
        return neo4jService.pushTermsToSet(cmd, amount);
    }

    public long pushAllTermsIntoQueue(String queueName) {
        return pushTermsIntoQueue(queueName, -1);
    }

    @Override
    public Iterator<IConcept> getTermsInSuggestionQueue() {
        return getTermsInQueue(TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.name());
    }

    @Override
    public Iterator<IConcept> getTermsInQueue(final String setLabel) {
        return new Iterator<IConcept>() {
            private int bufferSize = 10000;
            private Stack<IHierarchicalConcept> buffer = new Stack<>();

            @Override
            public boolean hasNext() {
                if (buffer.size() == 0) {
                    try {
                        loadTermBatch();
                    } catch (ConceptLoadingException e) {
                        log.error("Could not load the concept from queue {}.", setLabel, e);
                    } catch (ConceptCreationException e) {
                        log.error("Exception when trying to create a concept object from a database " +
                                "concept description when loading concepts from queue {}.", setLabel, e);
                    }
                }
                return buffer.size() > 0;
            }

            @Override
            public IHierarchicalConcept next() {
                if (hasNext())
                    return buffer.pop();
                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void loadTermBatch() throws ConceptLoadingException, ConceptCreationException {
                List<ConceptDescription> jsonTerms = neo4jService.popTermsFromSet(setLabel, bufferSize);
                for (ConceptDescription description : jsonTerms) {
                    IHierarchicalConcept concept = conceptCreator.createConceptFromDescription(description, DatabaseConcept.class);
                    buffer.push(concept);
                }
            }

        };
    }

    @Override
    public Iterator<IConcept> getTermsInQueryDictionaryQueue() {
        return getTermsInQueue(TermLabels.GeneralLabel.PENDING_FOR_QUERY_DICTIONARY.name());
    }

    @Override
    public long pushAllTermsIntoSuggestionQueue() {
        PushConceptsToSetCommand cmd = new PushConceptsToSetCommand(
                TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.name());
        cmd.eligibleConceptDefinition = cmd.new ConceptSelectionDefinition();
        // For suggestions, we only use the mapping aggregated Concepts (non-mapped
        // Concepts are their own aggregate).
        cmd.eligibleConceptDefinition.conceptLabel = TermLabels.GeneralLabel.MAPPING_AGGREGATE.name();
        cmd.eligibleConceptDefinition.facetLabel = FacetLabels.General.USE_FOR_SUGGESTIONS.name();
        cmd.excludeConceptDefinition = cmd.new ConceptSelectionDefinition();
        cmd.excludeConceptDefinition.conceptLabel = TermLabels.GeneralLabel.NO_SUGGESTIONS
                .name();
        return neo4jService.pushTermsToSet(cmd, -1);
    }


    @Override
    public long pushAllTermsIntoDictionaryQueue() {
        PushConceptsToSetCommand cmd = new PushConceptsToSetCommand(
                TermLabels.GeneralLabel.PENDING_FOR_QUERY_DICTIONARY.name());
        cmd.eligibleConceptDefinition = cmd.new ConceptSelectionDefinition();
        // For query Concept recognition, we only use the mapping aggregated Concepts
        // (non-mapped Concepts are their own
        // aggregate).
        cmd.eligibleConceptDefinition.conceptLabel = TermLabels.GeneralLabel.MAPPING_AGGREGATE.name();
        cmd.eligibleConceptDefinition.facetLabel = FacetLabels.General.USE_FOR_QUERY_DICTIONARY.name();
        cmd.excludeConceptDefinition = cmd.new ConceptSelectionDefinition();
        cmd.excludeConceptDefinition.conceptLabel = TermLabels.GeneralLabel.DO_NOT_USE_FOR_QUERY_DICTIONARY
                .name();
        return neo4jService.pushTermsToSet(cmd, -1);
    }

    @Override
    public int getNumTerms() {
        return neo4jService.getNumConcepts();
    }

    @Override
    public void loadChildrenOfTerm(Concept facetTerm, String termLabel) {
        log.debug("Loading children of term with ID {} and label {}.", facetTerm.getId(), termLabel);
        StopWatch w = new StopWatch();
        w.start();
        // NOTE: the method called in (and then by) the neo4jService takes every node connected by an outgoing relationship as being a "child". We get back BROADER_THEN relations but also HAS_ELEMENT etc.
        JSONObject childrenMap = neo4jService.getTermChildren(
                Lists.newArrayList(facetTerm.getId()), termLabel);

        JSONObject termChildrenMap = null;
        JSONArray jsonTerms = null;
        JSONObject relationshipMap = null;
        boolean error = false;
        int numRels = 0;

        if (!childrenMap.has(facetTerm.getId())) {
            error = true;
        } else {
            termChildrenMap = childrenMap.getJSONObject(facetTerm.getId());
            jsonTerms = termChildrenMap.getJSONArray(ConceptManager.RET_KEY_CHILDREN);
            relationshipMap = termChildrenMap.getJSONObject(ConceptManager.RET_KEY_RELTYPES);
        }

        if (null == jsonTerms) {
            error = true;
        }

        if (error) {
            log.error("No children (outgoing relationships) data of the term with ID \""
                    + facetTerm.getId()
                    + "\" were returned (not even that there are no children!). This most certainly means that the respective term ID was not found in the term database with the label \""
                    + TermLabels.GeneralLabel.MAPPING_AGGREGATE
                    + "\". Make sure that mapping aggregates have been built (even if there are no mappings!).");
            return;
        }

        try {
            for (int i = 0; i < jsonTerms.length(); i++) {
                JSONObject jsonTerm = jsonTerms.getJSONObject(i);
                String termId = jsonTerm.getString(ConceptConstants.PROP_ID);
                IConcept term = conceptCache.getIfPresent(termId);
                if (null == term) {
                    // TODO
                    JSONArray termLabels = jsonTerm.getJSONArray(ConceptConstants.KEY_LABELS);
                    term = null;//(Concept) conceptCreator.createConceptFromJson(
                    //jsonTerm.toCompactString(), termLabels);
                    // term = gson.fromJson(jsonTerm.toCompactString(),
                    // SyncDbConcept.class);
                    conceptCache.put(termId, term);
                }
            }

            // Map<IConceptRelation.Type, List<IConceptRelation>>
            // childRelationships = new HashMap<>();
            for (String termId : relationshipMap.keys()) {
                JSONArray reltypes = relationshipMap.getJSONArray(termId);
                for (int i = 0; i < reltypes.length(); i++) {
                    ++numRels;
                    String reltype = reltypes.getString(i);
                    ConceptRelationKey conceptRelationKey = new ConceptRelationKey(facetTerm.getId(),
                            termId, reltype);
                    IConceptRelation relationship = relationshipCache
                            .getIfPresent(conceptRelationKey);
                    if (null == relationship) {
                        relationship = new ConceptRelation(conceptRelationKey, this);
                        relationshipCache.put(conceptRelationKey, relationship);
                    }
                    facetTerm.addOutgoingRelationship(relationship);
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        w.stop();
        log.debug(
                "Loading of {} outgoing relations for term {} took {}ms ({}s).",
                new Object[]
                        {
                                numRels, facetTerm.getId(), w.getTime(), w.getTime() / 1000
                        });
    }

    @Override
    public void loadChildrenOfTerm(Concept facetTerm) {
        // the restriction to TERM here means that aggregates won't get their
        // elements with this method! So for those we
        // have to use the overloaded method and specify the MAPPING_AGGREGATE
        // label.
        loadChildrenOfTerm(facetTerm, TermLabels.GeneralLabel.TERM.name());
    }

    @Override
    public Collection<IPath> getAllPathsFromRootsInFacet(IConcept term, Facet facet) {
        String termId = term.getId();
        String facetId = null != facet ? facet.getId() : "";
        return allRootPathsInFacetCache.getUnchecked(new ImmutablePair<String, String>(termId,
                facetId));
    }

    @Override
    public IPath getShortestRootPathInFacet(IConcept node, Facet facet) {
        TermFacetKey key = new TermFacetKey(node.getId(), facet.getId());
        return shortestRootPathInFacetCache.getUnchecked(key).copyPath();
    }

    @Override
    public List<IConcept> getTermsByLabel(String label, boolean sort) {
        String[] ids = neo4jService.getTermIdsByLabel(label);
        List<IConcept> concepts = new ArrayList<>();

        for (int i = 0; i < ids.length; ++i) {
            concepts.add(getTerm(ids[i]));
        }

        if (sort) {
            Collections.sort(concepts, Comparator.comparing(IConcept::getPreferredName));
        }
        return concepts;
    }

    public static class ConceptCacheLoader extends AsyncCacheLoader<String, IConcept> {
        private IConceptDatabaseService neo4jService;
        private IConceptCreator conceptCreator;

        /**
         * @param log
         * @param neo4jService
         */
        public ConceptCacheLoader(
                Logger log, IConceptDatabaseService neo4jService, IConceptCreator conceptCreator) {
            super(log);
            this.neo4jService = neo4jService;
            this.conceptCreator = conceptCreator;
        }

        @Override
        SyncDbConcept getValueProxy(String id) {
            if (id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM)) {
                return (SyncDbConcept) conceptCreator.createDatabaseProxyConcept(id, AggregateTerm.class);
            }
            return (SyncDbConcept) conceptCreator.createDatabaseProxyConcept(id, SyncDbConcept.class);
        }

        @Override
        void loadAsyncBatch(ArrayList<String> batchList) {
            log.debug("Loading {} term(s): {}", batchList.size(), batchList);

            final Stream<ConceptDescription> conceptDescriptions = neo4jService.getConcepts(batchList);
            if (null == conceptDescriptions) {
                log.warn("No response was received from Neo4jService on concept loading request.");
                return;
            }

            Set<String> requestedIds = new HashSet<>(batchList);
            Set<String> retrievedIDs = new HashSet<>();

            for (Iterator<ConceptDescription> descIt = conceptDescriptions.iterator(); descIt.hasNext();) {
                final ConceptDescription description = descIt.next();

                String conceptId = description.getId();
                SyncDbConcept proxy = (SyncDbConcept) getPendingProxy(conceptId);

                if (null != proxy) {
                    conceptCreator.updateProxyConceptFromDescription(proxy, description);
                    retrievedIDs.add(conceptId);
                }
            }
            if (retrievedIDs.size() != requestedIds.size()) {
                // Remove the IDs we actually got returned; the rest has not
                // been found in the database.
                requestedIds.removeAll(retrievedIDs);
                throw new IllegalArgumentException(
                        "Concepts have been queried that do not exist in the database: "
                                + StringUtils.join(requestedIds, ", "));
            }
        }


//		@PostInjection
//		public void startupService(RegistryShutdownHub shutdownHub) {
//			System.out.println("TERM CACHE LOADER STARTUP");
//			super.startupService(shutdownHub);
//		}

    }

    public static class ConceptRelationsCacheLoader extends
            AsyncCacheLoader<ConceptRelationKey, IConceptRelation> {
        private Cache<String, IConcept> conceptCache;
        // Will be required when we need to load relationships apart from terms,
        // if ever.
        @SuppressWarnings("unused")
        private IConceptDatabaseService neo4jService;
        private IConceptService termService;

        public ConceptRelationsCacheLoader(Logger log, IConceptDatabaseService neo4jService,
                                           IConceptService termService) {
            super(log);
            this.neo4jService = neo4jService;
            this.termService = termService;
        }

        public void setConceptCache(LoadingCache<String, IConcept> termCache) {
            this.conceptCache = termCache;
        }

        /**
         * It could be the requested relation has actually already been loaded
         * with an incident term. Check this first. If nothing is found, proceed
         * as normal.
         */
        @Override
        public IConceptRelation load(ConceptRelationKey key) {
            if (null != conceptCache) {
                IHierarchicalConcept concept = (IHierarchicalConcept) conceptCache.getIfPresent(key.getStartId());
                if (null == concept)
                    concept = (IHierarchicalConcept) conceptCache.getIfPresent(key.getEndId());
                if (null != concept)
                    return concept.getRelationShipWithKey(key);
            }
            return super.load(key);
        }

        @Override
        IConceptRelation getValueProxy(ConceptRelationKey key) {
            return new ConceptRelation(key, termService);
        }

        @Override
        void loadAsyncBatch(ArrayList<ConceptRelationKey> batchList) {
            throw new RuntimeException(
                    "This method was not required when the Neo4j-related code was written. If it is needed now, you'll have to implement the appropriate getRelation() method for the Neo4jService, query the service here at create the appropriate FacetTermRelationship object.");

        }

    }

    public static class FacetRootCacheLoader extends CacheLoader<String, List<Concept>> {
        private IConceptDatabaseService neo4jService;
        private Logger log;
        private LoadingCache<String, IConcept> conceptCache;
        private IConceptCreator conceptCreator;

        public FacetRootCacheLoader(Logger log, IConceptDatabaseService neo4jService,
                                    IConceptCreator conceptCreator) {
            this.log = log;
            this.neo4jService = neo4jService;
            this.conceptCreator = conceptCreator;
        }

        public static Map<String, List<Concept>> createFacetRootsFromConceptDescriptions(
                Iterable<? extends String> facetIds, Multimap<String, ConceptDescription> facetRoots, Logger log,
                LoadingCache<String, IConcept> conceptCache, IConceptCreator conceptCreator) throws ConceptCreationException {
            StopWatch w = new StopWatch();
            w.start();

            Map<String, List<Concept>> rootsByFacetId = new HashMap<>();

            int rootsLoaded = 0;
            int newRootsLoaded = 0;
            Set<String> expectedFacetIds = new HashSet<>();
            for (String facetId : facetIds)
                expectedFacetIds.add(facetId);
            if (null == facetRoots || facetRoots.size() == 0) {
                log.warn("Query for facet roots did not return any results, either because there are no roots or because there are too many roots. Queried facet IDs: "
                        + StringUtils.join(facetIds, ", "));
                for (String facetId : facetIds) {
                    rootsByFacetId.put(facetId, Collections.emptyList());
                }
            } else {
                Set<String> facetIdKeys = facetRoots.keySet();
                for (String facetId : facetIdKeys) {
                    expectedFacetIds.remove(facetId);
                    Collection<ConceptDescription> jsonTerms = facetRoots.get(facetId);

                    for (ConceptDescription conceptDescription : jsonTerms) {
                        rootsLoaded++;

                        IConcept concept = conceptCache.getIfPresent(conceptDescription.getId());
                        if (null == concept) {
                            concept = conceptCreator.createConceptFromDescription(conceptDescription, DatabaseConcept.class);
                            conceptCache.put(concept.getId(), concept);
                            newRootsLoaded++;
                        }
                        List<Concept> facetRootList = rootsByFacetId.compute(facetId, (s, concepts) -> concepts != null ? concepts : new ArrayList<>());
                        facetRootList.add((Concept) concept);
                    }
                }
            }
            for (String missingFacetId : expectedFacetIds) {
                log.warn("Could not find any root concepts for facet {}.", missingFacetId);
                rootsByFacetId.put(missingFacetId, Collections.<Concept>emptyList());
            }

            w.stop();
            log.debug("Loaded facet roots for {} facets. Took {}ms ({}s)", new Object[]
                    {
                            rootsByFacetId.keySet().size(), w.getTime(), w.getTime() / 1000
                    });
            log.debug("Loaded {} roots, {} of which were not loaded before.", rootsLoaded, newRootsLoaded);

            return rootsByFacetId;
        }

        @Override
        public Map<String, List<Concept>> loadAll(Iterable<? extends String> facetIds)
                throws Exception {
            // return conceptService.loadFacetRoots(facetIds, null, 0);
            log.debug("Loading facet roots for facets with IDs: {}",
                    StringUtils.join(facetIds, ", "));

            // TODO magic number '200', is the max number of facet roots, also a
            // magic number in UIService, should be a
            // configurable setting
            Multimap<String, ConceptDescription> facetRoots = neo4jService.getFacetRootConcepts(facetIds, null, 200);
            return createFacetRootsFromConceptDescriptions(facetIds, facetRoots, log, conceptCache, conceptCreator);
        }

        @Override
        public List<Concept> load(String key) throws Exception {
            return loadAll(Lists.newArrayList(key)).get(key);
        }

        public void setConceptCache(LoadingCache<String, IConcept> conceptCache) {
            this.conceptCache = conceptCache;
        }
    }

    public static class ShortestRootPathInFacetCacheLoader extends CacheLoader<TermFacetKey, IPath> {

        private IConceptDatabaseService neo4jService;
        private IConceptService termService;
        private Logger log;

        public ShortestRootPathInFacetCacheLoader(Logger log, IConceptDatabaseService neo4jService,
                                                  IConceptService termService) {
            this.log = log;
            this.neo4jService = neo4jService;
            this.termService = termService;
        }

        @Override
        public IPath load(TermFacetKey key) {
            IPath rootPath = new Path();
            String[] pathFromRootJson = neo4jService.getShortestRootPathInFacet(key.getTermId(),
                    key.getFacetId());
            for (int i = 0; i < pathFromRootJson.length; i++) {
                String pathTermId = pathFromRootJson[i];
                IHierarchicalConcept pathTerm = (IHierarchicalConcept) termService.getTerm(pathTermId);
                if (null == pathTerm) {
                    log.warn(
                            "Shortest root path for term with ID {} in facet with ID {} contains an unknown term ID: {}. This is known to happen when terms HOLLOW terms lie on the path, i.e. when parents of terms imported into the database have not been importes themselves. Returning the empty path.",
                            new Object[]{key.getTermId(), key.getFacetId(), pathTermId});
                    return Path.EMPTY_PATH;
                }
                rootPath.appendNode((Concept) pathTerm);
            }
            return rootPath;
        }
    }

    public static class ShortestRootPathCacheLoader extends CacheLoader<String, IPath> {
        private IConceptDatabaseService neo4jService;
        private IConceptService termService;
        @SuppressWarnings("unused")
        private Logger log;

        public ShortestRootPathCacheLoader(Logger log, IConceptDatabaseService neo4jService,
                                           IConceptService termService) {
            this.log = log;
            this.neo4jService = neo4jService;
            this.termService = termService;
        }

        @Override
        public IPath load(String termId) {
            IPath rootPath = new Path();
            String[] pathFromRootJson = neo4jService.getShortestPathFromAnyRoot(termId);
            for (int i = 0; i < pathFromRootJson.length; i++) {
                String pathTermId = pathFromRootJson[i];
                IHierarchicalConcept pathTerm = (IHierarchicalConcept) termService.getTerm(pathTermId);
                rootPath.appendNode((Concept) pathTerm);
            }
            return rootPath;
        }
    }

    public static class AllRootPathsInFacetCacheLoader extends
            CacheLoader<Pair<String, String>, Collection<IPath>> {

        private IConceptDatabaseService neo4jService;
        private IConceptService termService;
        @SuppressWarnings("unused")
        private Logger log;

        public AllRootPathsInFacetCacheLoader(Logger log, IConceptDatabaseService neo4jService,
                                              IConceptService termService) {
            this.log = log;
            this.neo4jService = neo4jService;
            this.termService = termService;
        }

        @Override
        public Collection<IPath> load(Pair<String, String> termAndFacetIds) {
            String termId = termAndFacetIds.getLeft();
            String facetId = termAndFacetIds.getRight();
            String[][] pathsFromRoots = neo4jService.getPathsFromRootsInFacet(
                    Lists.newArrayList(termId), ConceptConstants.PROP_ID, true, facetId);
            List<IPath> ret = new ArrayList<>(pathsFromRoots.length);

            for (int i = 0; i < pathsFromRoots.length; i++) {
                IPath rootPath = new Path();
                String[] jsonPath = pathsFromRoots[i];
                for (int j = 0; j < jsonPath.length; j++) {
                    String pathConceptId = jsonPath[j];
                    IConcept pathTerm = termService.getTerm(pathConceptId);
                    rootPath.appendNode((Concept) pathTerm);
                }
                ret.add(rootPath);
            }
            return ret;
        }
    }
}