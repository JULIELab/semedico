package de.julielab.semedico.core.search.query;

import de.julielab.java.utilities.spanutils.SpanImplBase;
import de.julielab.scicopia.core.parsing.QueryPriority;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.concepts.CoreConcept.CoreConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import org.apache.commons.lang3.Range;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.*;

public class QueryToken extends SpanImplBase  implements Comparable<QueryToken> {

    /**
     * A placeholder for {@link Node} objects that have not set the original
     * query token they were built upon (may not even exist, e.g. implicit
     * logical operators).
     */
    public static final QueryToken UNSPECIFIED_QUERY_TOKEN = new QueryToken(0, 0, "");
    private Category type;
    private String originalValue;
    private List<IConcept> concepts = Collections.emptyList();
    private double score;
    private Map<IConcept, Facet> facetMapping;
    /**
     * Refers to the original user input token type. Did the user chose a
     * concept, did she enter a keyword etc.
     */
    private ITokenInputService.TokenType inputTokenType;
    private boolean userSelected;
    private String matchedSynonym;
    private QueryBuilder query;
    private QueryPriority priority;
    private Enum<MultiMatchQueryBuilder.Type> matchType;

    public QueryToken(String text) {
        this(0, text.length() - 1, text);
    }

    public QueryToken(int beginOffset, int endOffset) {
        this(beginOffset, endOffset, null);
    }

    public QueryToken(int beginOffset, int endOffset, String coveredText) {
        super(Range.between(beginOffset, endOffset));
        this.originalValue = coveredText;
        this.concepts = new ArrayList<>();
        this.facetMapping = new HashMap<>();
        this.inputTokenType = TokenType.FREETEXT;
    }

    public static List<QueryToken> copyQueryTokenList(List<QueryToken> list) {
        List<QueryToken> ret = new ArrayList<>();
        for (QueryToken qt : list)
            ret.add(qt.copy());
        return ret;
    }

    public static String printToString(List<QueryToken> tokens) {
        StringBuilder sbTokens = new StringBuilder();
        for (QueryToken t : tokens)
            sbTokens.append(t.toString()).append("\n");
        return sbTokens.toString();
    }

    public QueryBuilder getQuery() {
        return query;
    }

    public void setQuery(QueryBuilder query) {
        this.query = query;
    }

    ;

    public QueryPriority getPriority() {
        return priority;
    }

    public void setPriority(QueryPriority priority) {
        this.priority = priority;
    }

    public Enum<MultiMatchQueryBuilder.Type> getMatchType() {
        return matchType;
    }

    public void setMatchType(Enum<MultiMatchQueryBuilder.Type> matchType) {
        this.matchType = matchType;
    }

    public ITokenInputService.TokenType getInputTokenType() {
        return inputTokenType;
    }

    public void setInputTokenType(ITokenInputService.TokenType inputTokenType) {
        this.inputTokenType = inputTokenType;
    }

    public Category getType() {
        return type;
    }

    public void setType(Category type) {
        this.type = type;
    }

    public String getOriginalValue() {
        return this.originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public List<IConcept> getConceptList() {
        return concepts;
    }

    public void setConceptList(List<IConcept> terms) {
        this.concepts = terms;

        // Initialize the facet mapping with default values
        facetMapping = new HashMap<>();
        for (IConcept term : terms) {
            // Of course, each term should have at least one facet. This is for
            // convenience for testing.
            if (term.getFacets() != null && term.getFacets().size() > 0)
                facetMapping.put(term, term.getFirstFacet());
        }
    }

    /**
     * Adds <code>concept</code> to the list of concepts associated with this
     * specific query term. If more than two concepts are added to the list, the
     * <code>QueryToken</code> is ambiguous.
     *
     * @param concept A semantic concept denoted by this QueryToken
     */
    public void addConceptToList(IConcept concept) {
        if (concepts.isEmpty())
            concepts = new ArrayList<>();
        concepts.add(concept);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int compareTo(QueryToken token) {
        return getBegin() - token.getBegin();
    }

    @Override
    public String toString() {
        return "QueryToken [beginOffset=" + getBegin() + ", endOffset=" + getEnd() + ", type=" + type
                + ", originalValue=" + originalValue + ", inputTokenType: " + inputTokenType + "]";
    }

    public boolean isConceptToken() {
        // Keyword QueryTokens should have this one exact keyword as a term.
        // Everything else is a bug.
        return concepts.size() > 0
                && !(concepts.size() == 1 && concepts.get(0).getConceptType() == ConceptType.KEYWORD);
    }

    public boolean isKeywordToken() {
        return !isConceptToken();
    }

    /**
     * Returns a copy of this token. Manipulations on the fields of the copy
     * won't write through.
     *
     * @return
     */
    public QueryToken copy() {
        QueryToken copy = new QueryToken(getBegin(), getEnd());
        copy.setOriginalValue(originalValue);
        copy.setScore(score);
        copy.setConceptList(new ArrayList<>(concepts));
        copy.setType(type);
        return copy;
    }

    public void clearTermList() {
        concepts.clear();
    }

    public boolean hasEqualOffsets(QueryToken other) {
        return offsets.equals(other.offsets);
    }

    public boolean isWildCardToken() {
        if (concepts.isEmpty())
            return false;
        for (IConcept term : concepts) {
            if (term.getConceptType() == ConceptType.CORE) {
                CoreConcept ct = (CoreConcept) term;
                if (ct.getCoreConceptType() == CoreConceptType.ANY_TERM) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setFacetMapping(IConcept term, Facet facet) {
        facetMapping.put(term, facet);
    }

    public Facet getFacetMapping(IConcept concept) {
        return facetMapping.get(concept);
    }

    /**
     * Indicates whether this query term is frozen and thus is not supposed to
     * be changed any more. This is mainly used to forbid re-combining with
     * other query tokens in order to try and get longer term matches.
     *
     * @return Whether this query token is frozen and should not be split or
     * combined.
     */
    public boolean isFreetext() {
        return inputTokenType == TokenType.FREETEXT;
    }

    public boolean isUserSelected() {
        return userSelected;
    }

    public void setUserSelected(boolean userSelected) {
        this.userSelected = userSelected;
    }

    public boolean isAmbiguous() {
        if (null == concepts)
            return false;
        return concepts.size() > 1;
    }

    public String getMatchedSynonym() {
        return matchedSynonym;
    }

    public void setMatchedSynonym(String matchedSynonym) {
        this.matchedSynonym = matchedSynonym;

    }

    public IConcept getSingleConcept() {
        if (concepts == null || concepts.isEmpty())
            throw new IllegalArgumentException("This query token does not contain any concept.");
        if (concepts.size() > 1)
            throw new IllegalArgumentException("This query token contains more than one concept (" + concepts.size() + ").");
        return concepts.get(0);
    }
    public enum Category {
        ALPHANUM, ALPHA, APOSTROPHE, LEXER_TYPE, PHRASE, DASH, NUM, COMPOUND, IRI,
        AND, OR, NOT, LPAR, RPAR, PREFIXED, HASHTAG
    }

}
