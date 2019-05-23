#!/bin/bash
# This script contains all steps required to create the production database
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

# What follows just location definitions of a range of resource files. The
# intention is to make the resources configurable via environment variables.
# This way we can setup different resource sets (e.g. development, production,
# evaluation, project-specific setups...) while always using this script
# to organize the workflow.
if [ -z "$MESH" ]; then
	MESH="/data/data_resources/biology/MeSH/mesh2018/"
	echo "Using DEFAULT MeSH path $MESH"
else
	echo "Using VARIABLE-DEFINED MeSH path $MESH"
fi
if [ -z "$MESH_SUPP" ]; then
	MESH_SUPP="/data/data_resources/biology/MeSH/mesh2018/supp2018.gz"
	echo "Using DEFAULT MeSH-Supplementary path $MESH_SUPP"
else
	echo "Using VARIABLE-DEFINED MeSH-Supplementary path $MESH_SUPP"
fi
if [ -z "$ORGANISM_NAMES" ]; then
	ORGANISM_NAMES="/data/data_resources/biology/ncbi_tax/names.dmp"
	echo "Using DEFAULT organism names $ORGANISM_NAMES"
else
	echo "Using VARIABLE-DEFINED organism names $ORGANISM_NAMES"
fi
if [ -z "$HOMOLOGENE" ]; then
	HOMOLOGENE="/var/data/data_resources/biology/homologene/homologene.data"
	echo "Using DEFAULT homologene file $HOMOLOGENE"
else
	echo "Using VARIABLE-DEFINED homologene file $HOMOLOGENE"
fi
if [ -z "$GENE_GROUP" ]; then
	GENE_GROUP="/var/data/data_resources/biology/entrez/gene/gene_group"
	echo "Using DEFAULT gene_group file $GENE_GROUP"
else
	echo "Using VARIABLE-DEFINED homologene file $GENE_GROUP"
fi
if [ -z "$GENE_INFO_ORG_FILTERED" ]; then
	GENE_INFO_ORG_FILTERED="/data/data_resources/gene-mapper/5.0/default/resources/gene_info_organism_filtered.gz"
	echo "Using DEFAULT organism filtered gene info file $GENE_INFO_ORG_FILTERED"
else
	echo "Using VARIABLE-DEFINED organism filtered gene info file $GENE_INFO_ORG_FILTERED"
fi
if [ -z "$ORGANISMS" ]; then
	ORGANISMS="/data/data_resources/gene-mapper/5.0/default/makeResourcesAndIndicesTemp/organisms.taxid"
	echo "Using DEFAULT organisms $ORGANISMS"
else
	echo "Using VARIABLE-DEFINED organisms $ORGANISMS"
fi
if [ -z "$GENE2SUMMARY" ]; then
	GENE2SUMMARY="/data/data_resources/gene-mapper/5.0/default/resources/eg2summary"
	echo "Using DEFAULT gene2summary file $GENE2SUMMARY"
else
	echo "Using VARIABLE-DEFINED gene2summary file $GENE2SUMMARY"
fi
if [ -z "$BIOPORTAL_CLASSNAMES" ]; then
    BIOPORTAL_CLASSNAMES="/data/semedico/migration/bioportal/names"
	echo "Using DEFAULT BioPortal Concepts $BIOPORTAL_CLASSNAMES"
else
	echo "Using VARIABLE-DEFINED BioPortal Concepts $BIOPORTAL_CLASSNAMES"
fi
if [ -z "$BIOPORTAL_INFO" ]; then
    BIOPORTAL_INFO="/data/semedico/migration/bioportal/info"
	echo "Using DEFAULT BioPortal Concepts $BIOPORTAL_INFO"
else
	echo "Using VARIABLE-DEFINED BioPortal Concepts $BIOPORTAL_INFO"
fi
if [ -z "$BIOPORTAL_MAPPINGS" ]; then
	BIOPORTAL_MAPPINGS="/var/data/semedico/migration/bioportal/mappings"
	echo "Using DEFAULT BioPortal mappings $BIOPORTAL_MAPPINGS"
else
	echo "Using VARIABLE-DEFINED BioPortal mappings $BIOPORTAL_MAPPINGS"
fi

# Create database indexes
./runResourceTools.sh -ci
# Import the whole original MeSH. This also import the supplementary concepts and connectes
# the supplementary concepts to their parent MeSH heading.
./runResourceTools.sh -ti $MESH
# Here, we import the supplementary concepts again. The Neo4j plugin sorts it out, there are no duplicates in the database.
./runResourceTools.sh -ti data/input/ud-mesh/used_xml $MESH_SUPP
# echo -e 'y\n' just sends a 'y' to the script. This is necessary to answer the
# question whether to proceed without the MeSH Supplementary Concepts with
# 'yes'. We have imported them above and will import them again via BioPortal (duplicates should be sorted out be Neo4j
# Server plugin). So they should really be there eventually.
#echo -e 'y\n' | ./runResourceTools.sh -ti data/input/ud-mesh/immunology
./runResourceTools.sh -ti $GENE_INFO_ORG_FILTERED $ORGANISMS $ORGANISM_NAMES $GENE2SUMMARY $HOMOLOGENE $GENE_GROUP
# The original idea was not make facets out of all of BioPortal. We never got that far, there are quite some challenges due to the large number of concepts per document found, which facets to show and probably more. So restrict to the most important knowledge bases.
./runResourceTools.sh -ti $BIOPORTAL_CLASSNAMES $BIOPORTAL_INFO BCO CHEBI CHMO ENVO GO GRO NCBITAXON NCIT PATO
# Update Term Children
./runResourceTools.sh -utc
# Define event terms. That means that some classes of GO and GRO will be assigned to correspond to Positive_regulation, Transcription, etc, i.e. the event types of the BioNLP Shared Task09
# This is needed to later create a file mapping those Shared Task designations to the respective Semedico concept IDs.
./runResourceTools.sh -def data/event-terms.def
# See comment for BioPortal ontology import above
./runResourceTools.sh -mi $BIOPORTAL_MAPPINGS GO GRO
# Create Aggregates
./runResourceTools.sh -ca
# Copy aggregate properties
./runResourceTools.sh -cap
# Set facet root term numbers
./runResourceTools.sh -frtn
# Create NCBI Taxonomy list used by Linnaeus species tagger to then give
# the respective taxonomy terms a label to prevent them from being
# used for the gazetteer dictionary (since we have a - hopefully better - 
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


bash checkProductionStatus.sh
