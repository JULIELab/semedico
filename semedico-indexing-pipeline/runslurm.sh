#!/bin/bash
#SBATCH --mem 80g
#SBATCH --cpus-per-task 1 
#SBATCH -J semedico_processing 

srun java -jar -Xmx5g -Dlogback.configurationFile=/data/semedico/fushin-2.2/semedico-indexing-pipeline/config/logback.xml /var/data/semedico/jcore-pipeline-runner-*.jar pipelinerunner.xml

