GENE_IDS_IN_TEST_DOCS=gene_ids_test_docs.lst
GENE2SUMMARY=~/data/data_resources/gene_mapper/default/makeResourcesAndIndicesTemp/gene2summary
awk -v geneidsfile=$GENE_IDS_IN_TEST_DOCS '
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

