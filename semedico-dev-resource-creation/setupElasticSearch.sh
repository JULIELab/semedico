#!/bin/bash
VERSION=3.1.0-SNAPSHOT
cd dockercontext-es
docker build --tag semedico-es:$VERSION .
docker run -d --name semedico-es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" semedico-es:$VERSION
#TODO: Copy the semedico-indexing-pipeline and adapt it for the dev set indexing