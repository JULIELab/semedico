package de.julielab.semedico.core.query;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.BINARY_EVENT;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.UNARY_EVENT;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.UNARY_OR_BINARY_EVENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.EventQueryToken;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.CoreTerm;
import de.julielab.semedico.core.facetterms.CoreTerm.CoreTermType;
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
	private int type;
	private String originalValue;
	private List<IConcept> concepts;
	private double score;
	@Deprecated
	private boolean isEventArgument;
	private Map<IConcept, Facet> facetMapping;
	@Deprecated
	private boolean freetext;
	/**
	 * Refers to the original user input token type. Did the user chose a
	 * concept, did she enter a keyword etc.
	 */
	private ITokenInputService.TokenType inputTokenType;
	private boolean userSelected;
	private String matchedSynonym;

	public ITokenInputService.TokenType getInputTokenType() {
//		if (null == inputTokenType)
//			throw new IllegalStateException("The QueryToken " + this + " doesn't have its inputTokenType set.");
		return inputTokenType;
	}

	public void setInputTokenType(ITokenInputService.TokenType inputTokenType) {
		this.inputTokenType = inputTokenType;
	}

	@Deprecated
	public boolean isEventArgument() {
		return isEventArgument;
	}

	public QueryToken(int beginOffset, int endOffset) {
		this(beginOffset, endOffset, null);
	}

	public QueryToken(int beginOffset, int endOffset, String coveredText) {
		this.beginOffset = beginOffset;
		this.endOffset = endOffset;
		this.originalValue = coveredText;
		this.concepts = new ArrayList<IConcept>();
		this.facetMapping = new HashMap<>();
		this.freetext = false;
		this.inputTokenType = TokenType.FREETEXT;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
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

	public void setTermList(List<IConcept> terms) {
		this.concepts = terms;
		for (IConcept term : terms)
			determineEventType(term);
		
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
	public void addTermToList(IConcept concept) {
		if (concepts == null)
			concepts = new ArrayList<IConcept>();
		concepts.add(concept);
		// determineEventType(concept);
	}

	/**
	 * Changes the event type of this QueryToken depending on the given term. If
	 * this QueryToken has multiple terms, e.g. is ambiguous, the most relaxed
	 * type will be used that subsumes all event types of terms. I.e. if there
	 * is a unary and a binary event trigger, the event type will be set to
	 * "unary or binary".
	 * 
	 * @param term delete
	 */
	@Deprecated
	protected void determineEventType(IConcept term) {
		switch (term.getEventType()) {
		case BINARY:
			if (type == UNARY_EVENT)
				type = UNARY_OR_BINARY_EVENT;
			else
				type = BINARY_EVENT;
			break;
		case BOTH:
			type = UNARY_OR_BINARY_EVENT;
			break;
		case NONE:
			break;
		case UNARY:
			if (type == BINARY_EVENT)
				type = UNARY_OR_BINARY_EVENT;
			else
				type = UNARY_EVENT;
			break;
		}
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public int compareTo(QueryToken token) {
		return beginOffset - token.beginOffset;
	}

	@Override
	public String toString() {
		return "QueryToken [beginOffset=" + beginOffset + ", endOffset=" + endOffset + ", type=" + type
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

	@Deprecated
	public boolean isBinaryEvent() {
		return type == BINARY_EVENT || type == UNARY_OR_BINARY_EVENT;
	}

	@Deprecated
	public boolean isUnaryEvent() {
		return type == UNARY_EVENT || type == UNARY_OR_BINARY_EVENT;
	}

	@Deprecated
	public boolean isEventTrigger() {
		return isUnaryEvent() || isBinaryEvent();
	}

	@Deprecated
	public boolean hasNonEventTriggerTerm() {
		boolean has = false;
		for (IConcept term : concepts) {
			has |= !term.isEventTrigger();
			// if (term.getConceptType() != ConceptType.EVENT) {
			// if (term.getConceptType() == ConceptType.CORE) {
			// CoreTerm ct = (CoreTerm) term;
			// if (ct.getCoreTermType() !=
			// CoreTermType.ANY_MOLECULAR_INTERACTION)
			// return true;
			// } else if (term.isEventTrigger())
			// return true;
			// }
		}
		return has;
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
		copy.setScore(score);
		copy.setTermList(new ArrayList<IConcept>(concepts));
		copy.setType(type);
		return copy;
	}

	public static List<QueryToken> copyQueryTokenList(List<QueryToken> list) {
		List<QueryToken> ret = new ArrayList<>();
		for (QueryToken qt : list)
			ret.add(qt.copy());
		return ret;
	}

	public EventQueryToken asEventQueryToken(int type, QueryToken... arguments) {
		EventQueryToken eqt = new EventQueryToken(beginOffset, endOffset);
		eqt.setOriginalValue(originalValue);
		eqt.setScore(score);
		eqt.setTermList(concepts);
		eqt.setType(type);
		eqt.setArguments(arguments);
		for (int i = 0; i < arguments.length; i++) {
			QueryToken argument = arguments[i];
			argument.setIsEventArgument(true);
		}
		return eqt;
	}

	@Deprecated
	public EventQueryToken asEventQueryToken(QueryToken... arguments) {
		EventQueryToken eqt = new EventQueryToken(beginOffset, endOffset);
		eqt.setOriginalValue(originalValue);
		eqt.setScore(score);
		eqt.setTermList(concepts);
		eqt.setType(type);
		eqt.setArguments(arguments);
		for (int i = 0; i < arguments.length; i++) {
			QueryToken argument = arguments[i];
			argument.setIsEventArgument(true);
		}
		return eqt;
	}

	@Deprecated
	private void setIsEventArgument(boolean isEventArgument) {
		this.isEventArgument = isEventArgument;
	}

	public void clearTermList() {
		concepts.clear();
	}

	public boolean hasEqualOffsets(QueryToken other) {
		return beginOffset == other.beginOffset && endOffset == other.endOffset;
	}

	public boolean isWildCardToken() {
		if (concepts.isEmpty())
			return false;
		for (IConcept term : concepts) {
			if (term.getConceptType() == ConceptType.CORE) {
				CoreTerm ct = (CoreTerm) term;
				if (ct.getCoreTermType() == CoreTermType.ANY_TERM
						|| ct.getCoreTermType() == CoreTermType.ANY_MOLECULAR_INTERACTION) {
					return true;
				}
			}
		}
		return false;
	}

	@Deprecated
	public boolean isEventFunctional() {
		if (type == BINARY_EVENT || type == UNARY_EVENT || type == UNARY_OR_BINARY_EVENT)
			return true;
		if (concepts.isEmpty())
			return false;
		for (IConcept term : concepts) {
			if (term.isEventFunctional()) {
				return true;
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
	 * @deprecated user {@link #setInputTokenType(TokenType)}
	 * @param freetext
	 */
	@Deprecated
	public void setFreetext(boolean freetext) {
		this.freetext = freetext;
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
		return freetext;
	}

	public static String printToString(List<QueryToken> tokens) {
		StringBuilder sbTokens = new StringBuilder();
		for (QueryToken t : tokens)
			sbTokens.append(t.toString()).append("\n");
		return sbTokens.toString();
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

	public void setMatchedSynonym(String matchedSynonym) {
		this.matchedSynonym = matchedSynonym;
		
	}

	public String getMatchedSynonym() {
		return matchedSynonym;
	}

}
