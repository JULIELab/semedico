/**
 * SolrSearchCommand.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.search.components;

import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.SortCriterium;

/**
 * @author faessler
 * 
 */
public class SolrSearchCommand {

	public String solrQuery;
	public int start;
	public int rows;
	public boolean dofacet;
	public boolean dofacetdf;
	public boolean dohighlight;
	public List<FacetCommand> facetCmds;
	public List<HighlightCommand> hlCmds;
	public List<String> solrFilterQueries;
	public SortCriterium sortCriterium;
	public boolean filterReviews;

	/**
	 * @param fc
	 */
	public void addFacetCommand(FacetCommand fc) {
		if (null == facetCmds)
			facetCmds = new ArrayList<FacetCommand>();
		facetCmds.add(fc);
	}

	/**
	 * @param hlc
	 */
	public void addHighlightCmd(HighlightCommand hlc) {
		if (null == hlCmds)
			hlCmds = new ArrayList<HighlightCommand>();
		hlCmds.add(hlc);

	}
	
	public void addFilterQuery(String filterQuery) {
		if (null == solrFilterQueries)
			solrFilterQueries = new ArrayList<String>();
		solrFilterQueries.add(filterQuery);
	}
}