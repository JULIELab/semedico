#!/bin/bash

echo "Creating small homologene file homologene_test.data"
HOMOLOGENE=/data/data_resources/biology/homologene/homologene.data
awk -v geneidsfile=$GENE_IDS '
	BEGIN {
		FS="\t";
		while((getline line<geneidsfile) > 0) {
			geneids[line] = 1;
		}
	}
	{
		if ($3 in geneids)
			print $0
	}' $HOMOLOGENE > homologene_test.data
