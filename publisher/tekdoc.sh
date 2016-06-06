#!/bin/bash

export TEKDOC_JAR="~/.tekdoc/tekdoc.jar"

install() {
    echo "tekdoc - Installing..."
    mkdir ~/.tekdoc
    cp target/publisher-0.1.0-SNAPSHOT-standalone.jar ~/.tekdoc/tekdoc.jar
    cp tekdoc.sh ~/.tekdoc
}

publish() {
    export JAVA_CMD="-cp ~/.tekdoc/tekdoc.jar publisher.publish"
    java -cp ~/.tekdoc/tekdoc.jar publisher.publish
}

--help() {
  echo -e "Tekdoc v1.0\nUsage:\ntekdoc <command>\n\nCommands:"
  echo -e "install \t\t\t get tekdoc from web and install it locally"
  echo -e "publish <site-config-path> \t site-config-path should point to a site-config.yml file\n"
}

if [ -z "$@" ]
then
    --help
else
   $@
fi



