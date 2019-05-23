#!/bin/bash

echo "Creating file names_test.dmp"
TAXIDSINTESTGENES=organisms_test.taxid
GENENAMES=/data/data_resources/biology/ncbi_tax/names.dmp
awk -v namesfile=$TAXIDSINTESTGENES '
	BEGIN {
		FS="\t";
		while((getline line<namesfile) > 0) {
			taxids[line] = 1;
		}
	}
	{
		if ($1 in taxids)
			print $0
	}' $GENENAMES > names_test.dmp

