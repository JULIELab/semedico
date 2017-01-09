package de.julielab.semedico.core.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import de.julielab.elastic.query.util.TermCountCursor;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.neo4j.plugins.constants.semedico.TermConstants;
import de.julielab.neo4j.plugins.datarepresentation.ImportTerm;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.util.PairStream;

public class MiniTermService extends BaseConceptService {

	private int termNumber = 0;
	
	public MiniTermService(Logger log, IFacetTermFactory termFactory,
			@InjectService("StringTermService") IStringTermService stringTermService) {
		super(log, termFactory, stringTermService);
		setTermCache(createMiniTermCache());
	}
	
	private LoadingCache<String, IConcept> createMiniTermCache() {
		Gson gson = new Gson();
		JSONArray termLabels = new JSONArray(new Object[] {TermLabels.GeneralLabel.TERM.toString()});
		final List<IConcept> concepts = new ArrayList<>();
		
		concepts.add(createMiniTerm(gson, termLabels, "Alzheimer", 0, Collections.<String>emptyList()));
		concepts.add(createMiniTerm(gson, termLabels, "Parkinson's Disease", 0, Collections.<String>emptyList()));
		concepts.add(createMiniTerm(gson, termLabels, "Influenza", 0, Collections.<String>emptyList()));
		
		concepts.add(createMiniTerm(gson, termLabels, "Beta Blocker", 1, Collections.<String>emptyList()));
		
		concepts.add(createMiniTerm(gson, termLabels, "Allophycocyanin", 1, Arrays.asList("APC")));
		concepts.add(createMiniTerm(gson, termLabels, "Anaphase-promoting complex", 1, Arrays.asList("APC")));
		
		
		
		LoadingCache<String, IConcept> cache = CacheBuilder.newBuilder().build(new CacheLoader<String, IConcept>() {

			@Override
			public IConcept load(String key) throws Exception {
				throw new NotImplementedException();
			}

		
			});
		for (IConcept concept : concepts)
			cache.put(concept.getId(), concept);
		
		return cache;
		
//		ft.addFacet(facetService.getFacetById(NodeIDPrefixConstants.FACET + 0));
//		ft = new FacetTerm(NodeIDPrefixConstants.TERM + 1, "Beta-blocker");
//		ft.addFacet(facetService.getFacetById(NodeIDPrefixConstants.FACET + 1));
		
	}

	private IConcept createMiniTerm(Gson gson, JSONArray termLabels, String prefName,
			int facetNumber, List<String> synonyms) {
		ImportTerm importTerm = new ImportTerm(prefName, null);
		importTerm.synonyms = synonyms;
		String termJson = gson.toJson(importTerm);
		JSONObject termObject = new JSONObject(termJson);
		termObject.put("id", NodeIDPrefixConstants.TERM + termNumber++);
		// the "Diseases" facet as defined by the mini facet service
		termObject.put(TermConstants.PROP_FACETS, new JSONArray(new Object[] {NodeIDPrefixConstants.FACET + facetNumber}));
		IConcept ft = termFactory.createFacetTermFromJson(termObject.toCompactString(), termLabels);
		return ft;
	}
	
	@Override
	public String getStringTermId(String stringTerm, Facet facet) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String checkStringTermId(String stringTerm, Facet facet) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pair<String, String> getOriginalStringTermAndFacetId(String stringTermId)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Concept getTermObjectForStringTermId(String stringTermId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Concept getTermObjectForStringTerm(String stringTerm, Facet facet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Concept getTermObjectForStringTerm(String stringTerm, String facetId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStringTermID(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void buildAuthorSynsets() {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterator<byte[][]> getCanonicalAuthorNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Concept> getTermObjectsForStringTerms(
			PairStream<String, List<String>> termsWithVariants, Facet facet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, PairStream<Concept, Long>> getTermCountsForAuthorFacets(
			Map<String, TermCountCursor> authorCounts, int sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Pair<String, Long>, Set<String>> normalizeAuthorNameCounts(
			TermCountCursor nameCounts, int sessionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertTerm(IConcept term) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertIndexOccurrencesForTerm(IConcept term, Collection<String> indexOccurrences)
			throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Concept> getFacetRoots(Facet facet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void commitTerms() {
		// TODO Auto-generated method stub

	}


	@Override
	public Iterator<IConcept> getTerms() {
		return termCache.asMap().values().iterator();
	}

	@Override
	public Iterator<IConcept> getTerms(int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long pushAllTermsIntoSuggestionQueue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long pushAllTermsIntoDictionaryQueue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long pushAllTermsIntoQueue(String queueName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<IConcept> getTermsInSuggestionQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IConcept> getTermsInQueryDictionaryQueue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<IConcept> getTermsInQueue(String queueName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getShortestPathFromAnyRoot(IConcept node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPath getShortestRootPathInFacet(IConcept node, Facet facet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IPath> getAllPathsFromRootsInFacet(IConcept term, Facet facet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAncestorOf(IConcept candidate, IConcept term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getNumTerms() {
		return (int) termCache.size();
	}

	@Override
	public void loadChildrenOfTerm(Concept facetTerm) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumLoadedRoots(String facetId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, List<Concept>> assureFacetRootsLoaded(Map<Facet, List<String>> requestRootIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadChildrenOfTerm(Concept facetTerm, String termLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<IConcept> getTermsByLabel(String label, boolean sort) {
		// TODO Auto-generated method stub
		return null;
	}

}
