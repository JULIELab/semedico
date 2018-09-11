#!/bin/bash
#SBATCH --mem 32g
#SBATCH --cpus-per-task 5
#SBATCH -J semedico_processing 

srun java -jar -Xmx28g -Dlogback.configurationFile=/home/faessler/Coding/git/semedico/semedico-preprocessing-pipeline/config/logback.xml ~/bin/jcore-pipeline-runner-*.jar pipelinerunner5threads.xml

