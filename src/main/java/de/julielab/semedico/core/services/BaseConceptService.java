package de.julielab.semedico.core.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import com.google.common.cache.LoadingCache;

import de.julielab.elastic.query.util.TermCountCursor;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.CoreTerm;
import de.julielab.semedico.core.facetterms.CoreTerm.CoreTermType;
import de.julielab.semedico.core.facetterms.SyncFacetTerm;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.PairStream;

/**
 * A term service super class to assemble the elements that just each term
 * service in Semedico should have.
 * 
 * @author faessler
 * 
 */
public abstract class BaseConceptService implements ITermService {

	protected final Logger log;
	protected final IStringTermService stringTermService;
	protected final IFacetTermFactory termFactory;

	protected Map<String, CoreTerm> coreTermsById;
	protected Map<CoreTerm.CoreTermType, CoreTerm> coreTermsByType;

	protected LoadingCache<String, IConcept> termCache;

	public void setTermCache(LoadingCache<String, IConcept> termCache) {
		this.termCache = termCache;
	}

	public BaseConceptService(Logger log, IFacetTermFactory termFactory,
			IStringTermService stringTermService, LoadingCache<String, IConcept> termCache) {
		this.log = log;
		this.termFactory = termFactory;
		this.stringTermService = stringTermService;
		this.termCache = termCache;
		createCoreTerms();
	}
	
	public BaseConceptService(Logger log, IFacetTermFactory termFactory,
			IStringTermService stringTermService) {
		this(log, termFactory, stringTermService, null);
	}

	private void createCoreTerms() {
		Map<CoreTerm.CoreTermType, CoreTerm> coreTermsByType = new HashMap<>();

		int specialTermId = 0;

		CoreTerm anyEventArgument = new CoreTerm(CORE_TERM_PREFIX + specialTermId++, "Any term");
		anyEventArgument
				.setDescription(Arrays
						.asList("A special term indicating an arbitrary argument to an event."
								+ " An event with this term as argument represents all"
								+ " events (with the remaining argument and event type) that just have some argument at the specified position."));
		anyEventArgument.setSynonyms(Arrays.asList("any", "anything", "anyterm", "any concept", "*", "all", "?"));
		anyEventArgument.setWritingVariants(Arrays.asList("hmhmhm", "möp", "what"));
		anyEventArgument.setCoreTermType(CoreTerm.CoreTermType.ANY_TERM);
		coreTermsByType.put(CoreTerm.CoreTermType.ANY_TERM, anyEventArgument);

//		CoreTerm anyMolecularInteraction = new CoreTerm(CORE_TERM_PREFIX + specialTermId++,
//				"Any molecular interaction");
//		anyMolecularInteraction.setDescription(Arrays
//				.asList("A special term indicating an arbitrary molecular interaction. "
//						+ "An event with this term as event type represents all "
//						+ "events with the same arguments but with any event type."));
//		anyMolecularInteraction.setSynonyms(Arrays.asList("any", "*", "all", "?",
//				"molecular interaction", "interaction"));
//		anyMolecularInteraction.setWritingVariants(Arrays.asList("interacts", "interact",
//				"interacting", "interacted"));
//		anyMolecularInteraction.setCoreTermType(CoreTerm.CoreTermType.ANY_MOLECULAR_INTERACTION);
//		anyMolecularInteraction.setEventValence(new HashSet<>(Arrays.asList(1, 2)));
//		coreTermsByType.put(CoreTerm.CoreTermType.ANY_MOLECULAR_INTERACTION,
//				anyMolecularInteraction);

		// CoreTerm anyTerm = new CoreTerm(CORE_TERM_PREFIX + specialTermId++,
		// "Any term");
		// anyTerm.setDescription(Arrays.asList("A special term that serves as a wildcard to denoted any term."));
		// anyTerm.setSynonyms(Arrays.asList("any", "all", "*", "?"));
		// anyTerm.setCoreTermType(CoreTermType.ANY_TERM);
		// coreTermsByType.put(CoreTerm.CoreTermType.ANY_TERM, anyTerm);

		List<CoreTerm> specialTermsList = Arrays.<CoreTerm> asList(anyEventArgument
//				,				anyMolecularInteraction
				);
		Map<String, CoreTerm> coreTermsById = new HashMap<>();
		for (CoreTerm term : specialTermsList) {
			term.setFacets(Arrays.<Facet> asList(Facet.CORE_TERMS_FACET));
			coreTermsById.put(term.getId(), term);
		}

		this.coreTermsByType = coreTermsByType;
		this.coreTermsById = coreTermsById;
	}

	@Override
	public IConcept createKeywordTerm(String id, String name) {
		return termFactory.createKeywordTerm(id, name);
	}

	@Override
	public List<Concept> getFacetRoots(Facet facet) {
		throw new NotImplementedException(
				"Should be implemented, as it is now required, obviously.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#getStringTermId
	 * (java.lang.String, de.julielab.semedico.core.Facet)
	 */
	@Override
	public String getStringTermId(String stringTerm, Facet facet) throws IllegalStateException {
		return stringTermService.getStringTermId(stringTerm, facet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#checkStringTermId
	 * (java.lang.String, de.julielab.semedico.core.Facet)
	 */
	@Override
	public String checkStringTermId(String stringTerm, Facet facet) {
		return stringTermService.checkStringTermId(stringTerm, facet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getTermObjectForStringTermId(java.lang.String)
	 */
	@Override
	public Concept getTermObjectForStringTermId(String stringTermId) {
		return stringTermService.getTermObjectForStringTermId(stringTermId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getOriginalStringTermAndFacetId(java.lang.String)
	 */
	@Override
	public Pair<String, String> getOriginalStringTermAndFacetId(String stringTermId)
			throws IllegalArgumentException {
		return stringTermService.getOriginalStringTermAndFacetId(stringTermId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IStringTermService#
	 * getTermObjectForStringTerm(java.lang.String,
	 * de.julielab.semedico.core.Facet)
	 */
	@Override
	public Concept getTermObjectForStringTerm(String stringTerm, Facet facet) {
		return stringTermService.getTermObjectForStringTerm(stringTerm, facet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#isStringTermID(
	 * java.lang.String)
	 */
	@Override
	public boolean isStringTermID(String string) {
		return stringTermService.isStringTermID(string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#buildAuthorSynsets
	 * ()
	 */
	@Override
	public void buildAuthorSynsets() {
		stringTermService.buildAuthorSynsets();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IStringTermService#getCanonicalAuthorNames
	 * ()
	 */
	@Override
	public Iterator<byte[][]> getCanonicalAuthorNames() {
		return stringTermService.getCanonicalAuthorNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * getTermObjectForStringTerm(java.lang.String, int)
	 */
	@Override
	public Concept getTermObjectForStringTerm(String stringTerm, String facetId) {
		return stringTermService.getTermObjectForStringTerm(stringTerm, facetId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * mapQueryStringTerms(java.util.Collection)
	 */
	@Override
	public Collection<QueryToken> mapQueryStringTerms(Collection<QueryToken> inputTokens,
			long sessionId) {
		return stringTermService.mapQueryStringTerms(inputTokens, sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * getTermObjectsForStringTerms(de.julielab.util.PairStream,
	 * de.julielab.semedico.core.Facet)
	 */
	@Override
	public Collection<Concept> getTermObjectsForStringTerms(
			PairStream<String, List<String>> termsWithVariants, Facet facet) {
		return stringTermService.getTermObjectsForStringTerms(termsWithVariants, facet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * getTermCountsForAuthorFacets(java.util.Map)
	 */
	@Override
	public Map<String, PairStream<Concept, Long>> getTermCountsForAuthorFacets(
			Map<String, TermCountCursor> authorCounts, int sessionId) {
		return stringTermService.getTermCountsForAuthorFacets(authorCounts, sessionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.interfaces.IStringTermService#
	 * normalizeAuthorNameCounts(java.util.List)
	 */
	@Override
	public Map<Pair<String, Long>, Set<String>> normalizeAuthorNameCounts(
			TermCountCursor nameCounts, int sessionId) {
		return stringTermService.normalizeAuthorNameCounts(nameCounts, sessionId);
	}

	@Override
	public IConcept getTerm(String id) {
		if (id.startsWith(NodeIDPrefixConstants.TERM) || id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM))
			return termCache.getUnchecked(id);
		if (id.startsWith(CORE_TERM_PREFIX))
			return coreTermsById.get(id);
		log.warn("Unknown term id: {}, returning null", id);
		return null;
	}
	
	@Override
	public IConcept getTermSynchronously(String id) {
		IConcept term = getTerm(id);
		if (term instanceof SyncFacetTerm) {
			// check if the term was actually loaded: it must have a preferred name. So if there is non, database loading failed
			if (term.getPreferredName() == null)
				return null;
		}
		return term;
	}
	
	@Override
	public IConcept getTermIfCached(String id) {
		if (id.startsWith(NodeIDPrefixConstants.TERM) || id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM))
			return termCache.getIfPresent(id);
		if (id.startsWith(CORE_TERM_PREFIX))
			return coreTermsById.get(id);
		log.warn("Unknown term id: {}, returning null", id);
		return null;
	}

	@Override
	public boolean isTermID(String termId) {
		return termId.startsWith(NodeIDPrefixConstants.TERM.toString()) || termId.startsWith(CORE_TERM_PREFIX)
				|| termId.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM.toString());
	}
	
	@Override
	public Map<String, CoreTerm> getCoreTerms() {
		return coreTermsById;
	}

	@Override
	public CoreTerm getCoreTerm(CoreTermType type) {
		return coreTermsByType.get(type);
	}

	@Override
	public boolean hasTerm(String id) {
		return null != termCache.getUnchecked(id) || null != coreTermsById.get(id);
	}
}
