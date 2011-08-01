/** 
 * LabelHitCounterService.java
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
 * Creation date: 03.04.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;

public class FacetHitCollectorService implements IFacetHitCollectorService {
	private ITermService termService;
	private IFacetService facetService;
	private ILabelCacheService labelCacheService;

	// Part of a Solr response in which faceting has been enabled. Contains the
	// actual facet counts.
	private List<FacetField> facetFields;
	// Serves to mark whether a new List of FacetField object has been given,
	// i.e. a new search has been performed. So we know when to re-count and
	// when just to return already counted results.
	private boolean newCountRequired;

	public FacetHitCollectorService(ITermService termService,
			IFacetService facetService, ILabelCacheService labelCacheService) {
		this.termService = termService;
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
	}

	@Override
	public void setFacetFieldList(List<FacetField> facetFields) {
		this.facetFields = facetFields;
		newCountRequired = true;
	}

	public FacetHit collectFacetHits(
			Collection<FacetConfiguration> facetConfigurations) {
		FacetHit facetHit = null;
		if (newCountRequired) {
			// A map of labels for all the facet hits of the current search. It
			// will be used later to determine which facet terms have been hit
			// how often and hence should be displayed and whether a term has
			// sub term hits.
			facetHit = new FacetHit(labelCacheService, termService);

			for (FacetField field : facetFields) {
				// This field has no hit facets. When no documents were found,
				// no field will have any hits.
				if (field.getValues() == null)
					continue;
				// The the facet category counts, e.g. for "Proteins and Genes".
				else if (field.getName().equals(
						IndexFieldNames.FACET_CATEGORIES)) {
					// Iterate over the actual facet counts.
					for (Count count : field.getValues()) {
						Facet facet = facetService.getFacetWithId(Integer
								.parseInt(count.getName()));
						facetHit.setTotalFacetCount(facet, count.getCount());
					}
					// Set the facet counts aka term counts themselves.
				} else if (field.getName().equals(IndexFieldNames.FACET_TERMS)) {
					// This loop emits the term IDs which are stored in the
					// field. The order is by frequency as a default. So we
					// really can't say in advance, which terms come first. In
					// any case, the order will be independent from the term's
					// poly hierarchical structure.
					for (Count count : field.getValues()) {
						IFacetTerm term = termService.getNode(count.getName());
						// TODO this (null term) can currently happen for term
						// IDs like
						// "JOURNAL ARTICLE".
						// Organize the index in a way that such things cannot
						// happen. Because then, the term retrieval above can be
						// removed completely.
						if (term == null)
							continue;
						facetHit.addLabel(count.getName(), count.getCount());
					}
				}
			}
		}
		return facetHit;
	}

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

	public ILabelCacheService getLabelCacheService() {
		return labelCacheService;
	}

	public void setLabelCacheService(ILabelCacheService labelCacheService) {
		this.labelCacheService = labelCacheService;
	}
}
