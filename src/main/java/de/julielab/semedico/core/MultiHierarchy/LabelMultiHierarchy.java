/**
 * LabelMultiHierarchy.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 27.05.2011
 **/

package de.julielab.semedico.core.MultiHierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.ILabelCacheService;

/**
 * A <code>MultiHierarchy</code> with some additional algorithms useful when
 * dealing with term count labels.
 * 
 * @author faessler
 */
public class LabelMultiHierarchy extends MultiHierarchy<Label> {

	private ILabelCacheService labelCacheService;

	protected Multimap<Facet, Label> facetRoots;

	private long lastSearchTimestamp = -1;


	public LabelMultiHierarchy(ITermService termService, ILabelCacheService labelCacheService) {
		this.labelCacheService = labelCacheService;
		facetRoots = HashMultimap.create();
		mirrorHierarchy(termService);
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
	private void mirrorHierarchy(ITermService termHierarchy) {
		// Adds all roots and their subtrees to this hierarchy.
		for (FacetTerm term : termHierarchy.getRoots()) {
			copyNode(term);
		}

		// Now sort the roots according to their associated facets.
		for (Label root : getRoots()) {
			facetRoots.put(root.getTerm().getFacet(), root);
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
		if (!this.hasNode(term.getId()))
			addNode(new Label(term));
		if (term.hasChildren()) {
			for (int i = 0; i < term.getNumberOfChildren(); ++i) {
				FacetTerm child = (FacetTerm) term.getChild(i);
				if (!this.hasNode(child.getId()))
					copyNode(child);
				addParent(getNode(child.getId()), getNode(term.getId()));
			}
		}
	}

	/**
	 * Returns the children of the <code>Label</code> <code>label</code> whose
	 * associated terms were included in the last performed search.
	 * <p>
	 * The returned list is sorted downwards concerning the number of term hits.
	 * </p>
	 * 
	 * @param label
	 *            The <code>Label</code> whose children included in the last
	 *            search should be returned.
	 * @return A <code>List</code> of all children of <code>label</code> which
	 *         were included in the last search.
	 */
	public List<Label> getHitChildren(Label label) {
		List<Label> hitChildren = new ArrayList<Label>();
		for (int i = 0; i < label.getNumberOfChildren(); i++) {
			Label child = (Label) label.getChild(i);
			if (child.getSearchTimestamp() == lastSearchTimestamp)
				hitChildren.add(child);
		}
		Collections.sort(hitChildren);
		return hitChildren;
	}

	/**
	 * Returns the root Labels belonging to the Facet <code>facet</code> which
	 * were included in the last search.
	 * <p>
	 * The returned list is sorted downwards concerning the number of term hits.
	 * </p>
	 * 
	 * @param facet
	 *            The facet whose roots should be returned.
	 * @return A <code>List</code> of all roots belonging to <code>facet</code>
	 *         which were hit in the last search.
	 */
	public List<Label> getHitFacetRoots(Facet facet) {
		List<Label> hitFacetRoots = new ArrayList<Label>();
		for (Label root : facetRoots.get(facet)) {
			if (root.getSearchTimestamp() == lastSearchTimestamp)
				hitFacetRoots.add(root);
		}
		Collections.sort(hitFacetRoots);
		return hitFacetRoots;
	}

	public void setLastSearchTimestamp(long searchTimestamp) {
		lastSearchTimestamp = searchTimestamp;
	}
	
	public long getLastSearchTimestamp() {
		return lastSearchTimestamp;
	}

	public void release() {
		labelCacheService.releaseHierarchy(this);
	}

}
