#!/bin/bash
set -ex
sfctl application delete --application-id HelloWorld
sfctl application unprovision --application-type-name HelloWorldType --application-type-version 1.0.0
sfctl store delete --content-path HelloWorld
