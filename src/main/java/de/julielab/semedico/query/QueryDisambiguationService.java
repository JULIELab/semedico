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
 * //TODO insert short description
 **/

package de.julielab.semedico.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java_cup.runtime.Symbol;

import org.apache.commons.lang.StringUtils;
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
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.QueryPhrase;
import de.julielab.semedico.core.QueryToken;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.IStopWordService;
import de.julielab.semedico.core.services.ITermService;

public class QueryDisambiguationService implements IQueryDisambiguationService {
	private ITermService termService;

	public static final String DEFAULT_SNOWBALL_STEMMER = "English";
	public static final int DEFAULT_MAX_AMBIGUE_TERMS = 25;
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

	// oh java, where is they lambda...
	private static class BeginOffsetComparator implements
			Comparator<QueryToken> {
		@Override
		public int compare(QueryToken token1, QueryToken token2) {
			return token1.getBeginOffset() - token2.getBeginOffset();
		}
	}

	/**
	 * @param logger
	 * @param stopWords
	 * @param termService
	 * @param chunker
	 * @throws IOException
	 */
	public QueryDisambiguationService(Logger logger,
			IStopWordService stopWords, ITermService termService,
			Chunker chunker) throws IOException {
		super(); // why? it doesn't extend anything...
		analyzer = new QueryAnalyzer(stopWords.getAsArray(),
				DEFAULT_SNOWBALL_STEMMER);
		maxAmbigueTerms = DEFAULT_MAX_AMBIGUE_TERMS;
		this.termService = termService;
		this.chunker = chunker;
		this.logger = logger;
	}

	
	
	@Override
	/**
	 * ?
	 */
	public Multimap<String, IFacetTerm> disambiguateQuery(String query,
			String id) throws IOException {
		long time = System.currentTimeMillis();

		if (query.equals(""))
			return LinkedHashMultimap.create();

		List<QueryToken> tokens = new ArrayList<QueryToken>();
		if (id != null && !id.equals(""))
			mapDisambiguatedTerms(query, id, tokens);

		Collection<QueryPhrase> phrases = mapPhrases(query);

		mapDictionaryMatches(query, tokens, phrases);
		// mapIndexMatches(query, tokens, phrases);
		mapKeywords(query + " ", tokens, phrases);

		Multimap<String, IFacetTerm> result = LinkedHashMultimap.create();
		Collections.sort(tokens, BEGINN_OFFSET_COMPARATOR);
		for (QueryToken queryToken : tokens) {
			result.put(queryToken.getOriginalValue(), queryToken.getTerm());
		}

		distributeTermsEvenlyAccrossFacets(result);
		filterNonFacetTerms(result);
		removeDuplicateTerms(result);

		time = System.currentTimeMillis() - time;

		logger.debug("Extracted string to term mapping:");
		for (String queryString : result.keySet()) {
			logger.debug("{}\t->\t{}", queryString, StringUtils.join(
					Collections2.transform(result.get(queryString),
							new Function<IFacetTerm, String>() {
								@Override
								public String apply(IFacetTerm input) {
									return "[Name: " + input.getName()
											+ ", ID:" + input.getId() + "]";
								}
							}), ", "));
		}

		logger.info("disambiguateQuery() takes {} ms", time);
		return result;
	}

	/**
	 * Removes duplicate entries of a IFacetTerm from a Multimap.
	 * 
	 * @param result
	 * 			Mutlimap which may contain duplicates. Will be modified!
	 */
	private void removeDuplicateTerms(Multimap<String, IFacetTerm> result) {
		Multimap<String, IFacetTerm> duplicates = HashMultimap.create();
		Set<IFacetTerm> alreadySeen = new HashSet<IFacetTerm>();
		for (Map.Entry<String, IFacetTerm> entry : result.entries()) {
			// finding duplicates
			if (alreadySeen.contains(entry.getValue()))
				duplicates.put(entry.getKey(), entry.getValue()); // duplicate
			else
				alreadySeen.add(entry.getValue()); // found (at least) once
		}

		// Remove duplicates
		for (Map.Entry<String, IFacetTerm> entry : duplicates.entries()) {
			logger.debug(
					"Removing query term \"{}\" from queryTerms for search string \"{}\" due to duplicate removal.",
					entry.getValue().getName(), entry.getKey());
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
			Multimap<String, IFacetTerm> result) {
		Collection<String> queryTerms = result.keySet();
		for (String queryTerm : queryTerms) {
			Collection<IFacetTerm> terms = result.get(queryTerm);
			Multimap<Facet, IFacetTerm> termsByFacet = HashMultimap.create();
			for (IFacetTerm term : terms)
				termsByFacet.put(term.getFirstFacet(), term);

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
			result.replaceValues(queryTerm, filteredTerms);
		}

	}

	protected void filterNonFacetTerms(Multimap<String, IFacetTerm> result) {

		for (String queryTerm : result.keySet()) {
			Collection<IFacetTerm> terms = result.get(queryTerm);
			boolean facetTermFound = false;
			for (IFacetTerm term : terms)
				if (term != null) {
					facetTermFound |= !term.getFirstFacet().getId()
							.equals(FacetService.CONCEPT_FACET_ID);
				}
			if (facetTermFound)
				for (Iterator<IFacetTerm> termIterator = terms.iterator(); termIterator
						.hasNext();) {
					IFacetTerm term = termIterator.next();
					if (term != null
							&& term.getFirstFacet().getId()
									.equals(FacetService.CONCEPT_FACET_ID))
						termIterator.remove();
				}
		}
	}

	protected void mapDisambiguatedTerms(String query, String id,
			Collection<QueryToken> tokens) {
		if (id != null && id.length() > 0) {
			QueryToken token = new QueryToken(0, query.length(), query);
			token.setOriginalValue(query.substring(token.getBeginOffset(),
					token.getEndOffset()));
			IFacetTerm term = termService.getTermWithInternalIdentifier(id);
			if (term != null)
				tokens.add(token);
			token.setTerm(term);
		}
	}

	protected Collection<QueryPhrase> mapPhrases(String query)
			throws IOException {
		analyzer.setCurrentOperation(QueryAnalyzer.OPERATION_STEMMING);
		TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(
				query));
		Collection<QueryPhrase> phrases = new ArrayList<QueryPhrase>();
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
		return phrases;
	}

	protected void mapDictionaryMatches(String query,
			Collection<QueryToken> tokens, Collection<QueryPhrase> phrases) {
		Chunking chunking = chunker.chunk(query);
		Collection<QueryToken> chunkTokens = new ArrayList<QueryToken>();
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();

			if (containsTokenOverlappingSpan(start, end, tokens))
				continue;
			if (!complainsToPhrases(start, end, phrases))
				continue;

			QueryToken newToken = new QueryToken(start, end, query.substring(
					start, end));
			newToken.setScore(chunk.score());
			newToken.setOriginalValue(query.substring(start, end));

			IFacetTerm term = termService.getNode(chunk.type());
			if (term == null)
				throw new IllegalStateException("no term for " + chunk.type()
						+ " found!");
			newToken.setTerm(term);
			chunkTokens.add(newToken);
		}

		Collection<QueryToken> filteredTokens = filterLongestMatches(chunkTokens);
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
				if (hasOnlyTokensInSpan(start, token.getEndOffset(), tokens))
					tokens.add(token);
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
						.newArrayList(IndexFieldNames.SEARCHABLE_FIELDS));
				queryToken.setTerm(keywordTerm);
				tokens.add(queryToken);
			}
		}
	}

	/**
	 * Searches for tokens which overlap the span, e.g.
	 * 	foobar
	 * 01234567
	 * "foobar" would be returned for spans like 2-5
	 * @param begin 
	 * 			Begin of the span
	 * @param end
	 * 			End of the span
	 * @param tokens
	 * 			Tokens to test
	 * @return
	 * 			Those tokens which overlap the span
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
	 * @param begin 
	 * 			Begin of the span
	 * @param end
	 * 			End of the span
	 * @param tokens
	 * 			Tokens to test
	 * @return
	 * 			Those tokens which are inside the span
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
	 * Tests if there is at least a minimal overlap between the tokens and the span
	 * @param begin 
	 * 			Begin of the span
	 * @param end
	 * 			End of the span
	 * @param tokens
	 * 			Tokens to test
	 * @return
	 * 		True if at least one token has at least one character inside the span
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
	 * Tests if there is a token which overlaps with the span and is bigger than it
	 * @param begin 
	 * 			Begin of the span
	 * @param end
	 * 			End of the span
	 * @param tokens
	 * 			Tokens to test
	 * @return
	 * 		True if there is a token which overlaps with the span and is bigger than it
	 */
	protected boolean containsLongerTokenInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		boolean result = false;
		for (QueryToken token : tokens) {
			int tokenBegin = token.getBeginOffset();
			int tokenEnd = token.getEndOffset();
			result |= (
						//token begins in span
						(tokenBegin >= begin && tokenBegin <= end) 
						//token ends in span
						|| (tokenEnd >= begin && tokenEnd <= end) 
						//token begins and ends outside/at corner of span
						|| (tokenBegin <= begin && tokenEnd >= end)
					  )
					  //token is bigger than span
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
	 * @param begin 
	 * 			Begin of the span
	 * @param end
	 * 			End of the span
	 * @param tokens
	 * 			Tokens to test
	 * @return
	 * 		True if all tokens are inside the span
	 */
	protected boolean hasOnlyTokensInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		Collection<QueryToken> inSpan = tokensInSpan(begin, end, tokens);
		Collection<QueryToken> notInSpan = new HashSet<QueryToken>(tokens);
		notInSpan.removeAll(inSpan);
		
		Collection<QueryToken> exactInSpan = new HashSet<QueryToken>();
		for (QueryToken token : notInSpan)
			if (token.getBeginOffset() == begin && token.getEndOffset() == end)
				exactInSpan.add(token);

		notInSpan.removeAll(exactInSpan);
		return !containsTokenOverlappingSpan(begin, end, notInSpan);
		//TODO: better code below? needs proper test!
//		boolean allInside = true;
//		for(QueryToken token : tokens){
//			int tokBegin = token.getBeginOffset();
//			int tokEnd = token.getEndOffset();
//			if(!(tokBegin >= begin && tokBegin < end && tokEnd <= end && tokEnd > begin))
//				allInside = false;
//		}
//		return allInside;
	}

	/**
	 * @return The chunker.
	 */
	public Chunker getChunker() {
		return chunker;
	}

	/**
	 * @param dictionaryChunker 
	 * 				Chunker to use.
	 */
	public void setChunker(Chunker dictionaryChunker) {
		this.chunker = dictionaryChunker;
	}

	/**
	 * @return maxAmbigueTerms
	 */
	public int getMaxAmbigueTerms() {
		return maxAmbigueTerms;
	}

	/**
	 * @param maxAmbigueTerms
	 * 				Value for maxAmbigueTerms
	 */
	public void setMaxAmbigueTerms(int maxAmbigueTerms) {
		this.maxAmbigueTerms = maxAmbigueTerms;
	}

	/**
	 * @return minMatchingScore
	 */
	public double getMinMatchingScore() {
		return minMatchingScore;
	}
	
	/**
	 * @param maxAmbigueTerms
	 * 				Value for minMatchingScore
	 */
	public void setMinMatchingScore(double minMatchingScore) {
		this.minMatchingScore = minMatchingScore;
	}

	@Override
	/**
	 * Disambiguates a
	 */
	public Multimap<String, IFacetTerm> disambiguateSymbols(String id,
			Symbol... symbols) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Symbol s : symbols)
			if (s != null && s.value != null)
				sb.append((String) s.value).append(" ");
			else
				throw new IllegalArgumentException("Must only be used with Symbols containing text");
		String query = sb.toString().trim();
		return disambiguateQuery(query, id);
	}
}
