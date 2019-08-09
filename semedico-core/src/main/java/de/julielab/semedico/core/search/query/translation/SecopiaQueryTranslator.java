package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.*;
import de.julielab.java.utilities.spanutils.OffsetMap;
import de.julielab.scicopia.core.parsing.ScicopiaBaseListener;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.search.query.QueryToken;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecopiaQueryTranslator extends ScicopiaBaseListener {
    // ElasticSearch will throw errors when we allow too large expansions
    private static final int MAX_EXPANSION_SIZE = 300;
    private OffsetMap<QueryToken> queryTokens;
    private SemedicoIndexField field;
    private ConceptTranslation conceptTranslation;
    private Queue<SearchServerQuery> queryTranslation;

    public SecopiaQueryTranslator(OffsetMap<QueryToken> queryTokens, SemedicoIndexField field, ConceptTranslation conceptTranslation) {
        this.queryTokens = queryTokens;
        this.field = field;
        this.conceptTranslation = conceptTranslation;
    }

    public SearchServerQuery getQueryTranslation() {
        try {
            return queryTranslation.poll();
        } finally {
            if (!queryTranslation.isEmpty())
                throw new IllegalStateException("Programming error: the queryTranslation contains more than one queries. The expected state is one single query created from the walked ParseTree.");
        }
    }

    @Override
    public void exitQuery(ScicopiaParser.QueryContext ctx) {
        if (queryTranslation.size() > 1) {
            // This happens when multiple terms without any boolean operators are added
            collapsToBooleanQuery(BoolClause.Occur.MUST);
        }
    }

    @Override
    public void exitBool(ScicopiaParser.BoolContext ctx) {
        if (ctx.negation() == null) {
            BoolClause.Occur booleanOccur = ctx.AND() != null ? BoolClause.Occur.MUST : BoolClause.Occur.SHOULD;
            collapsToBooleanQuery(booleanOccur);
        }
    }

    private void collapsToBooleanQuery(BoolClause.Occur booleanOccur) {
        BoolClause andClause = new BoolClause();
        andClause.occur = booleanOccur;
        andClause.queries = new ArrayList<>(queryTranslation);
        queryTranslation.clear();

        BoolQuery andQuery = new BoolQuery();
        andQuery.addClause(andClause);
        queryTranslation.add(andQuery);
    }

    @Override
    public void exitNegation(ScicopiaParser.NegationContext ctx) {
        collapsToBooleanQuery(BoolClause.Occur.MUST_NOT);
    }

    @Override
    public void exitToken(ScicopiaParser.TokenContext ctx) {
        final int tokenStart = ctx.getSourceInterval().a;
        final int tokenEnd = ctx.getSourceInterval().b;
        final QueryToken qt = queryTokens.get(Range.between(tokenStart, tokenEnd));
        if (qt == null)
            throw new IllegalStateException("Could not find a query token with offsets " + tokenStart + "-" + tokenEnd);
        switch (qt.getInputTokenType()) {
            case KEYWORD:
                final IConcept concept = qt.getSingleConcept();
                final TermQuery q = new TermQuery();
                q.term = concept.getId();
                queryTranslation.add(q);
                break;
            case CONCEPT:
                queryTranslation.add(translateConceptToken(qt.getSingleConcept()));
                break;
            case AMBIGUOUS_CONCEPT:
                queryTranslation.add(translateAmbiguousConceptToken(qt.getConceptList()));
                break;
            default:
                throw new IllegalArgumentException("Unhandled query token input token type " + qt.getInputTokenType());
        }
    }

    private SearchServerQuery translateConceptToken(IConcept concept) {
        SearchServerQuery query;
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
        return query;
    }

    private SearchServerQuery translateAmbiguousConceptToken(List<IConcept> concepts) {
        SearchServerQuery query;
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
        return query;
    }
}
