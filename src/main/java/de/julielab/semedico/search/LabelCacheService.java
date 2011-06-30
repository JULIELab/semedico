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

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;
import de.julielab.semedico.core.MultiHierarchy.MultiHierarchy;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoSymbolProvider;

public class LabelCacheService extends MultiHierarchy<Label> implements
		ILabelCacheService {

	private Logger logger;

	private ITermService termService;

	private List<LabelMultiHierarchy> cache;

	public LabelCacheService(
			Logger logger,
			ITermService termService,
			@Symbol(SemedicoSymbolProvider.LABEL_HIERARCHY_INIT_CACHE_SIZE) int cacheSize) {
		this.logger = logger;
		this.termService = termService;
		cache = new ArrayList<LabelMultiHierarchy>(cacheSize);
	}

	@Override
	public LabelMultiHierarchy getCachedHierarchy() {
		LabelMultiHierarchy ret = null;
		if (cache.size() > 0) {
			logger.debug("Cached LabelHierarchy is returned.");
			ret = cache.get(cache.size() - 1);
			cache.remove(cache.size() - 1);
		} else {
			logger.debug("New LabelHierarchy instanciated");
			ret = new LabelMultiHierarchy(termService, this);
		}
		logger.debug("Number of cached LabelHierarchies: {}", cache.size());
		return ret;
	}

	@Override
	public void releaseHierarchy(LabelMultiHierarchy hierarchy) {
		if (!cache.contains(hierarchy)) {
			logger.debug("LabelHierarchy released into the cache.");
			cache.add(hierarchy);
		}
	}
}
