package de.julielab.semedico.core.suggestions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

/**
 * A term suggestion service that has only very few terms as a repository but
 * does not depend on a database. Used for mini-versions of Semedico for easy
 * development.
 * 
 * @author faessler
 * 
 */
public class MiniTermSuggestionService implements ITermSuggestionService {
	private ITermService termService;
	private Logger log;

	public MiniTermSuggestionService(Logger log, ITermService termService) {
		this.log = log;
		this.termService = termService;
	}

	@Override
	public List<FacetTermSuggestionStream> getSuggestionsForFragment(String termFragment,
			List<Facet> facets) {
		log.debug("Searching for concept matched by fragment \"{}\"", termFragment);
		List<FacetTermSuggestionStream> streams = new ArrayList<>();
		Multimap<Facet, IConcept> suggestionsByFacet = HashMultimap.create();
		Iterator<IConcept> terms = termService.getTerms();
		while (terms.hasNext()) {
			IFacetTerm concept = (IFacetTerm) terms.next();
			if (concept.getPreferredName().toLowerCase().contains(termFragment))
				suggestionsByFacet.put(concept.getFirstFacet(), concept);
			for (String synonym : concept.getSynonyms()) {
				if (synonym.toLowerCase().contains(termFragment))
					suggestionsByFacet.put(concept.getFirstFacet(), concept);
			}
		}

		for (Facet facet : suggestionsByFacet.keySet()) {
			Collection<IConcept> conceptSuggestions = suggestionsByFacet.get(facet);
			FacetTermSuggestionStream stream = new FacetTermSuggestionStream(facet);
			for (IConcept concept : conceptSuggestions)
				stream.addTermSuggestion(concept.getId(), concept.getPreferredName(), concept.getPreferredName(),
						concept.getSynonyms(),
						concept.getQualifiers() != null ? Arrays.asList(concept.getQualifiers()) : Collections.<String>emptyList(), facet.getName(), facet.getShortName(), "ALPHANUM", TokenType.CONCEPT);
			streams.add(stream);
		}
		streams.add(getKeywordSuggestion(termFragment));
		return streams;
	}

	private FacetTermSuggestionStream getKeywordSuggestion(String termFragment) {
		FacetTermSuggestionStream keywordSuggestion = new FacetTermSuggestionStream(Facet.KEYWORD_FACET);
		keywordSuggestion.addTermSuggestion(termFragment, termFragment, termFragment, null, null, Facet.KEYWORD_FACET.getName(), Facet.KEYWORD_FACET.getName(), "ALPHANUM", TokenType.KEYWORD);
		return keywordSuggestion;
	}

	@Override
	public void createSuggestionIndex() {
		throw new NotImplementedException(
				"This is only a mini-implementation for development purposes. There is no real index connected to this service.");
	}

}
