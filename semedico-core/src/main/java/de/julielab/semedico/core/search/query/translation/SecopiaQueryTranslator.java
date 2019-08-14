package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.*;
import de.julielab.java.utilities.spanutils.OffsetMap;
import de.julielab.scicopia.core.parsing.ScicopiaBaseListener;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.search.query.QueryToken;
import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.elastic.query.components.data.query.BoolClause.Occur.FILTER;
import static de.julielab.elastic.query.components.data.query.BoolClause.Occur.MUST;
import static de.julielab.elastic.query.components.data.query.BoolClause.Occur.MUST_NOT;
import static de.julielab.elastic.query.components.data.query.MultiMatchQuery.Type.phrase;

public class SecopiaQueryTranslator extends ScicopiaBaseListener {
    // ElasticSearch will throw errors when we allow too large expansions
    private static final int MAX_EXPANSION_SIZE = 300;
    private OffsetMap<QueryToken> queryTokens;
    private Collection<SemedicoIndexField> fields;
    private List<String> fieldNames;
    private ConceptTranslation conceptTranslation;
    private Queue<SearchServerQuery> queryTranslation;

    public SecopiaQueryTranslator(OffsetMap<QueryToken> queryTokens, Collection<SemedicoIndexField> fields, ConceptTranslation conceptTranslation) {
        this.queryTokens = queryTokens;
        this.fields = fields;
        fieldNames = fields.stream().map(SemedicoIndexField::getName).distinct().collect(Collectors.toList());
        this.conceptTranslation = conceptTranslation;
        queryTranslation = new ArrayDeque<>();
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
            collapseWideMatch();
            collapseDeepMatch(MUST);
        }
    }


    @Override
    public void exitBool(ScicopiaParser.BoolContext ctx) {
        // Here we handle nodes corresponding to AND or OR tokens occurring in the query.
        // the AND and OR tests are necessary because the operands of a boolean operator
        // node have the 'bool' node type themselves. So only continue here if
        // this is actually a AND or OR node.
        if (ctx.negation() == null && (ctx.AND() != null || ctx.OR() != null)) {
            BoolClause.Occur booleanOccur = ctx.AND() != null ? MUST : BoolClause.Occur.SHOULD;
            collapseDeepMatch(booleanOccur);
        }
    }

    private void collapseWideMatch() {
        List<String> currentQueryTerms = new ArrayList<>();
        String operator = null;
        MultiMatchQuery.Type type = null;
        List<String> fields = null;
        Queue<SearchServerQuery> collapsedTranslation = new ArrayDeque<>();
        for (SearchServerQuery q : queryTranslation) {
            MultiMatchQuery mmq;
            if (q instanceof MultiMatchQuery && (mmq = (MultiMatchQuery) q).type != phrase
                    && (operator == null || mmq.operator == null || mmq.operator.equals(operator))
                    && (type == null || mmq.type == null || mmq.type.equals(type))
                    && (fields == null || mmq.fields.equals(fields))) {
                if (operator == null)
                    operator = mmq.operator;
                if (type == null)
                    type = mmq.type;
                if (fields ==  null)
                    fields = mmq.fields;
                currentQueryTerms.add(mmq.query);
            } else {
                if (!currentQueryTerms.isEmpty()) {
                    final MultiMatchQuery collapsedMmq = new MultiMatchQuery();
                    collapsedMmq.fields = fields;
                    collapsedMmq.query = currentQueryTerms.stream().collect(Collectors.joining(" "));
                    collapsedMmq.operator = operator;
                    collapsedMmq.type = type;
                    collapsedTranslation.add(collapsedMmq);
                    currentQueryTerms.clear();
                    operator = null;
                    type = null;
                    fields = null;
                }
                collapsedTranslation.add(q);
            }
        }
        if (!currentQueryTerms.isEmpty()) {
            final MultiMatchQuery collapsedMmq = new MultiMatchQuery();
            collapsedMmq.fields = fields;
            collapsedMmq.query = currentQueryTerms.stream().collect(Collectors.joining(" "));
            collapsedMmq.operator = operator;
            collapsedMmq.type = type;
            collapsedTranslation.add(collapsedMmq);
        }

        queryTranslation = collapsedTranslation;
    }

    private void collapseDeepMatch(BoolClause.Occur booleanOccur) {
        List<String> queryTerms = new ArrayList<>();
        boolean oneMatchPossible = true;
        for (SearchServerQuery query : queryTranslation)
            oneMatchPossible = oneMatchPossible && collectSubtreeCollapseInformation(query, queryTerms, booleanOccur);

        if (!oneMatchPossible) {
            BoolClause andClause = new BoolClause();
            andClause.occur = booleanOccur;
            andClause.queries = new ArrayList<>(queryTranslation);
            queryTranslation.clear();

            BoolQuery andQuery = new BoolQuery();
            andQuery.addClause(andClause);
            queryTranslation.add(andQuery);
        } else {
            MultiMatchQuery mq = new MultiMatchQuery();
            mq.fields = fieldNames;
            mq.query = queryTerms.stream().collect(Collectors.joining(" "));
            mq.operator = booleanOccur == MUST ? "and" : "or";
            queryTranslation.clear();
            queryTranslation.add(mq);
        }
    }

    private boolean collectSubtreeCollapseInformation(SearchServerQuery query, List<String> queryTerms, BoolClause.Occur occur) {
        if (query instanceof TermQuery) {
            final TermQuery tq = (TermQuery) query;
            if (!(tq.term instanceof String))
                return false;
            queryTerms.add(tq.term.toString());
            return true;
        } else if (query instanceof MatchQuery) {
            queryTerms.add(((MatchQuery) query).query);
            return true;
        } else if (query instanceof MultiMatchQuery) {
            final MultiMatchQuery mmq = (MultiMatchQuery) query;
            if (mmq.type == phrase)
                return false;
            queryTerms.add(mmq.query);
            return true;
        } else if (!(query instanceof BoolQuery)) {
            return false;
        }
        BoolQuery q = (BoolQuery) query;
        BoolClause.Occur thisOccur = null;
        boolean ret = true;
        for (BoolClause c : q.clauses) {
            if (thisOccur == MUST_NOT || thisOccur == FILTER)
                return false;
            if (thisOccur == null)
                thisOccur = c.occur;
            if (thisOccur != c.occur)
                return false;
            if (occur != null && thisOccur != occur)
                return false;
            for (SearchServerQuery cq : c.queries) {
                ret = ret && collectSubtreeCollapseInformation(cq, queryTerms, thisOccur);
            }
        }
        return ret;
    }

    @Override
    public void exitNegation(ScicopiaParser.NegationContext ctx) {
        collapseDeepMatch(BoolClause.Occur.MUST_NOT);
    }

    @Override
    public void exitToken(ScicopiaParser.TokenContext ctx) {
        int tokenStart = ctx.start.getStartIndex();
        int tokenEnd = ctx.stop.getStopIndex() + 1;
        boolean isPhrase = ctx.quotes() != null;
        if (isPhrase) {
            ++tokenStart;
            --tokenEnd;
        }
        final QueryToken qt = queryTokens.get(Range.between(tokenStart, tokenEnd));
        if (qt == null)
            throw new IllegalStateException("Could not find a query token with offsets " + tokenStart + "-" + tokenEnd);
        switch (qt.getInputTokenType()) {
            case KEYWORD:
                final MultiMatchQuery q = new MultiMatchQuery();
                q.fields = fieldNames;
                q.operator = "and";
                q.query = qt.getOriginalValue();
                if (isPhrase)
                    q.type = MultiMatchQuery.Type.phrase;
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
                MultiMatchQuery matchPhraseQuery = new MultiMatchQuery();
                matchPhraseQuery.fields = fieldNames;
                matchPhraseQuery.query = concept.getPreferredName();
                matchPhraseQuery.type = phrase;
                namePhraseQueries.add(matchPhraseQuery);
                List<String> synonyms = concept.getSynonyms().size() >= MAX_EXPANSION_SIZE ? concept.getSynonyms().subList(0, MAX_EXPANSION_SIZE + 1) : concept.getSynonyms();
                synonyms.stream().map(s -> {
                    MultiMatchQuery mpq = new MultiMatchQuery();
                    mpq.fields = fieldNames;
                    mpq.query = s;
                    mpq.type = phrase;
                    return mpq;
                }).forEach(namePhraseQueries::add);

                BoolClause boolClause = new BoolClause();
                boolClause.occur = BoolClause.Occur.SHOULD;
                boolClause.queries = namePhraseQueries;
                boolQuery.addClause(boolClause);
                query = boolQuery;
                break;
            case EXPANSION_WORDS:
                MultiMatchQuery matchQuery = new MultiMatchQuery();
                matchQuery.fields = fieldNames;
                matchQuery.operator = "or";
                synonyms = concept.getSynonyms().size() >= MAX_EXPANSION_SIZE ? concept.getSynonyms().subList(0, MAX_EXPANSION_SIZE + 1) : concept.getSynonyms();
                matchQuery.query = concept.getPreferredName() + " "
                        + synonyms.stream().collect(Collectors.joining(" "));
                query = matchQuery;
                break;
            case ID:
                MultiMatchQuery termQuery = new MultiMatchQuery();
                termQuery.fields = fieldNames;
                termQuery.query = concept.getId();
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
                    MultiMatchQuery mpq = new MultiMatchQuery();
                    mpq.fields = fieldNames;
                    mpq.query = s;
                    mpq.type = phrase;
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
                MultiMatchQuery matchQuery = new MultiMatchQuery();
                matchQuery.fields = fieldNames;
                matchQuery.query = allNames;
                matchQuery.operator = "or";
                query = matchQuery;
                break;
            }
            case ID:
                MultiMatchQuery termsQuery = new MultiMatchQuery();
                termsQuery.query = concepts.stream().map(IConcept::getId).collect(Collectors.joining(" "));
                termsQuery.operator = "or";
                termsQuery.fields = fieldNames;
                query = termsQuery;
                break;
            default:
                throw new IllegalArgumentException(
                        "Concept translation mode \"" + conceptTranslation + "\" is currently not supported.");

        }
        return query;
    }
}
