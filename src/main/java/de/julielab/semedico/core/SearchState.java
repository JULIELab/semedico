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
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.parsing.ParseTree;
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
		private SolrQuery query;
		// Determines how the found documents are to be ordered for display.
		private SortCriterium sortCriterium;
		// Determines whether reviews articles should be shown.
		private boolean reviewsFiltered;
		// Set to true during the rendering phase after a query was put into the
		// input field and a search had been triggered (opposed to searches by
		// clicking on terms).
		private boolean newSearch;
		private String userQueryString;
		// Stores the parsed query
		private ParseTree parseTree;
		
		private Label selectedTerm;

		@Inject
		private Logger logger;
		private IFacetTerm disambiguatedTerm;
		private String solrQueryString;
		
		private int id  = 0;
		
		
		public SearchState(int id) {
			super();
			this.queryTerms = HashMultimap.create();
			this.queryTermFacetMap = new HashMap<IFacetTerm, Facet>();
			this.query = new SolrQuery();
			this.sortCriterium = SortCriterium.DATE_AND_RELEVANCE;
			this.reviewsFiltered = false;
			this.newSearch = true;
			this.id = id;
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
		public void setDisambiguatedQuery(Multimap<String, IFacetTerm> queryTerms) {
			this.queryTerms = queryTerms;
		}

		/**
		 * Do not use this to change the map. Any changes would not be represented in the parse tree!
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
		public String getUserQueryString() {
			return userQueryString;
		}

		/**
		 * @param userQueryString
		 */
		public void setUserQueryString(String userQueryString) {
			this.userQueryString = userQueryString;
		}

		/**
		 * 
		 * @return
		 */
		public String getSolrQueryString() {
			return solrQueryString;
		}
		
		/**
		 * @param solrQueryString
		 */
		public void setSolrQueryString(String solrQueryString) {
			this.solrQueryString = solrQueryString;
		}

		/**
		 * Sets the parseTree.
		 * @param parseTree the parseTree.
		 */
		public void setParseTree(ParseTree parseTree){
			this.parseTree = parseTree;
		}
		
		/**
		 * Be careful with manipulation, as changes will not be represented in the queryTermFacetMap.
		 * @return the parseTree
		 */
		public ParseTree getParseTree(){
			return parseTree;
		}
		
		/**
		 * Removes a term from the queryTermFacetMap and the parseTree.
		 * 
		 * @param queryTerm - Term to remove.
		 * @throws Exception - If the terms node can't be removed.
		 */
		public void removeTerm(String queryTerm) throws Exception {
			if(parseTree != null && queryTerms.containsKey(queryTerm) && parseTree.contains(queryTerm)){
				queryTerms.removeAll(queryTerm);
				parseTree.remove(queryTerm);
			}
			else throw new Exception(String.format("\"%s\" could not be removed.", queryTerm));
		}
		
		/**
		 * Updates parseTree and queryTermFacetMap with the corrected spelling.
		 * @param misspelled - term to replace.
		 * @param correction - replacement.
		 * @throws Exception - If the term can't be replaced.
		 */
		public void correctSpelling(String misspelled, String correction) throws Exception{
			if(queryTerms.containsKey(misspelled) && parseTree.contains(misspelled)){
				queryTerms.putAll(correction, queryTerms.get(misspelled));
				queryTerms.removeAll(misspelled);
				parseTree.expandTerm(misspelled, correction);
			}
			else 
				logger.error("Could not replace \"{}\" with \"{}\"", misspelled, correction);
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @param selectedTerm
		 */
		@Deprecated
		public void setSelectedTerm(Label selectedTerm) {
			this.selectedTerm = selectedTerm;
		}

		/**
		 * @return the selectedTerm
		 */
		@Deprecated
		public Label getSelectedTerm() {
			return selectedTerm;
		}

		/**
		 * @param selectedTerm2
		 */
		@Deprecated
		public void setDisambiguatedTerm(IFacetTerm disambiguatedTerm) {
			this.disambiguatedTerm = disambiguatedTerm;
		}

		/**
		 * @return the disambiguatedTerm
		 */
		@Deprecated
		public IFacetTerm getDisambiguatedTerm() {
			return disambiguatedTerm;
		}
}

