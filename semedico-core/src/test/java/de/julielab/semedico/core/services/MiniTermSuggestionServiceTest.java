package de.julielab.semedico.core.services;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;

public class MiniTermSuggestionServiceTest {
	private static Registry registry;

	@BeforeClass
	public static void setup() {
		registry = RegistryBuilder.buildAndStartupRegistry(SemedicoCoreMiniModule.class);
	}
	
	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
	
	@Test
	public void testSuggestions() {
		ITermSuggestionService suggestionService = registry.getService(ITermSuggestionService.class);
		List<FacetTermSuggestionStream> suggestionsForFragment = suggestionService.getSuggestionsForFragment("al", null);
		assertEquals(3, suggestionsForFragment.size());
	}
}
