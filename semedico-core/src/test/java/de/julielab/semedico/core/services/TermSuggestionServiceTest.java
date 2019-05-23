package de.julielab.semedico.core.services;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

public class TermSuggestionServiceTest {
	
	private static Registry registry;
	private static ITermSuggestionService suggestionService;
	
	
	@BeforeClass
	public static void setup() {
		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(TestUtils.searchServerUrl));
		
		registry = TestUtils.createTestRegistry();
		suggestionService = registry.getService(ITermSuggestionService.class);
		suggestionService.createSuggestionIndex();
	}
	

	@Test
	public void testGetSuggestionsForFragment() throws Exception {
		List<FacetTermSuggestionStream> suggestionsForFragment = suggestionService.getSuggestionsForFragment("electric stochastic pc", null);
		boolean foundSuggestion = false;
		for (FacetTermSuggestionStream stream : suggestionsForFragment) {
			while(stream.incrementTermSuggestion()) {
				if (!stream.getFacetName().equals(Facet.KEYWORD_FACET.getName())) {
					foundSuggestion = true;
					assertTrue(stream.getTermName().toLowerCase().startsWith("pc"));
				}
			}
		}
		assertTrue(foundSuggestion);
	}
	
	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
}
