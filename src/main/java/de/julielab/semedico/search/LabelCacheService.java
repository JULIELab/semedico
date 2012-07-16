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
import de.julielab.semedico.core.StringLabel;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.search.interfaces.ILabelCacheService;

public class LabelCacheService implements ILabelCacheService {

	private Logger logger;

	private final ITermService termService;

	private final ListMultimap<String, TermLabel> cacheTermLabels;
	private final ListMultimap<String, StringLabel> cacheStringLabels;

	public LabelCacheService(
			Logger logger,
			ITermService termService,
			@Symbol(SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE) int cacheSize) {
		this.logger = logger;
		this.termService = termService;
		int keySize = termService.getNodes()
				.size();
		cacheTermLabels = ArrayListMultimap.create(keySize, cacheSize);
		cacheStringLabels = ArrayListMultimap.create(keySize, cacheSize);
	}

	private synchronized Label getCachedLabel(
			ListMultimap<String, ? extends Label> cache, String id,
			Class<? extends Label> clazz) {
		Label ret = null;
		List<? extends Label> labels = cache.get(id);
		if (labels.size() > 0) {
			ret = labels.get(labels.size() - 1);
			cache.remove(id, ret);
		} else {
			if (clazz.equals(TermLabel.class)) {
				IFacetTerm term = termService.getNode(id);
				if (term != null)
					ret = new TermLabel(term);
				else // TODO hack...
					ret = new TermLabel(new FacetTerm("Unknown Term", "Unknown Term"));
			} else {
				ret = new StringLabel(id);
			}
		}
		return ret;
	}

	@Override
	public synchronized TermLabel getCachedTermLabel(String id) {
		return (TermLabel) getCachedLabel(cacheTermLabels, id, TermLabel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.ILabelCacheService#getCachedStringLabel(java
	 * .lang.String)
	 */
	@Override
	public synchronized StringLabel getCachedStringLabel(String name) {
		return (StringLabel) getCachedLabel(cacheStringLabels, name,
				StringLabel.class);
	}

	@Override
	public synchronized void releaseLabels(Collection<? extends Label> labels) {
		logger.debug("Caching back released labels.");
		if (labels.size() > 0) {
			if (labels.iterator().next() instanceof TermLabel)
				for (Label label : labels)
					cacheTermLabels.put(label.getId(), (TermLabel) label);
			else
				for (Label label : labels)
					cacheStringLabels.put(label.getId(), (StringLabel) label);
		}
	}
}
