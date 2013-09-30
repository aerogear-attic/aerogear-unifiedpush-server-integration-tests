#!/bin/bash

BASE_DIR=`pwd`
DOWNLOAD_URL="http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip"
JBOSS_ZIP="$BASE_DIR/jboss-as-7.1.1.Final.zip"
JBOSS_HOME="$BASE_DIR/jboss-as-7.1.1.Final"
AG_UPS_REPO="$BASE_DIR/aerogear-unifiedpush-server"

if [ -d "$AG_UPS_REPO" ]; then
    rm -rf $AG_UPS_REPO/
fi

if [ ! -d "$AG_UPS_REPO" ]; then
    git clone https://github.com/aerogear/aerogear-unifiedpush-server.git $AG_UPS_REPO
fi

if [ -d "$JBOSS_HOME" ]; then
    rm -rf $JBOSS_HOME/
fi

if [ ! -f "$JBOSS_ZIP" ]; then
    echo $DOWNLOAD_URL
    wget -c $DOWNLOAD_URL -P $BASE_DIR/
fi

if [ -f "$JBOSS_ZIP" ]; then
    unzip $JBOSS_ZIP -d $BASE_DIR/
else
    echo "The path does not contain a jboss distribution"
    exit 3
fi

SETUP_FILES_PATH="$BASE_DIR/jboss-as-conf"
STANDALONE_XML_PATH="$SETUP_FILES_PATH/standalone.xml"
KEYSTORE_PATH="$SETUP_FILES_PATH/aerogear.keystore"
JBOSS_AS_STANDALONE_XML_PATH="$JBOSS_HOME/standalone/configuration/standalone.xml"
KEYSTORE_PATTERN="KEYSTORE_PATH"

sed -e s,$KEYSTORE_PATTERN,$KEYSTORE_PATH,g $STANDALONE_XML_PATH > $JBOSS_AS_STANDALONE_XML_PATH

exit 0
