package de.julielab.semedico.core.services.query;

import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.ALPHANUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.APOSTROPHE;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.CJ;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.NUM;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.PHRASE;
import static de.julielab.semedico.core.services.query.QueryTokenizerImpl.WILDCARD_TOKEN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.TreeMultiset;

import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITermRecognitionService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class TermRecognitionService implements ITermRecognitionService {
	private static Logger logger = LoggerFactory.getLogger(TermRecognitionService.class);

	private static Chunker chunker;
	private static ITermService termService;

	/**
	 * Maximal number of terms assigned to an ambigue String in the query.
	 */
	public static final int DEFAULT_MAX_AMBIGUE_TERMS = 1000;

	/**
	 * Recognizes terms in (adjunct) text tokens. If <tt>prioritizeEvents</tt>
	 * is set to <tt>true</tt>, event terms overlapping other terms will be
	 * prioritized, even if they are shorter than other chunking possibilities.
	 * 
	 * @param chunker
	 *            The Chunker to use.
	 * @param termService
	 *            The TermService to use.
	 * @param facetService
	 *            The FacetService to use.
	 * @param eventRecognition
	 *            Whether terms that represent events - as recognized in
	 *            document text via JReX - should be prioritized.
	 */
	public TermRecognitionService(Chunker chunker, ITermService termService) {
		this.chunker = chunker;
		this.termService = termService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.interfaces.ITermRecognitionService
	 * #recognizeTerms(java.util.List, int)
	 */
	@Override
	public List<QueryToken> recognizeTerms(List<QueryToken> tokens, long sessionId) throws IOException {
		List<QueryToken> returnedTokens = new ArrayList<QueryToken>();
		List<QueryToken> textTokens = new ArrayList<QueryToken>();
		// TODO the 'eventQueries' parameter is not used in any subcall; the
		// eventQueries parameter and
		// 'termIdAndFacetId' will be replaced by 'user-fixed' query token
		// resulting from suggestions.
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
				case ALPHANUM:
				case APOSTROPHE:
				case NUM:
				case CJ:
					// case PHRASE:
					// case DASH:
					// case ALPHANUM_EMBEDDED_PAR:
				case WILDCARD_TOKEN:
					textTokens.add(qt);
					break;
				case PHRASE:
					qt.setInputTokenType(TokenType.KEYWORD);
					dontAnalyse = true;
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
					returnedTokens.addAll(combineAndRecognize(textTokens, sessionId));
					textTokens.clear();
				}
				returnedTokens.add(qt);
			}
		}
		if (!textTokens.isEmpty()) {
			returnedTokens.addAll(combineAndRecognize(textTokens, sessionId));
			textTokens.clear();
		}
		// if (!eventRecognition) {
		// // If we don't do event recognition here, event triggers are just
		// // normal terms and should not be interpreted
		// // as event triggers.
		// for (QueryToken qt : returnedTokens) {
		// if (qt.isEventTrigger())
		// qt.setType(ALPHANUM);
		// }
		// }

		return returnedTokens;
	}

	/**
	 * Combine text tokens to a String and try to recognize terms in this
	 * String. Longest matches, i.e. longest possible combinations of continuous
	 * text tokens that can be matched to a term, will be returned. If a token /
	 * token combination can be matched to multiple terms it will contain all of
	 * them.<br>
	 * 
	 * @param textTokens
	 *            Original text tokens for a (part of the) query String.
	 * @return New text tokens, some of which may be a combination of the
	 *         original ones. Will contain (possibly multiple) terms.
	 */
	private List<QueryToken> combineAndRecognize(List<QueryToken> textTokens, long sessionId) throws IOException {
		StringBuilder queryPart = new StringBuilder();
		List<QueryToken> originalTokens = new ArrayList<QueryToken>();
		List<QueryToken> returnedTokens = new ArrayList<QueryToken>();

		// Concatenate continuous text tokens unless they are phrases. The
		// resulting (parts of the) query string will be tokenized a second time
		// for term recognition.
		for (QueryToken qt : textTokens) {
			if (qt != null && qt.getOriginalValue() != null) {
				// if (qt.getType() != PHRASE) {
				queryPart = queryPart.append(qt.getOriginalValue()).append(" ");
				originalTokens.add(qt);
			}
		}
		if (queryPart.length() > 0)
			returnedTokens.addAll(recognizeTerms(queryPart.toString().trim(), originalTokens, sessionId));

		return returnedTokens;
	}

	/**
	 * Tokenizes and recognizes terms in a (part of the) query String.
	 * 
	 * @param query
	 *            (Part of the) query String.
	 * @param lexerTokens
	 *            The tokens from the first run of the lexer for this (part of
	 *            the) query String. Used to compare with new tokens in order to
	 *            determine keywords (i.e. non-matches).
	 * @param termIdAndFacetId
	 *            Term id and facet id as chosen by the user. Else use
	 *            <code>null</code>.
	 * @param eventQueries
	 * @return A sorted list of tokens for the (part of the) query String.
	 */
	static public Collection<QueryToken> recognizeTerms( // TODO war private
			String query, List<QueryToken> lexerTokens, long sessionId) {
		List<QueryToken> termTokens = new ArrayList<QueryToken>();
		// the original query string is not scanned for terms as a whole, but
		// only as spans between special tokens like
		// AND, OR and phrases. Thus, the term tokens have offsets relative to
		// their respective snippet, not to the
		// whole query. This must be adjusted when doing dictionary matching.
		int originalOffset = 0;
		if (lexerTokens.size() > 0) {
			originalOffset = lexerTokens.get(0).getBeginOffset();
		}
		recognizeWithDictionary(query, termTokens, originalOffset, sessionId);
		mapKeywords(termTokens, lexerTokens);

		// mark the new QueryTokens as being the result of automatic analysis
		// rather than user selection
		for (QueryToken qt : termTokens) {
			qt.setUserSelected(false);
		}

		debugRecognitionState(termTokens);

		List<QueryToken> returnedTokens = rearrangeTerms(termTokens);
		Collections.sort(returnedTokens, new BeginOffsetComparator());
		returnedTokens = mergeTokens(returnedTokens);

		return returnedTokens;
	}

	/**
	 * Merge tokens containing different terms for the same (ambigue) String in
	 * the query to only one token containing multiple terms.
	 * 
	 * @param tokens
	 *            A sorted list of tokens, some of which may belong to the same
	 *            String in the query.
	 * @return A list of tokens, each one belonging to different Strings in the
	 *         query.
	 */
	private static List<QueryToken> mergeTokens(List<QueryToken> tokens) {
		List<QueryToken> returnedTokens = new ArrayList<QueryToken>();

		ArrayList<IConcept> terms = new ArrayList<IConcept>();
		QueryToken currentToken = tokens.get(0);
		int currentOffset = currentToken.getBeginOffset();
		// For now there will be at most one term in each token. So only take
		// the
		// first one in the list.
		if (!currentToken.getTermList().isEmpty())
			terms.add(currentToken.getTermList().get(0));

		for (int i = 1; i < tokens.size(); i++) {
			QueryToken nextToken = tokens.get(i);
			int nextOffset = nextToken.getBeginOffset();
			if (nextOffset == currentOffset) {
				// Don't add "ambiguous keywords", that doesn't make sense.
				IConcept foundConcept = null;
				if (!nextToken.getTermList().isEmpty())
					foundConcept = nextToken.getTermList().get(0);
				if (null != foundConcept && (terms.isEmpty() || foundConcept.getConceptType() != ConceptType.KEYWORD)) {
					terms.add(foundConcept);
				}
			} else {
				QueryToken newToken = new QueryToken(currentToken.getBeginOffset(), currentToken.getEndOffset());
				newToken.setType(currentToken.getType());
				newToken.setInputTokenType(currentToken.getInputTokenType());
				newToken.setOriginalValue(currentToken.getOriginalValue());
				newToken.setMatchedSynonym(currentToken.getMatchedSynonym());
				// TODO: What do we do with the score when tokens containing
				// different terms are merged? Are the scores different at all?
				// They have actually already fulfilled their role when the
				// maximal number of ambigue terms was selected...
				// newToken.setScore(currentToken.getScore());
				for (IConcept term : terms) {
					newToken.addTermToList(term);
				}
				if (newToken.getTermList().size() > 1)
					newToken.setInputTokenType(ITokenInputService.TokenType.AMBIGUOUS_CONCEPT);
				returnedTokens.add(newToken);

				currentToken = nextToken;
				currentOffset = nextOffset;
				terms.clear();
				if (!currentToken.getTermList().isEmpty())
					terms.add(currentToken.getTermList().get(0));
			}
		}

		QueryToken newToken = new QueryToken(currentToken.getBeginOffset(), currentToken.getEndOffset());
		newToken.setType(currentToken.getType());
		newToken.setInputTokenType(currentToken.getInputTokenType());
		newToken.setOriginalValue(currentToken.getOriginalValue());
		newToken.setMatchedSynonym(currentToken.getMatchedSynonym());

		for (IConcept term : terms) {
			newToken.addTermToList(term);
		}

		if (newToken.getTermList().size() > 1)
			newToken.setInputTokenType(ITokenInputService.TokenType.AMBIGUOUS_CONCEPT);
		returnedTokens.add(newToken);

		logger.debug("Number of tokens after merging: " + returnedTokens.size());
		return returnedTokens;
	}

	/**
	 * Find dictionary matches in the query String.
	 * 
	 * @param query
	 *            The query String.
	 * @param tokens
	 *            Collection to which the tokens are added.
	 * @param originalOffset
	 *            The character offset of <tt>query</tt> relative to the whole
	 *            original query when only a query snippet is to be tagged for
	 *            terms.
	 * @param sessionId
	 */
	private static void recognizeWithDictionary(String query, Collection<QueryToken> tokens, int originalOffset,
			long sessionId) {
		// Scan the query for Strings that occur in the
		// dictionary.
		logger.debug("Chunking (part of) query String: " + query);
		Chunking chunking = chunker.chunk(query);
		Collection<QueryToken> chunkTokens = new ArrayList<QueryToken>();
		Collection<QueryToken> stringTermTokens = new ArrayList<QueryToken>();
		logger.debug("Number of initial chunks: " + chunking.chunkSet().size());
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String termId = chunk.type();

			QueryToken termToken = new QueryToken(start, end);
			termToken.setInputTokenType(TokenType.CONCEPT);
			IConcept term = null;
			if (termService.isStringTermID(termId)) {
				stringTermTokens.add(termToken);
			} else {
				term = termService.getTermSynchronously(termId);
				if (term == null) {
					logger.debug(
							"Dictionary matched the term {} with ID {}, but no such term could be found in the database or the database is down.",
							query.substring(start, end), termId);
					continue;
				}
				if (term.getFacets().isEmpty()) {
					logger.debug(
							"Term with ID {} has no facets, possible because it belongs to an inactive facet. Skipping this term.");
					continue;
				}
				// TODO what to do with core terms?
				// if (!eventRecognition) {
				// // in not-event-recognition mode, the event core types don't
				// // have meaning and should be treated as
				// // keywords
				// if (term.getConceptType() == ConceptType.CORE) {
				// CoreTerm ct = (CoreTerm) term;
				// if (ct.getCoreTermType() ==
				// CoreTermType.ANY_MOLECULAR_INTERACTION) {
				// continue;
				// }
				// }
				// }
				termToken.addTermToList(term);
				chunkTokens.add(termToken);
			}
			termToken.setScore(chunk.score());
		}

		// For all String terms, map the terms to another String representation,
		// e.g. author name canonicalization.
		if (stringTermTokens.size() > 0) {
			Collection<QueryToken> mappedQueryStringTerms = termService.mapQueryStringTerms(stringTermTokens,
					sessionId);
			logger.debug("Number of String term tokens: "
					+ (null != mappedQueryStringTerms ? mappedQueryStringTerms.size() : 0));
			if (null != mappedQueryStringTerms)
				chunkTokens.addAll(mappedQueryStringTerms);
		}

		// For all partly overlapping tokens or token combinations filter out
		// longest matches and take only these.
		Collection<QueryToken> filteredTokens = filterLongestMatches(chunkTokens);
		logger.debug("Number of tokens after filtering longest matches: " + filteredTokens.size());

		// For each (possibly ambiguous) String in the query select only a
		// certain
		// number of the highest ranking terms (i.e. select multiple tokens
		// accordingly).
		Multimap<Integer, QueryToken> tokensByStart = HashMultimap.create();
		for (QueryToken qt : filteredTokens) {
			tokensByStart.put(qt.getBeginOffset(), qt);
		}

		for (Integer start : TreeMultiset.create(tokensByStart.keySet())) {
			List<QueryToken> sortedTokens = new ArrayList<QueryToken>();
			Collection<QueryToken> tokenOnIndex = tokensByStart.get(start);
			sortedTokens.addAll(tokenOnIndex);
			Collections.sort(sortedTokens, new ScoreComparator());

			Iterator<QueryToken> tokenIterator = sortedTokens.iterator();
			for (int i = 0; tokenIterator.hasNext() && i < DEFAULT_MAX_AMBIGUE_TERMS; i++) {
				QueryToken token = tokenIterator.next();
				String originalValue = query.substring(token.getBeginOffset(), token.getEndOffset());
				token.setOriginalValue(originalValue);
				// adjust offsets to the original query (because we have here
				// possibly only a snippet of the whole
				// query).
				token.setBeginOffset(token.getBeginOffset() + originalOffset);
				token.setEndOffset(token.getEndOffset() + originalOffset);
				tokens.add(token);
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
				IConcept concept = queryToken.getTermList().get(0);
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

		logger.debug("Number of tokens after selecting only highest ranking terms: " + tokens.size());
	}

	/**
	 * Mark as keywords everything that could not be matched with the
	 * dictionary.
	 * 
	 * @param termTokens
	 *            The tokens created by matching with the dictionary.
	 * @param lexerTokens
	 *            The original tokens from the first run of the lexer.
	 */
	private static void mapKeywords(List<QueryToken> termTokens, List<QueryToken> lexerTokens) {
		int keyWords = 0;

		for (QueryToken qt : lexerTokens) {
			int begin = qt.getBeginOffset();
			int end = qt.getEndOffset();
			if (!containsTokenOverlappingSpan(begin, end, termTokens)) {
				qt.setInputTokenType(TokenType.KEYWORD);
				termTokens.add(qt);
				keyWords++;
			}
		}
		logger.debug("Number of keywords: " + keyWords);
	}

	/**
	 * There might be multiple tokens (containing different terms) for the same
	 * offsets, i.e. for the same ambigue String in the query. What follows is
	 * some rearranging of these terms.
	 * 
	 * @param tokens
	 *            List of tokens.
	 * @return List of tokens with rearranged terms.
	 */
	private static List<QueryToken> rearrangeTerms(List<QueryToken> tokens) {
		Multimap<String, QueryToken> tokenMap = LinkedHashMultimap.create();
		// Map multiple QueryTokens related to the same query String.
		for (QueryToken qt : tokens) {
			tokenMap.put(qt.getOriginalValue(), qt);
		}
		// If we have the same term multiple times within a
		// span, then both are part of the same
		// disjunction or conjunction, so duplicates make to sense.
		removeDuplicateTerms(tokenMap);

		List<QueryToken> returnedTokens = new ArrayList<QueryToken>();
		for (String queryString : tokenMap.keySet()) {
			for (QueryToken qt : tokenMap.get(queryString)) {
				returnedTokens.add(qt);
			}
		}
		logger.debug("Number of tokens after rearranging terms: " + returnedTokens.size());
		debugRecognitionState(returnedTokens);
		return returnedTokens;
	}

	protected static void debugRecognitionState(List<QueryToken> returnedTokens) {
		if (logger.isDebugEnabled()) {
			logger.debug("Current term recognition state:");
			for (QueryToken qt : returnedTokens) {
				String mappedTo = null;
				List<String> termStringList = new ArrayList<>();
				for (IConcept term : qt.getTermList())
					termStringList.add(term.getPreferredName() + ": " + term.getId());
				mappedTo = StringUtils.join(termStringList, ", ");
				if (!termStringList.isEmpty())
					logger.debug("{} --> {} ({})",
							new Object[] { qt.getOriginalValue(), mappedTo, qt.getInputTokenType() });
				else
					logger.debug("{} --> {} ({})",
							new Object[] { qt.getOriginalValue(), qt.getOriginalValue(), qt.getInputTokenType() });
			}
		}
	}

	private static void removeDuplicateTerms(Multimap<String, QueryToken> tokenMap) {
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
	 * Tests if there is at least a minimal overlap between the tokens and the
	 * span.
	 * 
	 * @param begin
	 *            Begin of the span.
	 * @param end
	 *            End of the span.
	 * @param tokens
	 *            Tokens to test.
	 * @return True if at least one token has at least one character inside the
	 *         span.
	 */
	private static boolean containsTokenOverlappingSpan(int begin, int end, Collection<QueryToken> tokens) {
		Range<Integer> span = Range.closed(begin, end);
		boolean overlap = false;
		for (QueryToken qt : tokens) {
			Range<Integer> qtRange = Range.closed(qt.getBeginOffset(), qt.getEndOffset());
			overlap |= qtRange.isConnected(span);
		}
		return overlap;
	}

	/**
	 * Filter out those tokens that constitute longest matches. If we are in
	 * {@link #eventRecognition} mode, terms that are event triggers are always
	 * preferred, regardless of length.
	 * 
	 * @param tokens
	 *            Tokens to filter.
	 * @return All tokens that constitute longest matches.
	 */
	static Collection<QueryToken> filterLongestMatches(Collection<QueryToken> tokens) {
		Collection<QueryToken> filteredTokens = new ArrayList<QueryToken>(tokens);
		for (QueryToken token : tokens) {
			int begin = token.getBeginOffset();
			int end = token.getEndOffset();
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
	 * @param begin
	 *            Begin of the span.
	 * @param end
	 *            End of the span.
	 * @param tokens
	 *            Tokens to test.
	 * @return True if there is a token which overlaps with the span and is
	 *         bigger than it.
	 */
	private static List<QueryToken> containsLongerTokenInSpan(int begin, int end, Collection<QueryToken> tokens) {
		Range<Integer> span = Range.closed(begin, end);
		int lengthSpan = end - begin;
		List<QueryToken> longer = new ArrayList<>();
		for (QueryToken qt : tokens) {
			Range<Integer> qtRange = Range.closed(qt.getBeginOffset(), qt.getEndOffset());
			int lengthQtRange = qt.getEndOffset() - qt.getBeginOffset();
			boolean isLonger = qtRange.isConnected(span) && (lengthQtRange > lengthSpan);
			if (isLonger)
				longer.add(qt);
		}
		return longer;
	}

	private static class BeginOffsetComparator implements Comparator<QueryToken> {
		@Override
		public int compare(QueryToken token1, QueryToken token2) {
			return token1.getBeginOffset() - token2.getBeginOffset();
		}
	}

	private static class ScoreComparator implements Comparator<QueryToken> {
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
