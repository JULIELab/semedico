package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.julielab.semedico.core.search.query.AbstractSemedicoElasticQuery;
import de.julielab.semedico.core.search.query.translation.AbstractQueryTranslator;
import de.julielab.semedico.core.search.query.translation.ConceptTranslation;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.MatchPhraseQuery;
import de.julielab.elastic.query.components.data.query.MatchQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermQuery;
import de.julielab.elastic.query.components.data.query.TermsQuery;
import de.julielab.elastic.query.components.data.query.WildcardQuery;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.parsing.BranchNode;
import de.julielab.semedico.core.parsing.CompressedBooleanNode;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public abstract class DocumentQueryTranslator extends AbstractQueryTranslator<AbstractSemedicoElasticQuery> {

	public static final String DEFAULT_TEXT_MINIMUM_SHOULD_MATCH = "10%";
	// ElasticSearch will throw errors when we allow too large expansions
	private static final int MAX_EXPANSION_SIZE = 300;

	protected boolean acceptsWildcards = true;
	protected ConceptTranslation conceptTranslation;

	public DocumentQueryTranslator(Logger log, String name, ConceptTranslation conceptTranslation) {
		super(log, name);
		this.conceptTranslation = conceptTranslation;
	}

	@Override
	public void configure(SymbolProvider symbolProvider) {
		getEnum(SemedicoSymbolConstants.CONCEPT_TRANSLATION, symbolProvider, ConceptTranslation.class)
				.ifPresent(this::setConceptTranslation);
	}

	private void setConceptTranslation(ConceptTranslation translation) {
		log.debug("Setting concept translation to {}", translation);
		this.conceptTranslation = translation;
	}

	/**
	 * Creates two <tt>match</tt> queries wrapped as <tt>should</tt> clauses into a
	 * <tt>BoolQuery</tt>. One clause only contains concept IDs, if any. The other
	 * contains the user input words, with a boost of .5. This is just a precaution
	 * for the case that the term recognition is extremely off (example: "male";
	 * adjective or the gene "malE"?)
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
			for (IConcept concept : conceptNode.getConcepts()) {
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
			TextNode conceptNode = (TextNode) node;
			if (IIndexInformationService.noConceptFields.contains(field)
					&& conceptTranslation == ConceptTranslation.ID) {
				log.debug(
						"No field query is created for concepts because field {} does not contain concept identifiers but concept translation is set to {}",
						field, conceptTranslation);
				break;
			}
			if (IIndexInformationService.conceptFields.contains(field) && conceptTranslation != ConceptTranslation.ID) {
				log.debug(
						"No field query is created for concepts because field {} does only contain concept identifiers but concept translation is set to {}",
						field, conceptTranslation);
				break;
			}
			// concept nodes have at least one term. They can have more if
			// they are ambiguous
			List<? extends IConcept> concepts = conceptNode.getConcepts();
			if (concepts.size() == 1) {
				IConcept concept = concepts.get(0);
				if (concept.getConceptType() == ConceptType.CORE) {
					// this is a core concept (e.g. a wildcard *)
					CoreConcept ct = (CoreConcept) concept;
					switch (ct.getCoreTermType()) {
					case ANY_MOLECULAR_INTERACTION:
					case ANY_TERM:
						if (!acceptsWildcards) {
							log.debug("Field {} does not accept wildcards and thus ignores core term {}", field,
									node.getText());
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
					switch (conceptTranslation) {
					case EXPANSION_PHRASES:
						BoolQuery boolQuery = new BoolQuery();
						List<SearchServerQuery> namePhraseQueries = new ArrayList<>();
						MatchPhraseQuery matchPhraseQuery = new MatchPhraseQuery();
						matchPhraseQuery.field = field;
						matchPhraseQuery.phrase = concept.getPreferredName();
						namePhraseQueries.add(matchPhraseQuery);
						List<String> synonyms = concept.getSynonyms().size() >= MAX_EXPANSION_SIZE ? concept.getSynonyms().subList(0, MAX_EXPANSION_SIZE+1) : concept.getSynonyms();
						synonyms.stream().map(s -> {
							MatchPhraseQuery mpq = new MatchPhraseQuery();
							mpq.field = field;
							mpq.phrase = s;
							return mpq;
						}).forEach(namePhraseQueries::add);

						BoolClause boolClause = new BoolClause();
						boolClause.occur = Occur.SHOULD;
						boolClause.queries = namePhraseQueries;
						boolQuery.addClause(boolClause);
						query = boolQuery;
						break;
					case EXPANSION_WORDS:
						MatchQuery matchQuery = new MatchQuery();
						matchQuery.field = field;
						matchQuery.operator = "or";
						synonyms = concept.getSynonyms().size() >= MAX_EXPANSION_SIZE ? concept.getSynonyms().subList(0, MAX_EXPANSION_SIZE+1) : concept.getSynonyms();
						matchQuery.query = concept.getPreferredName() + " "
								+ synonyms.stream().collect(Collectors.joining(" "));
						query = matchQuery;
						break;
					case ID:
						TermQuery termQuery = new TermQuery();
						termQuery.field = field;
						termQuery.term = concept.getId();
						query = termQuery;
						break;
					default:
						throw new IllegalArgumentException(
								"Concept translation mode \"" + conceptTranslation + "\" is currently not supported.");

					}
				}
			} else {
				// Ambiguous query token.
				switch (conceptTranslation) {
				case EXPANSION_PHRASES: {
					List<SearchServerQuery> queries = new ArrayList<>();
					Stream<String> prefNameStream = concepts.stream().map(IConcept::getPreferredName);
					Stream<String> synStream = concepts.stream().flatMap(c -> c.getSynonyms().stream());
					Stream<String> nameStream = Stream.concat(prefNameStream, synStream);
					nameStream.map(s -> {
						MatchPhraseQuery mpq = new MatchPhraseQuery();
						mpq.field = field;
						mpq.phrase = s;
						return mpq;
					}).forEach(queries::add);
					if (queries.size() >= MAX_EXPANSION_SIZE)
						queries = queries.subList(0, MAX_EXPANSION_SIZE + 1);
					BoolClause boolClause = new BoolClause();
					boolClause.queries = queries;
					boolClause.occur = Occur.SHOULD;
					BoolQuery boolQuery = new BoolQuery();
					boolQuery.addClause(boolClause);
					query = boolQuery;
					break;
				}
				case EXPANSION_WORDS: {
					Stream<String> prefNameStream = concepts.stream().map(IConcept::getPreferredName);
					Stream<String> synStream = concepts.stream().flatMap(c -> c.getSynonyms().stream());
					Stream<String> nameStream = Stream.concat(prefNameStream, synStream);
					List<String> nameList = nameStream.collect(Collectors.toList());
					if (nameList.size() >= MAX_EXPANSION_SIZE)
						nameList = nameList.subList(0, MAX_EXPANSION_SIZE + 1);
					String allNames = nameList.stream().collect(Collectors.joining(" "));
					MatchQuery matchQuery = new MatchQuery();
					matchQuery.field = field;
					matchQuery.query = allNames;
					query = matchQuery;
					break;
				}
				case ID:
					TermsQuery termsQuery = new TermsQuery(
							concepts.stream().map(IConcept::getId).collect(Collectors.toList()));
					termsQuery.field = field;
					query = termsQuery;
					break;
				default:
					throw new IllegalArgumentException(
							"Concept translation mode \"" + conceptTranslation + "\" is currently not supported.");

				}

				// TODO Why don't we just use "TermsQuery"...?
				// MatchQuery matchQuery = new MatchQuery();
				// matchQuery.operator = "or";
				// matchQuery.field = field;
				// StringBuilder sb = new StringBuilder();
				// for (IConcept concept : concepts) {
				// sb.append(concept.getId()).append(" ");
				// }
				// matchQuery.query = sb.toString();
				// matchQuery.analyzer = "whitespace";
				// query = matchQuery;
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
}
