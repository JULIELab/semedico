package de.julielab.semedico.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;

public class CLIOptions {
	/**
	 * We expect one or two arguments. The first is a directory of the original
	 * Semedico XML files, just one of those files or a BioPortal ontology
	 * download directory as created by julielab-bioportal-ontology-tools.
	 * The second argument only applies for the Semedico term directory and is
	 * the path to the MeSH Supplementary Concepts file. The supplementals
	 * concept that have parents in the Semedico MeSH are then appended to the
	 * corresponding terms.
	 * corresponding terms.
	 */
	@Option(name = "-ti", aliases = {
			"--termimport" }, usage = "Import terms into the Semedico term database.", metaVar = "<Term source and related data. Possible are: Semdico XML dir + Supplementary MeSH Concepts; Ontology Names Directory + Ontology Info Directory, allowed acronyms; NCBI gene_info, organisms.taxid, names.dmp, gene2Summary>", handler = StringArrayOptionHandler.class, forbids = {
					"-h", "-ld", "-t2f", "-mi", "-ca", "-da" })
	String[] termFilePaths;

	@Option(name = "-mi", aliases = {
			"--mappingimport" }, usage = "Import mappings into the Semedico term database.", metaVar = "<Mapping file or directory containing mappings. Mapping directories will be filtered for .dat files.> <allowed acronyms>", handler = StringArrayOptionHandler.class, forbids = {
					"-h", "-ld", "-t2f", "-ti", "-ca", "-da" })
	String[] mappingFilePath;

	@Option(name = "-cs", aliases = { "--createsuggestions" }, usage = "Creates the suggestion index.", forbids = {
			"-h", "-ld", "-t2f", "-ti", "-ca", "-da" })
	Boolean createSuggestionIndex;

	@Option(name = "-ca", aliases = {
			"--createaggregates" }, usage = "Creates term aggregates within the term database according to formely imported mappings (see the -mi option). A term label can be delivered which will then determine the set of terms considered for aggregate creation. Defaults to all terms.", metaVar = "[Term label to restrict aggregate creation to]", forbids = {
					"-h", "-ld", "-t2f", "-ti", "-da", "-mi" })
	Boolean createAggregates;

	@Option(name = "-frtn", aliases = {
			"--facetroottermnumber" }, usage = "Sets for all facets the number of root terms they have.", forbids = {
					"-h", "-ld", "-t2f", "-ti", "-da", "-mi" })
	Boolean setFacetRootTermNumber;

	@Option(name = "-atl", aliases = {
			"--addtermlabels" }, usage = "Adds labels to terms identified via a list of term IDs.", metaVar = "<ID file> <ID property> <labels, comma separated> <original source if using original ID>", handler = StringArrayOptionHandler.class, forbids = {
					"-h", "-ld", "-t2f", "-ti", "-da", "-mi" })
	String[] addTermLabelParameters;

	@Option(name = "-da", aliases = {
			"--deleteaggregates" }, usage = "Deletes ALL aggregates from the term database.", forbids = { "-h", "-ld",
					"-t2f", "-ti", "-ca", "-mi" })
	Boolean deleteAggregates;

	@Option(name = "-cap", aliases = {
			"--createaggregateproperties" }, usage = "Creates properties for aggregate terms by copying them from their element terms.", forbids = {
					"-h", "-ld", "-t2f", "-ti", "-ca", "-mi" })
	Boolean createAggregateProperties;

	@Option(name = "-ci", aliases = {
			"--createindexes" }, usage = "Creates database indexes for quicker access to particular database elements like terms and facets.", forbids = {
					"-h", "-ld", "-t2f", "-ti", "-ca", "-mi" })
	Boolean createIndexes;

	@Option(name = "-hy", aliases = {
			"--hypernyms" }, usage = "Export the LuCas hypernym file out of the database and store it to a file. You have to specify a term label to include for hypernyms generation. If set to the string 'null', no restriction will be used. You may specify facet labels to perform hypernyms file creation only for facets with those labels.", metaVar = "<hypernyms output file> <termLabel> [facetlabel1 facetlabel2 ...]", handler = StringArrayOptionHandler.class)
	String[] hypernymOutputFile;

	@Deprecated
	@Option(name = "-etp", aliases = {
			"--eventtermpatterns" }, usage = "Creates a file listing patterns to identify event term strings and maps them to the event facet the terms, matching the respective pattern, belong to.", metaVar = "<pattern output file> <termLabel> [facetlabel1 facetlabel2 ...]")
	File eventTermPatternOutputFile;

	@Deprecated
	@Option(name = "-cef", aliases = {
			"--createeventfacets" }, usage = "Create event facets induced by event terms defined before.", forbids = {
					"-h", "-ld", "-t2f", "-ti", "-ca", "-mi" })
	Boolean createEventFacets;

	@Option(name = "-t2f", aliases = {
			"--termid2facetid" }, usage = "Creates a list that contains for each term in the database a mapping to the ID of its facets.", handler = StringArrayOptionHandler.class, metaVar = "<term2facet map output file> <term label to create map for>")
	String[] t2fOutputFile;

	@Option(name = "-ld", aliases = {
			"--lingpipedict" }, usage = "Creates a term dictionary for the Lingpipe chunker. Used for user query analysis.", metaVar = "<dictionary output file> [<label,exclusion label1,exclusion label2,...>] [property1,property2,...]", handler = StringArrayOptionHandler.class)
	String[] dictOutputFile;

	@Option(name = "-tid", aliases = {
			"--termsidmapping" }, usage = "Creates a file mapping term property values - e.g. its source ID -  to database term IDs. The default property is "
					+ ConceptConstants.PROP_SRC_IDS
					+ ". [well actually this should only be done for mesh and uniprot because all other terms are directly recognized a tids, right?]", metaVar = "<id mapping output file> <term ID property> <term labels for which to create the mapping>", handler = StringArrayOptionHandler.class)
	String[] termsrc2idOutputFile;

	@Option(name = "-ri", aliases = {
			"--reindex" }, usage = "Reindexes an ElasticSearch index to another ElasticSearch index. This is useful when the mapping (the data schema) is to change, but the underlying data stays the same. This operation requires the special _source field to be activated at the source index.", metaVar = "<source index name> <target index name>", handler = StringArrayOptionHandler.class)
	String[] indexNames;

	@Option(name = "-def", aliases = {
			"--defineeventterms" }, usage = "Marks terms in the database as being event-terms according to a definition file.", metaVar = "<event term definition file>")
	String eventTermDefinitionFile;

	@Option(name = "-el2agg", aliases = {
			"--elementsToAggregateIds" }, usage = "Write a mapping file from element IDs to aggregate IDs for the given aggregate label.", metaVar = "<output file> <aggregate label>", handler = StringArrayOptionHandler.class)
	String[] elementsToAggregateIds;

	@Option(name = "-tem", aliases = {
			"--termeventmapping" }, usage = "Writes the mapping file from event specificTypes to term IDs.", metaVar = "<event term mapping file>")
	File eventTermMappingOutputFile;

	@Option(name = "-pmc", aliases = {
			"--pmcdevdocsextraction" }, usage = "Extracts documents from the BioC PMC corpus according to a delivered set of IDs. The IDs might correspond to PMC IDs or PubMed IDs, corresponding to the specified source. The selected documents are written into a single output file in BioC format.", metaVar = "<bioc unicode dir> <output file> <id file> <source>", handler = StringArrayOptionHandler.class)
	String[] biocPmcExtractionParams;

	@Option(name = "-pmd", aliases = {
			"--print-max-degree" }, usage = "Prints out the top N terms with the maximum degree of IS_BROADER_THAN relationships for information purposes.", metaVar = "<N - the number of top degree terms to print out.>")
	Boolean printMaxDegree;

	@Option(name = "-pfid", aliases = { "--printfacetids" }, usage = "Prints out facet names and their respective IDs.")
	Boolean printFacetIds;

	@Option(name = "-h", aliases = { "--help" }, usage = "Prints a help page.", hidden = true, help = true)
	Boolean printHelp;

	@Option(name = "-utc", aliases = {
			"--updatetermchildren" }, usage = "Performs an update of the child information property of all facet terms. This is used within Semedico to tell whether a term has children in a particular facet or not without having to load the children first.")
	Boolean updateTermChildrenInformation;

	@Option(name = "-cc", aliases = {
			"--checkconfiguration" }, usage = "Shows the currently active configuration and asks to except or reject it. A non-zero exit code means a rejection of the configuration which can be used in shell scripts.")
	Boolean checkConfiguration;

	@Option(name = "-wdi", aliases = {
			"--writedocumentids" }, usage = "Writes the IDs of all documents in files named after the respective index types.")
	Boolean writeDocumentIds;

	// receives other command line parameters than options
	@Argument
	List<String> arguments = new ArrayList<>();

	public enum Mode {
		ELE2AGG, CREATE_SUGGESTIONS, ADD_TERM_LABELS, CREATE_INDEXES, TERM2FACET, ERROR, TERMIMPORT, HYPERNYMS, LINGPIPE_DICT, AUTHORIMPORT, TERM_ID_MAPPING, EVENT_DEFINITION, EVENT_MAPPING, PRINT_MAX_DEGREE, PRINT_FACET_IDS, PRINT_HELP, UPDATE_CHILDREN_PROPERTY, MAPPING_IMPORT, CREATE_AGGREGATES, DELETE_AGGREGATES, CREATE_AGGREGATE_PROPERTIES, SET_FACET_ROOT_TERM_NUMBER, CHECK_CONFIG, @Deprecated
		CREATE_EVENT_FACETS, @Deprecated
		CREATE_EVENT_TERM_PATTERNS, REINDEX, AGGREGATE_ELEMENT_MAPPING, PMC_DOC_EXTRACTION, WRITE_DOCUMENT_IDS
	}

	public static Mode getMode(CLIOptions parsedOptions) {
		Mode mode = Mode.ERROR;

		if (parsedOptions.addTermLabelParameters != null)
			mode = Mode.ADD_TERM_LABELS;
		if (parsedOptions.t2fOutputFile != null)
			mode = Mode.TERM2FACET;
		if (parsedOptions.termFilePaths != null)
			mode = Mode.TERMIMPORT;
		if (parsedOptions.mappingFilePath != null)
			mode = Mode.MAPPING_IMPORT;
		if (parsedOptions.createAggregates != null)
			mode = Mode.CREATE_AGGREGATES;
		if (parsedOptions.deleteAggregates != null)
			mode = Mode.DELETE_AGGREGATES;
		if (parsedOptions.createAggregateProperties != null)
			mode = Mode.CREATE_AGGREGATE_PROPERTIES;
		if (parsedOptions.setFacetRootTermNumber != null)
			mode = Mode.SET_FACET_ROOT_TERM_NUMBER;
		if (parsedOptions.hypernymOutputFile != null)
			mode = Mode.HYPERNYMS;
		if (parsedOptions.dictOutputFile != null)
			mode = Mode.LINGPIPE_DICT;
		if (parsedOptions.termsrc2idOutputFile != null)
			mode = Mode.TERM_ID_MAPPING;
		if (parsedOptions.eventTermDefinitionFile != null)
			mode = Mode.EVENT_DEFINITION;
		if (parsedOptions.eventTermMappingOutputFile != null)
			mode = Mode.EVENT_MAPPING;
		if (parsedOptions.printMaxDegree != null)
			mode = Mode.PRINT_MAX_DEGREE;
		if (parsedOptions.printHelp != null)
			mode = Mode.PRINT_HELP;
		if (parsedOptions.printFacetIds != null)
			mode = Mode.PRINT_FACET_IDS;
		if (parsedOptions.updateTermChildrenInformation != null)
			mode = Mode.UPDATE_CHILDREN_PROPERTY;
		if (parsedOptions.createIndexes != null)
			mode = Mode.CREATE_INDEXES;
		if (parsedOptions.createSuggestionIndex != null)
			mode = Mode.CREATE_SUGGESTIONS;
		if (parsedOptions.checkConfiguration != null)
			mode = Mode.CHECK_CONFIG;
		if (parsedOptions.createEventFacets != null)
			mode = Mode.CREATE_EVENT_FACETS;
		if (parsedOptions.eventTermPatternOutputFile != null)
			mode = Mode.CREATE_EVENT_TERM_PATTERNS;
		if (parsedOptions.indexNames != null)
			mode = Mode.REINDEX;
		if (parsedOptions.biocPmcExtractionParams != null)
			mode = Mode.PMC_DOC_EXTRACTION;
		if (parsedOptions.elementsToAggregateIds != null)
			mode = Mode.ELE2AGG;
		if (parsedOptions.writeDocumentIds != null)
			mode = Mode.WRITE_DOCUMENT_IDS;

		return mode;
	}
}
