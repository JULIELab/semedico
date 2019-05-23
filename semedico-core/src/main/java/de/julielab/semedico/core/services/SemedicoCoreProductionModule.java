package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.ImportModule;

import de.julielab.semedico.core.services.interfaces.IDictionaryReaderService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;
import de.julielab.semedico.core.suggestions.TermSuggestionService;

@ImportModule(SemedicoCoreBaseModule.class)
public class SemedicoCoreProductionModule {

	public static void bind(ServiceBinder binder) {
		
		binder.bind(ITermSuggestionService.class, TermSuggestionService.class);
		binder.bind(ITermService.class, TermNeo4jService.class);
		binder.bind(IFacetService.class, FacetNeo4jService.class);
		
		binder.bind(IDictionaryReaderService.class, DictionaryReaderService.class);
	}
}
