package de.julielab.semedico.core.parsing;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.search.query.QueryToken;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a leaf in a LR td parse tree. Within Semedico,
 * instances of this class may represent identified terms, keywords and phrases.
 *
 * @author hellrich
 *
 */
public class TextNode extends Node implements ConceptNode {
	private NodeType nodeType;

	/**
	 * Constructor for the leaves of the LR td parse tree.
	 *
	 * @param text
	 *            Text in the original query referred to by this node.
	 */
	public TextNode(String text) {
		this(text, new QueryToken(0, text.length(), text));
	}

	public TextNode(String originalValue, QueryToken qt) {
		super(originalValue);
		this.queryToken = qt;
		this.nodeType = determineNodeType(qt);
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	@Override
	public String toString() {
		return toString(Serialization.NODE_TEXT);
	}

	/**
	 * Create a string representation of this node and its subtree (mostly for
	 * debugging and test purposes).
	 *
	 * @param serializationType
	 * @return A string representation of this node and its subtree.
	 */
	@Override
	public String toString(Serialization serializationType) {
		String s = "";
		switch (serializationType) {
		case CONCEPT_NAME_TYPE:
			if (!queryToken.getConceptList().isEmpty()) {
				if (!queryToken.isAmbiguous()) {
					s = queryToken.getConceptList().get(0).getPreferredName() + "[C]";
				} else {
					s = text + "[" + queryToken.getConceptList().size() + "AM]";
				}
				break;
			}
			// If there are no concepts, we just continue down to CONCEPT_IDS where this
			// case is handeled.
		case CONCEPT_IDS:
			if (queryToken == null)
				throw new IllegalStateException("This " + getClass().getSimpleName() + " (text: \"" + text
						+ "\") does not have a query token.");
			// Concatenate alternative terms with OR and put brackets
			// around
			// them.
			String disjunction;
			if (!queryToken.getConceptList().isEmpty())
				disjunction = queryToken.getConceptList().stream().map(t -> {
					return t.getId();
				}).collect(Collectors.joining(" OR "));
			else
				disjunction = text + "[KW]";
			if (queryToken.getConceptList().size() > 1)
				disjunction = "(" + disjunction + ")";
			s = disjunction;
			break;
		case NODE_TEXT:
			s = text;
			break;
		case NODE_IDS:
			s = String.valueOf(id);
			break;
		}
		return s;
	}

	@Override
	public boolean subtreeCanTakeNode() {
		return false;
	}

	public void setFacetMapping(IConcept term, Facet facet) {
		// if (!terms.contains(term))
		// throw new IllegalArgumentException("The node " + this + " does not
		// have the term " + term);
		// facetMap.put(term, facet);
		queryToken.setFacetMapping(term, facet);
	}
    /**
     * Set the terms matched to this text node.
     *
     * @param terms
     *            A list of terms matched to this text node.
     */
    @SuppressWarnings("unchecked")
    public <T extends IConcept> void setConcepts(List<T> terms) {
        queryToken.setConceptList((List<IConcept>) terms);
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
	public List<? extends IConcept> getConcepts() {
		// return terms;
		return queryToken != null ? queryToken.getConceptList() : Collections.<IConcept>emptyList();
	}

	@Override
	public NodeType getNodeType() {
		if (null == nodeType) {
			if (queryToken.getConceptList().isEmpty())
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
            if (qt.getType() == QueryToken.Category.KW_PHRASE ||
                    qt.getType() == QueryToken.Category.DASH ||
                    qt.getType() == QueryToken.Category.NUM ||
                    qt.getType() == QueryToken.Category.COMPOUND ||
                    qt.getType() == QueryToken.Category.IRI) {
                nodeType = NodeType.PHRASE;
            } else {
                nodeType = NodeType.KEYWORD;
            }
        } else if (qt.getConceptList().size() >= 1) {
            nodeType = NodeType.CONCEPT;
        } else {
            throw new IllegalArgumentException("Could not determine node type for QueryToken " + qt);
        }
        return nodeType;
    }

}
