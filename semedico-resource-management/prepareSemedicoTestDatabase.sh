#!/bin/bash
# This script contains all steps required to create the JUnit test database
# for Semedico, assumed an empty, running Neo4j server is available that
# is configured for the use with Semedico (i.e. there has to be the
# julie-neo4j-server-plugins-<verion>.jar and at least a configuration
# setting to allow server access from other machines than localhost).
# Don't forget to configure the HTTP address of this server in your
# resources/configuration.properties.<username> file.

export CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:target/classes

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
./runResourceTools.sh -ti data/input/semedico-junit-test-database/bioPortal/names data/input/semedico-junit-test-database/bioPortal/info GO GRO NCBITAXON
# We define the facets AFTER the term childen update because the event type
# terms currently don't actually have children in the database but they
# should indicate to have some anyway. This is because we know that in
# the data they HAVE children, but they are not added to the database
# (perhaps this should be changed in the future anyway and just import them
# into the DB...)
./runResourceTools.sh -def data/event-terms.def
# As of the NAR2016 submission we do not create event facets anymore;
# Wouldn't quite work anyway since the event representation in the index
# has changed dramatically (from specific term strings to nested document structures)
#./runResourceTools.sh -cef
./runResourceTools.sh -utc
./runResourceTools.sh -mi data/input/semedico-junit-test-database/mappings/
./runResourceTools.sh -ca
./runResourceTools.sh -cap
# facetroottermnumber
./runResourceTools.sh -frtn
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
