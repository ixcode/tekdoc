#!/bin/bash

# http://www.thegeekstuff.com/2010/07/logrotate-examples
# https://www.sans.org/reading-room/whitepapers/logging/ins-outs-system-logging-syslog-1168

# This script is designed to be added to your crontab config so that it will poll a gitlab repo where you are storing your documentation and
# If there are any changes it will re-publish them.
# It expects to be running on the same box as the website by default

# TODO - config so this can run on one machine and SSH to another to publish.

GIT_CHECKOUT_DIR=/Users/jmdb/tmp/technical-documentation

LOGFILE=~/.tekdoc/tekdoc-auto-publish.log

function log() {
    echo "[`date`] $1" >> $LOGFILE
}

log "Polling git for changes..."


