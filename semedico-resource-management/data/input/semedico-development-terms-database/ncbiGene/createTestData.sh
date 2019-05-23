./makeTestGene2Summary.sh
./makeTestGeneInfo.sh
cut -f1 gene_info_test | sort -u > organisms_test.taxid
./makeTestGeneNames.sh
