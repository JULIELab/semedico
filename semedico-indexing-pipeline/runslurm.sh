#!/bin/bash
#SBATCH --mem 12g
#SBATCH --cpus-per-task 1 
#SBATCH -J semedico_processing 

srun java -jar -Xmx8g -Dlogback.configurationFile=/data/semedico/fushin-2.2/semedico-indexing-pipeline/config/logback.xml ~/bin/jcore-pipeline-runner-*.jar pipelinerunner.xml

