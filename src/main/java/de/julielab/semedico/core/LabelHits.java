/**
 * LabelHits.java
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
 * Creation date: 29.07.2011
 **/

/**
 * 
 */
package de.julielab.semedico.core;

import java.util.HashMap;
import java.util.Map;

import de.julielab.semedico.core.MultiHierarchy.MultiHierarchyNode;
import de.julielab.semedico.search.ILabelCacheService;

/**
 * @author faessler
 * 
 */
public class LabelHits {
	private Map<String, Label> labels;
	private final ILabelCacheService labelCacheService;

	public LabelHits(ILabelCacheService labelCacheService) {
		this.labelCacheService = labelCacheService;
		this.labels = new HashMap<String, Label>();
	}

	public void addLabel(String termId, long frequency) {
		// First check, whether we already have added the label for termId. This
		// may happen when a label for a sub term is added first.
		Label label = labels.get(termId);
		if (label == null)
			label = labelCacheService.getCachedLabel(termId);
		label.setHits(frequency);
		// TODO change this to treat all parents
		// Mark the parent term as having a sub term hit. If we
		// don't already have met the parent term, we just
		// create it now and set its hits later when the loop
		// comes to it.
		MultiHierarchyNode parentTerm = label.getTerm().getFirstParent();
		Label parentLabel = labels.get(parentTerm.getId());
		if (parentLabel == null) {
			parentLabel = labelCacheService.getCachedLabel(parentTerm.getId());
			labels.put(parentTerm.getId(), parentLabel);
		}
		parentLabel.setHasChildHits();
	}

	public void releaseLabels() {
		labelCacheService.releaseHierarchy(labels.values());
		labels.clear();
	}

	/**
	 * @param label
	 */
	public void addLabel(Label label) {
		labels.put(label.getId(), label);
	}
}
