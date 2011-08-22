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

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.StringLabel;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;

public class LabelCacheService implements ILabelCacheService {

	@SuppressWarnings("unused")
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

	private synchronized Label getCachedLabel(String id, Class<? extends Label> clazz) {
		Label ret = null;
		List<Label> labels = cache.get(id);
		if (labels.size() > 0) {
			ret = labels.get(labels.size() - 1);
			cache.remove(id, ret);
		} else {
			if (clazz.equals(TermLabel.class)) {
				IFacetTerm term = termService.getNode(id);
				ret = new TermLabel(term);
			} else {
				ret = new StringLabel(id);
			}
		}
		return ret;
	}
	
	@Override
	public synchronized Label getCachedTermLabel(String id) {
		return getCachedLabel(id, TermLabel.class);
	}

	@Override
	public synchronized void releaseLabels(Collection<Label> labels) {
		for (Label label : labels)
			cache.put(label.getId(), label);
	}

	/* (non-Javadoc)
	 * @see de.julielab.semedico.search.ILabelCacheService#getCachedStringLabel(java.lang.String)
	 */
	@Override
	public synchronized Label getCachedStringLabel(String name) {
		return getCachedLabel(name, StringLabel.class);
	}


}
