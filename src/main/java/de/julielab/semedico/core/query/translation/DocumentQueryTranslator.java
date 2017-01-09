package de.julielab.semedico.core.query.translation;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.InnerHits;
import de.julielab.elastic.query.components.data.query.MatchPhraseQuery;
import de.julielab.elastic.query.components.data.query.MatchQuery;
import de.julielab.elastic.query.components.data.query.NestedQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.TermsQuery;
import de.julielab.elastic.query.components.data.query.WildcardQuery;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facetterms.CoreTerm;
import de.julielab.semedico.core.parsing.BranchNode;
import de.julielab.semedico.core.parsing.CompressedBooleanNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public abstract class DocumentQueryTranslator extends AbstractQueryTranslator {

	public static final String DEFAULT_TEXT_MINIMUM_SHOULD_MATCH = "2<75%";
	
	protected boolean acceptsWildcards = false;

	public DocumentQueryTranslator(Logger log, String name) {
		super(log, name);
	}

	/**
	 * Creates two <tt>match</tt> queries wrapped as <tt>should</tt> clauses
	 * into a <tt>BoolQuery</tt>. One clause only contains concept IDs, if any.
	 * The other contains the user input words, with a boost of .5. This is just
	 * a precaution for the case that the term recognition is extremely off
	 * (example: "male"; adjective or the gene "malE"?)
	 * 
	 * @param query
	 * @param field
	 * @return
	 */
	@Deprecated
	protected SearchServerQuery translateForMatch(ParseTree query, String field) {
		String idMatchString = convertConceptIdMatchClause(query);
		MatchQuery idMatch = null;
		if (!StringUtils.isBlank(idMatchString)) {
			idMatch = new MatchQuery();
			idMatch.field = field;
			idMatch.operator = "or";
			idMatch.query = idMatchString;
		}

		MatchQuery wordMatch = null;
		String wordMatchString = convertUserWordsMatchClause(query);
		if (!StringUtils.isBlank(wordMatchString)) {
			wordMatch = new MatchQuery();
			wordMatch.field = field;
			wordMatch.operator = "or";
			wordMatch.query = wordMatchString;
			// wordMatch.boost = .5f;
		}

		// in case we only got keywords
		if (idMatch == null)
			return wordMatch;

		BoolClause clause = new BoolClause();
		clause.occur = BoolClause.Occur.SHOULD;
		if (!StringUtils.isBlank(idMatchString))
			clause.addQuery(idMatch);
		if (!StringUtils.isBlank(wordMatchString))
			clause.addQuery(wordMatch);

		BoolQuery boolQuery = new BoolQuery();
		if (!clause.queries.isEmpty())
			boolQuery.addClause(clause);

		return boolQuery;
	}

	private String convertConceptIdMatchClause(ParseTree query) {
		StringBuilder sb = new StringBuilder();
		for (Node n : query.getConceptNodes()) {
			TextNode conceptNode = (TextNode) n;
			for (IConcept concept : conceptNode.getTerms()) {
				if (concept.getConceptType() == ConceptType.KEYWORD)
					continue;
				sb.append(concept.getId());
				sb.append(" ");
			}
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private String convertUserWordsMatchClause(ParseTree query) {
		StringBuilder sb = new StringBuilder();
		for (Node n : query.getConceptNodes()) {
			TextNode conceptNode = (TextNode) n;
			sb.append(conceptNode.getText());
			sb.append(" ");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	protected SearchServerQuery translateToBooleanQuery(ParseTree query, String field, String minimumShouldMatch) {
		return translateToBooleanQuery(query.getRoot(), field, minimumShouldMatch);
	}

	protected SearchServerQuery translateToBooleanQuery(Node node, String field, String minimumShouldMatch) {
		SearchServerQuery query;
		if (null == node.getNodeType())
			throw new IllegalArgumentException(
					"The node " + node + " is missing a node type which is required for query construction.");
		switch (node.getNodeType()) {
		case AND:
			BoolClause andClause = new BoolClause();
			andClause.occur = Occur.MUST;
			for (Node child : ((BranchNode) node).getChildren()) {
				SearchServerQuery childQuery = translateToBooleanQuery(child, field, minimumShouldMatch);
				if (null != childQuery)
					andClause.addQuery(childQuery);
			}
			if (andClause.queries.isEmpty())
				return null;
			BoolQuery andQuery = new BoolQuery();
			andQuery.addClause(andClause);
			query = andQuery;
			break;
		case NOT:
			BoolClause notClause = new BoolClause();
			notClause.occur = Occur.MUST_NOT;
			for (Node child : ((BranchNode) node).getChildren()) {
				SearchServerQuery childQuery = translateToBooleanQuery(child, field, minimumShouldMatch);
				if (null != childQuery)
					notClause.addQuery(childQuery);
			}
			if (notClause.queries.isEmpty())
				return null;
			BoolQuery notQuery = new BoolQuery();
			notQuery.addClause(notClause);
			query = notQuery;
			break;
		case OR:
			BoolClause orClause = new BoolClause();
			orClause.occur = Occur.SHOULD;
			if (null != minimumShouldMatch && minimumShouldMatch.length() > 0
					&& !node.getClass().equals(CompressedBooleanNode.class))
				throw new IllegalArgumentException(
						"The parse tree or root node passed for query translation must be compressed when disjunctions appear. This is required to set the 'minimum_should_match' paramter.");
			for (Node child : ((CompressedBooleanNode) node).getChildren()) {
				SearchServerQuery childQuery = translateToBooleanQuery(child, field, minimumShouldMatch);
				if (null != childQuery)
					orClause.addQuery(childQuery);
			}
			if (orClause.queries.isEmpty())
				return null;
			BoolQuery orQuery = new BoolQuery();
			orQuery.minimumShouldMatch = minimumShouldMatch;
			orQuery.addClause(orClause);
			query = orQuery;
			break;
		case CONCEPT:
			query = null;
			SearchServerQuery conceptQuery = null;
			SearchServerQuery wordQuery = null;
			TextNode conceptNode = (TextNode) node;
			if (!IIndexInformationService.noConceptFields.contains(field)) {
				// concept nodes have at least one term. They can have more if
				// they
				// are ambiguous
				if (conceptNode.getTerms().size() == 1) {
					IConcept concept = conceptNode.getTerms().get(0);
					if (concept.getConceptType() == ConceptType.CORE) {
						// this is a core concept (e.g. a wildcard *)
						CoreTerm ct = (CoreTerm) concept;
						switch (ct.getCoreTermType()) {
						case ANY_MOLECULAR_INTERACTION:
						case ANY_TERM:
							if (!acceptsWildcards) {
								log.debug("Field {} does not accept wildcards and thus ignores core term {}", field, node.getText());
								return null;
							}
							WildcardQuery wildcardQuery = new WildcardQuery();
							wildcardQuery.field = field;
							wildcardQuery.query = NodeIDPrefixConstants.TERM + "*";
							return wildcardQuery;
						default:
							break;
						}
					} else {
						TermQuery termQuery = new TermQuery();
						termQuery.field = field;
						termQuery.term = concept.getId();
						conceptQuery = termQuery;
					}
				} else {
					MatchQuery matchQuery = new MatchQuery();
					matchQuery.field = field;
					StringBuilder sb = new StringBuilder();
					for (IConcept concept : conceptNode.getTerms()) {
						sb.append(concept.getId()).append(" ");
					}
					matchQuery.query = sb.toString();
					matchQuery.analyzer = "whitespace";
					conceptQuery = matchQuery;
				}
				query = conceptQuery;
			}
			// if the underlying query token was not user selected - and thus
			// subject to automatic concept recognition - we also search for the
			// user given input text to avoid the case where the system pretends
			// to be smarter than the user and actually just fails to correctly
			// recognize what the user meant (example: "male"; could be
			// "masculine" but Semedico also knows a gene "malE"...)
			if (!conceptNode.getQueryToken().isUserSelected()
					&& !IIndexInformationService.conceptFields.contains(field)) {
				// to create the word-based query part, we copy the node but set
				// its
				// type to keyword
				TextNode copyNode = conceptNode.copy();
				copyNode.setNodeType(NodeType.KEYWORD);
				wordQuery = translateToBooleanQuery(copyNode, field, minimumShouldMatch);
				if (null != wordQuery)
					query = wordQuery;
			}

			// if we have a field that might contain concept IDs and words, we
			// combine the two query types
			if (null != conceptQuery && null != wordQuery) {
				BoolClause clause = new BoolClause();
				clause.occur = Occur.SHOULD;
				clause.addQuery(conceptQuery);
				clause.addQuery(wordQuery);
				BoolQuery boolQuery = new BoolQuery();
				boolQuery.addClause(clause);
				query = boolQuery;
			}
			if (null == query) {
				log.debug(
						"No query for field {} was created for concept {} because the field does not contain concept IDs and was user selected.",
						field, node.getText());
			}
			break;
		case KEYWORD:
		case PHRASE:
			if (IIndexInformationService.conceptFields.contains(field)) {
				log.debug(
						"Keyword or phrase was searched in field {} which is a pure concept ID field. It doesn't make sense to search for words or phrases, null is returned.",
						field);
				return null;
			}
			String nodeText = node.getText();
			// we currently do not search for punctuation but replace it with
			// whitespaces
			// matched punctuation symbols are
			// [!"#$%&'()*+,
			// \-./:;<=>?@
			// [\\\]^_`{|}~]
			nodeText = nodeText.replaceAll("\\p{Punct}+", " ");
			if (StringUtils.isBlank(nodeText)) {
				log.debug(
						"Phrase or keyword text to be searched would be empty after punctuation removal, returning null");
				return null;
			}
			// for Semedico, dash-compounds are also phrases; we need no special
			// handling, however, because ElasticSearch takes care of it.
			if (node.getNodeType() == NodeType.PHRASE || nodeText.contains(" ")) {
				MatchPhraseQuery phraseQuery = new MatchPhraseQuery();
				phraseQuery.field = field;
				phraseQuery.phrase = nodeText;
				// for cases like p70(S6)-kinase large phrase slops are required
				// (that expression requires a slop of 3, so 4 is just to be
				// sure)
				phraseQuery.slop = 4;
				query = phraseQuery;
			} else {
				MatchQuery matchQuery = new MatchQuery();
				matchQuery.field = field;
				matchQuery.query = nodeText;
				query = matchQuery;
			}
			break;
		default:
			throw new IllegalArgumentException(
					"Node type " + node.getNodeType() + " is not supported for query building.");

		}
		return query;
	}

	/**
	 * Searches the {@link IIndexInformationService.GeneralIndexStructure#text}
	 * field within the nested field <tt>field</tt>.
	 * 
	 * @param query
	 * @param field
	 *            The nested field to search for its inner field
	 *            {@link IIndexInformationService.GeneralIndexStructure#text}.
	 * @param innerHits
	 * @return
	 */
	protected NestedQuery translateForNestedTextField(ParseTree query, String field, String minimumShouldMatch,
			boolean innerHits) {
		SearchServerQuery fieldQuery = translateToBooleanQuery(query,
				field + "." + IIndexInformationService.GeneralIndexStructure.text, minimumShouldMatch);

		NestedQuery nestedQuery = new NestedQuery();
		nestedQuery.path = field;
		nestedQuery.query = fieldQuery;
		if (innerHits)
			nestedQuery.innerHits = new InnerHits();

		return nestedQuery;
	}
}
