package de.julielab.semedico.core.services;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import com.google.common.cache.LoadingCache;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.SyncFacetTerm;
import de.julielab.semedico.core.services.interfaces.IFacetTermFactory;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * A term service super class to assemble the elements that just each term
 * service in Semedico should have.
 * 
 * @author faessler
 * 
 */
public abstract class BaseConceptService implements ITermService {

	protected final Logger log;
	protected final IFacetTermFactory termFactory;

	protected LoadingCache<String, IConcept> termCache;

	public void setTermCache(LoadingCache<String, IConcept> termCache) {
		this.termCache = termCache;
	}

	public BaseConceptService(Logger log, IFacetTermFactory termFactory,
			LoadingCache<String, IConcept> termCache) {
		this.log = log;
		this.termFactory = termFactory;
		this.termCache = termCache;
	}
	
	public BaseConceptService(Logger log, IFacetTermFactory termFactory) {
		this(log, termFactory, null);
	}

	@Override
	public List<Concept> getFacetRoots(Facet facet) {
		throw new NotImplementedException(
				"Should be implemented, as it is now required, obviously.");
	}

	@Override
	public IConcept getTerm(String id) {
		if (id.startsWith(NodeIDPrefixConstants.TERM) || id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM)) {
			return termCache.getUnchecked(id);
		}
		log.warn("Unknown term id: {}, returning null", id);
		return null;
	}
	
	@Override
	public IConcept getTermSynchronously(String id) {
		IConcept term = getTerm(id);
		if (term instanceof SyncFacetTerm && term.getPreferredName() == null) {
			// check if the term was actually loaded: it must have a preferred name. So if there is non, database loading failed
				return null;
		}
		return term;
	}
	
	@Override
	public IConcept getTermIfCached(String id) {
		if (id.startsWith(NodeIDPrefixConstants.TERM) || id.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM)) {
			return termCache.getIfPresent(id);
		}
		log.warn("Unknown term id: {}, returning null", id);
		return null;
	}

	@Override
	public boolean isTermID(String termId) {
		return termId.startsWith(NodeIDPrefixConstants.TERM)
				|| termId.startsWith(NodeIDPrefixConstants.AGGREGATE_TERM);
	}

	@Override
	public boolean hasTerm(String id) {
		return null != termCache.getUnchecked(id);
	}
}
