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
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultiset;

import de.julielab.lucene.IIndexSearcherWrapper;
import de.julielab.lucene.QueryAnalyzer;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.QueryPhrase;
import de.julielab.semedico.core.QueryToken;
import de.julielab.semedico.core.services.FacetService;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;

public class QueryDisambiguationService implements IQueryDisambiguationService {

	private ITermService termService;
	private ITermOccurrenceFilterService filterService;

	public static final String DEFAULT_SNOWBALL_STEMMER = "English";
	public static final int DEFAULT_MAX_AMBIGUE_TERMS = 25;
	private Chunker chunker;
	private IIndexSearcherWrapper searcher;
	private QueryAnalyzer analyzer;
	private int maxAmbigueTerms;
	private double minMatchingScore;
	private static Logger LOGGER = Logger
			.getLogger(QueryDisambiguationService.class);

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

	private static class BeginOffsetComparator implements
			Comparator<QueryToken> {

		@Override
		public int compare(QueryToken token1, QueryToken token2) {

			return token1.getBeginOffset() - token2.getBeginOffset();
		}

	}

	public QueryDisambiguationService(Set<String> stopWords) throws IOException {
		super();
		String[] stopWordArray = new String[stopWords.size()];
		stopWords.toArray(stopWordArray);
		analyzer = new QueryAnalyzer(stopWordArray, DEFAULT_SNOWBALL_STEMMER);
		maxAmbigueTerms = DEFAULT_MAX_AMBIGUE_TERMS;
	}

	@Override
	public Multimap<String, FacetTerm> disambiguateQuery(String query, String id)
			throws IOException {
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

		Multimap<String, FacetTerm> result = LinkedHashMultimap.create();
		Collections.sort(tokens, BEGINN_OFFSET_COMPARATOR);
		for (QueryToken queryToken : tokens) {
			result.put(queryToken.getOriginalValue(), queryToken.getTerm());
		}

		distributeTermsEvenlyAccrossFacets(result);
		filterNonFacetTerms(result);

		time = System.currentTimeMillis() - time;
		LOGGER.info("disambiguateQuery() takes " + time + " ms");
		return result;
	}

	@Override
	public Collection<FacetTerm> mapQueryTerm(String queryTerm)
			throws IOException {
		Collection<QueryToken> tokens = new ArrayList<QueryToken>();
		mapDictionaryMatches(queryTerm, tokens, Collections.EMPTY_LIST);
		mapKeywords(queryTerm, tokens, Collections.EMPTY_LIST);
		Collection<FacetTerm> mappedTerms = new ArrayList<FacetTerm>();

		for (QueryToken token : tokens)
			mappedTerms.add(token.getTerm());

		return mappedTerms;
	}

	private void distributeTermsEvenlyAccrossFacets(
			Multimap<String, FacetTerm> result) {
		Collection<String> queryTerms = result.keySet();
		for (String queryTerm : queryTerms) {
			Collection<FacetTerm> terms = result.get(queryTerm);
			Multimap<Facet, FacetTerm> termsByFacet = HashMultimap.create();
			for (FacetTerm term : terms)
				termsByFacet.put(term.getFacet(), term);

			int maxTermsPerFacet = Math.round((float) maxAmbigueTerms
					/ (float) termsByFacet.keySet().size());
			Collection<FacetTerm> filteredTerms = new ArrayList<FacetTerm>();
			for (Facet facet : termsByFacet.keySet()) {
				int count = 0;
				for (FacetTerm term : termsByFacet.get(facet)) {
					if (count < maxTermsPerFacet) {
						filteredTerms.add(term);
						count++;
					}
				}
			}

			result.replaceValues(queryTerm, filteredTerms);
		}

	}

	protected void filterNonFacetTerms(Multimap<String, FacetTerm> result) {

		for (String queryTerm : result.keySet()) {
			Collection<FacetTerm> terms = result.get(queryTerm);
			boolean facetTermFound = false;
			for (FacetTerm term : terms)
				if (term != null)
					facetTermFound |= !term.getFacet().getId()
							.equals(Facet.CONCEPT_FACET_ID);

			if (facetTermFound)
				for (Iterator<FacetTerm> termIterator = terms.iterator(); termIterator
						.hasNext();) {
					FacetTerm term = termIterator.next();
					if (term != null
							&& term.getFacet().getId()
									.equals(Facet.CONCEPT_FACET_ID))
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
			FacetTerm term = termService
					.getTermWithInternalIdentifier(id);
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

			FacetTerm term = termService.getTermWithInternalIdentifier(
					chunk.type());
			if (term == null)
				throw new NullPointerException("no term for " + chunk.type()
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

				FacetTerm keywordTerm = new FacetTerm(-1);
				keywordTerm.setInternalIdentifier(queryToken.getValue());
				keywordTerm.setLabel(queryToken.getOriginalValue());
				keywordTerm.setFacet(FacetService.KEYWORD_FACET);
				keywordTerm.setIndexNames(Lists
						.newArrayList(IndexFieldNames.SEARCHABLE_FIELDS));
				queryToken.setTerm(keywordTerm);
				tokens.add(queryToken);
			}
		}
	}

	protected Collection<QueryToken> tokensOverSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		Collection<QueryToken> result = new ArrayList<QueryToken>();
		for (QueryToken token : tokens) {
			int tokenBegin = token.getBeginOffset();
			int tokenEnd = token.getEndOffset();
			if (tokenBegin < begin && tokenEnd > end)
				result.add(token);
		}
		return result;
	}

	protected Collection<QueryToken> tokensInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		Collection<QueryToken> result = new ArrayList<QueryToken>();
		for (QueryToken token : tokens) {
			int tokenBegin = token.getBeginOffset();
			int tokenEnd = token.getEndOffset();
			if (tokenBegin > begin && tokenEnd < end)
				result.add(token);
		}
		return result;
	}

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

	protected boolean containsLongerTokenInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		boolean result = false;
		for (QueryToken token : tokens) {
			int tokenBegin = token.getBeginOffset();
			int tokenEnd = token.getEndOffset();
			result |= ((tokenBegin >= begin && tokenBegin <= end)
					|| (tokenEnd >= begin && tokenEnd <= end) || (tokenBegin <= begin && tokenEnd >= end))
					&& (tokenEnd - tokenBegin > end - begin);

		}
		return result;
	}

	protected boolean complainsToPhrases(int begin, int end,
			Collection<QueryPhrase> phrases) {
		boolean result = true;
		for (QueryPhrase phrase : phrases) {
			int phraseBegin = phrase.getBeginOffset();
			int phraseEnd = phrase.getEndOffset();
			result &= (phraseBegin > begin && phraseEnd > end)
					|| (phraseBegin < begin && phraseEnd < end)
					|| (phraseBegin == begin && phraseEnd == end);
		}
		return result;

	}

	protected boolean hasOnlyTokensInSpan(int begin, int end,
			Collection<QueryToken> tokens) {
		Collection<QueryToken> inSpan = tokensInSpan(begin, end, tokens);
		Collection<QueryToken> notInSpan = new HashSet<QueryToken>(tokens);
		Collection<QueryToken> exactInSpan = new HashSet<QueryToken>();
		notInSpan.removeAll(inSpan);

		for (QueryToken token : notInSpan)
			if (token.getBeginOffset() == begin && token.getEndOffset() == end)
				exactInSpan.add(token);

		notInSpan.removeAll(exactInSpan);
		return !containsTokenOverlappingSpan(begin, end, notInSpan);
	}

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public Chunker getChunker() {
		return chunker;
	}

	public void setChunker(Chunker dictionaryChunker) {
		this.chunker = dictionaryChunker;
	}

	public IIndexSearcherWrapper getSearcher() {
		return searcher;
	}

	public void setSearcher(IIndexSearcherWrapper searcher) {
		this.searcher = searcher;
	}

	public int getMaxAmbigueTerms() {
		return maxAmbigueTerms;
	}

	public void setMaxAmbigueTerms(int maxAmbigueTerms) {
		this.maxAmbigueTerms = maxAmbigueTerms;
	}

	public double getMinMatchingScore() {
		return minMatchingScore;
	}

	public void setMinMatchingScore(double minMatchingScore) {
		this.minMatchingScore = minMatchingScore;
	}

	public ITermOccurrenceFilterService getFilterService() {
		return filterService;
	}

	public void setFilterService(ITermOccurrenceFilterService filterService) {
		this.filterService = filterService;
	}

}
