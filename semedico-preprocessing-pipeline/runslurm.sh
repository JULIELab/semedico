#!/bin/bash
#SBATCH --mem 70g
#SBATCH --cpus-per-task 20 
#SBATCH -J semedico_processing 

srun java -jar -Xmx60g -Dlogback.configurationFile=/home/faessler/Coding/git/semedico/semedico-preprocessing-pipeline/config/logback.xml /var/data/semedico/jcore-pipeline-runner-*.jar pipelinerunner.xml

