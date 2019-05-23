Letze Änderung: 04.Sept 2013
Autor: Philipp Lucas

### Zweck dieses README-Files ###

 1.: Erklärung der Verwendung während der Studienarbeit entwickelten Software zur Quellenverwaltung von Semedico
 2.: Erleichterung des Verständnis' der Software
 3.: Auflistung weiterführender Links

 
### 1. Schnell-Einstieg ###

# zur bloßen Anwendung der fertig vorbereitenen Skripte
 * einfach Process-Klasse ausführen! Als Argument wird ein ini-File erwartet, wobei zwei vorbereitet sind:
	* "data/config/config_create_current_aging_mesh.ini"
	* "data/config/config_create_current_semedico_mesh.ini"
 * allerdings sind die Input-Dateien nicht alle im SVN eingecheckt! -> siehe data/input/README.txt !!!

# zu Grundlagen / Hintergrundwissen
 * Kapitel 1 "Einleitung" und Kapitel 2 "Problemstellung"
 * Kapitel 3.1 "MeSH" 
 * Kapitel 3.3 bis einschließlich Kapitel 3.3.1 "Operationen und Transformationen"

# zur Implementierung / Software
 * Kapitel 6.2 "Design" 
	- Überblick zu Struktur und Zusammenspiel
 * Kapitel 6.7 "Benutzung" bzw. Klasse "Process" 
	- funktionsfähiges Anwendungsbeispiel
 * umfangreiche Kommentare im Quellcode als Java-Docs
	
# zur Internen Funktionsweise
 * für das bloße Verwenden der Software eigentlich nicht notwendig zu verstehen
 * Kapitel 4 und Kapitel 5 erklären detailliert anhand von Grafiken wie der Kern der Software funktioniert

 
### 2. Weiterführende Links ###

MeSH = Medical Subjects Heading
	* "is a comprehensive controlled vocabulary for the purpose of indexing journal articles and books in the life sciences; it can also serve as a thesaurus that facilitates searching"
	* 
		* first of all it is meant for indexing and thereby allowing users to easily find other similar and relevant literature

	* references
		* general introduction: http://www.nlm.nih.gov/mesh/introduction.html
		* more information: http://www.nlm.nih.gov/pubs/factsheets/mesh.html
		* on wikipedia: http://en.wikipedia.org/wiki/MeSH 
		* MeSH browser: http://www.nlm.nih.gov/mesh/MBrowser.html 
		* fact sheets: http://www.nlm.nih.gov/pubs/factsheets/factsubj.html 

	* MeSH record types
		* http://www.nlm.nih.gov/mesh/intro_record_types.html 
		* descriptors
			* known as "Main Headings" or "MeSH Heading"/"MH"
			* "A descriptor is now viewed as a class of concepts, and a concept as a class of synonymous terms." 
			* used for index citations

		* qualifiers
			* also known as "subheadings" (SH)
			* there are 83 of them
			* "Qualifiers afford a convenient means of grouping together those citations which are concerned with a particular aspect of a subject."

		* supplementary concept records
			* "are used to index chemicals, drugs, and other concepts for MEDLINE"
			* There are currently over 186,000 SCR records with over 465,000 SCR terms..

	* entry terms
		* http://www.nlm.nih.gov/mesh/intro_entry.html 
		* "Entry terms, sometimes called "See cross-references" in printed listings, are synonyms, alternate forms, and other closely related terms in a given MeSH record that are generally used interchangeably with the preferred term for the purposes of indexing and retrieval"

	* MeSH Structure
		* http://www.nlm.nih.gov/pubs/techbull/ma00/ma00_mesh.html 

	* MeSH Tree structure
		* fully navigatable tree structure: http://www.nlm.nih.gov/mesh/trees.html and also: http://www.nlm.nih.gov/mesh/2012/mesh_browser/MeSHtree.C.html  
		* explantion about it: http://www.nlm.nih.gov/mesh/intro_trees.html 
		* downloadable Mesh treee
			* http://www.nlm.nih.gov/mesh/2011/download/mtr_abt.html:
			* "Note that because a MeSH main heading may have more than one tree number, the number of entries is much greater than the number of headings."

	* structure of the MeSH descriptors
		* got a tree structure as described in: http://www.nlm.nih.gov/mesh/intro_trees.html 

	* XML in MeSH
		* http://www.nlm.nih.gov/mesh/concept_structure.html 
		* XML syntax available for MeSH: http://www.nlm.nih.gov/mesh/xml_data_elements.html
			* including descriptors, qualifiers and supplementary concept records
		* XML infos: http://www.nlm.nih.gov/mesh/xml_abt.html 

	* video on MeSH:
 		* http://www.nlm.nih.gov/bsd/disted/video/ 