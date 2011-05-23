/** 
 * SearchService.java
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
 * Creation date: 04.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.stemnet.search;

import java.io.IOException;

import org.apache.lucene.search.TermsFilter;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import de.julielab.stemnet.IndexFieldNames;
import de.julielab.stemnet.core.SortCriterium;

public class SearchService implements ISearchService {

//	private class FilterHitCollector extends HitCollector {
//
//		private BitSet includedDocs;
//		private HitCollector otherCollector;
//
//		public FilterHitCollector(BitSet includedDocs,
//				HitCollector otherCollector) {
//			super();
//			this.includedDocs = includedDocs;
//			this.otherCollector = otherCollector;
//		}
//
//		@Override
//		public void collect(int docId, float score) {
//			if (includedDocs.get(docId)) {
//				otherCollector.collect(docId, score);
//			}
//		}
//	}
//
//	private class RecorderHitCollector extends HitCollector {
//
//		private HitCollector otherCollector;
//		private OpenBitSet recordedHits;
//
//		public RecorderHitCollector(HitCollector otherCollector,
//				OpenBitSet recordedHits) {
//			super();
//			this.otherCollector = otherCollector;
//			this.recordedHits = recordedHits;
//		}
//
//		@Override
//		public void collect(int docId, float score) {
//			otherCollector.collect(docId, score);
//			recordedHits.set(docId);
//		}
//	}

//	private static final Logger logger = Logger.getLogger(SearchService.class);

	private SolrServer solr;
	private SolrQuery query;
	// private IIndexSearcherWrapper searcher;
//	private IFacetHitCollectorService labelHitCounterService;
//	private IDocumentService hitService;
//	private IQueryTranslationService queryTranslationService;
//	private IKwicService kwicService;

//	private TopFieldDocCollector topFieldCollector;
//	private Sort sort;
//	private BitSet reviewDocs;
//
//	private boolean reviewFilterActive;
//
//	private static final int MAX_VIEWABLE_DOCS = 250;
//
//	private static final SortField DATE_SORT_FIELD = new SortField(
//			IndexFieldNames.DATE, SortField.STRING, true);
//	private static final SortField[] DATE_AND_RELEVANCE_FIELDS = {
//			DATE_SORT_FIELD, SortField.FIELD_SCORE };

	private static final String REVIEW_TERM = "REVIEW";

	/**
	 * Creates an SearchService object.
	 * 
	 * @param indexPath
	 * @throws IOException
	 */
	public SearchService() {
//		BooleanQuery.setMaxClauseCount(5000);

	}

	public void init() {
		TermsFilter filter = new TermsFilter();
		filter.addTerm(new org.apache.lucene.index.Term(
				IndexFieldNames.PUBLICATION_TYPES, REVIEW_TERM));
		query = new SolrQuery();
		// try {
		// reviewDocs =
		// filter.bits(searcher.getIndexSearcher().getIndexReader());
		// } catch (IOException e) {
		// throw new IllegalStateException(e);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.stemnet.query.IndexSearchServiceInterface#processQuery(java
	 * .lang.String)
	 */
	public QueryResponse processQuery(String queryString,
			SortCriterium sortCriterium, boolean reviewFilter) {

		query.setQuery(queryString);
		// TODO Set faceting, review filter, max returned docs, hightlight
		QueryResponse response = null;
		try {
			response = solr.query(query);
			query.clear();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return response;
		//
		//
		// sort= new Sort();
		// if( sortCriterium == SortCriterium.DATE )
		// sort.setSort(DATE_SORT_FIELD);
		// else if( sortCriterium == SortCriterium.RELEVANCE )
		// sort.setSort(SortField.FIELD_SCORE);
		// else
		// sort.setSort(DATE_AND_RELEVANCE_FIELDS);
		// try {
		// topFieldCollector = new
		// TopFieldDocCollector(searcher.getIndexSearcher().getIndexReader(),
		// sort, MAX_VIEWABLE_DOCS);
		// } catch (IOException e1) {
		// throw new IllegalStateException(e1);
		// }
		//
		// logger.info("sort: " + sortCriterium + " query: " + query);
		//
		// if( query == null || query.equals("") )
		// return new TopDocs(0, new ScoreDoc[]{}, 0);
		//
		// HitCollector collector = new RecorderHitCollector(topFieldCollector,
		// recordedHits);
		// if( reviewFilter )
		// collector= new FilterHitCollector(reviewDocs, collector);
		//
		// long searchTime = System.currentTimeMillis();
		// searcher.getIndexSearcher().search(query, collector);
		// searchTime = System.currentTimeMillis() - searchTime;
		// logger.info("searching takes " + searchTime + " ms");
		//
		// return topFieldCollector.topDocs();
	}

//	public IDocumentService getHitService() {
//		return hitService;
//	}
//
//	public void setHitService(IDocumentService hitService) {
//		this.hitService = hitService;
//	}
//
//	public IQueryTranslationService getQueryTranslationService() {
//		return queryTranslationService;
//	}
//
//	public IKwicService getKwicService() {
//		return kwicService;
//	}
//
//	public void setKwicService(IKwicService kwicService) {
//		this.kwicService = kwicService;
//	}
//
//	public void setQueryTranslationService(
//			IQueryTranslationService queryTranslationService) {
//		this.queryTranslationService = queryTranslationService;
//	}
//
//	public IFacetHitCollectorService getLabelHitCounterService() {
//		return labelHitCounterService;
//	}
//
//	public void setLabelHitCounterService(
//			IFacetHitCollectorService counterService) {
//		this.labelHitCounterService = counterService;
//	}

	@Override
	public int getIndexSize() {
		SolrQuery allQuery = new SolrQuery("*:*");
		try {
			return (int) solr.query(allQuery).getResults().getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public SolrServer getSearcher() {
		return solr;
	}

	public void setSearcher(SolrServer searcher) {
		this.solr = solr;
	}

}