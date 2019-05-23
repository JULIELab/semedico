#!/bin/bash

echo "Creating small gene group file gene_group_test"
GENE_GROUP=/data/data_resources/biology/entrez/gene/gene_group
awk -v geneidsfile=$GENE_IDS '
	BEGIN {
		FS="\t";
		while((getline line<geneidsfile) > 0) {
			geneids[line] = 1;
		}
	}
	{
		if ($2 in geneids && $5 in geneids)
			print $0
	}' $GENE_GROUP > gene_group_test
