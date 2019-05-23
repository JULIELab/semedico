GENE_IDS_IN_TEST_DOCS=gene_ids_test_docs.lst
GENE_INFO=/data/data_resources/biology/entrez/gene/gene_info
awk -v geneidsfile=$GENE_IDS_IN_TEST_DOCS '
	BEGIN {
		FS="\t";
		while((getline line<geneidsfile) > 0) {
			geneids[line] = 1;
		}
	}
	{
		if ($2 in geneids)
			print $0
	}' $GENE_INFO > gene_info_test

