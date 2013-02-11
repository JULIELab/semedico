/** 
 * QueryDisambiguationService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 28.07.2008 
 * 
 * Modified by hellrich: Added some comments, minor refactoring, many methods still look 'strange'...
 **/

package de.julielab.semedico.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java_cup.runtime.Symbol;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultiset;

import de.julielab.parsing.QueryAnalyzer;
import de.julielab.parsing.QueryTokenizer;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Facet.SourceType;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.QueryPhrase;
import de.julielab.semedico.core.QueryToken;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

public class QueryDisambiguationService implements IQueryDisambiguationService {
	private ITermService termService;
	private final IFacetService facetService;

	public static final String DEFAULT_SNOWBALL_STEMMER = "English";
	public static final int DEFAULT_MAX_AMBIGUE_TERMS = 1000;
	private Chunker chunker;
	private QueryAnalyzer analyzer;
	private int maxAmbigueTerms;
	private double minMatchingScore;
	private Logger logger;

	private static final String PHRASE = "<PHRASE>";
	public static ScoreComparator SCORE_COMPARATOR = new ScoreComparator();
	public static BeginOffsetComparator BEGINN_OFFSET_COMPARATOR = new BeginOffsetComparator();

	public static final String PHRASES_INDEX_FIELD_NAME = "phrases";
	public static final String ID_INDEX_FIELD_NAME = "id";

	public static final int TEXT = 0;
	public static final int MAPPED_TEXT = 1;

	private static class ScoreComparator implements Comparator<QueryToken> {
		@Override
		public int compare(QueryToken token1, QueryToken token2) {
			double difference = token2.getScore() - token1.getScore(); // no
																		// epsilon?
			if (difference < 0)
				return -1;
			else if (difference > 0)
				return 1;
			else
				return 0;
		}
	}

	// oh java, where is they lambda...
	private static class BeginOffsetComparator implements
			Comparator<QueryToken> {
		@Override
		public int compare(QueryToken token1, QueryToken token2) {
			return token1.getBeginOffset() - token2.getBeginOffset();
		}
	}

	/**
	 * QueryDisambiguationService can be used to detect terms in query Strings
	 * 
	 * @param logger
	 * @param stopWords
	 * @param termService
	 * @param chunker
	 * @throws IOException
	 */
	public QueryDisambiguationService(Logger logger,
			IStopWordService stopWords, ITermService termService,
			IFacetService facetService, Chunker chunker) throws IOException {
		this.logger = logger;
		this.facetService = facetService;
		analyzer = new QueryAnalyzer(stopWords.getAsArray(),
				DEFAULT_SNOWBALL_STEMMER);
		this.termService = termService;
		this.chunker = chunker;
		maxAmbigueTerms = DEFAULT_MAX_AMBIGUE_TERMS;
	}

	/**
	 * Disambiguates a query, looking for terms.
	 * 
	 * @param query
	 *            String to disambiguate
	 * @param id
	 *            Id of a term chosen by user, use <code>null</code> otherwise
	 * @return A MultiMap, mapping Terms to their IDs
	 */
	// TODO Should actually just return a ParseTree which would then be the
	// global query structure for Semedico.
	public Multimap<String, TermAndPositionWrapper> disambiguateQuery(
			String query, Pair<String, String> termIdAndFacetId) {
		long time = System.currentTimeMillis();
		if (query == null || query.equals(""))
			return LinkedHashMultimap.create(); // empty

		List<QueryToken> tokens = getTokens(query, termIdAndFacetId);
//		for (QueryToken queryToken : tokens) {
//			System.out.println(queryToken.getOriginalValue());
//			System.out.println(queryToken.getValue());
//			
//		}
		Multimap<String, TermAndPositionWrapper> result = getResult(tokens);

		// lots of logging
		time = System.currentTimeMillis() - time;
		logger.debug("Extracted string to term mapping:");
		for (String queryString : result.keySet()) {
			logger.debug("{}\t->\t{}", queryString, StringUtils.join(
					Collections2.transform(result.get(queryString),
							new Function<TermAndPositionWrapper, String>() {
								@Override
								public String apply(TermAndPositionWrapper input) {
									return "[Name: "
											+ input.getTerm().getName()
											+ ", ID:" + input.getTerm().getId()
											+ "]";
								}
							}), ", "));
		}
		logger.info("disambiguateQuery() took {} ms", time);

		return result;
	}

	/**
	 * Refactored out of disambiguateQuery()
	 * 
	 * @param tokens
	 *            List of tokens to map
	 * @return MultiMap from tokens to terms
	 */
	private Multimap<String, TermAndPositionWrapper> getResult(
			List<QueryToken> tokens) {
		Multimap<String, TermAndPositionWrapper> result = LinkedHashMultimap
				.create();
		for (QueryToken queryToken : tokens) {
			result.put(queryToken.getOriginalValue(),
					new TermAndPositionWrapper(queryToken));
		}

		distributeTermsEvenlyAccrossFacets(result);
		filterNonFacetTerms(result);
		removeDuplicateTerms(result);
		return result;
	}

	/**
	 * Refactored out of disambiguateQuery()
	 * 
	 * @param query
	 *            String to disambiguate
	 * @param termIdAndFacetId
	 *            Id of a term chosen by user, use <code>null</code> otherwise
	 * @return A sorted list of tokens in the query
	 * @throws IOException
	 */
	private List<QueryToken> getTokens(String query,
			Pair<String, String> termIdAndFacetId) {
		ArrayList<QueryToken> tokens = new ArrayList<QueryToken>();
		try {
			if (termIdAndFacetId != null && !termIdAndFacetId.equals(""))
				mapDisambiguatedTerm(query, termIdAndFacetId, tokens);

			Collection<QueryPhrase> phrases = mapPhrases(query);
			mapDictionaryMatches(query, tokens, phrases);
			// mapIndexMatches(query, tokens, phrases); //hellrich: no clue what
			// this is about
			mapKeywords(query + " ", tokens, phrases);
			Collections.sort(tokens, BEGINN_OFFSET_COMPARATOR);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tokens;
	}

	/**
	 * Adds a token for a term with known id into an existing collection
	 * 
	 * @param query
	 *            Query which was identified as a term by the user
	 * @param termIdAndFacetId
	 *            .getLeft() Id given by user, may not be <code>null</code>
	 * @param tokens
	 *            Collection to which the token is added
	 */
	protected void mapDisambiguatedTerm(String query,
			Pair<String, String> termIdAndFacetId, Collection<QueryToken> tokens) {
		String termId = termIdAndFacetId.getLeft();
		if (termId != null && !termId.equals("")) {
			QueryToken token = new QueryToken(0, query.length(), query);
			token.setOriginalValue(query.substring(token.getBeginOffset(),
					token.getEndOffset()));

			Facet facet = facetService.getFacetById(Integer
					.parseInt(termIdAndFacetId.getRight()));
			if (facet == null)
				logger.error(
						"A term has been selected which supposedly belongs to the facet with ID {}; this facet could not be found.",
						termIdAndFacetId.getRight());

			SourceType facetType = facet.getSource().getType();
			IFacetTerm term = null;
			if (facetType.isTermSource()) {
				logger.debug("Fetching term with ID '{}' from term service.",
						termId);
				term = termService.getNode(termId);
			} else if (facetType.isStringTermSource()) {
				term = termService.getTermObjectForStringTermId(termId);
			}
			if (term != null)
				tokens.add(token);
			token.setTerm(term);
		}
	}

	protected Collection<QueryPhrase> mapPhrases(String query)
			throws IOException {
		Collection<QueryPhrase> phrases = null;
		analyzer.setCurrentOperation(QueryAnalyzer.OPERATION_STEMMING);
		TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(
				query));
		phrases = new ArrayList<QueryPhrase>();
		OffsetAttribute offsetAtt = (OffsetAttribute) tokenStream
				.addAttribute(OffsetAttribute.class);
		TypeAttribute typeAtt = (TypeAttribute) tokenStream
				.addAttribute(TypeAttribute.class);
		while (tokenStream.incrementToken()) {
			int begin = offsetAtt.startOffset();
			int end = offsetAtt.endOffset();

			if (typeAtt.type().equals(PHRASE))
				phrases.add(new QueryPhrase(begin + 1, end - 1));
		}
		for (QueryPhrase p : phrases) {
			System.out.println(p);
		}
		return phrases;
	}

	protected void mapDictionaryMatches(String query,
			Collection<QueryToken> tokens, Collection<QueryPhrase> phrases) {
		logger.debug("Chunking query '{}'", query);
		// First of all, scan the user input for string that occur in the
		// dictionary.
		Chunking chunking = chunker.chunk(query);
		Collection<QueryToken> chunkTokens = new ArrayList<QueryToken>();
		Collection<QueryToken> stringTermTokens = new ArrayList<QueryToken>();
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();

			if (containsTokenOverlappingSpan(start, end, tokens))
				continue;
			if (!complainsToPhrases(start, end, phrases))
				continue;

			String termId = chunk.type();
			QueryToken newToken = new QueryToken(start, end, termId);
			newToken.setScore(chunk.score());

			IFacetTerm term = null;
			if (termService.isStringTermID(termId)) {
				stringTermTokens.add(newToken);
			} else {
				term = termService.getNode(chunk.type());
				if (term == null)
					throw new IllegalStateException("No term for " + termId
							+ " found!");
				newToken.setTerm(term);
				chunkTokens.add(newToken);
			}
			// Do logging after string tokens have been mapped to their final
			// string representation
			// logger.debug("Term '{}' recognized.",
			// newToken.getTerm().getName());
		}

		// For all string terms, map the terms to another string representation,
		// e.g. author name canonicalization.
		Collection<QueryToken> mappedQueryStringTerms = termService.mapQueryStringTerms(stringTermTokens);
		chunkTokens.addAll(mappedQueryStringTerms);

		Collection<QueryToken> filteredTokens = filterLongestMatches(chunkTokens);
		logger.debug("After filtering of longest matches remain: {}",
				Arrays.toString(filteredTokens.toArray()));
		Multimap<Integer, QueryToken> tokensByStart = HashMultimap.create();

		for (QueryToken token : filteredTokens)
			tokensByStart.put(token.getBeginOffset(), token);

		for (Integer start : TreeMultiset.create(tokensByStart.keySet())) {
			List<QueryToken> sortedTokens = new ArrayList<QueryToken>();
			Collection<QueryToken> tokenOnIndex = tokensByStart.get(start);
			sortedTokens.addAll(tokenOnIndex);
			Collections.sort(sortedTokens, new ScoreComparator());

			Iterator<QueryToken> tokenIterator = sortedTokens.iterator();

			for (int i = 0; tokenIterator.hasNext() && i < maxAmbigueTerms; i++) {
				QueryToken token = tokenIterator.next();
				String originalValue = query.substring(token.getBeginOffset(), token.getEndOffset());
				token.setOriginalValue(originalValue);
				// if (hasOnlyTokensInSpan(start, token.getEndOffset(), tokens))
				tokens.add(token);
			}
		}
		logger.debug("Tokens returned: {}", Arrays.toString(tokens.toArray()));
	}

	@SuppressWarnings("deprecation")
	protected void mapKeywords(String query, Collection<QueryToken> tokens,
			Collection<QueryPhrase> phrases) throws IOException {
		analyzer.setCurrentOperation(QueryAnalyzer.OPERATION_STEMMING);
		TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(
				query));
		OffsetAttribute offsetAtt = (OffsetAttribute) tokenStream
				.addAttribute(OffsetAttribute.class);
		CharTermAttribute termAtt = (CharTermAttribute) tokenStream
				.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAtt = (TypeAttribute) tokenStream
				.addAttribute(TypeAttribute.class);
		while (tokenStream.incrementToken()) {
			int begin = offsetAtt.startOffset();
			int end = offsetAtt.endOffset();

			if (!containsTokenOverlappingSpan(begin, end, tokens)) {
				tokens.removeAll(tokensInSpan(begin, end, tokens));
				String tokenText = termAtt.toString();
				QueryToken queryToken = new QueryToken(begin, end, tokenText);

				if (typeAtt.type().equals(PHRASE))
					queryToken.setOriginalValue(query.substring(begin + 1,
							end - 1));
				else
					queryToken.setOriginalValue(query.substring(begin, end));

				IFacetTerm keywordTerm = new FacetTerm(queryToken.getValue(),
						queryToken.getOriginalValue());
				keywordTerm.addFacet(Facet.KEYWORD_FACET);
				keywordTerm.setIndexNames(Lists
						.newArrayList(IIndexInformationService.SEARCHABLE_FIELDS));
				queryToken.setTerm(keywordTerm);
				tokens.add(queryToken);
			}
		}
	}

	/**
	 * Removes duplicate entries of a IFacetTerm from a Multimap.
	 * 
	 * @param result
	 *            Mutlimap which may contain duplicates. Will be modified!
	 */
	private void removeDuplicateTerms(
			Multimap<String, TermAndPositionWrapper> result) {
		Multimap<String, TermAndPositionWrapper> duplicates = HashMultimap
				.create();
		Set<TermAndPositionWrapper> alreadySeen = new HashSet<TermAndPositionWrapper>();
		for (Map.Entry<String, TermAndPositionWrapper> entry : result.entries()) {
			// finding duplicates
			if (alreadySeen.contains(entry.getValue()))
				duplicates.put(entry.getKey(), entry.getValue()); // duplicate
			else
				alreadySeen.add(entry.getValue()); // found (at least) once
		}

		// Removing duplicates
		for (Map.Entry<String, TermAndPositionWrapper> entry : duplicates
				.entries()) {
			logger.debug(
					"Removing query term \"{}\" from queryTerms for search string \"{}\" due to duplicate removal.",
					entry.getValue().getTerm().getName(), entry.getKey());
			result.remove(entry.getKey(), entry.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	/**
	 * ?
	 */
	public Collection<IFacetTerm> mapQueryTerm(String queryTerm)
			throws IOException {
		Collection<QueryToken> tokens = new ArrayList<QueryToken>();
		mapDictionaryMatches(queryTerm, tokens, Collections.EMPTY_LIST);
		mapKeywords(queryTerm, tokens, Collections.EMPTY_LIST);

		Collection<IFacetTerm> mappedTerms = new ArrayList<IFacetTerm>();
		for (QueryToken token : tokens)
			mappedTerms.add(token.getTerm());

		return mappedTerms;
	}

	private void distributeTermsEvenlyAccrossFacets(
			Multimap<String, TermAndPositionWrapper> result) {
		Collection<String> queryTerms = result.keySet();
		for (String queryTerm : queryTerms) {
			Collection<TermAndPositionWrapper> termsAndPositions = result
					.get(queryTerm);
			Multimap<Facet, IFacetTerm> termsByFacet = HashMultimap.create();
			for (TermAndPositionWrapper tAndP : termsAndPositions)
				termsByFacet.put(tAndP.getTerm().getFirstFacet(),
						tAndP.getTerm());

			int maxTermsPerFacet = Math.round((float) maxAmbigueTerms
					/ (float) termsByFacet.keySet().size());
			Collection<IFacetTerm> filteredTerms = new ArrayList<IFacetTerm>();
			for (Facet facet : termsByFacet.keySet()) {
				int count = 0;
				for (IFacetTerm term : termsByFacet.get(facet)) {
					if (count < maxTermsPerFacet) {
						filteredTerms.add(term);
						count++;
					}
				}
			}
			TermAndPositionWrapper tAndP = result.get(queryTerm).iterator()
					.next();
			Collection<TermAndPositionWrapper> replacement = new ArrayList<TermAndPositionWrapper>();
			for (IFacetTerm term : filteredTerms)
				replacement.add(new TermAndPositionWrapper(term, tAndP
						.getBegin(), tAndP.getEnd()));

			result.replaceValues(queryTerm, replacement);
		}
	}

	protected void filterNonFacetTerms(
			Multimap<String, TermAndPositionWrapper> result) {

		for (String queryTerm : result.keySet()) {
			Collection<TermAndPositionWrapper> terms = result.get(queryTerm);
			boolean facetTermFound = false;
			for (TermAndPositionWrapper term : terms)
				if (term != null) {
					facetTermFound |= !term.getTerm().getFirstFacet().getId()
							.equals(FacetService.FACET_ID_CONCEPTS);
				}
			if (facetTermFound)
				for (Iterator<TermAndPositionWrapper> termIterator = terms
						.iterator(); termIterator.hasNext();) {
					TermAndPositionWrapper term = termIterator.next();
					if (term != null
							&& term.getTerm().getFirstFacet().getId()
									.equals(FacetService.FACET_ID_CONCEPTS))
						termIterator.remove();
				}
		}
	}

	private Collection<QueryToken> filterLongestMatches(
			Collection<QueryToken> tokens) {
		Collection<QueryToken> filteredTokens = new ArrayList<QueryToken>(
				tokens);
		for (QueryToken token : tokens) {
			int begin = token.getBeginOffset();
			int end = token.getEndOffset();

			if (containsLongerTokenInSpan(begin, end, tokens))
				filteredTokens.remove(token);
		}
		return filteredTokens;
	}

	/**
	 * Searches for tokens which overlap the span, e.g. foobar 01234567 "foobar"
	 * would be returned for spans like 2-5
	 * 
	 * @param begin
	 *            Begin of the span
	 * @param end
	 *            End of the span
	 * @param tokens
	 *            Tokens to test
	 * @return Those tokens which overlap the span
	 */
	protected Collection<QueryToken> tokensOverSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		Collection<QueryToken> result = new ArrayList<QueryToken>();
		for (QueryToken token : tokens) {
			if (token.getBeginOffset() < begin && token.getEndOffset() > end)
				result.add(token);
		}
		return result;
	}

	/**
	 * Searches for tokens inside a span
	 * 
	 * @param begin
	 *            Begin of the span
	 * @param end
	 *            End of the span
	 * @param tokens
	 *            Tokens to test
	 * @return Those tokens which are inside the span
	 */
	protected Collection<QueryToken> tokensInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		Collection<QueryToken> result = new ArrayList<QueryToken>();
		for (QueryToken token : tokens) {
			if (token.getBeginOffset() > begin && token.getEndOffset() < end)
				result.add(token);
		}
		return result;
	}

	/**
	 * Tests if there is at least a minimal overlap between the tokens and the
	 * span
	 * 
	 * @param begin
	 *            Begin of the span
	 * @param end
	 *            End of the span
	 * @param tokens
	 *            Tokens to test
	 * @return True if at least one token has at least one character inside the
	 *         span
	 */
	protected boolean containsTokenOverlappingSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		boolean result = false;
		for (QueryToken token : tokens) {
			int tokenBegin = token.getBeginOffset();
			int tokenEnd = token.getEndOffset();
			result |= (tokenBegin >= begin && tokenBegin <= end)
					|| (tokenEnd >= begin && tokenEnd <= end)
					|| (begin >= tokenBegin && begin <= tokenEnd)
					|| (end >= tokenBegin && end <= tokenEnd);
		}
		return result;
	}

	/**
	 * Tests if there is a token which overlaps with the span and is bigger than
	 * it
	 * 
	 * @param begin
	 *            Begin of the span
	 * @param end
	 *            End of the span
	 * @param tokens
	 *            Tokens to test
	 * @return True if there is a token which overlaps with the span and is
	 *         bigger than it
	 */
	protected boolean containsLongerTokenInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		boolean result = false;
		for (QueryToken token : tokens) {
			int tokenBegin = token.getBeginOffset();
			int tokenEnd = token.getEndOffset();
			result |= (
			// token begins in span
					(tokenBegin >= begin && tokenBegin <= end)
					// token ends in span
							|| (tokenEnd >= begin && tokenEnd <= end)
					// token begins and ends outside/at corner of span
					|| (tokenBegin <= begin && tokenEnd >= end))
					// token is bigger than span
					&& (tokenEnd - tokenBegin > end - begin);
		}
		return result;
	}

	/**
	 * working?
	 */
	protected boolean complainsToPhrases(int begin, int end,
			Collection<QueryPhrase> phrases) {
		boolean result = true;
		for (QueryPhrase phrase : phrases) {
			int phraseBegin = phrase.getBeginOffset();
			int phraseEnd = phrase.getEndOffset();
			result &= (phraseBegin > begin && phraseEnd > end)
					|| (phraseBegin < begin && phraseEnd < end)
					|| (phraseBegin == begin && phraseEnd == end);
			// is this working? phrase can be completely outside of span!
		}
		return result;
	}

	/**
	 * Tests if all tokens are inside the span
	 * 
	 * @param begin
	 *            Begin of the span
	 * @param end
	 *            End of the span
	 * @param tokens
	 *            Tokens to test
	 * @return True if all tokens are inside the span
	 */
	protected boolean hasOnlyTokensInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		boolean allInside = true;
		for (QueryToken token : tokens) {
			int tokBegin = token.getBeginOffset();
			int tokEnd = token.getEndOffset();
			if (!(tokBegin >= begin && tokBegin < end && tokEnd <= end && tokEnd > begin))
				allInside = false;
		}
		return allInside;
	}

	/**
	 * Returns Symbols containing a combination of the text in the original
	 * symbols.
	 * 
	 * @param symbols
	 *            Symbols to combine
	 * @return Symbols, some may be a combination of input symbols
	 */
	public Collection<Symbol> disambiguateSymbols(Collection<Symbol> symbols)
			throws IOException {
		String query = "";
		Map<Integer, String> wordAt = new HashMap<Integer, String>();
		List<Symbol> returnedSymbols = new ArrayList<Symbol>();
		for (Symbol s : symbols) {
			if (s != null && s.value != null
					&& s.value.getClass() == String.class) {
				if (s.sym != QueryTokenizer.PHRASE) {
					wordAt.put(query.length(), (String) s.value);
					query = query.concat((String) s.value).concat(" ");
				} else {
					returnedSymbols.addAll(runDisambiguation(query.trim(),
							wordAt));
					returnedSymbols.add(s);
					query = "";
				}
			} else
				throw new IllegalArgumentException(
						"Must only be used with Symbols containing text");
		}
		if (query.length() > 0)
			returnedSymbols.addAll(runDisambiguation(query.trim(), wordAt));
		return returnedSymbols;
	}

	/**
	 * creates symbols for a query
	 * 
	 * @param query
	 *            query to get terms for
	 * @param wordAt
	 *            map of terms by their position
	 * @param offset
	 * @return Symbols of Type Phrase, containing a String[] with text and the
	 *         id of its term
	 * @throws IOException
	 */
	private Collection<? extends Symbol> runDisambiguation(String query,
			Map<Integer, String> wordAt) throws IOException {
		List<Symbol> symbols = new ArrayList<Symbol>();
		List<QueryToken> tokens = getTokens(query, null);
		Multimap<String, TermAndPositionWrapper> tokenMap = getResult(tokens);
		for (String key : tokenMap.keySet()) {
			for (TermAndPositionWrapper tAndP : tokenMap.get(key)) {
				String[] originalAndmapped = new String[2];
				String original = "";
				for (int i : wordAt.keySet()) {
					if (i >= tAndP.getBegin() && i < tAndP.getEnd())
						original += wordAt.get(i) + " ";
				}
				originalAndmapped[TEXT] = original.trim();
				originalAndmapped[MAPPED_TEXT] = tAndP.getTerm().getId();
				symbols.add(new Symbol(QueryTokenizer.PHRASE, originalAndmapped));
			}
		}
		return symbols;
	}
}
