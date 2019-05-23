#!/bin/bash

echo "Creating small gene2summary file gene2summary_test"
GENE2SUMMARY=/data/data_resources/gene-mapper/default/makeResourcesAndIndicesTemp/gene2summary
awk -v geneidsfile=$GENE_IDS '
	BEGIN {
		FS="\t";
		while((getline line<geneidsfile) > 0) {
			geneids[line] = 1;
		}
	}
	{
		if ($1 in geneids)
			print $0
	}' $GENE2SUMMARY > gene2summary_test

