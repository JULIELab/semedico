#!/bin/bash
# This script contains all steps required to create the JUnit test database
# for GePi, assuming an empty, running Neo4j server is available that
# is configured for the use with GePi (i.e. there has to be the
# julie-neo4j-server-plugins-<verion>.jar and at least a configuration
# setting to allow server access from other machines than localhost).
# Don't forget to configure the HTTP address of this server in your
# resources/configuration.properties.<username> file.
# Note that this configuration resembles closely the semedico setup 
# to create the JUnit test database. 

export CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:target/classes
# run configuration check:
./runResourceTools.sh -cc
if [ 0 -ne $? ]; then
	echo "Current configuration rejected, aborting."
	exit 1;
fi

# Create database indexes
./runResourceTools.sh -ci
# echo -e 'y\n' just sends a 'y' to the script. This is necessary to answer the
# question whether to proceed without the MeSH Supplementary Concepts with
# 'yes'. For the test we don't include those concepts.
echo -e 'y\n' | ./runResourceTools.sh -ti data/input/semedico-junit-test-database/semedicoXml
./runResourceTools.sh -ti data/input/semedico-junit-test-database/ncbiGene/gene_info_test data/input/semedico-junit-test-database/ncbiGene/organisms_test.taxid data/input/semedico-junit-test-database/ncbiGene/names_test.dmp data/input/semedico-junit-test-database/ncbiGene/gene2summary_test data/input/semedico-junit-test-database/ncbiGene/homologene_test.data data/input/semedico-junit-test-database/ncbiGene/gene_group_test
# We currently restrict ourselves to the GO and GRO ontologies for event terms. In the future we would like to integrate more of BioPortal, of course
./runResourceTools.sh -ti data/input/semedico-junit-test-database/bioPortal GO GRO NCBITAXON
# Add event test terms:
./runResourceTools.sh -def data/event-terms.def
# Add mappings:
./runResourceTools.sh -mi data/input/semedico-junit-test-database/mappings/ 
# Create term aggregates according to imported mappings:
./runResourceTools.sh -ca
# Create term aggregate properties:
./runResourceTools.sh -cap
# Create NCBI Taxonomy list used by Linnaeus species tagger to then give
# the respective taxonomy terms a label to prevent them from being
# used for the gazetteer dictionary (since we have a - hopefully - better
# tagger for this). We cannot just give all NCBI Taxonomy terms this label
# because there are more terms in the taxonomy than are recognized
# by Linnaeus' dictionaries.
./createLinnaeusNcbiTaxList.sh
# We do not only add the NO_PROCESSING_GAZETTEER label but also the
# ID_MAP_NCBI_TAXONOMY label to indicate that those terms have to undergo
# ID mapping from taxonomy IDs to Semedico IDs in the Semedico UIMA
# pipeline. For those terms not covered by Linnaeus we don't have to do
# this since Linnaeus won't know them anyway and they will be only
# recognized by a gazetteer which does not need an ID mapping.
./runResourceTools.sh -atl data/input/ncbitax/linnaeus.taxid originalId NO_PROCESSING_GAZETTEER,ID_MAP_NCBI_TAXONOMY NCBITAXON
