package de.julielab.semedico.core.services;

import com.google.common.cache.LoadingCache;
import de.julielab.elastic.query.util.TermCountCursor;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.SyncDbConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IConceptCreator;
import de.julielab.semedico.core.services.interfaces.IStringTermService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.PairStream;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.util.*;

/**
 * A term service super class to assemble the elements that just each term
 * service in Semedico should have.
 *
 * @author faessler
 */
public abstract class BaseConceptService implements ITermService {

    protected final Logger log;
    protected final IStringTermService stringTermService;
    protected final IConceptCreator conceptCreator;

    protected Map<String, CoreConcept> coreConceptsById;
    protected Map<CoreConcept.CoreConceptType, CoreConcept> coreConceptsByType;

    protected LoadingCache<String, IConcept> conceptCache;

    public BaseConceptService(Logger log, IConceptCreator conceptCreator,
                              IStringTermService stringTermService, LoadingCache<String, IConcept> conceptCache) {
        this.log = log;
        this.conceptCreator = conceptCreator;
        this.stringTermService = stringTermService;
        this.conceptCache = conceptCache;
        createCoreTerms();
    }

    public BaseConceptService(Logger log, IConceptCreator conceptCreator,
                              IStringTermService stringTermService) {
        this(log, conceptCreator, stringTermService, null);
    }

    public void setConceptCache(LoadingCache<String, IConcept> conceptCache) {
        this.conceptCache = conceptCache;
    }

    private void createCoreTerms() {
        Map<CoreConcept.CoreConceptType, CoreConcept> coreTermsByType = new HashMap<>();

        int specialTermId = 0;


        CoreConcept anyTerm = new CoreConcept(CORE_TERM_PREFIX + specialTermId++,
                "Any term");
        anyTerm.setDescription(Arrays.asList("A special term that serves as a wildcard to denoted any term."));
        anyTerm.setSynonyms(Arrays.asList("any", "all", "*", "?"));
        anyTerm.setCoreConceptType(CoreConcept.CoreConceptType.ANY_TERM);
        coreTermsByType.put(CoreConcept.CoreConceptType.ANY_TERM, anyTerm);

        List<CoreConcept> coreConceptList = Arrays.<CoreConcept>asList(anyTerm);
        Map<String, CoreConcept> coreTermsById = new HashMap<>();
        for (CoreConcept term : coreConceptList) {
            coreTermsById.put(term.getId(), term);
        }

        this.coreConceptsByType = coreTermsByType;
        this.coreConceptsById = coreTermsById;
    }

    @Override
    public IConcept createKeywordConcept(String id, String name) {
        return conceptCreator.createKeywordConcept(id, name);
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
            return conceptCache.getUnchecked(id);
        if (id.startsWith(CORE_TERM_PREFIX))
            return coreConceptsById.get(id);
        log.warn("Unknown term id: {}, returning null", id);
        return null;
    }

    @Override
    public IConcept getTermSynchronously(String id) {
        IConcept term = getTerm(id);
        if (term instanceof SyncDbConcept) {
            // check if the term was actually loaded: it must have a preferred name. So if there is non, database loading failed
            if (term.getPreferredName() == null)
                return null;
        }
        return term;
    }

    @Override
    public IConcept getConceptIfCached(String id) {
        if (id.startsWith(NodeIDPrefixConstants.TERM) || id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM))
            return conceptCache.getIfPresent(id);
        if (id.startsWith(CORE_TERM_PREFIX))
            return coreConceptsById.get(id);
        log.warn("Unknown concept id: {}, returning null", id);
        return null;
    }

    @Override
    public boolean isConceptID(String termId) {
        return termId.startsWith(NodeIDPrefixConstants.TERM.toString()) || termId.startsWith(CORE_TERM_PREFIX)
                || termId.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM.toString());
    }

    @Override
    public Map<String, CoreConcept> getCoreConcepts() {
        return coreConceptsById;
    }

    @Override
    public CoreConcept getCoreTerm(CoreConcept.CoreConceptType type) {
        return coreConceptsByType.get(type);
    }

    @Override
    public boolean hasConcept(String id) {
        return null != conceptCache.getUnchecked(id) || null != coreConceptsById.get(id);
    }
}
