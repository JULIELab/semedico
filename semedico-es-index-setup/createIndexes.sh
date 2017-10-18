#!/bin/bash

# A POSIX variable
OPTIND=1         # Reset in case getopts has been used previously in the shell.

# Initialize our own variables:
host=""
port=""
prefix=""

while getopts "h:p:f:" opt; do
    case "$opt" in
    h)  host=$OPTARG
        ;;
    p)  port=$OPTARG
        ;;
    f)  prefix=$OPTARG
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

for i in mappings/*.json; do 
	  echo "Creating index '$i'."
	  indexname=$(echo $i | sed 's/mappings\/\(.*\)\.json/\1/')
	  indexname=$prefix$indexname
	  echo $indexname
	  curl -XPUT "http://$host:$port/$indexname" -d "`cat $i`"
	 echo ""
done
