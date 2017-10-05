#!/bin/bash
BASEDIR=$(dirname $0)
cd $BASEDIR
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$(pwd)/lib
echo $LD_LIBRARY_PATH
java -Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=y -Djava.library.path=$LD_LIBRARY_PATH -jar VotingWeb.jar