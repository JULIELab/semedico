Letzte Änderung: 04.09.2013

### Verzeichnis "aging" ###

 * enthält alle Input-Daten zur Erstellung des Aging-MeSH

### Verzeichins "mesh" ###

 * SOLLTE alle MeSH-Daten zur Erstellung des aktuellen Semedico-MeSHs enthalten
 * aufgrund der Größe sind die MeSH-Daten NICHT im SVN eingecheckt und müssen daher händisch in dieses Verzeichnis eingefügt werden
 * verfügbar hier: http://www.nlm.nih.gov/mesh/filelist.html
 
### Verzeichnis "testing" ###

 * enthält alle Input-Daten für automatisierte Tests
 
### Verzeichnis "ud-mesh" ###

 * enthält im Unterverzeichnis "used_xml" die XML-Dateien des veralteten, teil-manuell erstellten Semedico-MeSH
 * identisch mit https://www.coling.uni-jena.de/svn/stemnet/trunk/stemnet-tools/xml_kaputt/, ABER OHNE:
   * externe, sich ändernde Daten / Supplementals:	proteins.xml, chemical_supp.xml, igs_antibodies_supp.xml (in "external_or_supp")
   * leere Dateien: facet_tabs.txt.xml, query.add.dict.xml (in "useless_or_empty")
   * nofacet.xml: weil diese alle (??) Daten noch einmal enthält, aber ohne Information zum Facet (in "excluded")
 * ACHTUNG: im repository sind nur die genutzten Dateien (also die in used_xml) eingecheckt!!
