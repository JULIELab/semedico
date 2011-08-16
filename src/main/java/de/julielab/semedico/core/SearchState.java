/**
 * SearchState.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 15.08.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;

/**
 * @author faessler
 *
 */
public class SearchState {
	// The most important information about session state: These are the query
		// terms which determine the currently retrieved documents in the first
		// place.
		private Multimap<String, IFacetTerm> queryTerms;
		// Since a term can occur in multiple facets, this map stores the
		// information from which facet a particular term had been chosen by the
		// user.
		private Map<IFacetTerm, Facet> queryTermFacetMap;
		// Determines how the found documents are to be ordered for display.
		private SolrQuery query;
		private SortCriterium sortCriterium;
		// Determines whether reviews articles should be shown.
		private boolean reviewsFiltered;
		// Set to true during the rendering phase after a query was put into the
		// input field and a search had been triggered (opposed to searches by
		// clicking on terms).
		private boolean newSearch;
		private String rawQuery;
		
		public SearchState() {
			super();
			this.queryTerms = HashMultimap.create();
			this.queryTermFacetMap = new HashMap<IFacetTerm, Facet>();
			this.query = new SolrQuery();
			this.sortCriterium = SortCriterium.DATE_AND_RELEVANCE;
			this.reviewsFiltered = false;
			this.newSearch = true;
		}

		/**
		 * @return the sortCriterium
		 */
		public SortCriterium getSortCriterium() {
			return sortCriterium;
		}

		/**
		 * @param sortCriterium the sortCriterium to set
		 */
		public void setSortCriterium(SortCriterium sortCriterium) {
			this.sortCriterium = sortCriterium;
		}

		/**
		 * @return the reviewsFiltered
		 */
		public boolean isReviewsFiltered() {
			return reviewsFiltered;
		}

		/**
		 * @param reviewsFiltered the reviewsFiltered to set
		 */
		public void setReviewsFiltered(boolean reviewsFiltered) {
			this.reviewsFiltered = reviewsFiltered;
		}

		/**
		 * @return the newSearch
		 */
		public boolean isNewSearch() {
			return newSearch;
		}

		/**
		 * @param newSearch the newSearch to set
		 */
		public void setNewSearch(boolean newSearch) {
			this.newSearch = newSearch;
		}

		/**
		 * @return the queryTerms
		 */
		public Multimap<String, IFacetTerm> getQueryTerms() {
			return queryTerms;
		}

		/**
		 * @param queryTerms the queryTerms to set
		 */
		public void setQueryTerms(Multimap<String, IFacetTerm> queryTerms) {
			this.queryTerms = queryTerms;
		}

		/**
		 * @return the queryTermFacetMap
		 */
		public Map<IFacetTerm, Facet> getQueryTermFacetMap() {
			return queryTermFacetMap;
		}

		/**
		 * @return the query
		 */
		public SolrQuery getSolrQuery() {
			return query;
		}

		/**
		 * @return the rawQuery
		 */
		public String getRawQuery() {
			return rawQuery;
		}

		/**
		 * @param rawQuery
		 */
		public void setRawQuery(String rawQuery) {
			this.rawQuery = rawQuery;
			
		}


}

