# This script tries to find Semedico-related projects / files in the same
# workspace and to evaluate whether those projects (UIMA pipelines, for
# example) are configured for production. If not, warnings are displayed.
# To be checked:
#  semedico-app:
#      dbcConfiguration.xml - is active database a production or test DB?
#      CPE.xml - is unit test table or the semedico mirror table configured?
#      CPE.xml - is CasToESConsumer configured for production ES? The address
#                should match the one in the jpp-application (see below)!
#  jules-preprocessing-pipelines (jpp) in general:
#      dbcConfigurationMedlineXml.xml
#      dbcConfigurationXmi.xml
#  jpp-semedico-metadata:
#      bioportal.gazetteer.properties - is test or productive dict used?
#  jpp-application:
#      elasticsearch.properties - is it configured for development or production?

RELEASE_SUFFIX=""

SEMEDICOAPP="../semedico-app$RELEASE_SUFFIX"
SEMEDICOAPP_DBC="$SEMEDICOAPP/conf/dbcConfigurationMedlineMirror.xml"
SEMEDICOAPP_CPE="$SEMEDICOAPP/desc/CPEMedlineToES.xml"
SEMEDICOAPP_TDA="$SEMEDICOAPP/conf/tda_config"

JPP="../jules-preprocessing-pipelines$RELEASE_SUFFIX"
JPP_DBC_MEDLINE="$JPP/resources/dbcConfigurationMedlineXml.xml"
JPP_DBC_XMI="$JPP/resources/dbcConfigurationMedlineXmi.xml"
BIOPORTAL_SIMPLE_GAZ_PROPS="$JPP/jpp-semedico-metadata/resources/DictionaryAnnotatorParams/bioportal.gazetteer.simple.properties"
BIOPORTAL_AGG_GAZ_PROPS="$JPP/jpp-semedico-metadata/resources/DictionaryAnnotatorParams/bioportal.gazetteer.aggregates.properties"
BIOPORTAL_AGG_EL_GAZ_PROPS="$JPP/jpp-semedico-metadata/resources/DictionaryAnnotatorParams/bioportal.gazetteer.aggelements.properties"
ES_DOC_DELETER_CONF="$JPP/jpp-application/conf/elasticsearch.properties"

# Switch of case with string matching; only works with bash!
shopt -s nocasematch

# Parameter 1: XPath
# Parameter 2: XML File
function getXpathValue {
	xpath=$1;
	file=$2;
    local result;
	if [[ `uname` =~ .*linux.* ]]; then
		# Linux operating system
        result=`xpath -e $1 $2 2> /dev/null`
	else
		# Another operation system; for the time being, we assume a Mac
		result=`xpath $2 $1 2> /dev/null`
	fi;
    exitcode=$?;
    if [[ "$exitcode" != "0" ]]; then
        echo "xpath command execution error $exitcode, aborting" 1>&2
        exit $exitcode;
    fi;
	xpathResult=$result
}


# ============ semedico-app ================
echo
echo "=== Checking semedico-app for production configuration ==="
echo

# ---------== Checks on dbcConfiguration.xml ==-------------
echo "Checking file $SEMEDICOAPP_DBC for production configuration..."
getXpathValue //activeDBConnection $SEMEDICOAPP_DBC
activeDBConn=$xpathResult
if [ -z $activeDBConn ]; then
    echo "ERROR: Could not retrieve a value for the active database connection." 1>&2
else
    echo "Got '$activeDBConn' as active database connection for semedico-app."
    if [[ $activeDBConn =~ .*test.* ]]
    then
        echo "ERROR: Semedico-app dbcConfiguration.xml is configured for database '$activeDBConn' which seems to be a test data base." 1>&2
    else
        echo "OK." 
    fi
fi
echo

# ---------== Checks on CPE.xml== ---------------
echo "Checking file $SEMEDICOAPP_CPE for production configuration..."

# ------- MedlineReader ---------
getXpathValue //collectionIterator/configurationParameterSettings/nameValuePair[name=\"Table\"]/value/string  $SEMEDICOAPP_CPE
#xmiTable=`xpath $SEMEDICOAPP_CPE //collectionIterator/configurationParameterSettings/nameValuePair[name=\"Table\"]/value/string 2> /dev/null`
xmiTable=$xpathResult
if [ -z $xmiTable ]; then
    echo "ERROR: Could not retrieve a value for the set XMI table." 1>&2
else
    echo "Table for MedlineReader: Found value '$xmiTable' as table to be read from in the MedlineReader configuration"
    if [[ $xmiTable =~ .*test.* ]]
    then
        echo "ERROR: Semedico-app CPE.xml is configured to read from table '$xmiTable' which seems to be a test data table." 1>&2
    else
        echo "OK." 
    fi
fi
echo

# ------- ElasticSearch consumer -----------
getXpathValue  //casProcessor[@name=\"ElasticSearchConsumer\"]\
/configurationParameterSettings/nameValuePair[name=\"url\"]\
/value/string $SEMEDICOAPP_CPE
semedicoAppESUrl=$xpathResult
if [ -z $semedicoAppESUrl ]; then
    echo "ERROR: Could not retrieve a value for the ElasticSearch URL" 1>&2
else
    echo "ElasticSearch host: Found value '$semedicoAppESUrl' as ElasticSearch server URL in semedico-app CPE.xml."
    if [[ $semedicoAppESUrl =~ .*localhost.* ]]
    then
        echo "ERROR: Semedico-app CPE.xml is configured for an ElastiSearch server at 'localhost'. This should be a concrete server name." 1>&2
    else
    	echo "OK."; 
    fi
fi
echo

# ---------- Term Distance Annotator -----------
dbUrl=`grep DbUrl $SEMEDICOAPP_TDA`
echo "Got Neo4j endpoint $dbUrl for Term Distance Annotator"
if [[ $dbUrl =~ .*localhost.* ]];
then
    echo "ERROR: semedico-app Term Distance Annotator is configured for $dbUrl where the actual server should be configured." 1>&2
else
    echo "OK.";
fi
echo


# ================== jules-preprocessing-pipelines ==================
echo "=== Checking jules-preprocessing-pipelines ==="
echo
jppError=0
echo "Checking DBC configuration file $JPP_DBC_MEDLINE for production configuration..."
#activeDBConn=`xpath $JPP_DBC_MEDLINE //activeDBConnection 2> /dev/null`
getXpathValue //activeDBConnection $JPP_DBC_MEDLINE
activeDBConn=$xpathResult
echo "Got $activeDBConn for JPP Medline XML configuration"
if [[ $activeDBConn =~ .*test.* ]]
then
    echo "ERROR: jules-preprocessing-pipelines dbcConfigurationMedlineXml.xml is configured for database '$activeDBConn' which seems to be a test data base." 1>&2
    jppError=1
fi
echo "Checking DBC configuration file $JPP_DBC_XMI for production configuration..."
#activeDBConn=`xpath $JPP_DBC_XMI //activeDBConnection 2> /dev/null`
getXpathValue //activeDBConnection $JPP_DBC_XMI
activeDBConn=$xpathResult
echo "Got $activeDBConn for JPP XMI configuration"
if [[ $activeDBConn =~ .*test.* ]]
then
    echo "ERROR: jules-preprocessing-pipelines dbcConfigurationXmi.xml is configured for database '$activeDBConn' which seems to be a test data base." 1>&2
    jppError=1
fi
if [ $jppError -eq 0 ];
then
	echo "OK."
fi
echo


# ============ jpp-semedico-metadata ================
echo "--= Checking jpp-semedico-metadata for production configuration =--"
echo

# ---------------== Checks on gazetteer configuration file ==----------------
echo "Checking file $BIOPORTAL_SIMPLE_GAZ_PROPS for production configuration..."
bioPortalDict=`grep '^DictionaryFile' $BIOPORTAL_SIMPLE_GAZ_PROPS`
echo "Got $bioPortalDict as dictionary."
if [[ $bioPortalDict =~ .*test.* ]]
then
    echo "ERROR: jules-preprocessingpipelines/jpp-semedico-metadata BioPortal gazetteer is configured to read from dictionary file '$bioPortalDict' which seems to be a test dictionary." 1>&2
else
    echo "OK." 
fi
echo
echo "Checking file $BIOPORTAL_AGG_GAZ_PROPS for production configuration..."
bioPortalDict=`grep '^DictionaryFile' $BIOPORTAL_AGG_GAZ_PROPS`
echo "Got $bioPortalDict as dictionary."
if [[ $bioPortalDict =~ .*test.* ]]
then
    echo "ERROR: jules-preprocessingpipelines/jpp-semedico-metadata BioPortal gazetteer is configured to read from dictionary file '$bioPortalDict' which seems to be a test dictionary." 1>&2
else
    echo "OK." 
fi
echo
echo "Checking file $BIOPORTAL_AGG_EL_GAZ_PROPS for production configuration..."
bioPortalDict=`grep '^DictionaryFile' $BIOPORTAL_AGG_EL_GAZ_PROPS`
echo "Got $bioPortalDict as dictionary."
if [[ $bioPortalDict =~ .*test.* ]]
then
    echo "ERROR: jules-preprocessingpipelines/jpp-semedico-metadata BioPortal gazetteer is configured to read from dictionary file '$bioPortalDict' which seems to be a test dictionary." 1>&2
else
    echo "OK." 
fi
echo


echo "--= Checking jpp-application for production configuration =--"
echo

# ---------------== Checks on ElasticSearch document deleter configuration file ==----------------
echo "Checking file $ES_DOC_DELETER_CONF for production configuration..."
clusterName=`grep 'clusterName' $ES_DOC_DELETER_CONF`
host=`grep 'host' $ES_DOC_DELETER_CONF`
port=`grep 'port' $ES_DOC_DELETER_CONF`
esDeleterError=0
if [[ $clusterName =~ .*test.* || $clusterName =~ .*dev.*  ]]
then
    echo "ERROR: jules-preprocessing-pipelines/jpp-application ElaticSearch document deleter is set to cluster name '$clusterName' which seems to be a test or development cluster." 1>&2
    esDeleterError=1
fi
if [[ $host =~ .*localhost.* || $host =~ .*notset.* ]]
then
    echo "ERROR: jules-preprocessing-pipelines/jpp-application ElaticSearch document deleter is set to host name '$host'. It should be set to the actual server hosting ElasticSearch." 1>&2
    esDeleterError=1
fi
if [[ "$esDeleterError" -eq 0 ]];
then
    echo "Got ES cluster name $clusterName."
    echo "Got ES host $host."
    echo "Got ES port $port."
	echo "OK."
fi
echo
