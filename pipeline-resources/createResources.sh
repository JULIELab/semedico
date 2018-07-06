#!/bin/bash
# This script is the one to use as of July 2018 and replaces (finally!) the semedico-resource-management
# script 'createSemedicoProductionResources.sh'.

# This uses the Concept Database Manager to export the required data files from the database.
mvn compile exec:java -Dexec.mainClass=de.julielab.concepts.db.application.ConceptDatabaseApplication -Dexec.args="-e -c neo4j-export.xml"

# This is just for the LuCas select filter which removes any terms we don't know. Perhaps due to different
# resource versions or something. So this is more of a consistency thing.
cut -d"=" -f1 esConsumer/term.2fid > esConsumer/validterms.lst
cut -d"=" -f1 esConsumer/aggregates.2fid >> esConsumer/validterms.lst