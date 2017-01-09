package de.julielab.semedico.core.services;

import org.slf4j.Logger;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.IQueryTranslationService;
import de.julielab.semedico.core.services.interfaces.ISearchTermProvider;

/**
 * A search term provider for event terms. If the facet is the event facet and the concept is a Semedico concept, it is
 * assumed that this concept is an event type, e.g. 'regulation'. The returned search term will be a pattern describing
 * all regulation events but not the pure regulation concept. This is done to separate the results of event extraction
 * tools from the pure concept annotations, done e.g. by a gazetteer on string surface basis.
 * 
 * @author faessler
 * 
 */
public class EventSearchTermProvider implements ISearchTermProvider {

	private IQueryTranslationService queryTranslationService;
	private IFacetService facetService;
	private Logger log;

	public EventSearchTermProvider(Logger log, IQueryTranslationService queryTranslationService, IFacetService facetService) {
		this.log = log;
		this.queryTranslationService = queryTranslationService;
		this.facetService = facetService;

	}

	@Override
	public String getSearchTerm(IConcept concept, Facet facet) {
		if (facet.hasGeneralLabel(FacetLabels.General.EVENTS)) {
			log.debug("Got concept in events facet: ", concept);
			if (concept.isEventTrigger()) {
				// Return the search term for this event type. This short-circuits the chain-of-command, making this the
				// end result.
				String searchTerm = queryTranslationService.createSearchTermForEventType(concept);
				log.debug("Got search term {} for concept {}", searchTerm, concept);
				return searchTerm;
			}
		}
		log.debug("Did not get an event search term for concept {}, returning null.", concept);
		// null means that this component does not apply to the given concept; continue to the next component.
		return null;
	}

	@Override
	public String getSearchTerm(String termId) {
		log.error("THIS IS NOT IMPLEMENTED AND ALWAYS RETURNS NULL. Term ID: {}", termId);
		return null;
	}

}
