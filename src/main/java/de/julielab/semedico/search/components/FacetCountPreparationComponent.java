/**
 * FacetSearchPreparatorComponent.java
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
 * Creation date: 09.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.search.components;

import static de.julielab.semedico.core.services.interfaces.IIndexInformationService.FACETS;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * @author faessler
 * 
 */
public class FacetCountPreparationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetCountPreparation {
	}

	private final ApplicationStateManager asm;
	private final IUIService uiService;
	private final Logger log;

	public FacetCountPreparationComponent(Logger log,
			ApplicationStateManager asm, IUIService uiService) {
		this.log = log;
		this.asm = asm;
		this.uiService = uiService;

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
		SemedicoSearchCommand searchCmd = searchCarrier.searchCmd;
		UserInterfaceState uiState = asm.get(UserInterfaceState.class);
		LabelStore labelStore = uiState.getLabelStore();
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		boolean noFacetsToCountDelivered = searchCmd == null
				|| searchCmd.facetsToCount == null
				|| searchCmd.facetsToCount.size() == 0;
		
		if (null == solrCmd || StringUtils.isEmpty(solrCmd.solrQuery))
			throw new IllegalArgumentException(
					"Non-null "
							+ SolrSearchCommand.class.getName()
							+ " object is expected which knows about the actual Solr query. However, no such object is present.");
		if (noFacetsToCountDelivered
				&& labelStore.hasFacetGroupLabels(uiState
						.getSelectedFacetGroup())) {
			log.debug("Terminating search chain because the selected facet group already has its facet counts.");
			return true;
		}
		// if (null == searchCmd)
		// throw new IllegalArgumentException("Non-null "
		// + SemedicoSearchCommand.class.getName() + " is expected.");

		solrCmd.dofacet = true;
		// Global facet settings.
		FacetCommand fc = new FacetCommand();
		fc.mincount = 1;
		fc.limit = 100;
		solrCmd.addFacetCommand(fc);

		// Facet-global counts. Don't do when there are only particular facets
		// to count because then we have an update on a few facets only and no
		// new document search.
		if (noFacetsToCountDelivered) {
			fc = new FacetCommand();
			fc.fields.add(FACETS);
			solrCmd.addFacetCommand(fc);
		}

		// Facet-term counts. Default: count for selected facet group.
		FacetGroup<UIFacet> facetsToCount = noFacetsToCountDelivered ? uiState
				.getSelectedFacetGroup() : searchCmd.facetsToCount;
		Multimap<UIFacet, IFacetTerm> displayedTermsFacetGroup = uiService
				.getDisplayedTermsInFacetGroup(facetsToCount);
		for (UIFacet uiFacet : facetsToCount) {
			Collection<IFacetTerm> facetTerms = displayedTermsFacetGroup
					.get(uiFacet);

			fc = new FacetCommand();
			fc.fields.add(uiFacet.getSource().getName());

			Collection<String> termsToCount = new ArrayList<String>(
					facetTerms.size());
			for (IFacetTerm term : facetTerms) {
				String id = term.getId();

				if (labelStore.termIdAlreadyQueried(id))
					continue;

				labelStore.addQueriedTermId(id);
				termsToCount.add(id);
			}
			fc.terms = termsToCount;
			if (termsToCount.size() > 0 || uiFacet.isForcedToFlatFacetCounts()
					|| uiFacet.isFlat())
				solrCmd.addFacetCommand(fc);
		}

		return false;
	}

}
