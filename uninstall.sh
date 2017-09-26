#!/bin/bash
set -ex
sfctl application delete --application-id ReliableCollectionsSample
sfctl application unprovision --application-type-name ReliableCollectionsSampleType --application-type-version 1.0
sfctl store delete --content-path ReliableCollectionsSample