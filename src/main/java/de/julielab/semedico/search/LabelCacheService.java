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

import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;

public class LabelCacheService implements ILabelCacheService {

	private Logger logger;

	private final ITermService termService;

	private ListMultimap<String, Label> cache;

	public LabelCacheService(
			Logger logger,
			ITermService termService,
			@Symbol(SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE) int cacheSize) {
		this.logger = logger;
		this.termService = termService;
		cache = ArrayListMultimap.create(termService.getNodes().size(),
				cacheSize);
	}

	@Override
	public synchronized Label getCachedLabel(String id) {
		Label ret = null;
		List<Label> labels = cache.get(id);
		if (labels.size() > 0) {
			ret = labels.get(labels.size() - 1);
			cache.remove(id, ret);
		} else {
			IFacetTerm term = termService.getNode(id);
			ret = new Label(term);
		}
		return ret;
	}

	@Override
	public synchronized void releaseHierarchy(Collection<Label> labels) {
		for (Label label : labels)
			cache.put(label.getId(), label);
	}
}
