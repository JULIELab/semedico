#!/bin/bash

GENERA_DICT="https://www.coling.uni-jena.de/svn/uima-components/trunk/jules-linnaeus-species-genera-proxies-dict/src/main/resources/de/julielab/jules/resources/linnaeus/genera_proxies/dict-genera-proxy.tsv"
SPECIES_DICT="https://www.coling.uni-jena.de/svn/uima-components/trunk/jules-linnaeus-species-proxies-dict/src/main/resources/de/julielab/jules/resources/linnaeus/species_proxies/dict-species-proxy.tsv"
GENERA_OUT="data/input/ncbitax/dict-genera-proxy.taxid"
SPECIES_OUT="data/input/ncbitax/dict-species-proxy.taxid"

LINNAEUS_IDS=data/input/ncbitax/linnaeus.taxid

echo "Creating list of NCBI Taxonomy IDs known by Linnaeus species tagger as used by the JULIE Lab."

if [ ! -f "$GENERA_OUT" ]; then
	echo "Exporting genera dictionary from $SPECIES_DICT"
	svn export $GENERA_DICT $GENERA_OUT
else
	echo "Genera dictionary was found at $GENERA_OUT and is not downloaded again."
fi

if [ ! -f "$SPECIES_OUT" ]; then
	echo "Exporting species dictionary from $SPECIES_DICT"
	svn export $SPECIES_DICT $SPECIES_OUT
else
	echo "Species proxy dictionary was found at $SPECIES_OUT and is not downloaded again."
fi

echo "Extracting NCBI Taxonomy IDs from both dictionaries and saving to file $LINNAEUS_IDS"
cut -f1 $GENERA_OUT | sed 's/.*:.*:\([0-9]*\)/\1/' | sort -u > dict-genera.taxid
cut -f1 $SPECIES_OUT | sed 's/.*:.*:\([0-9]*\)/\1/' | sort -u > dict-species.taxid
cat dict-genera.taxid dict-species.taxid | sort -u > $LINNAEUS_IDS
echo "Removing temporary files."
rm dict-genera.taxid
rm dict-species.taxid
echo "Done."