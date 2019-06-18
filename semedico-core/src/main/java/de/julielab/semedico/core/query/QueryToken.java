package de.julielab.semedico.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilder;

import de.julielab.scicopia.core.parsing.QueryPriority;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class QueryToken implements Comparable<QueryToken> {

	/**
	 * A placeholder for {@link Node} objects that have not set the original
	 * query token they were built upon (may not even exist, e.g. implicit
	 * logical operators).
	 */
	public static final QueryToken UNSPECIFIED_QUERY_TOKEN = new QueryToken(0, 0, "");
	private int beginOffset;
	private int endOffset;
	private String originalValue;
	private List<IConcept> concepts;
	private Category type;
	private Map<IConcept, Facet> facetMapping;
	private QueryBuilder query;
	private QueryPriority priority;
	private Enum<MultiMatchQueryBuilder.Type> matchType;
	
	public enum Category {ALPHANUM, ALPHA, APOSTROPHE, LEXER_TYPE, PHRASE, DASH, NUM, COMPOUND, IRI,
		AND, OR, NOT, LPAR, RPAR, PREFIXED};

	/**
	 * Refers to the original user input token type. Did the user chose a
	 * concept, did she enter a keyword etc.
	 */
	private ITokenInputService.TokenType inputTokenType;
	private boolean userSelected;
	private String matchedSynonym;

	public ITokenInputService.TokenType getInputTokenType() {
		return inputTokenType;
	}

	public void setInputTokenType(ITokenInputService.TokenType inputTokenType) {
		this.inputTokenType = inputTokenType;
	}

	public QueryToken(String text) {
		this(0, text.length() - 1, text);
	}
	
	public QueryToken(int beginOffset, int endOffset) {
		this(beginOffset, endOffset, null);
	}

	public QueryToken(int beginOffset, int endOffset, String coveredText) {
		this.beginOffset = beginOffset;
		this.endOffset = endOffset;
		this.originalValue = coveredText;
		this.concepts = new ArrayList<>();
		this.facetMapping = new HashMap<>();
		this.inputTokenType = TokenType.FREETEXT;
		this.query = null;
		this.priority = QueryPriority.MUST;
		this.matchType = MultiMatchQueryBuilder.Type.BEST_FIELDS;
	}

	public int getBeginOffset() {
		return beginOffset;
	}

	public void setBeginOffset(int beginOffset) {
		this.beginOffset = beginOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
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

	public List<IConcept> getTermList() {
		return concepts;
	}

	public void setTermList(List<IConcept> terms) {
		this.concepts = terms;

		// Initialize the facet mapping with default values
		facetMapping = new HashMap<>();
		for (IConcept term : terms) {
			// Of course, each term should have at least one facet. This is for
			// convenience for testing.
			if (term.getFacets() != null && !term.getFacets().isEmpty()) {
				facetMapping.put(term, term.getFirstFacet());
			}
		}
	}

	/**
	 * Adds <code>concept</code> to the list of concepts associated with this
	 * specific query term. If more than two concepts are added to the list, the
	 * <code>QueryToken</code> is ambiguous.
	 * 
	 * @param concept A semantic concept denoted by this QueryToken
	 */
	public void addTermToList(IConcept concept) {
		if (concepts == null) {
			concepts = new ArrayList<>();
		}
		concepts.add(concept);
	}

	public int compareTo(QueryToken token) {
		return beginOffset - token.beginOffset;
	}

	@Override
	public String toString() {
		return "QueryToken [beginOffset=" + beginOffset + ", endOffset=" + endOffset + ", type=" + type
				+ ", originalValue=" + originalValue + ", inputTokenType: " + inputTokenType
				+ ", query:" + (query != null ? query.toString() : null) + ", priority:" + (priority != null ? priority.toString() : null) + "]";
	}

	public boolean isConceptToken() {
		// Keyword QueryTokens should have this one exact keyword as a term.
		// Everything else is a bug.
		return !concepts.isEmpty()
				&& concepts.size() == 1 && concepts.get(0).getConceptType() == ConceptType.KEYWORD;
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
		QueryToken copy = new QueryToken(beginOffset, endOffset);
		copy.setOriginalValue(originalValue);
		copy.setTermList(new ArrayList<>(concepts));
		copy.setType(type);
		copy.setQuery(query);
		copy.setPriority(priority);
		return copy;
	}

	public QueryPriority getPriority() {
		return priority;
	}
	
	public void setPriority(QueryPriority priority) {
		this.priority = priority;
	}

	public static List<QueryToken> copyQueryTokenList(List<QueryToken> list) {
		List<QueryToken> ret = new ArrayList<>();
		for (QueryToken qt : list) {
			ret.add(qt.copy());
		}
		return ret;
	}

	public void clearTermList() {
		concepts.clear();
	}

	public boolean hasEqualOffsets(QueryToken other) {
		return beginOffset == other.beginOffset && endOffset == other.endOffset;
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
	 *         combined.
	 */
	public boolean isFreetext() {
		return inputTokenType == TokenType.FREETEXT;
	}

	public static String printToString(List<QueryToken> tokens) {
		StringBuilder sbTokens = new StringBuilder();
		for (QueryToken t : tokens) {
			sbTokens.append(t.toString()).append("\n");
		}
		return sbTokens.toString();
	}

	public boolean isUserSelected() {
		return userSelected;
	}

	public void setUserSelected(boolean userSelected) {
		this.userSelected = userSelected;
	}
	
	public boolean isAmbiguous() {
		if (null == concepts) {
			return false;
		}
		return concepts.size() > 1;
	}

	public String getMatchedSynonym() {
		return matchedSynonym;
	}
	
	public void setMatchedSynonym(String matchedSynonym) {
		this.matchedSynonym = matchedSynonym;
	}

	public QueryBuilder getQuery() {
		return query;
	}
	
	public void setQuery(QueryBuilder query) {
		this.query = query;
	}
	
	public Enum<Type> getMatchType() {
		return matchType;
	}
	
	public void setMatchType(Enum<Type> matchType) {
		this.matchType = matchType;
	}
}
