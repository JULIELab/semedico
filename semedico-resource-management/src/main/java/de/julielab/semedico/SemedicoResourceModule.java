package de.julielab.semedico;

import IElementsAggregateIdMappingWriter.ElementsAggregateIdMappingWriter;
import de.julielab.semedico.core.services.ConfigurationSymbolProvider;
import de.julielab.semedico.core.services.Neo4jImportService;
import de.julielab.semedico.core.services.SemedicoCoreProductionModule;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import de.julielab.semedico.resources.*;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.SymbolProvider;

@ImportModule({ SemedicoCoreProductionModule.class })
public class SemedicoResourceModule {

	public static void contributeSymbolSource(@Autobuild ConfigurationSymbolProvider symbolProvider, final OrderedConfiguration<SymbolProvider> configuration) {
		configuration.add("SemedicoConfigurationSymbols", symbolProvider, "before:ApplicationDefaults");
	}
	
	public static void bind(ServiceBinder binder) {
		// From Semedico Core
		binder.bind(ITermDatabaseImportService.class, Neo4jImportService.class);
		
		// From the Semedico Resources
		binder.bind(ITermImporter.class, TermImporter.class);
		binder.bind(IMappingImporter.class, MappingImporter.class);
		binder.bind(IHypernymListCreator.class, HypernymListCreator.class);
		binder.bind(ILingpipeTermDictionaryCreator.class, LingpipeTermDictionaryCreator.class);
		binder.bind(ITermIdToFacetIdMapCreator.class, TermIdToFacetIdMapCreator.class);
		binder.bind(IFacetIdPrinter.class, FacetIdPrinter.class);
		binder.bind(ITermIdMappingCreator.class, TermIdMappingCreator.class);
		binder.bind(IEventTermDefiner.class, EventTermDefiner.class);
		binder.bind(IEventTermMappingCreator.class, EventTermMappingCreator.class);
		binder.bind(ITermChildrenUpdater.class, TermChildrenUpdater.class);
		binder.bind(IAggregateCreator.class, AggregateCreator.class);
		binder.bind(IAggregateDeleter.class, AggregateDeleter.class);
		binder.bind(IAggregatePropertyCreator.class, AggregatePropertyCreator.class);
		binder.bind(IFacetRootTermNumberSetter.class, FacetRootTermNumberSetter.class);
		binder.bind(ITermDatabaseIndexCreator.class, TermDatabaseIndexCreator.class);
		binder.bind(ITermLabelAdder.class, TermLabelAdder.class);
		binder.bind(IConfigurationAcknowledger.class, ConfigurationAcknowledger.class);
		binder.bind(IEventInstancePatternCreator.class, EventInstancePatternCreator.class);
		binder.bind(IEventFacetCreator.class, EventFacetCreator.class);
		binder.bind(IReindexer.class, Reindexer.class);
		binder.bind(IBioCPMCDevDocExtractor.class, BioCPMCDevDocExtractor.class);
		binder.bind(IElementsAggregateIdMappingWriter.class, ElementsAggregateIdMappingWriter.class);
		binder.bind(IIndexDocumentIdsWriter.class, IndexDocumentIdsWriter.class);
	}
}
