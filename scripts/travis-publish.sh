#!/bin/bash

# Example setting to use at command line for testing:
# export TRAVIS_SCALA_VERSION=2.10.5;export TRAVIS_PULL_REQUEST="false";export TRAVIS_BRANCH="master"

# if [[ $TRAVIS_PULL_REQUEST == "false" && $TRAVIS_BRANCH == "master" && $(cat version.sbt) =~ "-SNAPSHOT" ]]; then
#   export publish_cmd="publish gitSnapshots publish"
# fi

sbt_cmd="sbt ++${TRAVIS_SCALA_VERSION}!"
scala_js="$sbt_cmd macrosJS/test && $sbt_cmd coreJS/test && $sbt_cmd extrasJS/test && $sbt_cmd lawsJS/test && $sbt_cmd testsJS/test"

if [[ "$TRAVIS_SCALA_VERSION" == 2.13* ]]; then
    # tut currently has issues with 2.13
    # see https://github.com/tpolecat/tut/issues/246
    scala_jvm="$sbt_cmd clean coverage validateJVM"
else
    scala_jvm="$sbt_cmd clean coverage validateJVM docs/tut"
fi

run_cmd="$scala_js && $scala_jvm"
eval $run_cmd
