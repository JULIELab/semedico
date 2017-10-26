#!/bin/bash

MEDLINE_INDEX_SETTINGS=`cat semedicoIndexSettings.json`
GEPI_INDEX_SETTINGS=`cat gepiIndexSettings.json`
SUGGESTION_SEARCH_INDEX_SETTINGS=`cat suggestionsSearchIndexSettings.json`
SUGGESTION_COMPLETION_INDEX_SETTINGS=`cat suggestionsCompletionIndexSettings.json`

# A POSIX variable
OPTIND=1         # Reset in case getopts has been used previously in the shell.

# Initialize our own variables:
semedicoindexname=""
host=""
port=""
suggindexname=""
gepiindexname=""

while getopts "m:h:p:s:g:" opt; do
    case "$opt" in
    m)  semedicoindexname=$OPTARG
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

if [ ! -z "$semedicoindexname" ]; then
  echo "Creating index '$semedicoindexname'."
  curl -XPUT "http://$host:$port/$semedicoindexname" -d "$MEDLINE_INDEX_SETTINGS"
 echo ""
fi

if [ ! -z "$gepiindexname" ]; then
  echo "Creating index '$gepiindexname'."
  curl -XPUT "http://$host:$port/$gepiindexname" -d "$GEPI_INDEX_SETTINGS"
 echo ""
fi

if [ ! -z "$suggindexname" ]; then
  echo "Creating index 'suggestions' (completion strategy)".
  curl -XPUT "http://$host:$port/$suggindexname" -d "$SUGGESTION_COMPLETION_INDEX_SETTINGS"
  echo ""
fi
