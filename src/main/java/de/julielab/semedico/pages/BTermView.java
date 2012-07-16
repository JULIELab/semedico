/**
 * BTermView.java
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
 * Creation date: 03.07.2012
 **/

/**
 * 
 */
package de.julielab.semedico.pages;

import java.util.List;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.google.common.collect.Multimap;

import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.StringLabel;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

/**
 * @author faessler
 * 
 */
public class BTermView {

	@SessionState
	@Property
	private BTermUserInterfaceState uiState;

	@Inject
	private IBTermService bTermService;

	@Inject
	private ITermService termService;

	@Inject
	private ILabelCacheService labelCacheService;

	@Persist
	private List<Multimap<String, IFacetTerm>> searchNodes;

	// TODO: Because the Bterm-Labels are retrieved here, reloading the
	// BTermView page results in adding all terms anew.
	void setupRender() {
		for (Multimap<String, IFacetTerm> sn : searchNodes)
			System.out.println(sn);
		List<Label> bTermLabelList = bTermService
				.determineBTermLabelList(searchNodes);
		LabelStore labelStore = uiState.getLabelStore();

		// TODO inefficient!!
		for (Label l : bTermLabelList) {
			if (termService.hasNode(l.getName())) {
				TermLabel cachedTermLabel = labelCacheService
						.getCachedTermLabel(l.getName());
				cachedTermLabel.setCount(l.getCount());
				labelStore.addTermLabel(cachedTermLabel);
			} else {
				StringLabel cachedStringLabel = labelCacheService
						.getCachedStringLabel(l.getName());
				cachedStringLabel.setCount(l.getCount());
				labelStore.addStringLabel(cachedStringLabel,
						IFacetService.BTERMS_FACET);
			}
		}
		for (FacetConfiguration configuration : uiState
				.getFacetConfigurations().values())
			labelStore.sortLabelsIntoFacet(configuration);
	}

	public void setSearchNodes(List<Multimap<String, IFacetTerm>> searchNodes) {
		this.searchNodes = searchNodes;
	}

}
