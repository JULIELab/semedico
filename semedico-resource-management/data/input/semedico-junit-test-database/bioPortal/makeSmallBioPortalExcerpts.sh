
# This script selects very few BioPortal classes - a few MeSH terms 
# and all event terms - from the complete ontologies for unit
# tests. The included MeSH terms are given within Medline to the document
# with PMID 1347750 that is included in the Semedico unit test
# document set.
# To take only these terms will completely break the structure of the
# ontologies as given by BioPortal. However, we don't care at this
# point because all we want to have is a small test set to check whether
# MeSH terms and events are working within Semedico as expected. For this, the
# specific facet structure is not required.
zgrep 'prefLabel":"Activins' ../../semedico-development-terms-database/bioPortal/MESH.dat.gz > MESH.dat
zgrep 'prefLabel":"Amino Acid Sequence' ../../semedico-development-terms-database/bioPortal/MESH.dat.gz >> MESH.dat
zgrep 'prefLabel":"Kinetics' ../../semedico-development-terms-database/bioPortal/MESH.dat.gz >> MESH.dat
rm MESH.dat.gz
gzip MESH.dat

# Event terms
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0010467' ../../semedico-development-terms-database/bioPortal/GO.dat.gz > GO.dat
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0005488' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0051179' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0016310' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0030163' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0065007' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0010468' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
# Term 'cell' to show that the mapping to the GRO term 'cell' is working as expected
zgrep 'id":"http://purl.obolibrary.org/obo/GO_0005623' ../../semedico-development-terms-database/bioPortal/GO.dat.gz >> GO.dat
rm GO.dat.gz
gzip GO.dat

zgrep 'id":"http://www.bootstrep.eu/ontology/GRO#NegativeRegulation"' ../../semedico-development-terms-database/bioPortal/GRO.dat.gz > GRO.dat
zgrep 'id":"http://www.bootstrep.eu/ontology/GRO#PositiveRegulation"' ../../semedico-development-terms-database/bioPortal/GRO.dat.gz >> GRO.dat
zgrep 'id":"http://www.bootstrep.eu/ontology/GRO#Transcription"' ../../semedico-development-terms-database/bioPortal/GRO.dat.gz >> GRO.dat
# Term 'cell' to show that the mapping to GO term 'cell' is working as expected
zgrep 'id":"http://www.bootstrep.eu/ontology/GRO#Cell"' ../../semedico-development-terms-database/bioPortal/GRO.dat.gz >> GRO.dat
rm GRO.dat.gz
gzip GRO.dat

zgrep 'id":"http://purl.bioontology.org/ontology/NCBITAXON/9606"' ../../semedico-development-terms-database/bioPortal/NCBITAXON.dat.gz > NCBITAXON.dat
zgrep 'id":"http://purl.bioontology.org/ontology/NCBITAXON/10090"' ../../semedico-development-terms-database/bioPortal/NCBITAXON.dat.gz >> NCBITAXON.dat
zgrep 'id":"http://purl.bioontology.org/ontology/NCBITAXON/6239"' ../../semedico-development-terms-database/bioPortal/NCBITAXON.dat.gz >> NCBITAXON.dat
zgrep 'id":"http://purl.bioontology.org/ontology/NCBITAXON/10116"' ../../semedico-development-terms-database/bioPortal/NCBITAXON.dat.gz >> NCBITAXON.dat
zgrep 'id":"http://purl.bioontology.org/ontology/NCBITAXON/37733"' ../../semedico-development-terms-database/bioPortal/NCBITAXON.dat.gz >> NCBITAXON.dat
rm NCBITAXON.dat.gz
gzip NCBITAXON.dat