/**
 * IndexInformationService.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 15.11.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * @author faessler
 * 
 */
public class IndexInformationService implements IIndexInformationService {

	private String[] btermFieldNames;

	private final IFacetService facetService;

	/**
	 * 
	 */
	public IndexInformationService(IFacetService facetService) {
		this.facetService = facetService;
		buildBTermFiledNameArray();
	}

	/**
	 * Gathers the names of all Solr index fields which may yield B-terms. These
	 * are all facets in the B-term facet group list retrieved by the facet
	 * service as well as the fields holding synonyms, hypernyms and specialist
	 * lexicon entries.
	 */
	private void buildBTermFiledNameArray() {
		List<String> bTermFieldNames = new ArrayList<String>();
		List<FacetGroup<Facet>> facetGroupsBTerms = facetService
				.getFacetGroupsBTerms();
		for (FacetGroup<Facet> fg : facetGroupsBTerms) {
			for (Facet f : fg) {
				String sourceName = f.getSource().getName();
				if (null != sourceName)
					bTermFieldNames.add(sourceName);
			}
		}
		bTermFieldNames.add(BTERMS_ABSTRACT_HYPERNYMS);
		bTermFieldNames.add(BTERMS_ABSTRACT_SPECIALIST);
		bTermFieldNames.add(BTERMS_ABSTRACT_SYNONYMS);
		bTermFieldNames.add(BTERMS_TITLE_HYPERNYMS);
		bTermFieldNames.add(BTERMS_TITLE_SPECIALIST);
		bTermFieldNames.add(BTERMS_TITLE_SYNONYMS);

		btermFieldNames = bTermFieldNames.toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.interfaces.IIndexInformationService
	 * #getBTermFieldNames()
	 */
	@Override
	public String[] getBTermFieldNames() {
		return btermFieldNames;
	}

}
