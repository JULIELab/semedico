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

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

public class LabelCacheService implements ILabelCacheService {

	private Logger logger;

	private final ITermService termService;

	private final ListMultimap<String, Label> cache;

	public LabelCacheService(
			Logger logger,
			ITermService termService,
			@Symbol(SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE) int cacheSize) {
		this.logger = logger;
		this.termService = termService;
		int keySize = termService.getNodes().size();
		cache = ArrayListMultimap.create(keySize, cacheSize);
	}

	private synchronized Label getCachedLabel(String id, IFacetTerm term) {
		if (StringUtils.isEmpty(id) && term == null)
			throw new IllegalArgumentException("One of id or term must be not null.");
		Label ret = null;
		List<Label> labels = cache.get(id);
		if (labels.size() > 0) {
			ret = labels.get(labels.size() - 1);
			cache.remove(id, ret);
		} else {
			if (term == null)
				term = termService.getNode(id);
			if (term != null)
				ret = new TermLabel(term);
			// else
			// // TODO hack...
			// ret = new TermLabel(new FacetTerm("Unknown Term",
			// "Unknown Term"));
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see de.julielab.semedico.search.interfaces.ILabelCacheService#getCachedLabel(java.lang.String)
	 */
	@Override
	public synchronized Label getCachedLabel(String id) {
		return getCachedLabel(id, null);
	}

	/*
	 * (non-Javadoc)
	 * @see de.julielab.semedico.search.interfaces.ILabelCacheService#getCachedLabel(de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm)
	 */
	@Override
	public synchronized Label getCachedLabel(IFacetTerm term) {
		return getCachedLabel(null, term);
	}

	@Override
	public synchronized void releaseLabels(Collection<? extends Label> labels) {
		logger.trace("Caching back {} released labels.", labels.size());
		if (labels.size() > 0) {
			for (Label label : labels)
				cache.put(label.getId(), (TermLabel) label);
		}
	}
}
