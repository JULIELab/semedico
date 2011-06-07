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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.MultiHierarchy.LabelMultiHierarchy;
import de.julielab.semedico.core.MultiHierarchy.MultiHierarchy;
import de.julielab.semedico.core.services.ITermService;

public class LabelCacheService extends MultiHierarchy<Label> implements
		ILabelCacheService {

	private ITermService termService;

	private Set<LabelMultiHierarchy> cache;
	
	private static int nr = 0;

	public LabelCacheService(ITermService termService) {
		this.termService = termService;
		cache = new HashSet<LabelMultiHierarchy>();
		LabelCacheService.nr += 1;
	}

	// How to do proper T5 IoC logging to know if everything is alright
	// here...?!
	@Override
	public LabelMultiHierarchy getCachedHierarchy() {
		Iterator<LabelMultiHierarchy> cacheIt = cache.iterator();
		LabelMultiHierarchy ret = null;
		if (cacheIt.hasNext()) {
			ret = cacheIt.next();
			cache.remove(ret);
		} else {
			ret = new LabelMultiHierarchy(termService, this);
		}
		return ret;
	}

	@Override
	public void releaseHierarchy(LabelMultiHierarchy hierarchy) {
		if (!cache.contains(hierarchy))
			cache.add(hierarchy);
	}
}
