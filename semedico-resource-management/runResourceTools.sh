#!/bin/bash
#SBATCH --mem 11000

export CLASSPATH=`echo lib/*.jar | tr ' ' ':'`:target/classes
java -Xmx20g $SEMEDICO_JVM_OPTS -Dfile.encoding=UTF-8 de.julielab.semedico.resources.SemedicoResourceTools $*
