#!/bin/bash
set -ex
sfctl application upload --path HelloWorld --show-progress
sfctl application provision --application-type-build-path HelloWorld
sfctl application create --app-name fabric:/HelloWorld --app-type HelloWorldType --app-version 1.0.0
