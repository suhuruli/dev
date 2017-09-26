#!/bin/bash
set -ex
sfctl application upload --path ReliableCollectionsSample --show-progress
sfctl application provision --application-type-build-path ReliableCollectionsSample
sfctl store delete --content-path ReliableCollectionsSample
sfctl application create --app-name fabric:/ReliableCollectionsSample --app-type ReliableCollectionsSampleType --app-version 1.0
