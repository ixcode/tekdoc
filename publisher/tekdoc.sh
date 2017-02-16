#!/bin/bash

export TEKDOC_JAR="~/.tekdoc/tekdoc.jar"

COMMAND=$1
SITE_CONFIG=$2

install() {
    echo "tekdoc [install] - Installing to ~/.tekdoc"
    mkdir -p ~/.tekdoc
    cp target/publisher-0.1.0-SNAPSHOT-standalone.jar ~/.tekdoc/tekdoc.jar
    cp tekdoc.sh ~/.tekdoc
    echo "tekdoc [install] - Completed OK"
}

publish() {
    echo "tekdoc [publish] - Using site config [${SITE_CONFIG}]"
    #export JAVA_CMD="-cp ~/.tekdoc/tekdoc.jar publisher.publish"
    java -cp ~/.tekdoc/tekdoc.jar publisher.publish ${SITE_CONFIG}
    if [ $? -eq 0 ]; then
        echo "tekdoc [publish] - Completed OK"
        exit 0
    else
        echo "tekdoc [publish] - Failed to Publish!"
        exit -1        
    fi    
}

ci() {
    echo "Using site config ${SITE_CONFIG} going into CI mode"
    #export JAVA_CMD="-cp ~/.tekdoc/tekdoc.jar publisher.publish"
    java -cp ~/.tekdoc/tekdoc.jar publisher.ci ${SITE_CONFIG}
}

--help() {
  echo -e "Tekdoc v1.0\nUsage:\ntekdoc <command>\n\nCommands:"
  echo -e "install \t\t\t get tekdoc from web and install it locally"
  echo -e "publish <site-config-path> \t site-config-path should point to a site-config.yml file\n"
}

if [ -z "$COMMAND" ]
then
    --help
else
   $COMMAND
fi



