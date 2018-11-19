#!/bin/bash
cd /data/semedico/fushin-2.2
echo "Starting MEDLINE update"
java -jar costosys-1.2.2-SNAPSHOT-cli-assembly.jar -um pubmed2018.xml -dbc semedico-preprocessing-pipeline/config/costosys.xml
echo "Database MEDLINE XML update complete"
cd semedico-preprocessing-pipeline/
echo "Starting NLP preprocessing of new MEDLINE documents."
response=$(sbatch --nodelist h6 runslurm.sh)
jobid=${response##* }
echo "Got SLURM job $jobid for the preprocessing"
cd ..
cd semedico-indexing-pipeline/
echo "Sending indexing job to SLURM depending on job $jobid"
sbatch --nodelist h6 -n 10 -d afterok:$jobid runslurm.sh
