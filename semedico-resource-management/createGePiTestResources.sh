# Convenience script to create the GePi test resources dependent on the term 
# database in-place. Thus, some details of this script might have to be
# adapted in the future (semedico-core path, the labels to create ID
# mappings for, ...)
# NOTE 1: jules preprocessing pipelines not needed for GePi, as we are allowing any user queries (only one or two lists of entrez IDs are accepted at the current state) 
# NOTE 2: This script is derived from createSemedicoTestResources.sh. Please refer to this script for the complete list of semedico relevant runResourceTools.sh calls

# by convention all GePi relevant term data is stored in ../resources/terms
RESOURCE_PATH="../resources/terms"
 
# Creating term mappings. In the UIMA pipeline, the original resource IDs are used, e.g. NCBI Gene IDs
# for genes, or the Immunology facet that has terms which have been developed directly within JULIE Lab
# in the course of the StemNet project. We need to unify all those different ID types to the term IDs
# in our term database. Thus, we create mappings that can than be used in the UIMA pipeline, with the
# jules-feature-value-replacement component, or in LuCas with a replace filter.
./runResourceTools.sh -tem $RESOURCE_PATH/idMappings/eventTermMapping
./runResourceTools.sh -tid $RESOURCE_PATH/idMappings/genes.tidmap originalId ID_MAP_NCBI_GENES
# as NCBITAXON is currently (161116) not reachable the following import will not run
./runResourceTools.sh -tid $RESOURCE_PATH/idMappings/ncbitaxon.tidmap originalId ID_MAP_NCBI_TAXONOMY
# TODO: runResourceTools.sh -el2agg test
