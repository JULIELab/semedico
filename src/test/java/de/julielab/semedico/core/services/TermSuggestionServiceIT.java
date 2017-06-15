package de.julielab.semedico.core.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.julielab.neo4j.plugins.constants.semedico.NodeIDPrefixConstants;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.AggregateTerm;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ISearchService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

// Actually, this shouldn't be ignored. The issue is that the payload field now longer exists for completion suggestions but we're still trying to read those. We need to use _source instead.
@Ignore
public class TermSuggestionServiceIT {

	private static Registry registry;
	private static ITermSuggestionService suggestionService;
	private static ISearchService searchService;

	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		suggestionService = registry.getService(ITermSuggestionService.class);
		suggestionService.createSuggestionIndex();
		searchService = registry.getService(ISearchService.class);
	}

	@Test
	public void testGetSuggestionsForFragment() throws Exception {
		List<FacetTermSuggestionStream> suggestionsForFragment = suggestionService
				.getSuggestionsForFragment("electric stochastic pc", null);
		boolean foundSuggestion = false;
		for (FacetTermSuggestionStream stream : suggestionsForFragment) {
			while (stream.incrementTermSuggestion()) {
				if (!stream.getFacetName().equals(Facet.KEYWORD_FACET.getName())) {
					foundSuggestion = true;
					assertTrue(stream.getTermName().toLowerCase().startsWith("pc"));
				}
			}
		}
		assertTrue(foundSuggestion);
	}

	@Test
	public void testSuggestionSearch() throws Exception {

		LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
				.doSuggestionSearch("mp", null).get();
		assertNotNull(searchResult);
		ArrayList<FacetTermSuggestionStream> suggestions = searchResult.suggestions;
		assertNotNull(suggestions);
		assertTrue(suggestions.size() > 0);
		for (FacetTermSuggestionStream stream : suggestions) {
			assertTrue(stream.size() > 0);
			while (stream.incrementTermSuggestion()) {
				String suggestion = stream.getTermName();
				assertTrue("Suggestion " + suggestion + " does not start with 'mp'.",
						suggestion.toLowerCase().contains("mp"));
			}
		}

		// The same as above only that now we want to restrict our search to a
		// different facet than the term above is
		// in. So there shouldn't be results
		IFacetService facetService = registry.getService(IFacetService.class);
		searchResult = (LegacySemedicoSearchResult) searchService.doSuggestionSearch("mp",
				Lists.newArrayList(facetService.getFacetById(NodeIDPrefixConstants.FACET + 8))).get();
		assertNotNull(searchResult);
		suggestions = searchResult.suggestions;
		assertNotNull(suggestions);
		assertTrue(suggestions.size() == 0);

		// This time, we look for correct facet so there SHOULD be the result
		searchResult = (LegacySemedicoSearchResult) searchService.doSuggestionSearch("mp",
				Lists.newArrayList(facetService.getFacetById(NodeIDPrefixConstants.FACET + 5))).get();
		assertNotNull(searchResult);
		suggestions = searchResult.suggestions;
		assertNotNull(suggestions);
		assertTrue(suggestions.size() > 0);
		for (FacetTermSuggestionStream stream : suggestions) {
			assertTrue(stream.size() > 0);
			while (stream.incrementTermSuggestion()) {
				assertTrue(stream.getTermName().toLowerCase().contains("mp"));
			}
		}

		searchResult = (LegacySemedicoSearchResult) searchService.doSuggestionSearch("cell", null).get();
		assertNotNull(searchResult);
		suggestions = searchResult.suggestions;
		assertNotNull(suggestions);
		assertTrue(suggestions.size() > 0);
		boolean foundCellAggregate = false;
		ITermService termService = registry.getService(ITermService.class);
		AggregateTerm aggregate = (AggregateTerm) termService.getTerm(NodeIDPrefixConstants.AGGREGATE_TERM + 1340);
		List<Concept> elements = aggregate.getElements();
		List<String> foundElementIds = new ArrayList<>();
		for (FacetTermSuggestionStream stream : suggestions) {
			while (stream.incrementTermSuggestion()) {
				// We want to get the aggregate as a suggestion...
				if (stream.getTermId().startsWith(NodeIDPrefixConstants.AGGREGATE_TERM)
						&& stream.getTermName().toLowerCase().equals("cell"))
					foundCellAggregate = true;
				// ...but not its elements!
				for (Concept aggElement : elements)
					if (aggElement.getId().equals(stream.getTermId()))
						foundElementIds.add(aggElement.getId());
			}
		}
		assertTrue("The aggregate term for 'cell' was not returned as a suggestion", foundCellAggregate);
		assertTrue("The aggregate elements " + StringUtils.join(foundElementIds, ", ")
				+ " for 'cell' were returned but shouldn't have.", foundElementIds.isEmpty());
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
}
