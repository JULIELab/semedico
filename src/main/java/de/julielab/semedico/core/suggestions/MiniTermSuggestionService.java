package de.julielab.semedico.core.suggestions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.core.services.query.QueryTokenizerImpl;

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

	// private final List<IConcept> diseaseItems;
	// private final List<IConcept> chemicalItems;
	// private Facet diseases;
	// private Facet chemicals;

	public MiniTermSuggestionService(Logger log, ITermService termService) {
		this.log = log;
		this.termService = termService;
		// diseaseItems = new ArrayList<>();
		// chemicalItems = new ArrayList<>();
		// FacetTerm ft;
		//
		// diseases = new Facet(NodeIDPrefixConstants.FACET + 0, "Diseases");
		// chemicals = new Facet(NodeIDPrefixConstants.FACET + 1, "Chemicals");
		//
		// int termnum = 0;
		// ft = new FacetTerm(NodeIDPrefixConstants.TERM + termnum++,
		// "Alzheimer");
		// ft.addFacet(diseases);
		// diseaseItems.add(ft);
		// ft = new FacetTerm(NodeIDPrefixConstants.TERM + termnum++,
		// "Beta-blocker");
		// ft.addFacet(chemicals);
		// chemicalItems.add(ft);
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
						concept.getQualifiers() != null ? Arrays.asList(concept.getQualifiers()) : Collections.<String>emptyList(), facet.getName(), facet.getShortName(), QueryTokenizerImpl.ALPHANUM, TokenType.CONCEPT);
			streams.add(stream);
		}
		streams.add(getKeywordSuggestion(termFragment));
		return streams;
	}

	private FacetTermSuggestionStream getKeywordSuggestion(String termFragment) {
		FacetTermSuggestionStream keywordSuggestion = new FacetTermSuggestionStream(Facet.KEYWORD_FACET);
		keywordSuggestion.addTermSuggestion(termFragment, termFragment, termFragment, null, null, Facet.KEYWORD_FACET.getName(), Facet.KEYWORD_FACET.getName(), QueryTokenizerImpl.ALPHANUM, TokenType.KEYWORD);
		return keywordSuggestion;
	}
	
	// @Override
	// public List<FacetTermSuggestionStream> getSuggestionsForFragment(String
	// termFragment,
	// List<Facet> facets) {
	// List<FacetTermSuggestionStream> streams = new ArrayList<>();
	//
	// // could be done much better of course; first get the disease
	// // suggestions, then those for chemicals. If this class should get
	// // larger, one should create a map of facets with their names and a map
	// // of the respective items. However, currently it's not clear how
	// // suggestions will look in the future, so don't overstretch this yet.
	// FacetTermSuggestionStream stream = new
	// FacetTermSuggestionStream(diseases);
	// for (IConcept concept : diseaseItems) {
	// if (concept.getPreferredName().toLowerCase().contains(termFragment)) {
	// stream.addTermSuggestion(concept.getId(), concept.getPreferredName(),
	// StringUtils
	// .join(concept.getSynonyms(), ", "),
	// concept.getQualifiers() != null ? Arrays.asList(concept.getQualifiers())
	// : Collections.<String> emptyList(), QueryTokenizerImpl.ALPHANUM);
	// }
	// }
	// streams.add(stream);
	//
	// stream = new FacetTermSuggestionStream(chemicals);
	// for (IConcept concept : chemicalItems) {
	// if (concept.getPreferredName().toLowerCase().contains(termFragment)) {
	// stream.addTermSuggestion(concept.getId(), concept.getPreferredName(),
	// StringUtils
	// .join(concept.getSynonyms(), ", "),
	// concept.getQualifiers() != null ? Arrays.asList(concept.getQualifiers())
	// : Collections.<String> emptyList(), QueryTokenizerImpl.ALPHANUM);
	// }
	// }
	// streams.add(stream);
	//
	// return streams;
	// }

	@Override
	public void createSuggestionIndex() {
		throw new NotImplementedException(
				"This is only a mini-implementation for development purposes. There is no real index connected to this service.");
	}

}
