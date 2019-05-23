# Convenience script to create the Semedico resources dependent on the term 
# database in-place. Thus, some details of this script might have to be
# adapted in the future (semedico-app path, the labels to create ID
# mappings for, ...)

BASEDIR=$1
if [ -z "$BASEDIR" ]; then
	echo "Please specify the base directory to where the resources sould be written."
	exit 1
fi

./runResourceTools.sh -cc
if [ 0 -ne $? ]; then
	echo "Current configuration rejected, aborting."
	exit 1;
fi
SEMEDICO_APP=$BASEDIR"/semedico-app"
JPP=$BASEDIR"/jules-preprocessing-pipelines"

# Creating the BioPortal gazetteer dictionary for the UIMA component. This is the dictionary used
# to recognize BioPortal ontology classes in document text.
./runResourceTools.sh -ld bioportal.gazetteer.simple.dict MAPPING_AGGREGATE,AGGREGATE,NO_PROCESSING_GAZETTEER
./runResourceTools.sh -ld bioportal.gazetteer.aggregates.dict AGGREGATE,NO_PROCESSING_GAZETTEER
./runResourceTools.sh -ld bioportal.gazetteer.aggelements.dict TERM,MAPPING_AGGREGATE,NO_PROCESSING_GAZETTEER
# Move the dictionaries to the dictionary directory where they will be accessed from the UIMA pipelines.
if [ ! -d "$JPP/jpp-semedico-metadata/src/main/resources/" ]; then
	echo "Creating directory $JPP/jpp-semedico-metadata/src/main/resources/";
	install -dD "$JPP/jpp-semedico-metadata/src/main/resources/";
fi
mv bioportal.gazetteer.simple.dict $JPP/jpp-semedico-metadata/src/main/resources/.
mv bioportal.gazetteer.aggregates.dict $JPP/jpp-semedico-metadata/src/main/resources/.
mv bioportal.gazetteer.aggelements.dict $JPP/jpp-semedico-metadata/src/main/resources/.
# Creating term mappings. In the UIMA pipeline, the original resource IDs are used, e.g. NCBI Gene IDs
# for genes, or the Immunology facet that has terms which have been developed directly within JULIE Lab
# in the course of the StemNet project. We need to unify all those different ID types to the term IDs
# in our term database. So we create mappings that can than be used in the UIMA pipeline, with the
# jules-feature-value-replacement component, or in LuCas with a replace filter.
if [ ! -d "$SEMEDICO_APP/resources/idMappings/" ]; then
	echo "Creating directory $SEMEDICO_APP/resources/idMappings/";
	install -dD "$SEMEDICO_APP/resources/idMappings/";
fi
./runResourceTools.sh -tem $SEMEDICO_APP/resources/idMappings/eventTermMapping
./runResourceTools.sh -tid $SEMEDICO_APP/resources/idMappings/genes.tidmap originalId ID_MAP_NCBI_GENES
./runResourceTools.sh -tid $SEMEDICO_APP/resources/idMappings/ncbitaxon.tidmap originalId ID_MAP_NCBI_TAXONOMY
./runResourceTools.sh -tid $SEMEDICO_APP/resources/idMappings/immunology.tidmap originalId ID_MAP_IMMUNOLOGY
# Note that for MeSH headings, as delivered with the Medline XML, we don't create a map from the original ID
# to the term ID but from the preferredName to the term ID, i.e. from the original heading. Of course, this
# assumes that the heading was used as the preferred name in BioPortal and ultimately in our term database.
# Please note also, that for MeSH mentions - i.e. explicit mentions of MeSH terms in the text - we don't
# create any mapping since those MeSH mentions are created via a dictionary lookup where the term ID
# is directly included in the dictionary. This will most probably happen right within the bio portal
# gazetteer lookup since MESH is included in BioPortal.
if [ ! -d "$SEMEDICO_APP/resources/LucasParams/" ]; then
	echo "Creating directory $SEMEDICO_APP/resources/LucasParams/";
	install -dD "$SEMEDICO_APP/resources/LucasParams/";
fi
./runResourceTools.sh -tid $SEMEDICO_APP/resources/LucasParams/meshheadings.tidmap preferredName ID_MAP_MESH
# Create the file with the event term patterns to map event terms to their induced facet (all phosphorylations into
# the phosphorylation facet)
./runResourceTools.sh -etp $SEMEDICO_APP/resources/LucasParams/eventtermpatterns
# This map exposes which term ID belongs to which facet (multiple facets for one term ID are possible, of course).
# We use this within LuCas to create the facet category counts and to automatically distribute the terms among
# Lucene fields where every facet has its own field.
echo "Creating term ID to facet ID map for all terms (not aggregates)."
./runResourceTools.sh -t2f $SEMEDICO_APP/resources/LucasParams/term.2fid
echo "Creating term ID to facet ID map for all aggregates."
./runResourceTools.sh -t2f $SEMEDICO_APP/resources/LucasParams/aggregates.2fid AGGREGATE
echo "Creating gene term ID to homology aggregate ID map."
./runResourceTools.sh -el2agg $SEMEDICO_APP/resources/LucasParams/elements.2aggid AGGREGATE_TOP_HOMOLOGY
# This is just for the LuCas select filter which removes any terms we don't know. Perhaps due to different
# resource versions or something. So this is more of a consistency thing.
cut -d"=" -f1 $SEMEDICO_APP/resources/LucasParams/term.2fid > $SEMEDICO_APP/resources/LucasParams/validterms.lst
cut -d"=" -f1 $SEMEDICO_APP/resources/LucasParams/aggregates.2fid >> $SEMEDICO_APP/resources/LucasParams/validterms.lst
# The query dictionary that is used to recognize terms in user queries issued to Semedico. Create dictionary
# entries for terms with label MAPPING_AGGREGATE so that equal terms are not recognized individually
# by the query chunker.
./runResourceTools.sh -ld query.dic MAPPING_AGGREGATE,NO_QUERY_DICTIONARY
# Copy the dictionary to the Semedico directory. By convention, the semedico-resource-manager is at
# /data/semedico/production-data/uima-pipeline/semedico-resource-management
# and we want to have the query.dic in /data/semedico/production-data$RELEASE_SUFFIX, so:
mv query.dic $BASEDIR/query.dic
# This file goes into LuCas' hypernym filter. For each term, all of its predecessors - or hypernyms - are
# added to the respective document field as well. This way, we may search for a more abstract term in Semedico
# and still find all documents annotated with a more special term in the end.
echo "Creating hypernyms for all terms."
./runResourceTools.sh -hy $SEMEDICO_APP/resources/LucasParams/hypernyms.tid
