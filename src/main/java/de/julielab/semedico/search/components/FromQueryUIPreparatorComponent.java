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
package de.julielab.semedico.search.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.core.taxonomy.interfaces.IPath;

/**
 * @author faessler
 * 
 */
public class FromQueryUIPreparatorComponent implements ISearchComponent {

	private ApplicationStateManager asm;

	public FromQueryUIPreparatorComponent(ApplicationStateManager asm) {
		this.asm = asm;
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
		UserInterfaceState uiState = asm.get(UserInterfaceState.class);
		SearchState searchState = asm.get(SearchState.class);
		ITermService termService = asm.get(ITermService.class);

		collapseAllFacets(uiState, searchState, termService);

		// expand menu were query was found and collapse all other
		for (FacetGroup<UIFacet> f : uiState.getFacetGroups()) {
			for (UIFacet a : f) {
				a.setCollapsed(true);
			}
		}

		for (IFacetTerm term : (searchState).getQueryTerms().values()) {
			IPath currentPath = termService.getPathFromRoot(term);

			for (Facet facet : term.getFacets()) {

				if (facet.isHierarchic()) {
					uiState.getFacetConfigurations().get(facet)
							.setCurrentPath(currentPath.copyPath());

					for (FacetGroup<UIFacet> f : uiState.getFacetGroups()) {

						if (f.contains(facet)) {

							for (UIFacet a : f) {
								if (a.equals(facet)) {

								} else {
									a.setCollapsed(true);
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private void collapseAllFacets(UserInterfaceState uiState,
			SearchState searchState, ITermService termService) {
		for (FacetGroup<UIFacet> f : uiState.getFacetGroups()) {

			for (UIFacet a : f) {
				a.setCollapsed(true);
			}
			for (UIFacet b : getTermList(uiState, searchState)) {
				if (f.contains(b)) {
					expandQueryTerms(f, b, uiState, searchState, termService);
				}
			}
		}
	}

	/*
	 * will expand the facet if it contains a query term
	 */
	private void expandQueryTerms(FacetGroup<UIFacet> group, UIFacet uifacet,
			UserInterfaceState uiState, SearchState searchState,
			ITermService termService) {
		for (IFacetTerm term : searchState.getQueryTerms().values()) {
			IPath currentPath = termService.getPathFromRoot(term);

			if (uifacet.isHierarchic()) {
				uifacet.setCurrentPath(currentPath.copyPath());
				uifacet.setCollapsed(false);
				uiState.setFirstFacet(group, uifacet);
			}
		}
		//otherNodeList(termService);
		for (IFacetTerm c : otherNodeList(termService)) {
			IPath currentPath = termService.getPathFromRoot(c);

			if (uifacet.isHierarchic()) {
				uifacet.setCurrentPath(currentPath.copyPath());
				uifacet.setCollapsed(false);
				// uiState.setFirstFacet(group,uifacet);
			}
		}
	}

	/*
	 * returns a list of all query terms
	 */
	private List<UIFacet> getTermList(UserInterfaceState uiState,
			SearchState searchState) {
		List<UIFacet> queryterms = new ArrayList<UIFacet>();
		for (IFacetTerm term : searchState.getQueryTerms().values()) {

			for (Facet facet : term.getFacets()) {
				queryterms.add(uiState.getFacetConfigurations().get(facet));
			}
		}
		return queryterms;
	}

	private List<IFacetTerm> otherNodeList(ITermService termService) {
		ArrayList<IFacetTerm> nodeList = new ArrayList<IFacetTerm>();
		nodeList.add(termService.getNode("BDLF2_EBV"));
		nodeList.add(termService.getNode("140U_DROME"));
		nodeList.add(termService.getNode("ARAB_ECK1"));
		nodeList.add(termService.getNode("143BB_XENLA"));
		nodeList.add(termService.getNode("A1AG_BOVINO"));
		System.out.println("NODELIST " + termService.getNode("BDLF2_EBV"));
		return nodeList;
	}
}
