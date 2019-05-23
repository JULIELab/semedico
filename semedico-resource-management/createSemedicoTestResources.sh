# Convenience script to create the Semedico test resources dependent on the term 
# database in-place. Thus, some details of this script might have to be
# adapted in the future (semedico-core path, the labels to create ID
# mappings for, ...)

# Creating the BioPortal gazetteer dictionary for the UIMA component. This is the dictionary used
# to recognize BioPortal ontology classes in document text.
./runResourceTools.sh -ld ../jules-preprocessing-pipelines/jpp-semedico-metadata/src/test/resources/bioportal.gazetteer.simple.dict MAPPING_AGGREGATE,AGGREGATE,NO_PROCESSING_GAZETTEER
./runResourceTools.sh -ld ../jules-preprocessing-pipelines/jpp-semedico-metadata/src/test/resources/bioportal.gazetteer.aggregates.dict AGGREGATE,NO_PROCESSING_GAZETTEER
./runResourceTools.sh -ld ../jules-preprocessing-pipelines/jpp-semedico-metadata/src/test/resources/bioportal.gazetteer.aggelements.dict TERM,MAPPING_AGGREGATE,NO_PROCESSING_GAZETTEER
# Creating term mappings. In the UIMA pipeline, the original resource IDs are used, e.g. NCBI Gene IDs
# for genes, or the Immunology facet that has terms which have been developed directly within JULIE Lab
# in the course of the StemNet project. We need to unify all those different ID types to the term IDs
# in our term database. So we create mappings that can than be used in the UIMA pipeline, with the
# jules-feature-value-replacement component, or in LuCas with a replace filter.
./runResourceTools.sh -tem ../semedico-app/resources/idMappings/eventTermMapping
./runResourceTools.sh -tid ../semedico-app/resources/idMappings/genes.tidmap originalId ID_MAP_NCBI_GENES
./runResourceTools.sh -tid ../semedico-app/resources/idMappings/ncbitaxon.tidmap originalId ID_MAP_NCBI_TAXONOMY
./runResourceTools.sh -tid ../semedico-app/resources/idMappings/immunology.tidmap originalId ID_MAP_IMMUNOLOGY
# Note that for MeSH headings, as delivered with the Medline XML, we don't create a map from the original ID
# to the term ID but from the preferredName to the term ID, i.e. from the original heading. Of course, this
# assumes that the heading was used as the preferred name in BioPortal and ultimately in our term database.
# Please note also, that for MeSH mentions - i.e. explicit mentions of MeSH terms in the text - we don't
# create any mapping since those MeSH mentions are created via a dictionary lookup where the term ID
# is directly included in the dictionary. This will most probably happen right within the bio portal
# gazetteer lookup since MESH is included in BioPortal.
./runResourceTools.sh -tid ../semedico-app/resources/LucasParams/meshheadings.tidmap preferredName ID_MAP_MESH
# Create the file with the event term patterns to map event terms to their induced facet (all phosphorylations into
# the phosphorylation facet)
# This is not done anymore since for NAR2016 we removed the events facet completely
#./runResourceTools.sh -etp ../semedico-app/resources/LucasParams/eventtermpatterns
# This map disposes which term ID belongs to which facet (multiple facets for one term ID are possible, of course).
# We use this within LuCas to create the facet category counts and to automatically distribute the terms among
# Lucene fields where every facet has its own field.
echo "Creating term ID to facet ID map for all terms (not aggregates)."
./runResourceTools.sh -t2f ../semedico-app/resources/LucasParams/term.2fid
echo "Creating term ID to facet ID map for all aggregates."
./runResourceTools.sh -t2f ../semedico-app/resources/LucasParams/aggregates.2fid AGGREGATE
echo "Creating gene term ID to homology aggregate ID map."
./runResourceTools.sh -el2agg ../semedico-app/resources/LucasParams/elements.2aggid AGGREGATE_TOP_HOMOLOGY
# This is just for the LuCas select filter which removes any terms we don't know. Perhaps due to different
# resource versions or something. So this is more of a consistency thing.
cut -d"=" -f1 ../semedico-app/resources/LucasParams/term.2fid > ../semedico-app/resources/LucasParams/validterms.lst
cut -d"=" -f1 ../semedico-app/resources/LucasParams/aggregates.2fid >> ../semedico-app/resources/LucasParams/validterms.lst
# The query dictionary that is used to recognize terms in user queries issued to Semedico. Create dictionary
# entries for terms with label MAPPING_AGGREGATE so that equal terms are not recognized individually
# by the query chunker.
./runResourceTools.sh -ld ../semedico-core/src/test/resources/query-test.dic MAPPING_AGGREGATE,NO_QUERY_DICTIONARY
# This file goes into LuCas' hypernym filter. For each term, all of its predecessors - or hypernyms - are
# added to the respective document field as well. This way, we may search for a more abstract term in Semedico
# and still find all documents annotated with a more special term in the end.
./runResourceTools.sh -hy ../semedico-app/resources/LucasParams/hypernyms.tid
# For the special event facet we create a separate file. This facet is just a "view" of terms that come
# from other facets (GO and GRO) so that the hypernyms outside the event scope are not included in the facetEvents field.
# We cannot just use a select filter to restrict the field content to valid terms because the field is also populated
# with the explicit event mentions in the form of "high-tid5234-tid53-tid934" that are not known in advance.
./runResourceTools.sh -hy ../semedico-app/resources/LucasParams/hypernymsEventFacet.tid EVENTS