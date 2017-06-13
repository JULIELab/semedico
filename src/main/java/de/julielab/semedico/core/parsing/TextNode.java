package de.julielab.semedico.core.parsing;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.ALPHANUM_EMBEDDED_PAR;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.DASH;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.PHRASE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseTree.SERIALIZATION;
import de.julielab.semedico.core.query.QueryToken;

/**
 * This class represents a leaf in a LR td parse tree. Within Semedico,
 * instances of this class may represent identified terms, keywords and phrases.
 * 
 * @author hellrich
 * 
 */
public class TextNode extends Node implements ConceptNode {
	@Deprecated
	private boolean isPhrase;
	/**
	 * @deprecated We shouldn't keep the concepts in the node and in the query
	 *             token. Let the query token keep it.
	 */
	@Deprecated
	private List<? extends IConcept> terms = Collections.emptyList();
	/**
	 * @deprecated We shouldn't keep the map in the node and in the query token.
	 *             Let the query token keep it.
	 */
	@Deprecated
	private Map<IConcept, Facet> facetMap = Collections.emptyMap();
	private NodeType nodeType;

	/**
	 * Constructor for the leaves of the LR td parse tree.
	 * 
	 * @param text
	 *            Text in the original query referred to by this node.
	 * @param nodeType
	 */
	public TextNode(String text) {
		this(text, false);
	}

	/**
	 * Constructor for the leaves of the LR td parse tree.
	 * 
	 * @param text
	 *            Text in the original query referred to by this node.
	 * @param isPhrase
	 *            Phrase status of the node.
	 */
	public TextNode(String text, boolean isPhrase) {
		super(text);
		this.isPhrase = isPhrase;
	}

	public TextNode(String originalValue, QueryToken qt) {
		this(originalValue);
		this.queryToken = qt;
		this.nodeType = determineNodeType(qt);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public String toString() {
		return toString(SERIALIZATION.TEXT);
	}

	/**
	 * Create a string representation of this node and its subtree (mostly for
	 * debugging and test purposes).
	 * 
	 * @param useTerms
	 *            True if terms instead of original text values shall be used
	 *            for text tokens.
	 * @return A string representation of this node and its subtree.
	 */
	@Override
	public String toString(SERIALIZATION serializationType) {
		String s = "";
		if (isPhrase) {
			switch (serializationType) {
			case TERMS:
			case TEXT:
				s = text;
				break;
			case IDS:
				s = String.valueOf(id);
				break;
			}
		} else {
			switch (serializationType) {
			case TERMS:
				// Concatenate alternative terms with OR and put brackets
				// around
				// them.
				String disjunction = queryToken.getConceptList().stream().map(t -> t.getId())
						.collect(Collectors.joining(" OR "));
				if (queryToken.getConceptList().size() > 1)
					disjunction = "(" + disjunction + ")";
				return disjunction;
			case TEXT:
				s = text;
				break;
			case IDS:
				s = String.valueOf(id);
				break;
			}
		}
		return s;
	}

	@Override
	public boolean subtreeCanTakeNode() {
		return false;
	}

	/**
	 * Determine whether this text node is a phrase.
	 * 
	 * @return True if this text node is a phrase.
	 */
	@Deprecated
	public boolean isPhrase() {
		return isPhrase;
	}

	/**
	 * Set the terms matched to this text node.
	 * 
	 * @param terms
	 *            A list of terms matched to this text node.
	 */
	@SuppressWarnings("unchecked")
	public <T extends IConcept> void setTerms(List<T> terms) {
		// this.terms = terms;
		// // Initialize the facet mapping with default values
		// facetMap = new HashMap<>();
		// for (T term : terms) {
		// // Of course, each term should have at least one facet. This is for
		// // convenience for testing.
		// if (term.getFacets() != null && term.getFacets().size() > 0)
		// facetMap.put(term, term.getFirstFacet());
		// }
		assert queryToken != null;
		queryToken.setTermList((List<IConcept>) terms);
	}

	public void setFacetMapping(IConcept term, Facet facet) {
		// if (!terms.contains(term))
		// throw new IllegalArgumentException("The node " + this + " does not
		// have the term " + term);
		// facetMap.put(term, facet);
		queryToken.setFacetMapping(term, facet);
	}

	public Facet getMappedFacet(IConcept term) {
		// return facetMap.get(term);
		return queryToken.getFacetMapping(term);
	}

	/**
	 * Get the terms matched to this text node.
	 * 
	 * @return A list of terms matched to this text node.
	 */
	public List<? extends IConcept> getTerms() {
		// return terms;
		return queryToken != null ? queryToken.getConceptList() : Collections.<IConcept>emptyList();
	}

	@Deprecated
	public boolean isAmbigue() {
		return terms.size() > 1;
	}

	@Override
	public NodeType getNodeType() {
		if (null == nodeType) {
			if (terms.isEmpty())
				nodeType = NodeType.KEYWORD;
			else
				nodeType = NodeType.CONCEPT;
		}
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public boolean isAmbiguous() {
		// if (null == terms)
		// return false;
		// return terms.size() > 1;
		return queryToken.isAmbiguous();
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public TextNode copy() {
		TextNode copy = new TextNode(text);
		copy.nodeType = nodeType;
		copy.terms = terms;
		copy.facetMap = facetMap;
		copy.isPhrase = isPhrase;
		copy.height = height;
		copy.originalBeginOffset = originalBeginOffset;
		copy.originalEndOffset = originalEndOffset;
		copy.tokenType = tokenType;
		copy.queryToken = queryToken.copy();
		return copy;
	}

	private NodeType determineNodeType(QueryToken qt) {
		NodeType nodeType;
		if (qt.getConceptList().isEmpty()) {
			if (qt.getType() == PHRASE)
				nodeType = NodeType.PHRASE;
			// the DASH lexer type denotes words with embedded dashes. We take
			// those words to belong together and thus search them as a phrase
			else if (qt.getType() == DASH)
				nodeType = NodeType.PHRASE;
			// similarly, parenthesis expressions
			else if (qt.getType() == ALPHANUM_EMBEDDED_PAR)
				nodeType = NodeType.PHRASE;
			// the NUM lexer type is used for expressions consisting at least of
			// one number and some punctuation embedded. It overlaps with DASH,
			// DASH has higher priority. We handle numerical expressions as
			// phrases, too
			else if (qt.getType() == NUM)
				nodeType = NodeType.PHRASE;
			else
				nodeType = NodeType.KEYWORD;
		} else if (qt.getConceptList().size() >= 1) {
			nodeType = NodeType.CONCEPT;
		} else {
			throw new IllegalArgumentException("Could not determine node type for QueryToken " + qt);
		}
		return nodeType;
	}

}
