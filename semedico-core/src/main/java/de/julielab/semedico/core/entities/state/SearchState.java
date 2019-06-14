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
package de.julielab.semedico.core.entities.state;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.query.UserQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author faessler
 * 
 */

public class SearchState
{
	/**
	 * The most important information about session state: These are the query
	 * terms which determine the currently retrieved documents in the first
	 * place.
	 * It is a list in order to store multiple queries that can then be analyzed
	 * for B-term computation. The index of the query currently active forthe
	 * user is given by currentSearchNodeIndex.
	 * private List<Multimap<String, IFacetTerm>> queryTerms;
	 */
	
	private List<ParseTree> queryTerms = new ArrayList<ParseTree>();

	// Since a term can occur in multiple facets, this map stores the
	// information from which facet a particular term had been chosen by the
	// user.
	// Also a list parallel to queryTerms.
	private List<Map<IConcept, Facet>> queryTermFacetMap;
	
	// When doing a multi-node-search, each search node is done by performing a
	// normal Semedico search. This index indicates which search node is
	// currently active.
	private int activeSearchNodeIndex;

	// Determines how the found documents are to be ordered for display.
	private SortCriterium sortCriterium;

	// Determines whether reviews articles should be shown.
	private boolean reviewsFiltered;

	// Set to true during the rendering phase after a query was put into the
	// input field and a search had been triggered (opposed to searches by
	// clicking on terms).
	private boolean newSearch;

	// The string that has been passed by the user, be it a selected suggestion
	// or free text input.
	private UserQuery userQuery;

	// Stores the parsed query
	private ParseTree parseTree;

	private Label selectedTerm;

	private Logger logger = LoggerFactory.getLogger(SearchState.class);
	private IConcept disambiguatedTerm;
	/**
	 * Was historically used for author name normalization (to distinguish the
	 * temporary tables per user)
	 */
	@Deprecated
	private long id = 0;
	private List<String> bTermQueryStrings;
	/**
	 * @deprecated we remove the special selected concepts list but just add selected concepts to the query
	 */
	@Deprecated
	private List<Concept> selectedFacetConcepts;

	public SearchState()
	{
		super();
		this.queryTermFacetMap = new ArrayList<>();
		this.sortCriterium = SortCriterium.RELEVANCE;
		this.reviewsFiltered = false;
		this.newSearch = true;
		this.id = 0;
		this.activeSearchNodeIndex = -1;
		this.bTermQueryStrings = new ArrayList<>();
		this.createNewSearchNode();
	}

	/**
	 * @return the sortCriterium
	 */
	public SortCriterium getSortCriterium()
	{
		return sortCriterium;
	}

	/**
	 * @param sortCriterium the sortCriterium to set
	 */
	public void setSortCriterium(SortCriterium sortCriterium) // holt nur den Datentyp
	{
		if (null != logger)
		{
			logger.debug("Setting sort criterium to {}", sortCriterium);
		}
		this.sortCriterium = sortCriterium;
	}

	/**
	 * @return the reviewsFiltered
	 */
	public boolean isReviewsFiltered()
	{
		return reviewsFiltered;
	}

	/**
	 * @param reviewsFiltered
	 *            the reviewsFiltered to set
	 */
	public void setReviewsFiltered(boolean reviewsFiltered)
	{
		this.reviewsFiltered = reviewsFiltered;
	}

	/**
	 * @return the newSearch
	 */
	public boolean isNewSearch()
	{
		return newSearch;
	}

	/**
	 * @param newSearch
	 *            the newSearch to set
	 */
	public void setNewSearch(boolean newSearch)
	{
		this.newSearch = newSearch;
	}

	// /**
	// * @return the queryTerms
	// */
	// public Multimap<String, IFacetTerm> getQueryTerms() {
	// return queryTerms.get(activeSearchNodeIndex);
	// }

	/**
	 * @return The parse tree.
	 */
	public ParseTree getSemedicoQuery()
	{
		return queryTerms.get(activeSearchNodeIndex);
	}

	// public List<Multimap<String, IFacetTerm>> getSearchNodes() {
	// return queryTerms;
	// }

	public List<ParseTree> getSearchNodes()
	{
		return queryTerms;
	}

	// public Multimap<String, IFacetTerm> getSearchNode(int index) {
	// return queryTerms.get(index);
	// }

	public ParseTree getSearchNode(int index)
	{
		return queryTerms.get(index);
	}

	// /**
	// * @param queryTerms
	// * the queryTerms to set
	// */
	// public void setDisambiguatedQuery(Multimap<String, IFacetTerm>
	// queryTerms) {
	// this.queryTerms.set(activeSearchNodeIndex, queryTerms);
	// }

	/**
	 * @param parseTree
	 *            The parse tree to set.
	 */
	public void setDisambiguatedQuery(ParseTree parseTree)
	{
		logger.debug("Setting current query to {}", parseTree);
		this.queryTerms.set(activeSearchNodeIndex, parseTree);
	}

	/**
	 * This method starts a new search node for a multi-node-search.
	 * 
	 */
	public void createNewSearchNode()
	{
		this.queryTerms.add(null);
		this.queryTermFacetMap.add(new HashMap<>());
		this.activeSearchNodeIndex++;
	}

	/**
	 * Do not use this to change the map. Any changes would not be represented
	 * in the parse tree!
	 * 
	 * @return the queryTermFacetMap
	 */
	// public Map<IConcept, Facet> getQueryTermFacetMap() {
	// return queryTermFacetMap.get(activeSearchNodeIndex);
	// }

	/**
	 * @return the rawQuery
	 */
	public UserQuery getUserQueryString()
	{
		return userQuery;
	}

	/**
	 * @param userQuery
	 */
	public void setUserQueryString(UserQuery userQuery)
	{
		this.userQuery = userQuery;
	}


	/**
	 * Sets the parseTree.
	 * 
	 * @param parseTree
	 *            the parseTree.
	 */
	public void setParseTree(ParseTree parseTree)
	{
		this.parseTree = parseTree;
	}

	/**
	 * Be careful with manipulation, as changes will not be represented in the
	 * queryTermFacetMap.
	 * 
	 * @deprecated The ParseTree structure is now fully integrated into
	 *             semedico. Use {@link #getSemedicoQuery()} instead.
	 * @return the parseTree
	 */
	@Deprecated
	public ParseTree getParseTree()
	{
		return parseTree;
	}

	// /**
	// * Removes a term from the queryTermFacetMap and the parseTree regarding
	// the
	// * active search node.
	// *
	// * @param queryTerm
	// * - Term to remove.
	// * @throws Exception
	// * - If the terms node can't be removed.
	// */
	// public void removeTerm(String queryTerm) throws Exception {
	// Multimap<String, IFacetTerm> queryTerms = this.queryTerms
	// .get(activeSearchNodeIndex);
	// if (parseTree != null && queryTerms.containsKey(queryTerm)
	// && parseTree.contains(queryTerm)) {
	// queryTerms.removeAll(queryTerm);
	// parseTree.remove(queryTerm);
	// } else
	// throw new Exception(String.format("\"%s\" could not be removed.",
	// queryTerm));
	// }

	// /**
	// * Updates parseTree and queryTermFacetMap with the corrected spelling for
	// * the active search node.
	// *
	// * @param misspelled
	// * - term to replace.
	// * @param correction
	// * - replacement.
	// * @throws Exception
	// * - If the term can't be replaced.
	// */
	// public void correctSpelling(String misspelled, String correction)
	// throws Exception {
	// Multimap<String, IFacetTerm> queryTerms = this.queryTerms
	// .get(activeSearchNodeIndex);
	// if (queryTerms.containsKey(misspelled)
	// && parseTree.contains(misspelled)) {
	// queryTerms.putAll(correction, queryTerms.get(misspelled));
	// queryTerms.removeAll(misspelled);
	// parseTree.expandTerm(misspelled, correction);
	// } else
	// logger.error("Could not replace \"{}\" with \"{}\"", misspelled,
	// correction);
	// }

	/**
	 * Updates parseTree and queryTermFacetMap with the corrected spelling for
	 * the active search node.
	 * 
	 * @param misspelled
	 *            - term to replace.
	 * @param correction
	 *            - replacement.
	 * @throws Exception
	 *             - If the term can't be replaced.
	 */
	public void correctSpelling(String misspelled, String correction) throws Exception
	{
		ParseTree parseTree = this.queryTerms.get(activeSearchNodeIndex);
		if (parseTree.contains(misspelled))
		{
			parseTree.expandTerm(misspelled, correction);
		}
		else
		{
			logger.error("Could not replace \"{}\" with \"{}\"", misspelled, correction);
		}
	}

	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}


	public int getActiveSearchNodeIndex()
	{
		return activeSearchNodeIndex;
	}

	public void setActiveSearchNodeIndex(int index)
	{
		if (index >= queryTerms.size())
		{
			throw new IllegalArgumentException(
					"Cannot set the active search node index to "
					+ index
					+ ": Only "
					+ queryTerms.size()
					+ " search nodes exist.");
		}
		activeSearchNodeIndex = index;
	}

	/**
	 * 
	 */
	public void clear()
	{
		queryTerms.clear();
		queryTermFacetMap.clear();
		activeSearchNodeIndex = -1;
		disambiguatedTerm = null;
		userQuery = null;
		createNewSearchNode();
		sortCriterium = SortCriterium.RELEVANCE;
		if (null != selectedFacetConcepts)
		selectedFacetConcepts.clear();
	}

	/**
	 * @param targetSNIndex
	 * @param solrQueryString
	 */
	public void setBTermQueryString(int targetSNIndex, String solrQueryString)
	{
		if (bTermQueryStrings.size() <= targetSNIndex)
		{
			bTermQueryStrings.add(solrQueryString);
			if (targetSNIndex != bTermQueryStrings.size() - 1)
			{
				throw new IllegalStateException(
						"Something went wrong when adding a new b term query. The query should have the index "
								+ targetSNIndex
								+ " but it was "
								+ (bTermQueryStrings.size() - 1));
			}
		}
		else
		{
			bTermQueryStrings.set(targetSNIndex, solrQueryString);
		}
	}

	/**
	 * @param searchNodeIndex
	 * @return
	 */
	public String getBTermQuery(int searchNodeIndex)
	{
		if (searchNodeIndex < bTermQueryStrings.size())
		{
			return bTermQueryStrings.get(searchNodeIndex);
		}
		return null;
	}

}
