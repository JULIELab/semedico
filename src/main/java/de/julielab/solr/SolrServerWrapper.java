/**
 * SolrServerWrapper.java
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
 * Creation date: 27.04.2011
 **/

package de.julielab.solr;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * TODO insert description
 * @author faessler
 */
public class SolrServerWrapper implements ISolrServerWrapper {

	private SolrServer solr;
	
	public SolrServerWrapper(SolrServer solr) {
		this.solr = solr;
	}

	@Override
	public QueryResponse query(SolrQuery query) throws SolrServerException {
		return solr.query(query);
	}

}
