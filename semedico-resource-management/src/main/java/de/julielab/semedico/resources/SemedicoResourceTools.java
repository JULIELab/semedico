/**
 * Neo4jTest.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 28.07.2011
 **/

/**
 * 
 */
package de.julielab.semedico.resources;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.semedico.SemedicoResourceModule;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.suggestions.ITermSuggestionService;
import de.julielab.semedico.resources.IBioCPMCDevDocExtractor.IdSource;

/**
 * @author faessler
 * 
 */
public class SemedicoResourceTools {

	private static Registry registry;

	public static void main(String[] args) throws Exception {
		Stopwatch w = Stopwatch.createStarted();

		ParserProperties parserProperties = ParserProperties.defaults();
		parserProperties.withUsageWidth(120);
		CLIOptions options = new CLIOptions();
		CmdLineParser parser = new CmdLineParser(options, parserProperties);
		parser.parseArgument(args);
		// if( options.arguments.isEmpty() )
		// throw new CmdLineException(parser, new
		// IllegalArgumentException("No arguments given."));
		if (args.length == 0) {
			parser.printUsage(System.out);
			System.exit(1);
		}

		CLIOptions.Mode mode = CLIOptions.getMode(options);

		String outputFilePath;
		try {
			switch (mode) {

			case ERROR:
			case PRINT_HELP:
				parser.printUsage(System.out);
				break;
			case AUTHORIMPORT:
				System.err.println("To be created, not yet implemented.");
				break;
			case HYPERNYMS: {
				Registry registry = getRegistry();
				outputFilePath = options.hypernymOutputFile[0];
				String termLabel = null;
				if (options.hypernymOutputFile.length > 1) {
					termLabel = options.hypernymOutputFile[1];
					if (termLabel.equals("null"))
						termLabel = null;
				}
				String[] facetLabels = new String[0];
				if (options.hypernymOutputFile.length > 2) {
					facetLabels = new String[options.hypernymOutputFile.length - 2];
					System.arraycopy(options.hypernymOutputFile, 2, facetLabels, 0, facetLabels.length);
				}
				IHypernymListCreator hypernymListCreator = registry.getService(IHypernymListCreator.class);
				hypernymListCreator.writeHypernymList(outputFilePath, termLabel, facetLabels);
				registry.shutdown();
				break;
			}
			case ADD_TERM_LABELS: {
				Registry registry = getRegistry();
				ITermLabelAdder termLabelAdder = registry.getService(ITermLabelAdder.class);
				String[] parameters = options.addTermLabelParameters;
				String idFile = parameters[0];
				String idProperty = parameters[1];
				String labelString = parameters[2];
				String[] labels = labelString.split(",");
				String originalId = null;
				if (parameters.length > 3)
					originalId = parameters[3];
				termLabelAdder.addTermLabels(idFile, idProperty, originalId, labels);
				registry.shutdown();
				break;
			}
			case LINGPIPE_DICT: {
				Registry registry = getRegistry();
				ILingpipeTermDictionaryCreator termDictionaryCreator = registry
						.getService(ILingpipeTermDictionaryCreator.class);
				String[] parameters = options.dictOutputFile;
				outputFilePath = parameters[0];
				String csvLabels = null;
				String label = null;
				String[] excludeLabels = null;
				String[] properties = null;
				if (parameters.length > 1) {
					csvLabels = parameters[1];
					String[] labels = csvLabels.split(",");
					label = labels[0];
					if (labels.length > 1) {
						excludeLabels = new String[labels.length - 1];
						System.arraycopy(labels, 1, excludeLabels, 0, labels.length - 1);
					}
				}
				if (parameters.length > 2) {
					properties = parameters[2].split(",");
				}
				termDictionaryCreator.writeLingpipeDictionary(outputFilePath, label, excludeLabels, properties);
				registry.shutdown();
				break;
			}
			case TERM2FACET: {
				Registry registry = getRegistry();
				ITermIdToFacetIdMapCreator termIdToFacetIdMapCreator = registry
						.getService(ITermIdToFacetIdMapCreator.class);
				String[] params = options.t2fOutputFile;
				outputFilePath = params[0];
				String label = null;
				if (params.length > 1)
					label = params[1];
				termIdToFacetIdMapCreator.writeMapping(outputFilePath, label);
				registry.shutdown();
				break;
			}
			case TERMIMPORT: {
				Registry registry = getRegistry();
				ITermImporter termImporter = registry.getService(ITermImporter.class);
				// This file could be an XML file with Semedico terms or a
				// directory with BioPortal classes as downloaded by
				// BioPortalOntologiesNewAPI.
				String[] sourceFiles = options.termFilePaths;
				termImporter.importTerms(sourceFiles);
				registry.shutdown();
				break;
			}
			case MAPPING_IMPORT: {
				Registry registry = getRegistry();
				IMappingImporter mappingImporter = registry.getService(IMappingImporter.class);
				String sourceFile = options.mappingFilePath[0];
				Set<String> allowedAcronyms = new HashSet<>();
				for (int i = 1; i < options.mappingFilePath.length; ++i)
					allowedAcronyms.add(options.mappingFilePath[i]);
				mappingImporter.importMappings(sourceFile, allowedAcronyms);
				registry.shutdown();
				break;
			}
			case TERM_ID_MAPPING: {
				Registry registry = getRegistry();
				ITermIdMappingCreator termIdMappingCreator = registry.getService(ITermIdMappingCreator.class);
				String[] parameters = options.termsrc2idOutputFile;
				String outputFile = parameters[0];
				String idProperty = null;
				String[] labels = null;
				if (parameters.length > 1) {
					idProperty = parameters[1];
					if (parameters.length > 2)
						labels = Arrays.copyOfRange(parameters, 2, parameters.length);
				}
				if (null == idProperty)
					idProperty = ConceptConstants.PROP_SRC_IDS;
				termIdMappingCreator.writeIdMapping(outputFile, idProperty, labels);
				registry.shutdown();
				break;
			}
			case EVENT_DEFINITION: {
				Registry registry = getRegistry();
				IEventTermDefiner eventDefiner = registry.getService(IEventTermDefiner.class);
				String eventTermDefinitionFile = options.eventTermDefinitionFile;
				eventDefiner.defineEventTerms(eventTermDefinitionFile);
				registry.shutdown();
				break;
			}
			case EVENT_MAPPING: {
				Registry registry = getRegistry();
				IEventTermMappingCreator eventTermMappingCreator = registry.getService(IEventTermMappingCreator.class);
				File eventTermMappingOutputFile = options.eventTermMappingOutputFile;
				eventTermMappingCreator.writeEventTermMapping(eventTermMappingOutputFile);
				registry.shutdown();
				break;
			}
			case CREATE_EVENT_FACETS: {
				Registry registry = getRegistry();
				IEventFacetCreator eventFacetCreator = registry.getService(IEventFacetCreator.class);
				eventFacetCreator.createEventFacets();
				registry.shutdown();
				break;
			}
			case CREATE_EVENT_TERM_PATTERNS: {
				Registry registry = getRegistry();
				IEventInstancePatternCreator eventInstancePatternCreator = registry
						.getService(IEventInstancePatternCreator.class);
				File outputFile = options.eventTermPatternOutputFile;
				eventInstancePatternCreator.writeEventTermPatterns(outputFile);
				registry.shutdown();
				break;
			}
			case PRINT_MAX_DEGREE:
				System.err.println("To be created, not yet implemented.");
				break;
			case PRINT_FACET_IDS:
				Registry registry = getRegistry();
				IFacetIdPrinter printer = registry.getService(IFacetIdPrinter.class);
				printer.printFacetIds();
				registry.shutdown();
				break;
			case UPDATE_CHILDREN_PROPERTY: {
				registry = getRegistry();
				ITermChildrenUpdater childrenUpdater = registry.getService(ITermChildrenUpdater.class);
				childrenUpdater.updateChildrenInformation();
				registry.shutdown();
				break;
			}
			case CREATE_AGGREGATES: {
				registry = getRegistry();
				IAggregateCreator aggregateCreator = registry.getService(IAggregateCreator.class);
				String termLabel = null;
				if (options.arguments != null && !options.arguments.isEmpty()) {
					termLabel = options.arguments.get(0);
				}
				aggregateCreator.createAggregates(Sets.newHashSet("LOOM"), termLabel,
						TermLabels.GeneralLabel.MAPPING_AGGREGATE.name());
				registry.shutdown();
				break;
			}
			case DELETE_AGGREGATES: {
				registry = getRegistry();
				IAggregateDeleter aggregateDeleter = registry.getService(IAggregateDeleter.class);
				aggregateDeleter.deleteAggregates(TermLabels.GeneralLabel.MAPPING_AGGREGATE.name());
				registry.shutdown();
				break;
			}
			case CREATE_AGGREGATE_PROPERTIES: {
				registry = getRegistry();
				IAggregatePropertyCreator aggregatePropertyCreator = registry
						.getService(IAggregatePropertyCreator.class);
				aggregatePropertyCreator.createAggregateProperties();
				registry.shutdown();
				break;
			}
			case SET_FACET_ROOT_TERM_NUMBER: {
				registry = getRegistry();
				IFacetRootTermNumberSetter facetRootTermNumberSetter = registry
						.getService(IFacetRootTermNumberSetter.class);
				facetRootTermNumberSetter.setFacetRootTermNumbers();
				registry.shutdown();
				break;
			}
			case CREATE_INDEXES: {
				registry = getRegistry();
				ITermDatabaseIndexCreator indexCreator = registry.getService(ITermDatabaseIndexCreator.class);
				indexCreator.createdIndexes();
				registry.shutdown();
				break;
			}
			case CREATE_SUGGESTIONS: {
				registry = getRegistry();
				ITermSuggestionService termSuggestionService = registry.getService(ITermSuggestionService.class);
				termSuggestionService.createSuggestionIndex();
				registry.shutdown();
				break;
			}
			case CHECK_CONFIG: {
				registry = getRegistry();
				IConfigurationAcknowledger acknowledger = registry.getService(IConfigurationAcknowledger.class);
				int response = acknowledger.acknowledgeConfiguration();
				registry.shutdown();
				if (response != 0) {
					System.out.println("Configuration was not accepted, aborting with exit code " + response);
					System.exit(response);
				}
				break;
			}
			case REINDEX: {
				registry = getRegistry();
				String[] indexNames = options.indexNames;
				if (indexNames.length != 2)
					throw new IllegalArgumentException(
							"You have to provide the source and the target index for reindexing.");
				String source = indexNames[0];
				String target = indexNames[1];
				IReindexer reindexer = registry.getService(IReindexer.class);
				reindexer.reindex(source, target);
				registry.shutdown();
				break;
			}
			case PMC_DOC_EXTRACTION: {
				// registry = getRegistry();
				String[] params = options.biocPmcExtractionParams;
				if (params.length != 4)
					throw new IllegalArgumentException(
							"You have to provide an input directory, output file, selection ID file and the ID source.");
				String inputDir = params[0];
				String outputFile = params[1];
				Set<String> selectionIds = new HashSet<>(FileUtils.readLines(new File(params[2]), "UTF-8"));
				IdSource source = BioCPMCDevDocExtractor.IdSource.valueOf(params[3]);
				// IBioCPMCDevDocExtractor extractor =
				// registry.getService(IBioCPMCDevDocExtractor.class);
				BioCPMCDevDocExtractor.extractSemedicoDevDocuments2(new File(inputDir), new File(outputFile),
						selectionIds, source);
				// registry.shutdown();
				break;
			}
			case ELE2AGG: {
				registry = getRegistry();
				IElementsAggregateIdMappingWriter writer = registry.getService(IElementsAggregateIdMappingWriter.class);
				if (options.elementsToAggregateIds.length != 2)
					throw new IllegalArgumentException(
							"You have to provide the output file path and the aggregate label for whose elements to create the mapping file.");
				File outputFile = new File(options.elementsToAggregateIds[0]);
				String aggregateLabel = options.elementsToAggregateIds[1];
				writer.writeMapping(aggregateLabel, outputFile);
				registry.shutdown();
				break;
			}
			case WRITE_DOCUMENT_IDS: {
				registry = getRegistry();
				IIndexDocumentIdsWriter idsWriter = registry.getService(IIndexDocumentIdsWriter.class);
				idsWriter.writeDocumentIdsInIndex();
				registry.shutdown();
				break;
			}
			default:
				System.err.println("Unknown option mode " + mode
						+ ". This is an internal error that should not happen. Sorry :-(");
				break;
			}
		} catch (Exception e) {
			if (null != registry)
				registry.shutdown();
			throw e;
		}

		w.stop();
		System.out.println("Time elapsed: " + w.elapsed(TimeUnit.SECONDS) + " seconds (" + w.elapsed(TimeUnit.MINUTES)
				+ " minutes).");
	}

	private static Registry getRegistry() {
		registry = RegistryBuilder.buildAndStartupRegistry(SemedicoResourceModule.class);
		return registry;
	}

}
