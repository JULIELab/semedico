#!/bin/bash
#SBATCH --mem 60g
#SBATCH --cpus-per-task 20 
#SBATCH -J semedico_processing 

srun java -jar -Xmx55g -Dlogback.configurationFile=/home/faessler/Coding/git/semedico/semedico-preprocessing-pipeline/config/logback.xml ~/bin/jcore-pipeline-runner-*.jar pipelinerunner.xml

