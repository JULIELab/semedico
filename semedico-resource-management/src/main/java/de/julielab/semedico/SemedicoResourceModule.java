package de.julielab.semedico;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.SymbolProvider;

import IElementsAggregateIdMappingWriter.ElementsAggregateIdMappingWriter;
import de.julielab.semedico.core.services.ConfigurationSymbolProvider;
import de.julielab.semedico.core.services.Neo4jImportService;
import de.julielab.semedico.core.services.SemedicoCoreProductionModule;
import de.julielab.semedico.core.services.interfaces.ITermDatabaseImportService;
import de.julielab.semedico.resources.AggregateCreator;
import de.julielab.semedico.resources.AggregateDeleter;
import de.julielab.semedico.resources.AggregatePropertyCreator;
import de.julielab.semedico.resources.BioCPMCDevDocExtractor;
import de.julielab.semedico.resources.ConfigurationAcknowledger;
import de.julielab.semedico.resources.EventFacetCreator;
import de.julielab.semedico.resources.EventInstancePatternCreator;
import de.julielab.semedico.resources.EventTermDefiner;
import de.julielab.semedico.resources.EventTermMappingCreator;
import de.julielab.semedico.resources.FacetIdPrinter;
import de.julielab.semedico.resources.FacetRootTermNumberSetter;
import de.julielab.semedico.resources.HypernymListCreator;
import de.julielab.semedico.resources.IAggregateCreator;
import de.julielab.semedico.resources.IAggregateDeleter;
import de.julielab.semedico.resources.IAggregatePropertyCreator;
import de.julielab.semedico.resources.IBioCPMCDevDocExtractor;
import de.julielab.semedico.resources.IConfigurationAcknowledger;
import de.julielab.semedico.resources.IElementsAggregateIdMappingWriter;
import de.julielab.semedico.resources.IEventFacetCreator;
import de.julielab.semedico.resources.IEventInstancePatternCreator;
import de.julielab.semedico.resources.IEventTermDefiner;
import de.julielab.semedico.resources.IEventTermMappingCreator;
import de.julielab.semedico.resources.IFacetIdPrinter;
import de.julielab.semedico.resources.IFacetRootTermNumberSetter;
import de.julielab.semedico.resources.IHypernymListCreator;
import de.julielab.semedico.resources.IIndexDocumentIdsWriter;
import de.julielab.semedico.resources.ILingpipeTermDictionaryCreator;
import de.julielab.semedico.resources.IMappingImporter;
import de.julielab.semedico.resources.IReindexer;
import de.julielab.semedico.resources.ITermChildrenUpdater;
import de.julielab.semedico.resources.ITermDatabaseIndexCreator;
import de.julielab.semedico.resources.ITermIdMappingCreator;
import de.julielab.semedico.resources.ITermIdToFacetIdMapCreator;
import de.julielab.semedico.resources.ITermImporter;
import de.julielab.semedico.resources.ITermLabelAdder;
import de.julielab.semedico.resources.IndexDocumentIdsWriter;
import de.julielab.semedico.resources.LingpipeTermDictionaryCreator;
import de.julielab.semedico.resources.MappingImporter;
import de.julielab.semedico.resources.Reindexer;
import de.julielab.semedico.resources.TermChildrenUpdater;
import de.julielab.semedico.resources.TermDatabaseIndexCreator;
import de.julielab.semedico.resources.TermIdMappingCreator;
import de.julielab.semedico.resources.TermIdToFacetIdMapCreator;
import de.julielab.semedico.resources.TermImporter;
import de.julielab.semedico.resources.TermLabelAdder;

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
