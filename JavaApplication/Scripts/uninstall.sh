#!/bin/bash -e

if [[ "$#" != "0" ]];then 
	version="$1"
else 
	version="1.0.0"
fi

appplicationCount=`sfctl application list | grep fabric:/JavaApplication | wc -l`
if [[ "$appplicationCount" -eq "0" ]];then
	echo "Nothing to uninstall"
	exit 0
fi	

sfctl application delete --application-id JavaApplication
if [ $? -ne 0 ]; then
    echo "Application removal failed."
    exit 1
fi

sfctl application unprovision --application-type-name JavaApplicationType --application-type-version $version
if [ $? -ne 0 ]; then
    echo "Unregistering application type failed."
    exit 1
fi

sfctl store delete --content-path JavaApplication
if [ $? -ne 0 ]; then
    echo "Unable to delete image store content."
    exit 1
fi
echo "Uninstall script executed successfully."
