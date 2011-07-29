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
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;
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
			long searchTimestamp = System.currentTimeMillis();
			LabelMultiHierarchy labelHierarchy = labelCacheService
					.getCachedHierarchy();
			labelHierarchy.setLastSearchTimestamp(searchTimestamp);
			facetHit = new FacetHit(labelHierarchy);

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
					for (Count count : field.getValues()) {
						FacetTerm term = termService.getNode(count.getName());
						// TODO this (null term) can currently happen for term
						// IDs like
						// "JOURNAL ARTICLE".
						// Organize the index in a way that such things cannot
						// happen.
						if (term == null)
							continue;
						// Store the count.
						Label label = labelHierarchy.getNode(term.getId());
						// label.setTerm(term);
						label.setHits(count.getCount());
						label.setSearchTimestamp(searchTimestamp);

						// Mark parent term as having a subterm hit.
						Label parentLabel = (Label) label.getFirstParent();
						if (parentLabel != null)
							parentLabel.setHasChildHits();

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
