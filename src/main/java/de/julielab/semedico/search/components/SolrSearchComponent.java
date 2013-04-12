/**
 * SolrSearchComponent.java
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

import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.DATE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.tapestry5.ioc.annotations.InjectService;

import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * @author faessler
 * 
 */
public class SolrSearchComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SolrSearch {
	}

	private static final String REVIEW_TERM = "Review";
	private final SolrServer solr;

	/**
	 * 
	 */
	public SolrSearchComponent(@InjectService("SolrSearcher") SolrServer solr) {
		this.solr = solr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.components.ISearchComponent#process(de.julielab
	 * .semedico.search.components.SearchCarrier)
	 */
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		if (null == solrCmd)
			throw new IllegalArgumentException("A "
					+ SolrSearchCommand.class.getName()
					+ " is required for a Solr search, but none is present.");

		SolrQuery q = new SolrQuery(solrCmd.solrQuery);
		if (null != solrCmd.solrFilterQueries)
			for (String filterQuery : solrCmd.solrFilterQueries)
				q.addFilterQuery(filterQuery);
		q.setStart(solrCmd.start);
		q.setRows(solrCmd.rows);

		if (solrCmd.dofacet) {
			q.setFacet(true);
			for (FacetCommand fc : solrCmd.facetCmds) {
				// For global facet settings.
				if (fc.fields.size() == 0)
					configureFacets(q, fc, null);
				for (String facetField : fc.fields) {
					configureFacets(q, fc, facetField);
				}
			}
		}
		if (solrCmd.dofacetdf)
			q.set("facetdf", "true");

		if (solrCmd.dohighlight) {
			q.setHighlight(true);
			if (solrCmd.hlCmds.size() > 1)
				throw new IllegalArgumentException(
						"Support for multiple highlight commands (multiple fields highlighted differently) is currently not implemented.");
			HighlightCommand hlc = solrCmd.hlCmds.get(0);
			for (String hlField : hlc.fields)
				q.addHighlightField(hlField);
			if (hlc.fragsize != Integer.MIN_VALUE)
				q.setHighlightFragsize(hlc.fragsize);
			if (hlc.snippets != Integer.MIN_VALUE)
				q.setHighlightSnippets(hlc.snippets);
			if (!StringUtils.isEmpty(hlc.pre))
				q.setHighlightSimplePre(hlc.pre);
			if (!StringUtils.isEmpty(hlc.post))
				q.setHighlightSimplePost(hlc.post);
		}

		if (null != solrCmd.sortCriterium) {
			// Sorting
			switch (solrCmd.sortCriterium) {
			case DATE:
				q.setSortField("date", ORDER.desc);
				break;
			case DATE_AND_RELEVANCE:
				q.set("sort", DATE + " desc,score desc");
				break;
			case RELEVANCE:
				q.setSortField("score", ORDER.desc);
			}
		}

		if (solrCmd.filterReviews) {
			q.addFilterQuery(IIndexInformationService.FACET_PUBTYPES + ":"
					+ REVIEW_TERM);
		}

		try {
			QueryResponse queryResponse = solr.query(q);
			searchCarrier.solrResponse = queryResponse;
		} catch (SolrServerException e) {
			throw new RuntimeException(
					"A Solr error occurred when querying the Solr server. ", e);
		}

		return false;
	}

	/**
	 * If there is non-empty facetField given, all facet commands will be
	 * relative to this facetField. Otherwise, the facet commands will be
	 * global.
	 * 
	 * @param q
	 * @param fc
	 * @param facetField
	 */
	private void configureFacets(SolrQuery q, FacetCommand fc, String facetField) {
		if (!StringUtils.isEmpty(facetField)) {
			if (null == fc.terms || 0 == fc.terms.size())
				q.add("facet.field", facetField);
			else
				q.add("facet.field",
						"{!terms=" + StringUtils.join(fc.terms, ",") + "}"
								+ facetField);
			// Append a period for following facet field specific commands
			// below.
			facetField += ".";
		} else
			facetField = "";

		if (fc.mincount != Integer.MIN_VALUE)
			q.add("facet." + facetField + "mincount",
					String.valueOf(fc.mincount));
		if (fc.limit != Integer.MIN_VALUE)
			q.add("facet." + facetField + "limit", String.valueOf(fc.limit));
		if (!StringUtils.isEmpty(fc.sort))
			q.add("facet." + facetField + "sort", fc.sort);
	}

}
