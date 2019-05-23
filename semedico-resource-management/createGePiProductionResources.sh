# NOTE: This script needs to be run on a server or machine having mounted the
# NFS /data directory. Also, you have to make sure that all relative
# paths are fitting.
# Convenience script to create the GePi resources dependent on the term 
# database in-place. Thus, some details of this script might have to be
# adapted in the future (semedico-app path, the labels to create ID
# mappings for, ...)
#
# NOTE 1: jules preprocessing pipelines not needed for GePi, as we are allowing any user queries (only one or two lists of entrez IDs are accepted at the current state)
# NOTE 2: This script is derived from createSemedicoProductionResources.sh. Please refer to this script for the complete list of semedico relevant runResourceTools.sh calls

./runResourceTools.sh -cc
if [ 0 -ne $? ]; then
	echo "Current configuration rejected, aborting."
	exit 1;
fi

# by convention all GePi relevant term data is stored in ../resources/terms
RESOURCE_PATH="../resources/terms"

# Creating term mappings. In the UIMA pipeline, the original resource IDs are used, e.g. NCBI Gene IDs
# for genes, or the Immunology facet that has terms which have been developed directly within JULIE Lab
# in the course of the StemNet project. We need to unify all those different ID types to the term IDs
# in our term database. So we create mappings that can than be used in the UIMA pipeline, with the
# jules-feature-value-replacement component, or in LuCas with a replace filter.
./runResourceTools.sh -tem $RESOURCE_PATH/idMappings/eventTermMapping
./runResourceTools.sh -tid $RESOURCE_PATH/idMappings/genes.tidmap originalId ID_MAP_NCBI_GENES
./runResourceTools.sh -tid $RESOURCE_PATH/idMappings/ncbitaxon.tidmap originalId ID_MAP_NCBI_TAXONOMY
echo "Creating gene term ID to homology aggregate ID map."
./runResourceTools.sh -el2agg $RESOURCE_PATH/LucasParams/elements.2aggid AGGREGATE_TOP_HOMOLOGY
