/** 
 * LabelCacheService.java
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
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.MultiHierarchy.IMultiHierarchy;
import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;
import de.julielab.semedico.core.MultiHierarchy.MultiHierarchyNode;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.TermService;

public class LabelCacheService extends LabelMultiHierarchy implements
		IMultiHierarchy {

	private ITermService termService;

	protected Multimap<Facet, MultiHierarchyNode> facetRoots;
	
	public LabelCacheService(ITermService termService) {
		this.termService = termService;
		facetRoots = HashMultimap.create();
		mirrorHierarchy((TermService) termService);
	}

	/**
	 * Creates and connects <code>Label</code> nodes into this hierarchy so that
	 * the result equals <code>termHierarchy</code> structurally.
	 * <p>
	 * For each node in <code>termHierarchy</code> a node will be added to this
	 * hierarchy with same name and identifier. The new node will be connected
	 * according to the structure of the copied node.
	 * </p>
	 * 
	 * @param termHierarchy
	 */
	private void mirrorHierarchy(TermService termHierarchy) {
		// Adds all roots and their subtrees to this hierarchy.
		for (MultiHierarchyNode node : termHierarchy.getNodes()) {
			copyNode((FacetTerm) node);
		}
		
		// Now sort the roots according to their associated facets.
		for (MultiHierarchyNode root : getRoots()) {
			Label lroot = (Label)root;
			facetRoots.put(lroot.getTerm().getFacet(), lroot);
		}
	}

	/**
	 * Adds the <code>FacetTerm</code> <code>term</code> as a new
	 * <code>Label</code> to this label hierarchy.
	 * <p>
	 * Identifier and name of the <code>FacetTerm</code> are copied into the new
	 * <code>Label</code> node. For each child of <code>term</code> a new
	 * <code>Label</code> node will be created - if not yet existing - and be
	 * connected to the <code>Label</code> node associated with
	 * <code>term</code> according to the parent-child-structure given by
	 * <code>term</code>.<br/>
	 * The children of <code>term</code> will be copied and added to this
	 * hierarchy likewise. Thus, the complete sub-hierarchy below
	 * <code>term</code> will be added to this hierarchy. By calling this method
	 * on all roots of a <code>FacetTerm</code> multi-hierarchy, the entire
	 * hierarchy is duplicated as a <code>Label</code> hierarchy.
	 * </p>
	 * 
	 * @param term
	 *            The <code>FacetTerm</code> to copy to a <code>Label</code>
	 *            node in this hierarchy.
	 */
	private void copyNode(FacetTerm term) {
		if (!hasNode(term.getId()))
			addNode(new Label(term));
		if (term.hasChildren()) {
			for (int i = 0; i < term.getNumberOfChildren(); ++i) {
				FacetTerm child = (FacetTerm) term.getChild(i);
				if (!this.hasNode(child.getId()))
					copyNode(child);
				addParent(child, getNode(term.getId()));
			}
		}
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public ITermService getTermService() {
		return termService;
	}

}
