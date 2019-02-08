package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.*;
import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryTranslation {
    public static final String DEFAULT_TEXT_MINIMUM_SHOULD_MATCH = "10%";
    private final static Logger log = LoggerFactory.getLogger(QueryTranslation.class);
    // ElasticSearch will throw errors when we allow too large expansions
    private static final int MAX_EXPANSION_SIZE = 300;

    public static SearchServerQuery translateToBooleanQuery(ParseTree query, SemedicoIndexField field, String minimumShouldMatch, boolean acceptsWildcards, ConceptTranslation conceptTranslation) {
        return translateToBooleanQuery(query.getRoot(), field, minimumShouldMatch, acceptsWildcards, conceptTranslation);
    }

    public static SearchServerQuery translateToBooleanQuery(Node node, SemedicoIndexField field, String minimumShouldMatch, boolean acceptsWildcards, ConceptTranslation conceptTranslation) {
        SearchServerQuery query;
        if (null == node.getNodeType())
            throw new IllegalArgumentException(
                    "The node " + node + " is missing a node type which is required for query construction.");
        switch (node.getNodeType()) {
            case AND:
                BoolClause andClause = new BoolClause();
                andClause.occur = BoolClause.Occur.MUST;
                for (Node child : ((BranchNode) node).getChildren()) {
                    SearchServerQuery childQuery = translateToBooleanQuery(child, field, minimumShouldMatch, acceptsWildcards, conceptTranslation);
                    if (null != childQuery)
                        andClause.addQuery(childQuery);
                }
                if (andClause.queries.isEmpty())
                    return null;
                if (andClause.queries.stream().filter(MatchQuery.class::isInstance).count() == andClause.queries.size()) {
                    final MatchQuery matchQuery = new MatchQuery();
                    matchQuery.operator = "and";
                    matchQuery.query = andClause.queries.stream().map(MatchQuery.class::cast).map(mq -> mq.query).collect(Collectors.joining(" "));
                    matchQuery.field = field.getName();
                    matchQuery.minimumShouldMatch = minimumShouldMatch;
                    query = matchQuery;
                } else {
                    BoolQuery andQuery = new BoolQuery();
                    andQuery.addClause(andClause);
                    query = andQuery;
                }
                break;
            case NOT:
                BoolClause notClause = new BoolClause();
                notClause.occur = BoolClause.Occur.MUST_NOT;
                for (Node child : ((BranchNode) node).getChildren()) {
                    SearchServerQuery childQuery = translateToBooleanQuery(child, field, minimumShouldMatch, acceptsWildcards, conceptTranslation);
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
                orClause.occur = BoolClause.Occur.SHOULD;
                for (Node child : ((BranchNode) node).getChildren()) {
                    SearchServerQuery childQuery = translateToBooleanQuery(child, field, minimumShouldMatch, acceptsWildcards, conceptTranslation);
                    if (null != childQuery)
                        orClause.addQuery(childQuery);
                }
                if (orClause.queries.isEmpty())
                    return null;
                if (orClause.queries.stream().filter(MatchQuery.class::isInstance).count() == orClause.queries.size()) {
                    final MatchQuery matchQuery = new MatchQuery();
                    matchQuery.operator = "or";
                    matchQuery.query = orClause.queries.stream().map(MatchQuery.class::cast).map(mq -> mq.query).collect(Collectors.joining(" "));
                    matchQuery.field = field.getName();
                    matchQuery.minimumShouldMatch = minimumShouldMatch;
                    query = matchQuery;
                } else {
                    BoolQuery orQuery = new BoolQuery();
                    orQuery.minimumShouldMatch = minimumShouldMatch;
                    orQuery.addClause(orClause);
                    query = orQuery;
                }
                break;
            case CONCEPT:
                query = null;
                TextNode conceptNode = (TextNode) node;
                if (!field.getType().contains(SemedicoIndexField.Type.CONCEPTS)
                        && conceptTranslation == ConceptTranslation.ID) {
                    log.debug(
                            "No field query is created for concepts because field {} does not contain concept identifiers but concept translation is set to {}",
                            field, conceptTranslation);
                    break;
                }
                if (field.isOnly(SemedicoIndexField.Type.CONCEPTS) && conceptTranslation != ConceptTranslation.ID) {
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
                        switch (ct.getCoreConceptType()) {
                            case ANY_TERM:
                                if (!acceptsWildcards) {
                                    log.debug("Field {} does not accept wildcards and thus ignores core term {}", field,
                                            node.getText());
                                    return null;
                                }
                                return new MatchAllQuery();
                            default:
                                break;
                        }
                    } else {
                        switch (conceptTranslation) {
                            case EXPANSION_PHRASES:
                                BoolQuery boolQuery = new BoolQuery();
                                List<SearchServerQuery> namePhraseQueries = new ArrayList<>();
                                MatchPhraseQuery matchPhraseQuery = new MatchPhraseQuery();
                                matchPhraseQuery.field = field.getName();
                                matchPhraseQuery.phrase = concept.getPreferredName();
                                namePhraseQueries.add(matchPhraseQuery);
                                List<String> synonyms = concept.getSynonyms().size() >= MAX_EXPANSION_SIZE ? concept.getSynonyms().subList(0, MAX_EXPANSION_SIZE + 1) : concept.getSynonyms();
                                synonyms.stream().map(s -> {
                                    MatchPhraseQuery mpq = new MatchPhraseQuery();
                                    mpq.field = field.getName();
                                    mpq.phrase = s;
                                    return mpq;
                                }).forEach(namePhraseQueries::add);

                                BoolClause boolClause = new BoolClause();
                                boolClause.occur = BoolClause.Occur.SHOULD;
                                boolClause.queries = namePhraseQueries;
                                boolQuery.addClause(boolClause);
                                query = boolQuery;
                                break;
                            case EXPANSION_WORDS:
                                MatchQuery matchQuery = new MatchQuery();
                                matchQuery.field = field.getName();
                                matchQuery.operator = "or";
                                synonyms = concept.getSynonyms().size() >= MAX_EXPANSION_SIZE ? concept.getSynonyms().subList(0, MAX_EXPANSION_SIZE + 1) : concept.getSynonyms();
                                matchQuery.query = concept.getPreferredName() + " "
                                        + synonyms.stream().collect(Collectors.joining(" "));
                                query = matchQuery;
                                break;
                            case ID:
                                TermQuery termQuery = new TermQuery();
                                termQuery.field = field.getName();
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
                                mpq.field = field.getName();
                                mpq.phrase = s;
                                return mpq;
                            }).forEach(queries::add);
                            if (queries.size() >= MAX_EXPANSION_SIZE)
                                queries = queries.subList(0, MAX_EXPANSION_SIZE + 1);
                            BoolClause boolClause = new BoolClause();
                            boolClause.queries = queries;
                            boolClause.occur = BoolClause.Occur.SHOULD;
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
                            matchQuery.field = field.getName();
                            matchQuery.query = allNames;
                            query = matchQuery;
                            break;
                        }
                        case ID:
                            TermsQuery termsQuery = new TermsQuery(
                                    concepts.stream().map(IConcept::getId).collect(Collectors.toList()));
                            termsQuery.field = field.getName();
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
                if (field.isOnly(SemedicoIndexField.Type.CONCEPTS)) {
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
                if (node.getNodeType() == Node.NodeType.PHRASE || nodeText.contains(" ")) {
                    MatchPhraseQuery phraseQuery = new MatchPhraseQuery();
                    phraseQuery.field = field.getName();
                    phraseQuery.phrase = nodeText;
                    // for cases like p70(S6)-kinase large phrase slops are required
                    // (that expression requires a slop of 3, so 4 is just to be
                    // sure)
                    phraseQuery.slop = 4;
                    query = phraseQuery;
                } else {
                    MatchQuery matchQuery = new MatchQuery();
                    matchQuery.field = field.getName();
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
