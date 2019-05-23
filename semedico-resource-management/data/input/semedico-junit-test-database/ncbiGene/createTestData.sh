#!/bin/bash
GENE_IDS=`cat gene-id-lists/*.lst | cut -f2 | sort -u`

echo "`echo \"$GENE_IDS\" | wc -l` unique gene IDs were found in gene ID lists under directory gene-id-lists/. Writing to temporary file all-gene-ids.tmp"
echo "$GENE_IDS" > all-gene-ids.tmp
export GENE_IDS=all-gene-ids.tmp

./makeTestGene2Summary.sh
./makeTestGeneInfo.sh
cut -f1 gene_info_test | sort -u > organisms_test.taxid
./makeTestGeneNames.sh
./makeTestHomologene.sh
./makeTestGeneGroup.sh
