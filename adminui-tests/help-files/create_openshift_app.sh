#!/bin/bash


if [ $# -ne 4 ]; then
    echo "Usage:"
    echo "  "`basename $0`" <login> <password> <appname> <github-repository-and-commit>"
    exit 1
fi

# login to OpenShift, do not log password to console
set +x
echo yes | rhc setup -l $1 -p $2 --server openshift.redhat.com
set -x

# list apps on OpenShift and delete one with the same name if it exitss
apps=`rhc apps`
echo "existing apps: $apps"
if [[ "$apps" =~ "$3" ]]; then
    echo "$3 app exists and will be deleted"
    echo yes | rhc app delete $3
fi

# create app
rhc app create --no-git $3 "https://cartreflect-claytondev.rhcloud.com/reflect?github=$4" mysql-5.1
rhc logout
