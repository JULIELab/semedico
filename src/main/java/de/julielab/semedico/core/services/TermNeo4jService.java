package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

import de.julielab.neo4j.plugins.TermManager;
import de.julielab.neo4j.plugins.constants.semedico.NodeConstants;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.neo4j.plugins.constants.semedico.TermConstants;
import de.julielab.neo4j.plugins.datarepresentation.PushTermsToSetCommand;
import de.julielab.semedico.core.FacetTermRelation;
import de.julielab.semedico.core.TermFacetKey;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.Path;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation.Type;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.facetterms.AggregateTerm;
import de.julielab.semedico.core.facetterms.Event;
import de.julielab.semedico.core.facetterms.SyncFacetTerm;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import de.julielab.semedico.core.services.interfaces.ICacheService.Region;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class TermNeo4jService extends BaseConceptService
{
	public static class TermCacheLoader extends AsyncCacheLoader<String, IConcept>
	{
		private ITermDatabaseService neo4jService;
		private IFacetTermFactory termFactory;

		/**
		 * 
		 * @param log
		 * @param neo4jService
		 * @param facetService
		 * @param termService
		 *            Used by created relationships to load their begin and end
		 *            nodes, if required.
		 */
		public TermCacheLoader(
				Logger log, ITermDatabaseService neo4jService,IFacetTermFactory termFactory)
		{
			super(log);
			this.neo4jService = neo4jService;
			this.termFactory = termFactory;
		}

		@Override
		SyncFacetTerm getValueProxy(String id)
		{
			if (id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM))
			{
				return (SyncFacetTerm) termFactory.createDatabaseProxyTerm(id, AggregateTerm.class);
			}
			return (SyncFacetTerm) termFactory.createDatabaseProxyTerm(id, SyncFacetTerm.class);
		}

		@Override
		void loadAsyncBatch(ArrayList<String> batchList)
		{
			log.debug("Loading {} term(s): {}", batchList.size(), batchList);

			JSONArray termRows = neo4jService.getTerms(batchList);
			if (null == termRows)
			{
				log.warn("No response were received from Neo4jService on concept loading request.");
				return;
			}
			
			Set<String> requestedIds = new HashSet<String>(batchList);
			Set<String> retrievedIDs = new HashSet<String>();
			
			for (int i = 0; i < termRows.length(); i++)
			{
				// get the ith row object; get the row's columns array; get the
				// only column holding the term object
				JSONObject jsonTerm
					= termRows.getJSONObject(i).getJSONArray(Neo4jService.ROW).getJSONObject(0);
				JSONArray termLabels
					= termRows.getJSONObject(i).getJSONArray(Neo4jService.ROW).getJSONArray(1);
				String termId
					= jsonTerm.getString(TermConstants.PROP_ID);
				SyncFacetTerm proxy = (SyncFacetTerm) getPendingProxy(termId);
				
				if (null != proxy)
				{
					convertTermJSONObject(proxy, jsonTerm, termLabels);
					retrievedIDs.add(termId);
				}
			}
			if (retrievedIDs.size() != requestedIds.size())
			{
				// Remove the IDs we actually got returned; the rest has not
				// been found in the database.
				requestedIds.removeAll(retrievedIDs);
				throw new IllegalArgumentException(
						"Terms have been queried that do not exist in the database: "
								+ StringUtils.join(requestedIds, ", "));
			}
		}

		private void convertTermJSONObject(
				IFacetTerm proxy, JSONObject termRow,JSONArray termLabels)
		{
			String termId = proxy.getId();
			termFactory.updateProxyTermFromJson(proxy, termRow.toCompactString(), termLabels);
			if (!proxy.getId().equals(termId))
				throw new IllegalArgumentException("Proxy ID and retrieved ID do not match.");
			proxy.setNonDatabaseTerm(false);
		}

	}

	/**
	 * @deprecated There are currently no plans to explicitly model events as
	 *             concept terms. Events may be searched for and they have a
	 *             representation in the index, but they are not represented at
	 *             any time as a Concept. It is questionable whether this was a
	 *             good idea to begin with since events are complex entities and
	 *             need more care than simple concepts
	 * @author faessler
	 * 
	 */
	@Deprecated
	public static class EventCacheLoader extends CacheLoader<String, Event>
	{
		private ITermService termService;
		private IFacetService facetService;
		private Logger log;

		public EventCacheLoader(Logger log, ITermService termService, IFacetService facetService)
		{
			this.log = log;
			this.termService = termService;
			this.facetService = facetService;
		}

		@Override
		public Event load(String key) throws Exception
		{
			log.trace("Requested event: {}", key);
			// Create the event object itself.
			Pattern p = Pattern.compile("jrex:(" + NodeIDPrefixConstants.TERM + "[0-9]+)-("
					+ NodeIDPrefixConstants.TERM + "[0-9]+)-(" + "[a-z0-9]+)" + "(-[a-z]+)?");
			Matcher m = p.matcher(key);
			if (!m.matches())
				throw new IllegalArgumentException("No valid event term: " + key);

			String eventTermId = m.group(1);
			List<String> argumentIds = new ArrayList<>();
			argumentIds.add(m.group(2));
			if (!m.group(3).equals("none"))
				argumentIds.add(m.group(3));
			// likelihood is currently not included in the facet event field, so
			// this will most likely be null
			String likelihood = m.group(4);
			log.trace("Identified event with likelihood {}, arg1 {}, event type {} and arg2 {}",
					new Object[] { likelihood, argumentIds.get(0), eventTermId,
							argumentIds.size() > 1 ? argumentIds.get(1) : null });

			List<Concept> arguments = new ArrayList<>();
			for (String argumentId : argumentIds)
			{
				Concept term = (Concept) termService.getTerm(argumentId);
				if (null != term)
				{
					log.trace("Adding conept {} as argument to event term.", term);
					arguments.add(term);
				}
				else
					log.trace(
							"Argument ID {} was not found as a concept in the database, omitting event argument.",
							argumentId);
			}
			IFacetTerm eventTerm = (IFacetTerm) termService.getTerm(eventTermId);
			Event event = new Event(key, eventTerm, arguments, likelihood);
			event.setPreferredName(eventTerm.getPreferredName()
					+ ": ["
					+ arguments.get(0).getPreferredName()
					+ "]"
					+ (arguments.size() > 1 ? ", [" + arguments.get(1).getPreferredName() + "]"
							: ""));
			Facet inducedFacet = null;
			for (Facet eventFacet : facetService.getFacetsByLabel(FacetLabels.General.EVENTS))
			{
				if (eventFacet.getInducingTermId().equals(eventTerm.getId()))
					inducedFacet = eventFacet;
			}
			event.setFacets(Lists.newArrayList(inducedFacet));

			// Create a relationship to its event-term (i.e. a GO term or the
			// respective class from GRO that describe
			// the event)
			// and connect event and event-term.
			// This relationship is only valid for the special Event-Facet and
			// will not change the way GO or GRO are
			// displayed.
			// TermRelationKey relationKey = new TermRelationKey(
			// eventTerm.getId(), event.getId(),
			// IFacetTermRelation.Type.IS_BROADER_THAN + "_"
			// + facetService.getEventFacet().getId());
			// FacetTermRelation relation = new FacetTermRelation(relationKey,
			// termService);
			// eventTerm.addOutgoingRelationship(relation);
			// event.addIncomingRelationship(relation);

			// Also set the general relation type that is independent from
			// facets
			// TermRelationKey relationKeyGeneral = new TermRelationKey(
			// eventTerm.getId(), event.getId(),
			// IFacetTermRelation.Type.IS_BROADER_THAN.name());
			// FacetTermRelation generalRelation = new FacetTermRelation(
			// relationKeyGeneral, termService);
			// eventTerm.addOutgoingRelationship(generalRelation);
			// event.addIncomingRelationship(generalRelation);

			return event;
		}

	}

	public static class FacetTermRelationsCacheLoader extends
			AsyncCacheLoader<TermRelationKey, IFacetTermRelation>
	{
		private Cache<String, IConcept> termCache;
		// Will be required when we need to load relationships apart from terms,
		// if ever.
		@SuppressWarnings("unused")
		private ITermDatabaseService neo4jService;
		private ITermService termService;

		public FacetTermRelationsCacheLoader(Logger log, ITermDatabaseService neo4jService,
				ITermService termService)
		{
			super(log);
			this.neo4jService = neo4jService;
			this.termService = termService;
		}

		public void setTermCache(LoadingCache<String, IConcept> termCache)
		{
			this.termCache = termCache;
		}

		/**
		 * It could be the requested relation has actually already been loaded
		 * with an incident term. Check this first. If nothing is found, proceed
		 * as normal.
		 */
		@Override
		public IFacetTermRelation load(TermRelationKey key)
		{
			if (null != termCache)
			{
				IFacetTerm term = (IFacetTerm) termCache.getIfPresent(key.getStartId());
				if (null == term)
					term = (IFacetTerm) termCache.getIfPresent(key.getEndId());
				if (null != term)
					return term.getRelationShipWithKey(key);
			}
			return super.load(key);
		}

		@Override
		IFacetTermRelation getValueProxy(TermRelationKey key)
		{
			return new FacetTermRelation(key, termService);
		}

		@Override
		void loadAsyncBatch(ArrayList<TermRelationKey> batchList)
		{
			throw new RuntimeException(
					"This method was not required when the Neo4j-related code was written. If it is needed now, you'll have to implement the appropriate getRelation() method for the Neo4jService, query the service here at create the appropriate FacetTermRelationship object.");

		}

	}

	public static class FacetRootCacheLoader extends CacheLoader<String, List<Concept>>
	{
		private ITermDatabaseService neo4jService;
		private Logger log;
		private LoadingCache<String, IConcept> termCache;
		private IFacetTermFactory termFactory;

		public FacetRootCacheLoader(Logger log, ITermDatabaseService neo4jService,
				IFacetTermFactory termFactory)
		{
			this.log = log;
			this.neo4jService = neo4jService;
			this.termFactory = termFactory;
		}

		@Override
		public Map<String, List<Concept>> loadAll(Iterable<? extends String> facetIds)
				throws Exception
		{
			// return termService.loadFacetRoots(facetIds, null, 0);
			log.debug("Loading facet roots for facets with IDs: {}",
					StringUtils.join(facetIds, ", "));

			// TODO magic number '200', is the max number of facet roots, also a
			// magic number in UIService, should be a
			// configurable setting
			JSONObject facetRoots = neo4jService.getFacetRootTerms(facetIds, null, 200);
			return createFacetRootsFromJson(facetIds, facetRoots, log, termCache, termFactory);
			// int rootsLoaded = 0;
			// int newRootsLoaded = 0;
			// Set<String> expectedFacetIds = new HashSet<>();
			// for (String facetId : facetIds)
			// expectedFacetIds.add(facetId);
			// if (facetRoots.length() == 0) {
			// log.warn("Query for facet roots did not return any results, either because there are no roots or because there are too many roots. Queried facet IDs: "
			// + StringUtils
			// .join(facetIds, ", "));
			// for (String facetId : facetIds) {
			// rootsByFacetId.put(facetId, Collections.<Concept> emptyList());
			// }
			// } else {
			// JSONArray facetIdKeys = facetRoots.names();
			// for (int i = 0; i < facetIdKeys.length(); i++) {
			// String facetId = facetIdKeys.getString(i);
			//
			// expectedFacetIds.remove(facetId);
			// JSONArray jsonTerms = facetRoots.getJSONArray(facetId);
			// for (int j = 0; j < jsonTerms.length(); j++) {
			// JSONObject jsonTerm = jsonTerms.getJSONObject(j);
			// JSONArray labels =
			// jsonTerm.getJSONArray(NodeConstants.KEY_LABELS);
			// String termId = jsonTerm.getString(TermConstants.PROP_ID);
			// rootsLoaded++;
			//
			// SyncFacetTerm term = termCache.getIfPresent(termId);
			// if (null == term) {
			// term =
			// (SyncFacetTerm)
			// termFactory.createFacetTermFromJson(jsonTerm.toCompactString(),
			// labels, SyncFacetTerm.class);
			// termCache.put(termId, term);
			// newRootsLoaded++;
			// }
			//
			// List<Concept> facetRootList = rootsByFacetId.get(facetId);
			// if (null == facetRootList) {
			// facetRootList = new ArrayList<>();
			// rootsByFacetId.put(facetId, facetRootList);
			// }
			// facetRootList.add(term);
			// }
			// }
			// }
			// for (String missingFacetId : expectedFacetIds) {
			// log.warn("Could not find any root terms for facet {}.",
			// missingFacetId);
			// rootsByFacetId.put(missingFacetId, Collections.<Concept>
			// emptyList());
			// }
			//
			// w.stop();
			// log.debug("Loaded facet roots for {} facets. Took {}ms ({}s)",
			// new Object[] {
			// rootsByFacetId.keySet().size(), w.getTime(), w.getTime() / 1000
			// });
			// log.debug("Loaded {} roots, {} of which were not loaded before.",
			// rootsLoaded, newRootsLoaded);
			//
			// return rootsByFacetId;
		}

		public static Map<String, List<Concept>> createFacetRootsFromJson(
				Iterable<? extends String> facetIds, JSONObject facetRoots, Logger log,
				LoadingCache<String, IConcept> termCache, IFacetTermFactory termFactory)
		{
			StopWatch w = new StopWatch();
			w.start();

			Map<String, List<Concept>> rootsByFacetId = new HashMap<>();

			int rootsLoaded = 0;
			int newRootsLoaded = 0;
			Set<String> expectedFacetIds = new HashSet<>();
			for (String facetId : facetIds)
				expectedFacetIds.add(facetId);
			if (null == facetRoots || facetRoots.length() == 0)
			{
				log.warn("Query for facet roots did not return any results, either because there are no roots or because there are too many roots. Queried facet IDs: "
						+ StringUtils.join(facetIds, ", "));
				for (String facetId : facetIds)
				{
					rootsByFacetId.put(facetId, Collections.<Concept> emptyList());
				}
			}
			else
			{
				JSONArray facetIdKeys = facetRoots.names();
				for (int i = 0; i < facetIdKeys.length(); i++)
				{
					String facetId = facetIdKeys.getString(i);

					// JSONArray termArray =
					// facetRoots.getJSONObject(i).getJSONArray(Neo4jService.ROW);
					// String facetId = termArray.getString(0);
					expectedFacetIds.remove(facetId);
					JSONArray jsonTerms = facetRoots.getJSONArray(facetId);
					// JSONArray jsonTerms = termArray.getJSONArray(1);
					// JSONArray termLabels = termArray.getJSONArray(2);

					for (int j = 0; j < jsonTerms.length(); j++)
					{
						JSONObject jsonTerm = jsonTerms.getJSONObject(j);
						// JSONArray labels = termLabels.getJSONArray(j);
						JSONArray labels = jsonTerm.getJSONArray(NodeConstants.KEY_LABELS);
						String termId = jsonTerm.getString(TermConstants.PROP_ID);
						rootsLoaded++;

						IConcept term = termCache.getIfPresent(termId);
						if (null == term)
						{
							term = termFactory.createFacetTermFromJson(jsonTerm.toCompactString(),
									labels, SyncFacetTerm.class);
							termCache.put(termId, term);
							newRootsLoaded++;
						}

						List<Concept> facetRootList = rootsByFacetId.get(facetId);
						if (null == facetRootList)
						{
							facetRootList = new ArrayList<>();
							rootsByFacetId.put(facetId, facetRootList);
						}
						facetRootList.add((Concept) term);
					}
				}
			}
			for (String missingFacetId : expectedFacetIds)
			{
				log.warn("Could not find any root terms for facet {}.", missingFacetId);
				rootsByFacetId.put(missingFacetId, Collections.<Concept> emptyList());
			}

			w.stop();
			log.debug("Loaded facet roots for {} facets. Took {}ms ({}s)", new Object[]
				{
					rootsByFacetId.keySet().size(), w.getTime(), w.getTime() / 1000
				});
			log.debug("Loaded {} roots, {} of which were not loaded before.", rootsLoaded,newRootsLoaded);

			return rootsByFacetId;
		}

		@Override
		public List<Concept> load(String key) throws Exception
		{
			return loadAll(Lists.newArrayList(key)).get(key);
		}

		public void setTermCache(LoadingCache<String, IConcept> termCache)
		{
			this.termCache = termCache;
		}
	}

	public static class ShortestRootPathInFacetCacheLoader extends CacheLoader<TermFacetKey, IPath>
	{

		private ITermDatabaseService neo4jService;
		private ITermService termService;
		private Logger log;

		public ShortestRootPathInFacetCacheLoader(Logger log, ITermDatabaseService neo4jService,
				ITermService termService)
		{
			this.log = log;
			this.neo4jService = neo4jService;
			this.termService = termService;
		}

		@Override
		public IPath load(TermFacetKey key)
		{
			IPath rootPath = new Path();
			JSONArray pathFromRootJson = neo4jService.getShortestRootPathInFacet(key.getTermId(),
					key.getFacetId());
			for (int i = 0; i < pathFromRootJson.length(); i++)
			{
				String pathTermId = pathFromRootJson.getString(i);
				IFacetTerm pathTerm = (IFacetTerm) termService.getTerm(pathTermId);
				if (null == pathTerm)
				{
					log.warn(
							"Shortest root path for term with ID {} in facet with ID {} contains an unknown term ID: {}. This is known to happen when terms HOLLOW terms lie on the path, i.e. when parents of terms imported into the database have not been importes themselves. Returning the empty path.",
							new Object[] { key.getTermId(), key.getFacetId(), pathTermId });
					return Path.EMPTY_PATH;
				}
				rootPath.appendNode((Concept) pathTerm);
			}
			return rootPath;
		}
	}

	public static class ShortestRootPathCacheLoader extends CacheLoader<String, IPath>
	{
		private ITermDatabaseService neo4jService;
		private ITermService termService;
		@SuppressWarnings("unused")
		private Logger log;

		public ShortestRootPathCacheLoader(Logger log, ITermDatabaseService neo4jService,
				ITermService termService)
		{
			this.log = log;
			this.neo4jService = neo4jService;
			this.termService = termService;
		}

		@Override
		public IPath load(String termId)
		{
			IPath rootPath = new Path();
			JSONArray pathFromRootJson = neo4jService.getShortestPathFromAnyRoot(termId);
			for (int i = 0; i < pathFromRootJson.length(); i++)
			{
				String pathTermId = pathFromRootJson.getString(i);
				IFacetTerm pathTerm = (IFacetTerm) termService.getTerm(pathTermId);
				rootPath.appendNode((Concept) pathTerm);
			}
			return rootPath;
		}
	}

	public static class AllRootPathsInFacetCacheLoader extends
			CacheLoader<Pair<String, String>, Collection<IPath>>
	{

		private ITermDatabaseService neo4jService;
		private ITermService termService;
		@SuppressWarnings("unused")
		private Logger log;

		public AllRootPathsInFacetCacheLoader(Logger log, ITermDatabaseService neo4jService,
				ITermService termService)
		{
			this.log = log;
			this.neo4jService = neo4jService;
			this.termService = termService;
		}

		@Override
		public Collection<IPath> load(Pair<String, String> termAndFacetIds)
		{
			String termId = termAndFacetIds.getLeft();
			String facetId = termAndFacetIds.getRight();
			JSONArray pathsFromRootsJson = neo4jService.getPathsFromRootsInFacet(
					Lists.newArrayList(termId), TermConstants.PROP_ID, true, facetId);
			List<IPath> ret = new ArrayList<>(pathsFromRootsJson.length());
			
			for (int i = 0; i < pathsFromRootsJson.length(); i++)
			{
				IPath rootPath = new Path();
				JSONArray jsonPath = pathsFromRootsJson.getJSONArray(i);
				for (int j = 0; j < jsonPath.length(); j++) {
					String pathTermId = jsonPath.getString(j);
					IConcept pathTerm = termService.getTerm(pathTermId);
					rootPath.appendNode((Concept) pathTerm);
				}
				ret.add(rootPath);
			}
			return ret;
		}
	}

	private ITermDatabaseService neo4jService;

	private LoadingCache<String, List<Concept>> facetRootCache;
	private LoadingCache<TermRelationKey, IFacetTermRelation> relationshipCache;
	private LoadingCache<TermFacetKey, IPath> shortestRootPathInFacetCache;
	private LoadingCache<String, IPath> shortestRootPathCache;
	private LoadingCache<Pair<String, String>, Collection<IPath>> allRootPathsInFacetCache;

	public TermNeo4jService(Logger logger, ICacheService cacheService,
			ITermDatabaseService neo4jService, IFacetTermFactory termFactory,
			@InjectService("StringTermService") IStringTermService stringTermService)
			throws Exception {
		super(logger, termFactory, stringTermService, cacheService
				.<String, IConcept> getCache(Region.TERM));
		this.neo4jService = neo4jService;
		this.facetRootCache = cacheService.getCache(Region.FACET_ROOTS);
		this.shortestRootPathCache = cacheService.getCache(Region.ROOT_PATHS);
		this.allRootPathsInFacetCache = cacheService.getCache(Region.ROOT_PATHS_IN_FACET);
		this.shortestRootPathInFacetCache = cacheService
				.getCache(Region.SHORTEST_ROOT_PATH_IN_FACET);
		this.relationshipCache = cacheService.getCache(Region.RELATIONSHIP);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.ITermService#getFacetRoots(de.julielab
	 * .semedico.core.Facet)
	 */
	@Override
	public List<Concept> getFacetRoots(Facet facet)
	{
		return facetRootCache.getUnchecked(facet.getId());
	}

	@Override
	public Map<String, List<Concept>> assureFacetRootsLoaded(Map<Facet, List<String>> requestRootIds)
	{
		Map<String, List<String>> missingPotentialRoots = new HashMap<>();
		for (Entry<Facet, List<String>> facetRootRequest : requestRootIds.entrySet())
		{
			Facet facet = facetRootRequest.getKey();
			if (facet.allDBRootsLoaded())
			{
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
			if (null != loadedRootList && !loadedRootList.isEmpty())
			{
				for (Concept loadedRoot : loadedRootList)
					loadedRootIds.add(loadedRoot.getId());
			}
			for (String potentialRootId : requestedPotentialRootIds)
			{
				if (!loadedRootIds.contains(potentialRootId))
				{
					missingPotentialRootIdList.add(potentialRootId);
				}
			}
			if (!missingPotentialRootIdList.isEmpty())
			{
				missingPotentialRoots.put(facetId, missingPotentialRootIdList);
				log.debug("Loading {} missing potential roots for facet {} (ID: {})", new Object[]
					{
						missingPotentialRootIdList.size(), facet.getName(), facet.getId() });
			}
			else
			{
				log.debug(
						"Skipping {} because all terms in the input list have already been loaded or are not root terms.",
						facet.getName());
			}
		}

		JSONObject loadedRootTermsJson = neo4jService.getFacetRootTerms(
				missingPotentialRoots.keySet(), missingPotentialRoots, 0);
		Map<String, List<Concept>> loadedRootTermsMap = FacetRootCacheLoader
				.createFacetRootsFromJson(missingPotentialRoots.keySet(), loadedRootTermsJson, log,
						termCache, termFactory);
		for (String facetId : missingPotentialRoots.keySet())
		{
			List<Concept> newlyLoadedRoots = loadedRootTermsMap.get(facetId);
			if (null != newlyLoadedRoots && newlyLoadedRoots.size() > 0)
			{
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
	public int getNumLoadedRoots(String facetId)
	{
		List<Concept> roots = facetRootCache.getIfPresent(facetId);
		if (null == roots)
			return 0;
		return roots.size();
	}


	@Override
	public Iterator<IConcept> getTerms()
	{
		return getTerms(-1);
	}

	@Override
	public Iterator<IConcept> getTerms(int limit)
	{
		int breakPoint = 1000000;
		int numTerms = neo4jService.getNumTerms();
		if (numTerms <= breakPoint || (limit > 0 && limit < breakPoint))
		{
			final JSONArray termsArray = neo4jService.getTerms(limit > 0 ? limit : breakPoint);
			// final ITermService termService = this;
			return new Iterator<IConcept>()
			{
				private int i = 0;

				@Override
				public boolean hasNext()
				{
					return i < termsArray.length();
				}

				@Override
				public IFacetTerm next()
				{
					IFacetTerm term = null;
					if (hasNext()) {
						JSONArray termRow = termsArray.getJSONObject(i).getJSONArray(
								Neo4jService.ROW);
						JSONObject termJsonOBject = termRow.getJSONObject(0);
						JSONArray termLabels = termRow.getJSONArray(1);
						// String termId = termArray.getString(0);
						// term = new FacetTerm(termId, termService);
						term = termFactory.createFacetTermFromJson(
								termJsonOBject.toCompactString(), termLabels);
						// FacetTermCacheLoader.convertTermJSONObject(term,
						// termJsonOBject, facetService);

						i++;
					}
					return term;
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}
		else
		{
			// We have too many terms to return them all at once. Thus we fall
			// back to the queue approach.
			String setName = "TEMP_QUEUE_" + System.currentTimeMillis();
			pushAllTermsIntoQueue(setName);
			return getTermsInQueue(setName);
		}
	}

	@Override
	public IPath getShortestPathFromAnyRoot(IConcept node)
	{
		return shortestRootPathCache.getUnchecked(node.getId()).copyPath();
	}

	@Override
	public boolean isAncestorOf(IConcept candidate, IConcept term)
	{
		return neo4jService.termPathExists(candidate.getId(), term.getId(), Type.IS_BROADER_THAN);
	}

	// @Override
	// public Iterator<IFacetTerm> getTermsWithLabel(final General label) {
	// return new Iterator<IFacetTerm>() {
	// private int bufferSize = 10000;
	// private int currentTermNumber = 0;
	// private Stack<IFacetTerm> buffer = new Stack<>();
	//
	// @Override
	// public boolean hasNext() {
	// if (buffer.size() == 0)
	// loadTermBatch();
	// return buffer.size() > 0;
	// }
	//
	// @Override
	// public IFacetTerm next() {
	// if (hasNext())
	// return buffer.pop();
	// return null;
	// }
	//
	// @Override
	// public void remove() {
	// throw new UnsupportedOperationException();
	// }
	//
	// private IFacetTerm convertUnconnectedTerm(JSONArray termRow)
	// throws JSONException {
	// int pos = 0;
	// String termId = termRow.getString(pos++);
	// String preferredName = termRow.getString(pos++);
	// List<String> synonyms = JSON.jsonArray2List(JSON.getJSONArray(
	// termRow, pos++));
	// List<String> writingVariants = JSON.jsonArray2List(JSON
	// .getJSONArray(termRow, pos++));
	// List<String> facets = JSON.jsonArray2List(termRow
	// .getJSONArray(pos++));
	// String description = JSON.getString(termRow, pos++);
	//
	// FacetTerm term = new FacetTerm(termId);
	//
	// term.setPreferredName(preferredName);
	// term.setSynonyms(synonyms);
	// term.setWritingVariants(writingVariants);
	// term.setFacets(facetService.getFacetsById(facets));
	// term.setDescription(description);
	//
	// return term;
	// }
	//
	// private void loadTermBatch() {
	// try {
	// JSONArray jsonTerms = neo4jService
	// .getUnconnectedTermsWithLabel(label,
	// currentTermNumber, bufferSize);
	// currentTermNumber += bufferSize;
	// for (int i = 0; i < jsonTerms.length(); i++) {
	// JSONArray termRow = jsonTerms.getJSONArray(i);
	// IFacetTerm term = convertUnconnectedTerm(termRow);
	// buffer.push(term);
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// }
	// };
	// }

	public long pushTermsIntoQueue(String queueName, int amount)
	{
		PushTermsToSetCommand cmd = new PushTermsToSetCommand(queueName);
		return neo4jService.pushTermsToSet(cmd, amount);
	}

	public long pushAllTermsIntoQueue(String queueName)
	{
		return pushTermsIntoQueue(queueName, -1);
	}

	@Override
	public Iterator<IConcept> getTermsInSuggestionQueue()
	{
		return getTermsInQueue(TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.name());
	}

	@Override
	public Iterator<IConcept> getTermsInQueue(final String setLabel)
	{
		return new Iterator<IConcept>()
		{
			private int bufferSize = 10000;
			private Stack<IFacetTerm> buffer = new Stack<>();

			@Override
			public boolean hasNext()
			{
				if (buffer.size() == 0)
					loadTermBatch();
				return buffer.size() > 0;
			}

			@Override
			public IFacetTerm next()
			{
				if (hasNext())
					return buffer.pop();
				return null;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			private void loadTermBatch()
			{
				JSONArray jsonTerms = neo4jService.popTermsFromSet(setLabel, bufferSize);
				for (int i = 0; i < jsonTerms.length(); i++)
				{
					JSONObject termObject = jsonTerms.getJSONObject(i);
					JSONArray termLabels = termObject.getJSONArray(TermConstants.KEY_LABELS);
					IFacetTerm term = termFactory.createFacetTermFromJson(
							termObject.toCompactString(), termLabels);// convertTermJSONObject(termObject);
					buffer.push(term);
				}
			}

		};
	}

	@Override
	public Iterator<IConcept> getTermsInQueryDictionaryQueue()
	{
		return getTermsInQueue(TermLabels.GeneralLabel.PENDING_FOR_QUERY_DICTIONARY.name());
	}

	@Override
	public long pushAllTermsIntoSuggestionQueue()
	{
		PushTermsToSetCommand cmd = new PushTermsToSetCommand(
				TermLabels.GeneralLabel.PENDING_FOR_SUGGESTIONS.name());
		cmd.eligibleTermDefinition = cmd.new TermSelectionDefinition();
		// For suggestions, we only use the mapping aggregated terms (non-mapped
		// terms are their own aggregate).
		cmd.eligibleTermDefinition.termLabel = TermLabels.GeneralLabel.MAPPING_AGGREGATE.name();
		cmd.eligibleTermDefinition.facetLabel = FacetLabels.General.USE_FOR_SUGGESTIONS.name();
		cmd.excludeTermDefinition = cmd.new TermSelectionDefinition();
		cmd.excludeTermDefinition.termLabel = TermLabels.GeneralLabel.NO_SUGGESTIONS
				.name();
		return neo4jService.pushTermsToSet(cmd, -1);
	}

	@Override
	public long pushAllTermsIntoDictionaryQueue()
	{
		PushTermsToSetCommand cmd = new PushTermsToSetCommand(
				TermLabels.GeneralLabel.PENDING_FOR_QUERY_DICTIONARY.name());
		cmd.eligibleTermDefinition = cmd.new TermSelectionDefinition();
		// For query term recognition, we only use the mapping aggregated terms
		// (non-mapped terms are their own
		// aggregate).
		cmd.eligibleTermDefinition.termLabel = TermLabels.GeneralLabel.MAPPING_AGGREGATE.name();
		cmd.eligibleTermDefinition.facetLabel = FacetLabels.General.USE_FOR_QUERY_DICTIONARY.name();
		cmd.excludeTermDefinition = cmd.new TermSelectionDefinition();
		cmd.excludeTermDefinition.termLabel = TermLabels.GeneralLabel.DO_NOT_USE_FOR_QUERY_DICTIONARY
				.name();
		return neo4jService.pushTermsToSet(cmd, -1);
	}

	@Override
	public int getNumTerms()
	{
		return neo4jService.getNumTerms();
	}

	@Override
	public void loadChildrenOfTerm(Concept facetTerm, String termLabel)
	{
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

		if (!childrenMap.has(facetTerm.getId()))
		{
			error = true;
		}
		else
		{
			termChildrenMap = childrenMap.getJSONObject(facetTerm.getId());
			jsonTerms = termChildrenMap.getJSONArray(TermManager.RET_KEY_CHILDREN);
			relationshipMap = termChildrenMap.getJSONObject(TermManager.RET_KEY_RELTYPES);
		}

		if (null == jsonTerms)
		{
			error = true;
		}

		if (error)
		{
			log.error("No children (outgoing relationships) data of the term with ID \""
					+ facetTerm.getId()
					+ "\" were returned (not even that there are no children!). This most certainly means that the respective term ID was not found in the term database with the label \""
					+ TermLabels.GeneralLabel.MAPPING_AGGREGATE
					+ "\". Make sure that mapping aggregates have been built (even if there are no mappings!).");
			return;
		}

		try
		{
			for (int i = 0; i < jsonTerms.length(); i++)
			{
				JSONObject jsonTerm = jsonTerms.getJSONObject(i);
				String termId = jsonTerm.getString(TermConstants.PROP_ID);
				IConcept term = termCache.getIfPresent(termId);
				if (null == term)
				{
					JSONArray termLabels = jsonTerm.getJSONArray(TermConstants.KEY_LABELS);
					term = (Concept) termFactory.createFacetTermFromJson(
							jsonTerm.toCompactString(), termLabels);
					// term = gson.fromJson(jsonTerm.toCompactString(),
					// SyncFacetTerm.class);
					termCache.put(termId, term);
				}
			}

			// Map<IFacetTermRelation.Type, List<IFacetTermRelation>>
			// childRelationships = new HashMap<>();
			for (String termId : relationshipMap.keys())
			{
				JSONArray reltypes = relationshipMap.getJSONArray(termId);
				for (int i = 0; i < reltypes.length(); i++)
				{
					++numRels;
					String reltype = reltypes.getString(i);
					TermRelationKey termRelationKey = new TermRelationKey(facetTerm.getId(),
							termId, reltype);
					IFacetTermRelation relationship = relationshipCache
							.getIfPresent(termRelationKey);
					if (null == relationship)
					{
						relationship = new FacetTermRelation(termRelationKey, this);
						relationshipCache.put(termRelationKey, relationship);
					}
					facetTerm.addOutgoingRelationship(relationship);
					// List<IFacetTermRelation> relationshipList =
					// childRelationships.get(relationship.getType());
					// if (null == relationshipList) {
					// relationshipList = new ArrayList<>();
					// childRelationships.put(relationship.getType(),
					// relationshipList);
					// }
					// relationshipList.add(relationship);
				}
			}
			// facetTerm.setOutgoingRelationships(childRelationships);
		} catch (JsonSyntaxException e)
		{
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
	public void loadChildrenOfTerm(Concept facetTerm)
	{
		// the restriction to TERM here means that aggregates won't get their
		// elements with this method! So for those we
		// have to use the overloaded method and specify the MAPPING_AGGREGATE
		// label.
		loadChildrenOfTerm(facetTerm, TermLabels.GeneralLabel.TERM.name());
	}

	@Override
	public Collection<IPath> getAllPathsFromRootsInFacet(IConcept term, Facet facet)
	{
		String termId = term.getId();
		String facetId = null != facet ? facet.getId() : "";
		return allRootPathsInFacetCache.getUnchecked(new ImmutablePair<String, String>(termId,
				facetId));
	}

	@Override
	public IPath getShortestRootPathInFacet(IConcept node, Facet facet)
	{
		TermFacetKey key = new TermFacetKey(node.getId(), facet.getId());
		return shortestRootPathInFacetCache.getUnchecked(key).copyPath();
	}

	@Override
	public List<IConcept> getTermsByLabel(String label, boolean sort)
	{
		JSONArray ids = neo4jService.getTermIdsByLabel(label);
		List<IConcept> terms = new ArrayList<>();
		
		for (int i = 0; i < ids.length(); ++i)
		{
			terms.add(getTerm(ids.getString(i)));
		}
		
		if (sort)
		{
			Comparator<IConcept> conceptByNameComparator = new Comparator<IConcept>()
			{
				@Override
				public int compare(IConcept o1, IConcept o2)
				{
					return o1.getPreferredName().compareTo(o2.getPreferredName());
				}
			};
			Collections.sort(terms, conceptByNameComparator);
		}
		return terms;
	}
}