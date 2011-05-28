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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
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
	// When creating the correct term counts and distributing them to the
	// correct facet, we only know the term (its ID is given by the Solr facet
	// count). As the term knows its facet, we can get the FacetHit object to
	// add the count by this map.
	private Map<Facet, FacetHit> facetHitMap;
	// This List is used to collect the FacetHit corresponding to a particular
	// collection of FacetConfigurations, i.e. the FacetHit objects are returned
	// whose facets should be displayed to the user on the front end.
	private List<FacetHit> facetHits;

	public FacetHitCollectorService(ITermService termService,
			IFacetService facetService, ILabelCacheService labelCacheService) {
		this.termService = termService;
		this.facetService = facetService;
		this.labelCacheService = labelCacheService;
		// Initialize FacetHit objects so they don't have to be built for each
		// search anew.
		// In a similar fashion, initialize the facetHits List, so we can always
		// just return the same list instance.
		facetHitMap = new HashMap<Facet, FacetHit>();
		facetHits = new ArrayList<FacetHit>();
		for (Facet facet : facetService.getFacets()) {
			FacetHit facetHit = new FacetHit(facet);
			facetHitMap.put(facet, facetHit);
		}
	}

	@Override
	public void setFacetFieldList(List<FacetField> facetFields) {
		this.facetFields = facetFields;
		newCountRequired = true;
	}

	public List<FacetHit> collectFacetHits(
			Collection<FacetConfiguration> facetConfigurations) {
		facetHits.clear();
		for (FacetConfiguration conf : facetConfigurations)
			facetHits.add(facetHitMap.get(conf.getFacet()));

		if (newCountRequired) {
			// Reset FacetHits for collecting new facet counts.
			for (FacetHit facetHit : facetHitMap.values())
				facetHit.clear();

			for (FacetField field : facetFields) {
				// The the facet category counts, e.g. for "Proteins and Genes".
				if (field.getName().equals(IndexFieldNames.FACET_CATEGORIES)) {
					// Iterate over the actual facet counts.
					for (Count count : field.getValues()) {
						Facet facet = facetService.getFacetWithId(Integer
								.parseInt(count.getName()));
						FacetHit facetHit = facetHitMap.get(facet);
						facetHit.setTotalFacetCount(count.getCount());
					}
					// Set the facet counts aka term counts themselves.
				} else if (field.getName().equals(IndexFieldNames.FACET_TERMS)) {
					for (Count count : field.getValues()) {
						FacetTerm term = termService
								.getTermWithInternalIdentifier(count.getName());
						// TODO this (null term) can currently happen for term
						// IDs like
						// "JOURNAL ARTICLE".
						// Organize the index in a way that such things cannot
						// happen.
						if (term == null)
							continue;
						// Store the count.
						// TODO hat sich alles erledigt, muss durch die LabelMultiHierarchy gemacht werden
						Label label = labelCacheService.getCachedLabel(term);
//						label.setTerm(term);
						label.setHits(count.getCount());

						// Mark parent term as having a subterm hit.
						Label parentLabel = labelCacheService
								.getCachedLabel(term.getParent());
						if (parentLabel != null)
							parentLabel.setHasChildHits();

						FacetHit facetHit = facetHitMap.get(term.getFacet());
						facetHit.add(label);
					}
				}
			}
		}
		for (FacetHit hit : facetHits) {
			System.out.println(hit);
			for (Label term : hit)
				System.out.println(term);
		}
		return facetHits;
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
