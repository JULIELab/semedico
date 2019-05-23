This directory is meant as a place for a small number of terms but still
enough to create a realistic Semedico user experience. Using the terms
in this directory - and the original Semedico terms which are not
included here because of redundancy reasons (those terms are stored
in data/input/ud-mesh anyway) - one can set up a running Semedico system
comprised of a Semedico term database, the UIMA semedico-app (using the
term database as input) and a respective ElasticSearch server. While
this will result in a working system, not all of BioPortal is included
to keep things a bit more flexible, faster and simpler during development.