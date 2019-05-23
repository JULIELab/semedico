package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.ImportConcept;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;

public class MiniTermService extends BaseConceptService {

	private int termNumber = 0;
	
	public MiniTermService(Logger log, IFacetTermFactory termFactory) {
		super(log, termFactory);
		setTermCache(createMiniTermCache());
	}
	
	private LoadingCache<String, IConcept> createMiniTermCache() {
		Gson gson = new Gson();
		JSONArray termLabels = new JSONArray(TermLabels.GeneralLabel.TERM.toString());
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
	}

	private IConcept createMiniTerm(Gson gson, JSONArray termLabels, String prefName,
			int facetNumber, List<String> synonyms) {
		ImportConcept importTerm = new ImportConcept(prefName, null);
		importTerm.synonyms = synonyms;
		String termJson = gson.toJson(importTerm);
		JSONObject termObject = new JSONObject(termJson);
		termObject.put("id", NodeIDPrefixConstants.TERM + termNumber++);
		// the "Diseases" facet as defined by the mini facet service
		termObject.put(ConceptConstants.PROP_FACETS, new JSONArray(NodeIDPrefixConstants.FACET + facetNumber));
		return termFactory.createFacetTermFromJson(termObject.toCompactString(), termLabels);
	}

	@Override
	public List<Concept> getFacetRoots(Facet facet) {
		return null;
	}

	@Override
	public Iterator<IConcept> getTerms() {
		return termCache.asMap().values().iterator();
	}

	@Override
	public Iterator<IConcept> getTerms(int limit) {
		return null;
	}

	@Override
	public long pushAllTermsIntoSuggestionQueue() {
		return 0;
	}

	@Override
	public long pushAllTermsIntoDictionaryQueue() {
		return 0;
	}

	@Override
	public long pushAllTermsIntoQueue(String queueName) {
		return 0;
	}

	@Override
	public Iterator<IConcept> getTermsInSuggestionQueue() {
		return null;
	}

	@Override
	public Iterator<IConcept> getTermsInQueryDictionaryQueue() {
		return null;
	}

	@Override
	public Iterator<IConcept> getTermsInQueue(String queueName) {
		return null;
	}

	@Override
	public IPath getShortestPathFromAnyRoot(IConcept node) {
		return null;
	}

	@Override
	public IPath getShortestRootPathInFacet(IConcept node, Facet facet) {
		return null;
	}

	@Override
	public Collection<IPath> getAllPathsFromRootsInFacet(IConcept term, Facet facet) {
		return null;
	}

	@Override
	public boolean isAncestorOf(IConcept candidate, IConcept term) {
		return false;
	}

	@Override
	public int getNumTerms() {
		return (int) termCache.size();
	}

	@Override
	public void loadChildrenOfTerm(Concept facetTerm) {
	}

	@Override
	public int getNumLoadedRoots(String facetId) {
		return 0;
	}

	@Override
	public Map<String, List<Concept>> assureFacetRootsLoaded(Map<Facet, List<String>> requestRootIds) {
		return null;
	}

	@Override
	public void loadChildrenOfTerm(Concept facetTerm, String termLabel) {
	}

	@Override
	public List<IConcept> getTermsByLabel(String label, boolean sort) {
		return null;
	}

}
