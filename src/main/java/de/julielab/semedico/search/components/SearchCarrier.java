/**
 * SearchCarrier.java
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

import org.apache.commons.lang3.time.StopWatch;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * @author faessler
 *
 */
public class SearchCarrier {
	public QueryAnalysisCommand queryCmd;
	public SemedicoSearchCommand searchCmd;
	public SolrSearchCommand solrCmd;
	public QueryResponse solrResponse;
	public SemedicoSearchResult searchResult;
	public StopWatch sw;
	
	public SearchCarrier() {
		sw = new StopWatch();
		sw.start();
	}
	
	public void setElapsedTime() {
		if (null != searchResult)
			searchResult.elapsedTime = sw.getTime();
		sw.stop();
	}
}

