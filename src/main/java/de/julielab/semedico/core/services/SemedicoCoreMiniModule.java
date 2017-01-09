package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.ImportModule;

import de.julielab.semedico.core.lingpipe.IDictionaryReaderService;
import de.julielab.semedico.core.lingpipe.MiniDictionaryReaderService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;
import de.julielab.semedico.core.suggestions.MiniTermSuggestionService;

@ImportModule(SemedicoCoreBaseModule.class)
public class SemedicoCoreMiniModule {
	public static void bind(ServiceBinder binder) {
		
		binder.bind(ITermSuggestionService.class, MiniTermSuggestionService.class);
		binder.bind(IFacetService.class, MiniFacetService.class);
		binder.bind(ITermService.class, MiniTermService.class);
		
		binder.bind(IDictionaryReaderService.class, MiniDictionaryReaderService.class);
	}
}
