package de.julielab.semedico.core.services;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.search.services.ISearchTermProvider;
import de.julielab.semedico.core.services.interfaces.IConceptService;

/**
 * TODO Repair this class.
 */
public class CoreTermSearchTermProvider implements ISearchTermProvider {

	private IConceptService termService;
	private Logger log;

	public CoreTermSearchTermProvider(Logger log, IConceptService termService) {
		this.log = log;
		this.termService = termService;

	}

	@Override
	public String getSearchTerm(IConcept concept, Facet facet) {
		if (concept.getConceptType() != ConceptType.CORE) {
			log.debug("Got non-core concept {}, returning null.", concept);
			return null;
		}
		log.debug("Got core concept {}, search term.", concept);
		return getSearchTerm(concept.getId());
	}

	@Override
	public String getSearchTerm(String termId) {
		log.debug("Term ID: {}", termId);
		IConcept term = null;
		if (termService.isConceptID(termId))
			term = termService.getTerm(termId);
		if (null == term || !term.isCoreTerm())
			return null;

		CoreConcept coreTerm = (CoreConcept) term;
//
//		String searchTerm = queryTranslationService.createSearchTermForCoreTerm(coreTerm);
//		log.debug("Got search term {}", searchTerm);
//		return searchTerm;
		throw new NotImplementedException();
	}

}
