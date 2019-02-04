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

package de.julielab.semedico.core.search;

import java.util.Collection;
import java.util.List;

import de.julielab.semedico.core.services.interfaces.IConceptService;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.julielab.semedico.core.entities.StringLabel;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.components.data.TermLabel;
import de.julielab.semedico.core.search.interfaces.ILabelCacheService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;

public class LabelCacheService implements ILabelCacheService {

	private Logger logger;

	private final IConceptService termService;

	private final ListMultimap<String, Label> cache;

	public LabelCacheService(
			Logger logger,
			IConceptService termService,
			@Symbol(SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE) int cacheSize) {
		this.logger = logger;
		this.termService = termService;
		cache = ArrayListMultimap.create(cacheSize, cacheSize);
	}

	private synchronized Label getCachedLabel(String id, Concept term) {
		if (StringUtils.isEmpty(id) && term == null)
			throw new IllegalArgumentException(
					"One of id or term must be not null.");
		Label ret = null;
		Concept retTerm = term;
		String termId = id;
		if (StringUtils.isBlank(termId) && null != term)
			termId = term.getId();
		List<Label> labels = cache.get(termId);
		if (labels.size() > 0) {
			ret = labels.get(labels.size() - 1);
			ret.reset();
			cache.remove(termId, ret);
		} else {
			if (retTerm == null)
				retTerm = (Concept) termService.getTerm(termId);
			if (retTerm != null)
				ret = new TermLabel(retTerm);
			// Still no label? Then we have no TermLabel but any string.
			if (ret == null)
				ret = new StringLabel(termId);
			// // TODO hack...
			// ret = new TermLabel(new FacetTerm("Unknown Term",
			// "Unknown Term"));
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.interfaces.ILabelCacheService#getCachedLabel
	 * (java.lang.String)
	 */
	@Override
	public synchronized Label getCachedLabel(String id) {
		return getCachedLabel(id, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.interfaces.ILabelCacheService#getCachedLabel
	 * (de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm)
	 */
	@Override
	public synchronized Label getCachedLabel(Concept term) {
		return getCachedLabel(null, term);
	}

	@Override
	public synchronized void releaseLabels(Collection<? extends Label> labels) {
		logger.trace("Caching back {} released labels.", labels.size());
		if (labels.size() > 0) {
			for (Label label : labels) {
				cache.put(label.getId(), label);
			}
		}
	}
}
