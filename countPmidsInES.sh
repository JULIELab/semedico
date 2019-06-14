#!/bin/bash

curl http://localhost:9200/documents/_search -d '{  "query": {    "match_all": {}  },  "size": 0,  "aggs": {    "ids": {      "terms": {        "field": "pmid","size":30000000      }    }  }}'
