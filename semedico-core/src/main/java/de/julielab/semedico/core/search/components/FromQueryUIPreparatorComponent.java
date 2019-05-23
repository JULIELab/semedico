/**
 * FromQueryUIPreparatorComponent.java
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
package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroupSection;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * Settings to the user interface depending on the user query.
 * 
 * @author faessler
 * 
 */
public class FromQueryUIPreparatorComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FromQueryUIPreparation {
		//
	}

	private Logger log;
	private ITermService termService;

	public FromQueryUIPreparatorComponent(Logger log, ITermService termService) {
		this.log = log;
		this.termService = termService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.components.ISearchComponent#process(de.julielab.semedico.search.components.SearchCarrier
	 * )
	 */
	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.getSearchCommand();
		if (null == searchCmd) {
			throw new IllegalStateException("Component " + FromQueryUIPreparatorComponent.class.getSimpleName()
					+ " requires the analyzed user query, but the search command - containing the query - was null.");
		}
		List<QueryToken> semedicoQuery = semCarrier.getUserQuery();
		if (null == semedicoQuery) {
			throw new IllegalStateException("Component " + FromQueryUIPreparatorComponent.class.getSimpleName()
					+ " requires a non null semedico query, but it was null.");
		}

		Set<Facet> handledFacets = new HashSet<>();
		log.debug("Drilling down facets according to query terms.");
		Map<UIFacetGroupSection, Integer> sectionTopPositions = new HashMap<>();
		AbstractUserInterfaceState uiState = semCarrier.getUiState();
		for (QueryToken node : semedicoQuery) {
			// The facets that are drilled down will also be placed to the top of their facet section.
			UIFacet uiFacetOfTerm = null;
			//			ConceptNode conceptNode = (ConceptNode) node;
			// We don't prepare facets for ambiguous terms because we don't know which one was meant.
			if (node.isAmbiguous()) {
				continue;
			}
			List<IConcept> terms = node.getTermList();
			if (null == terms || terms.isEmpty()) {
				continue;
			}

			IConcept term = terms.get(0);
			Facet facet = term.getFirstFacet();
			if (term.getConceptType() == ConceptType.KEYWORD || handledFacets.contains(facet) || facet.isFlat()) {
				continue;
			}
			Concept conceptTerm = (Concept) term;
			log.debug("Term in query: {} (ID: {})", conceptTerm.getPreferredName(), conceptTerm.getId());

			handledFacets.add(facet);
			uiFacetOfTerm = uiState.getUIFacets().get(facet);
			if (null == uiFacetOfTerm) {
				continue;
			}
			IPath rootPath = termService.getShortestRootPathInFacet(term, facet);
			log.debug(
					"Setting current path of facet {} to {} due to automatic drill-down according to query terms",
					facet.getName(), rootPath);
			if (conceptTerm.hasChildrenInFacet(facet.getId())) {
				uiFacetOfTerm.setCurrentPath(rootPath);
			} else if (rootPath.length() > 0) {
				uiFacetOfTerm.setCurrentPath(rootPath.subPath(0, rootPath.length() - 1));
			}
			// Now move the drilled-down facet to facet section top
			UIFacetGroupSection section = uiFacetOfTerm.getFacetGroupSection();
			// the section can be null if we have a no-facet and thus has no UI-equivalent
			if (null != section) {
				int positionInSection = section.indexOf(uiFacetOfTerm);
				section.moveFacet(positionInSection, getTopPositionInSection(section, sectionTopPositions));
			}
		}
		return false;
	}

	private Integer getTopPositionInSection(UIFacetGroupSection section,
			Map<UIFacetGroupSection, Integer> sectionTopPositions) {
		Integer position = sectionTopPositions.get(section);
		if (null == position) {
			position = 0;
		}
		sectionTopPositions.put(section, position + 1);
		return position;
	}

}
