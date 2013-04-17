/**
 * FacetChildrenSearchPreparatorComponent.java
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
 * Creation date: 08.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IUIService;

/**
 * @author faessler
 * 
 */
public class FacetChildrenCountPreparationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetChildrenCountPreparation {
	}

	private ApplicationStateManager asm;
	private final Logger log;
	private final IUIService uiService;

	public FacetChildrenCountPreparationComponent(Logger log,
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
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		UserInterfaceState uiState = asm.get(UserInterfaceState.class);
		LabelStore labelStore = uiState.getLabelStore();
		SemedicoSearchCommand searchCmd = searchCarrier.searchCmd;
		boolean noFacetsToCountDelivered = searchCmd == null
				|| searchCmd.facetsToCount == null
				|| searchCmd.facetsToCount.size() == 0;

		if (null == solrCmd)
			throw new IllegalArgumentException(
					"This component requires a faceted Solr search already done. However, the Solr search command is was null.");
		if (!solrCmd.dofacet)
			throw new IllegalArgumentException(
					"This component only works with a faceted search command, but the Solr search command is non-faceted.");

		log.trace("Creating labels for children of displayed facet group terms.");
		long time = System.currentTimeMillis();
		FacetGroup<UIFacet> facetsToCount = noFacetsToCountDelivered ? uiState
				.getSelectedFacetGroup()
				: searchCarrier.searchCmd.facetsToCount;

		Multimap<String, String> termsToUpdate = HashMultimap.create();
		for (UIFacet facetConfiguration : facetsToCount) {
			// Already sorts out flat facets.
			uiService.storeUnknownChildrenOfDisplayedTerms(facetConfiguration,
					termsToUpdate, labelStore);
		}

		// Not entirely true but the FacetResponseProcessComponent should
		// follow and then its true.
		labelStore.setFacetGroupHasLabels(uiState.getSelectedFacetGroup());

		if (termsToUpdate.size() > 0) {
			solrCmd.rows = 0;
			solrCmd.start = 0;
			// If there is no filter query, use the original query as filter
			// query to circumvent scoring computation and other performance
			// costs. We don't care for these, we just want facet counts.
			if (null == solrCmd.solrFilterQueries) {
				solrCmd.addFilterQuery(solrCmd.solrQuery);
				solrCmd.solrQuery = "*:*";
			}
			for (FacetCommand fc : solrCmd.facetCmds) {
				// For global Solr facet commands, like facet.mincount, there is
				// no specific field name - and no specific terms to count
				// either.
				if (fc.fields.size() > 0) {
					// We only facet on single fields currently.
					fc.terms = termsToUpdate.get(fc.fields.get(0));
				}
			}

		} else {
			log.debug(
					"Terminating search chain. There are no unknown child terms of displayed terms to count ({}ms}.",
					System.currentTimeMillis() - time);
			// Terminate the chain - nothing more to do.
			return true;
		}

		// Remove facet-global counts if existent, we should already have them.
		int facetCountIndex = -1;
		for (int i = 0; i < solrCmd.facetCmds.size(); ++i) {
			FacetCommand fc = solrCmd.facetCmds.get(i);
			// fc.fields could be of zero size for global facet parameters (e.g.
			// facet.limit, facet.mincount...)
			if (fc.fields.size() > 0
					&& fc.fields.get(0).equals(IIndexInformationService.FACETS)) {
				facetCountIndex = i;
			}
		}
		if (facetCountIndex > -1)
			solrCmd.facetCmds.remove(facetCountIndex);

		return false;
	}

}
