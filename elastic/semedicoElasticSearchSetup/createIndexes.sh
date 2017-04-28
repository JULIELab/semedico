#!/bin/bash

MEDLINE_INDEX_SETTINGS=`cat medlineIndexSettings.json`
GEPI_INDEX_SETTINGS=`cat gepiIndexSettings.json`
SUGGESTION_SEARCH_INDEX_SETTINGS=`cat suggestionsSearchIndexSettings.json`
SUGGESTION_COMPLETION_INDEX_SETTINGS=`cat suggestionsCompletionIndexSettings.json`

# A POSIX variable
OPTIND=1         # Reset in case getopts has been used previously in the shell.

# Initialize our own variables:
medlinename=""
host=""
port=""
suggindexname=""
gepiindexname=""

while getopts "m:h:p:s:g:" opt; do
    case "$opt" in
    m)  medlinename=$OPTARG
        ;;
    h)  host=$OPTARG
        ;;
    p)  port=$OPTARG
        ;;
    s)  suggindexname=$OPTARG
        ;;
    g)  gepiindexname=$OPTARG
        ;;
    esac
done

shift $((OPTIND-1))

[ "$1" = "--" ] && shift

if [ -z "$host" ]; then
	host="localhost";
fi
if [ -z "$port" ]; then
	port="9200";
fi

if [ ! -z "$medlinename" ]; then
  echo "Creating index '$medlinename'."
  curl -XPOST "http://$host:$port/$medlinename" -d "$MEDLINE_INDEX_SETTINGS"
 echo ""
fi

if [ ! -z "gepiindexnamee" ]; then
  echo "Creating index '$gepiindexname'."
  curl -XPOST "http://$host:$port/$gepiindexname" -d "$GEPI_INDEX_SETTINGS"
 echo ""
fi

if [ ! -z "$suggindexname" ]; then
  echo "Creating index 'suggestions' (completion strategy)".
  curl -XPOST "http://$host:$port/suggestions" -d "$SUGGESTION_COMPLETION_INDEX_SETTINGS"
  echo ""
fi
