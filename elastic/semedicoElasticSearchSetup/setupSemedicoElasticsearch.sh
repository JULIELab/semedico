#############################################
#
# NOTE: Works for ElasticSearch version 5.3.2
#
##############################################

if [ $# -eq 0 ]; then
    echo "Usage: $0 <path to fresh downloaded and extracted ElasticSearch directory>"
    exit 1
fi

if [ ! -d "$1" ]; then
    echo "ElasticSearch directory '$1' does not exist.";
    exit 1
fi

ES_PID=`pgrep -f ElasticSearch`
if [ -n "$ES_PID" ]; then
		echo "ElasticSearch is already running at PID $ES_PID. Please shutdown the old installation first."
		exit 1
fi

ELASTICSEARCH_DIR=$1

#INDEX_ANALYSER_SETTINGS=indexAnalyzerSettings.yml
CLUSTER_NAME="cluster.name: semedicoDev"
MANDATORY_PLUGINS="plugin.mandatory: elasticsearch-mapper-preanalyzed,analysis-icu"


echo "Setting cluster name to $CLUSTER_NAME."
echo $CLUSTER_NAME >> $ELASTICSEARCH_DIR/config/elasticsearch.yml

echo "Setting mandatory plugins: $MANDATORY_PLUGINS."
echo $MANDATORY_PLUGINS >> $ELASTICSEARCH_DIR/config/elasticsearch.yml

#echo "Appending index analyzer settings to elasticsearch.yml."
#cat $INDEX_ANALYSER_SETTINGS >> $ELASTICSEARCH_DIR/config/elasticsearch.yml

echo "Adding ElasticSearch configuration"
cat configuration.yml >> $ELASTICSEARCH_DIR/config/elasticsearch.yml


echo "Installing elasticsearch-mapper-preanalyzed plugin"
$ELASTICSEARCH_DIR/bin/elasticsearch-plugin install de.julielab:elasticsearch-mapper-preanalyzed:5.3.2

echo "Installing ICU plugin."
$ELASTICSEARCH_DIR/bin/elasticsearch-plugin install analysis-icu

echo "Copying configuration files like ICU rules and stopwords."
cp -r config/* $ELASTICSEARCH_DIR/config

echo "Starting up ElasticSearch for index configuration."
$ELASTICSEARCH_DIR/bin/elasticsearch -d -p pid
sleep 15

./createIndexes.sh -m semedico

sleep 10

echo "Shutting down ElasticSearch."
kill `cat pid`

echo "Done."
