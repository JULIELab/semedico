package de.julielab.semedico.core.services.query;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultiset;
import de.julielab.java.utilities.spanutils.OffsetMap;
import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.TopicTag;
import de.julielab.semedico.core.search.query.QueryAnalysis;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IServiceReconfigurationHub;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.services.interfaces.ReconfigurableService;
import de.julielab.semedico.core.util.ReconfigurationSymbolProvider;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConceptRecognitionService implements IConceptRecognitionService, ReconfigurableService {
    /**
     * Maximal number of terms assigned to an ambigue String in the query.
     */
    public static final int DEFAULT_MAX_AMBIGUE_TERMS = 1000;
    private static Logger logger = LoggerFactory.getLogger(ConceptRecognitionService.class);
    private DisambiguatingRangeChunker chunker;
    private IConceptService termService;
    private QueryAnalysis queryAnalysis;

    /**
     * Recognizes terms in (adjunct) text tokens. If <tt>prioritizeEvents</tt>
     * is set to <tt>true</tt>, event terms overlapping other terms will be
     * prioritized, even if they are shorter than other chunking possibilities.
     *
     * @param chunker     The Chunker to use.
     * @param termService The TermService to use.
     */
    public ConceptRecognitionService(DisambiguatingRangeChunker chunker, IConceptService termService, SymbolSource symbolSource) {
        this.chunker = chunker;
        this.termService = termService;
        this.configure(new ReconfigurationSymbolProvider(symbolSource));
    }

    @Override
    public void configure(SymbolProvider symbolProvider) {
        getEnum(SemedicoSymbolConstants.QUERY_ANALYSIS, symbolProvider, QueryAnalysis.class)
                .ifPresent(this::setQueryAnalysis);
    }

    private void setQueryAnalysis(QueryAnalysis analysis) {
        logger.debug("Setting query analysis to {}", analysis);
        this.queryAnalysis = analysis;
    }

    @PostInjection
    public void startupService(IServiceReconfigurationHub reconfigurationHub) {
        reconfigurationHub.registerService(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.julielab.semedico.core.services.interfaces.IConceptRecognitionService
     * #recognizeTerms(java.util.List, int)
     */
    @Override
    public List<QueryToken> recognizeTerms(List<QueryToken> tokens) {
        List<QueryToken> returnedTokens = new ArrayList<>();
        List<QueryToken> textTokens = new ArrayList<>();
        // Idea: it doesn't make sense to try and recognize concepts for query
        // tokens that have been user selected as a concept or have been
        // selected to be searched verbatim, i.e. as keyword or phrase. Thus, we
        // only find concepts in continuous sequences of query tokens that are
        // freetext tokens.
        // Thus, the "dontAnalyse" indicator is set to true if we encounter a
        // query token that should not undergo concept recognition. Then, all
        // previous freetext query tokens, stored in the textTokens list, are
        // taken together (combined) and
        // analyzed.
        for (QueryToken qt : tokens) {
            // TODO as soon as we have token input, there should be types
            // indicating that a token is fixed because the
            // user has already determined what the token should have for a
            // term. This also means that the combined
            // tokens are then additionally interrupted by those fixed tokens.

            // if the input token type already fixes the meaning of the
            // QueryToken, don't try to recognize terms in it
            boolean dontAnalyse = false;
            switch (qt.getInputTokenType()) {
                case TOPIC_TAG:
                    qt.setConceptList(Collections.singletonList(new TopicTag(qt.getOriginalValue())));
                case AMBIGUOUS_CONCEPT:
                case CONCEPT:
                case KEYWORD:
                    dontAnalyse = true;
                    break;
                default:
                    // if the input token type did not fix the QueryToken, then its
                    // lexer type still might (e.g. boolean operators)
                    switch (qt.getType()) {
                        // Collect adjunct text tokens.
                        case ALPHA:
                        case ALPHANUM:
                        case APOSTROPHE:
                        case NUM:
                        case COMPOUND:
                            textTokens.add(qt);
                            break;
                        case PHRASE:
                            qt.setInputTokenType(TokenType.KEYWORD);
                            dontAnalyse = true;
                            break;
                        case DASH:
                            // Dash expressions (e.g. il-2) could be concepts but could
                            // also be meant rather as a phrase. We check if the token
                            // is fully recognized as a single concept token. If not,
                            // only a part has been tagged as a concept which we don't
                            // want (e.g. when the user searches for water-level, we
                            // shouldn't search just for "water").
                            OffsetMap<QueryToken> conceptAnalyzedPhrase = new OffsetMap<>();
                            recognizeWithDictionary(qt.getOriginalValue(), conceptAnalyzedPhrase, 0);
                            if (conceptAnalyzedPhrase.size() == 1) {
                                QueryToken conceptToken = conceptAnalyzedPhrase.get(0);
                                // the whole token must be recognized as a single concept, otherwise we prohibit the analysis
                                if (conceptToken.getOriginalValue().length() == qt.getOriginalValue().length())
                                    textTokens.add(qt);
                                else {
                                    dontAnalyse = true;
                                    qt.setInputTokenType(TokenType.KEYWORD);
                                }
                            }
                            if (conceptAnalyzedPhrase.size() != 1 || (conceptAnalyzedPhrase.size() == 1 && conceptAnalyzedPhrase
                                    .get(0).getOriginalValue().length() != qt.getOriginalValue().length())) {
                                dontAnalyse = true;
                                qt.setInputTokenType(TokenType.KEYWORD);
                            }
                            break;
                        case IRI:
                            break;
                        // A non-text token was found.
                        default:
                            dontAnalyse = true;
                            break;
                    }
                    break;
            }
            if (dontAnalyse) {
                // Text tokens that were collected before are now (perhaps)
                // combined and matched to longest dictionary entries.
                if (!textTokens.isEmpty()) {
                    returnedTokens.addAll(combineAndRecognize(textTokens));
                    textTokens.clear();
                }
                returnedTokens.add(qt);
            }
        }
        if (!textTokens.isEmpty()) {
            returnedTokens.addAll(combineAndRecognize(textTokens));
            textTokens.clear();
        }

        return returnedTokens;
    }

    /**
     * Combine text tokens to a String and try to recognize terms in this
     * String. Longest matches, i.e. longest possible combinations of continuous
     * text tokens that can be matched to a term, will be returned. If a token /
     * token combination can be matched to multiple terms it will contain all of
     * them.<br>
     *
     * @param textTokens Original text tokens for a (part of the) query String.
     * @return New text tokens, some of which may be a combination of the
     * original ones. Will contain (possibly multiple) terms.
     */
    private List<QueryToken> combineAndRecognize(List<QueryToken> textTokens) {
        StringBuilder queryPart = new StringBuilder();
        List<QueryToken> originalTokens = new ArrayList<>();
        List<QueryToken> returnedTokens = new ArrayList<>();

        // Concatenate continuous text tokens unless they are phrases. The
        // resulting (parts of the) query string will be tokenized a second time
        // for term recognition.
        for (QueryToken qt : textTokens) {
            if (qt != null && qt.getOriginalValue() != null) {
                queryPart = queryPart.append(qt.getOriginalValue()).append(" ");
                originalTokens.add(qt);
            }
        }
        if (queryPart.length() > 0) {
            returnedTokens.addAll(recognizeTerms(queryPart.toString().trim(), originalTokens));
        }
        return returnedTokens;
    }

    /**
     * Tokenizes and recognizes terms in a (part of the) query String.
     *
     * @param query       (Part of the) query String.
     * @param lexerTokens The tokens from the first run of the lexer for this (part of
     *                    the) query String. Used to compare with new tokens in order to
     *                    determine keywords (i.e. non-matches).
     * @return A sorted list of tokens for the (part of the) query String.
     */
    private Collection<QueryToken> recognizeTerms(
            String query, List<QueryToken> lexerTokens) {
        OffsetMap<QueryToken> termTokens = new OffsetMap<>();

        // the original query string is not scanned for terms as a whole, but
        // only as spans between special tokens like
        // AND, OR and phrases. Thus, the term tokens have offsets relative to
        // their respective snippet, not to the
        // whole query. This must be adjusted when doing dictionary matching.
        int originalOffset = 0;
        if (!lexerTokens.isEmpty()) {
            originalOffset = lexerTokens.get(0).getBegin();
        }
        if (queryAnalysis == QueryAnalysis.CONCEPTS)
            recognizeWithDictionary(query, termTokens, originalOffset);
        mapKeywords(termTokens, lexerTokens);

        // mark the new QueryTokens as being the result of automatic analysis
        // rather than user selection
        for (QueryToken qt : termTokens.values()) {
            qt.setUserSelected(false);
        }

        debugRecognitionState(termTokens.values());

        List<QueryToken> returnedTokens = rearrangeTerms(termTokens.values());
        Collections.sort(returnedTokens, new BeginOffsetComparator());
        returnedTokens = mergeTokens(returnedTokens, lexerTokens);

        return returnedTokens;
    }

    /**
     * Merge tokens containing different concepts for the same (ambiguous)
     * String in the query to only one token containing multiple terms.
     *
     * @param tokens A sorted list of tokens, some of which may belong to the same
     *               String in the query.
     * @param lexerTokens
     * @return A list of tokens, each one belonging to different Strings in the
     * query.
     */
    private List<QueryToken> mergeTokens(List<QueryToken> tokens, List<QueryToken> lexerTokens) {
        OffsetMap<QueryToken> lexerTokensOffsetMap = new OffsetMap<>(lexerTokens);
        List<QueryToken> returnedTokens = new ArrayList<>();

        ArrayList<IConcept> concepts = new ArrayList<>();
        QueryToken currentToken = tokens.get(0);
        int currentOffset = currentToken.getBegin();
        // For now there will be at most one term in each token. So only take
        // the
        // first one in the list.
        if (!currentToken.getConceptList().isEmpty())
            concepts.add(currentToken.getConceptList().get(0));

        for (int i = 1; i < tokens.size(); i++) {
            QueryToken nextToken = tokens.get(i);
            int nextOffset = nextToken.getBegin();
            if (nextOffset == currentOffset) {
                // Don't add "ambiguous keywords", that doesn't make sense.
                IConcept foundConcept = null;
                if (!nextToken.getConceptList().isEmpty())
                    foundConcept = nextToken.getConceptList().get(0);
                if (null != foundConcept && (concepts.isEmpty() || foundConcept.getConceptType() != ConceptType.KEYWORD)) {
                    concepts.add(foundConcept);
                }
            } else {
                QueryToken newToken = new QueryToken(currentToken.getBegin(), currentToken.getEnd());
                newToken.setType(lexerTokensOffsetMap.getLargestOverlapping(newToken.getOffsets()).getType());
                newToken.setInputTokenType(currentToken.getInputTokenType());
                newToken.setOriginalValue(currentToken.getOriginalValue());
                newToken.setMatchedSynonym(currentToken.getMatchedSynonym());
                // TODO: What do we do with the score when tokens containing
                // different terms are merged? Are the scores different at all?
                // They have actually already fulfilled their role when the
                // maximal number of ambigue terms was selected...
                // newToken.setScore(currentToken.getScore());
                for (IConcept term : concepts) {
                    newToken.addConceptToList(term);
                }
                if (newToken.getConceptList().size() > 1)
                    newToken.setInputTokenType(ITokenInputService.TokenType.AMBIGUOUS_CONCEPT);
                returnedTokens.add(newToken);

                currentToken = nextToken;
                currentOffset = nextOffset;
                concepts.clear();
                if (!currentToken.getConceptList().isEmpty())
                    concepts.add(currentToken.getConceptList().get(0));
            }
        }

        QueryToken newToken = new QueryToken(currentToken.getBegin(), currentToken.getEnd());
        newToken.setType(lexerTokensOffsetMap.getLargestOverlapping(newToken.getOffsets()).getType());
        newToken.setInputTokenType(currentToken.getInputTokenType());
        newToken.setOriginalValue(currentToken.getOriginalValue());
        newToken.setMatchedSynonym(currentToken.getMatchedSynonym());

        for (IConcept term : concepts) {
            newToken.addConceptToList(term);
        }

        if (newToken.getConceptList().size() > 1)
            newToken.setInputTokenType(ITokenInputService.TokenType.AMBIGUOUS_CONCEPT);
        returnedTokens.add(newToken);

        logger.debug("Number of tokens after merging: " + returnedTokens.size());
        return returnedTokens;
    }

    /**
     * Find dictionary matches in the query String.
     *  @param query          The query String.
     * @param tokens         Collection to which the tokens are added.
     * @param originalOffset The character offset of <tt>query</tt> relative to the whole
 *                       original query when only a query snippet is to be tagged for
     */
    private void recognizeWithDictionary(String query, OffsetMap<QueryToken> tokens, int originalOffset) {
        // Scan the query for Strings that occur in the
        // dictionary.
        logger.debug("Chunking (part of) query String: {}", query);
//		CollectingSetListener listener = new CollectingSetListener();
        chunker.reset();
        chunker.match(query);
        Multimap<Range<Integer>, String> matches = chunker.getMatches();
        Collection<QueryToken> chunkTokens = new ArrayList<>();
        logger.debug("Number of initial chunks: {}", matches.size());
        for (Map.Entry<Range<Integer>, String> entry : matches.entries()) {
            Range<Integer> range = entry.getKey();
            int start = range.getMinimum();
            int end = range.getMaximum();
            String termId = entry.getValue();
            logger.debug("Chunk {}, {}", termId, query.substring(start, end));

            QueryToken termToken = new QueryToken(start, end);
            termToken.setInputTokenType(TokenType.CONCEPT);
            IConcept concept;

            concept = termService.getTermSynchronously(termId);
            if (concept == null) {
                logger.debug(
                        "Dictionary matched the term {} with ID {}, but no such term could be found in the database or the database is down.",
                        query.substring(start, end), termId);
                continue;
            }
            if (concept.getFacets().isEmpty()) {
                logger.debug(
                        "Term with ID {} has no facets, possible because it belongs to an inactive facet. Skipping this term.", concept.getId());
                continue;
            }

            termToken.addConceptToList(concept);
            chunkTokens.add(termToken);
        }

        // For all partly overlapping tokens or token combinations filter out
        // longest matches and take only these.
        Collection<QueryToken> filteredTokens = filterLongestMatches(chunkTokens);
        logger.debug("Number of tokens after filtering longest matches: {}", filteredTokens.size());

        // For each (possibly ambiguous) String in the query select only a
        // certain
        // number of the highest ranking terms (i.e. select multiple tokens
        // accordingly).
        Multimap<Integer, QueryToken> tokensByStart = HashMultimap.create();
        for (QueryToken qt : filteredTokens) {
            tokensByStart.put(qt.getBegin(), qt);
        }

        for (Integer start : TreeMultiset.create(tokensByStart.keySet())) {
            List<QueryToken> sortedTokens = new ArrayList<>();
            Collection<QueryToken> tokenOnIndex = tokensByStart.get(start);
            sortedTokens.addAll(tokenOnIndex);

            Iterator<QueryToken> tokenIterator = sortedTokens.iterator();
            for (int i = 0; tokenIterator.hasNext() && i < DEFAULT_MAX_AMBIGUE_TERMS; i++) {
                QueryToken token = tokenIterator.next();
                String originalValue = query.substring(token.getBegin(), token.getEnd());
                token.setOriginalValue(originalValue);
                // adjust offsets to the original query (because we have here
                // possibly only a snippet of the whole
                // query).
                token.setBegin(token.getBegin() + originalOffset);
                token.setEnd(token.getEnd() + originalOffset);
                tokens.put(token);
            }

            // for non-ambiguous tokens we want to set that
            // designation of the determined concept to display for the user
            // which closest resembles the user's input
            String bestOccurrence = null;
            int bestScore = Integer.MAX_VALUE;
            // We prefer Synonyms with more uppercase letters
            int maxNumUpperLetters = -1;
            if (sortedTokens.size() == 1) {
                QueryToken queryToken = sortedTokens.get(0);
                IConcept concept = queryToken.getConceptList().get(0);
                List<String> occurrences = concept.getOccurrences();
                String upperCaseOriginalValue = queryToken.getOriginalValue().toLowerCase(Locale.ENGLISH);
                for (String occurrence : occurrences) {
                    int levenshteinDistance = StringUtils.getLevenshteinDistance(occurrence.toLowerCase(Locale.ENGLISH),
                            upperCaseOriginalValue);
                    int numUpperLetters = 0;
                    for (int i = 0; i < occurrence.length(); ++i) {
                        if (Character.isUpperCase(occurrence.charAt(i)))
                            ++numUpperLetters;
                    }
                    if (levenshteinDistance < bestScore && numUpperLetters > maxNumUpperLetters) {
                        bestOccurrence = occurrence;
                        bestScore = levenshteinDistance;
                    }
                }
                queryToken.setMatchedSynonym(bestOccurrence);
            }
        }

        logger.debug("Number of tokens after selecting only highest ranking terms: {}", tokens.size());
    }

    /**
     * Mark as keywords everything that could not be matched with the
     * dictionary.
     *  @param termTokens  The tokens created by matching with the dictionary.
     * @param lexerTokens The original tokens from the first run of the lexer.
     */
    private void mapKeywords(OffsetMap<QueryToken> termTokens, List<QueryToken> lexerTokens) {
        int keyWords = 0;

        for (QueryToken qt : lexerTokens) {
            final NavigableMap<Range<Integer>, QueryToken> overlappingConcepts = termTokens.getOverlapping(qt);
            if (overlappingConcepts.isEmpty()) {
                qt.setInputTokenType(TokenType.KEYWORD);
                termTokens.put(qt);
                keyWords++;
            }
        }
        logger.debug("Number of keywords: " + keyWords);
    }

    /**
     * There might be multiple tokens (containing different terms) for the same
     * offsets, i.e. for the same ambiguous String in the query. What follows is
     * some rearranging of these terms.
     *
     * @param tokens List of tokens.
     * @return List of tokens with rearranged terms.
     */
    private List<QueryToken> rearrangeTerms(Collection<QueryToken> tokens) {
        // TODO how much sense does this method make? Does it produce issues when the same string is repeated in the query?
        Multimap<String, QueryToken> tokenMap = LinkedHashMultimap.create();
        // Map multiple QueryTokens related to the same query String.
        for (QueryToken qt : tokens) {
            tokenMap.put(qt.getOriginalValue(), qt);
        }

        // Remove duplicates of tokens that are equal according to hashCode()
        // and equals() (unification by using a HashMap).
        removeDuplicateTokens(tokenMap);

        List<QueryToken> returnedTokens = new ArrayList<>();
        for (String queryString : tokenMap.keySet()) {
            for (QueryToken qt : tokenMap.get(queryString)) {
                returnedTokens.add(qt);
            }
        }
        logger.debug("Number of tokens after rearranging terms: " + returnedTokens.size());
        debugRecognitionState(returnedTokens);
        return returnedTokens;
    }

    private void debugRecognitionState(Collection<QueryToken> returnedTokens) {
        if (logger.isDebugEnabled()) {
            logger.debug("Current term recognition state:");
            for (QueryToken qt : returnedTokens) {
                String mappedTo = null;
                List<String> termStringList = new ArrayList<>();
                for (IConcept term : qt.getConceptList())
                    termStringList.add(term.getPreferredName() + ": " + term.getId());
                mappedTo = StringUtils.join(termStringList, ", ");
                if (!termStringList.isEmpty())
                    logger.debug("{} --> {} ({})",
                            new Object[]{qt.getOriginalValue(), mappedTo, qt.getInputTokenType()});
                else
                    logger.debug("{} --> {} ({})",
                            new Object[]{qt.getOriginalValue(), qt.getOriginalValue(), qt.getInputTokenType()});
            }
        }
    }

    /**
     * Removes duplicates of tokens that are equal according to hashCode() and
     * equals() (unification by using a HashMap).
     *
     * @param tokenMap The query token map that maps query (multi-)words to
     *                 QueryTokens.
     */
    private void removeDuplicateTokens(Multimap<String, QueryToken> tokenMap) {
        Multimap<String, QueryToken> duplicates = HashMultimap.create();
        Set<QueryToken> alreadySeen = new HashSet<QueryToken>();
        for (Map.Entry<String, QueryToken> entry : tokenMap.entries()) {
            if (alreadySeen.contains(entry.getValue())) {
                duplicates.put(entry.getKey(), entry.getValue());
            } else {
                alreadySeen.add(entry.getValue());
            }
        }

        for (Map.Entry<String, QueryToken> entry : duplicates.entries()) {
            tokenMap.remove(entry.getKey(), entry.getValue());
        }
    }



    /**
     * Filter out those tokens that constitute longest matches.
     *
     * @param tokens Tokens to filter.
     * @return All tokens that constitute longest matches.
     */
    private Collection<QueryToken> filterLongestMatches(Collection<QueryToken> tokens) {
        Collection<QueryToken> filteredTokens = new ArrayList<QueryToken>(tokens);
        for (QueryToken token : tokens) {
            int begin = token.getBegin();
            int end = token.getEnd();
            List<QueryToken> longerTokens = containsLongerTokenInSpan(begin, end, tokens);
            boolean remove = longerTokens.size() > 0;
            if (remove)
                filteredTokens.remove(token);
        }
        return filteredTokens;
    }

    /**
     * Tests if there is a token which overlaps with the span and is bigger than
     * it.
     *
     * @param begin  Begin of the span.
     * @param end    End of the span.
     * @param tokens Tokens to test.
     * @return True if there is a token which overlaps with the span and is
     * bigger than it.
     */
    private List<QueryToken> containsLongerTokenInSpan(int begin, int end, Collection<QueryToken> tokens) {
        Range<Integer> span = Range.between(begin, end);
        int lengthSpan = end - begin;
        List<QueryToken> longer = new ArrayList<>();
        for (QueryToken qt : tokens) {
            Range<Integer> qtRange = Range.between(qt.getBegin(), qt.getEnd());
            int lengthQtRange = qt.getEnd() - qt.getBegin();
            boolean isLonger = qtRange.isOverlappedBy(span) && (lengthQtRange > lengthSpan);
            if (isLonger)
                longer.add(qt);
        }
        return longer;
    }

    private class BeginOffsetComparator implements Comparator<QueryToken> {
        @Override
        public int compare(QueryToken token1, QueryToken token2) {
            return token1.getBegin() - token2.getBegin();
        }
    }

    private class ScoreComparator implements Comparator<QueryToken> {
        @Override
        public int compare(QueryToken token1, QueryToken token2) {
            double difference = token2.getScore() - token1.getScore();
            if (difference < 0)
                return -1;
            else if (difference > 0)
                return 1;
            else
                return 0;
        }
    }
}
