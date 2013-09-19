#!/bin/bash

BASE_DIR=`pwd`/$(dirname $0)
DOWNLOAD_URL="http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip"
JBOSS_ZIP="$BASE_DIR/jboss-as-7.1.1.Final.zip"
JBOSS_HOME="$BASE_DIR/jboss-as-7.1.1.Final"
BASE_REPO_DIR="$BASE_DIR/.."
AG_UPS_REPO="$BASE_REPO_DIR/aerogear-unifiedpush-server"

if [ -d "$AG_UPS_REPO" ]; then
    rm -rf $AG_UPS_REPO/
fi

if [ ! -d "$AG_UPS_REPO" ]; then
    git clone https://github.com/aerogear/aerogear-unifiedpush-server.git
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

chmod +x $JBOSS_HOME/bin/standalone.sh
nohup $JBOSS_HOME/bin/standalone.sh &

echo "Waiting JBoss to start up"
jboss_status="0"
failures=0
until [[ "$jboss_status" != "0" ]]; do
    jboss_status=`netstat -an | grep 127.0.0.1:9999 | grep -v grep | wc -l`
    echo "$jboss_status"
    if [[ "$jboss_status" == "0" ]]; then
        let "failures +=1"
        echo "$failures"
        if [[ $failures -gt 3 ]]; then
            echo "4 Failures"
            exit 1
        fi
    fi
    sleep 4
done

# setup aerogear keystore
echo "Setting up keystore"
groovy -cp "$BASE_DIR/jboss-cli-client.jar" $BASE_DIR/jboss-setup.groovy -n aerogear-ssl -w aerogear -a aerogear -l TLSv1 -f $BASE_DIR/aerogear.keystore

# stop
$JBOSS_HOME/bin/jboss-cli.sh --connect command=:shutdown

exit 0
