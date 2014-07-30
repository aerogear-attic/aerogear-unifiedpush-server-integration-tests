#!/bin/bash

# Instance of OpenShift brooker on Beaker has token-based access and it is already good to go.
#
# Only necessary thing to do is to create an application itself.

if [ $# -ne 2 ]; then
    echo "Usage:"
    echo "  "`basename $0`" <appname> <github-repository-and-commit>"
    exit 1
fi

# list apps on OpenShift and delete one with the same name if it exitss
apps=`rhc apps`
echo "existing apps: $apps"
if [[ "$apps" =~ "$1" ]]; then
    echo "$1 app exists and will be deleted"
    echo yes | rhc app delete $1
fi

# create app
rhc app create --no-git $1 "https://cartreflect-claytondev.rhcloud.com/reflect?github=$2" mysql-5.1
