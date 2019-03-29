#!/bin/bash
#SBATCH --mem 70g
#SBATCH --cpus-per-task 20 
#SBATCH -J semedico_processing 

srun java -jar -Xmx60g /var/data/semedico/jcore-pipeline-runner-*.jar pipelinerunner.xml

