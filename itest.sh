#!/usr/bin/env bash
set -euo pipefail
IFS=$'\t\n'
set -x

# Build the template itself
lein do clean, test

# Generate a project based on the template and run tests in it
cd target
# We don't need to install it to ~/.m2, because it's already available on the classpath

export CYRUS_TEST=1

run-test() {
    DEBUG=1 lein new cyrus "$@"
    local project_dir=${1##*/}
    pushd "$project_dir"
        lein ancient
        lein test
        lein uberjar
        TEST_TIMEOUT=1000 NREPL_ENABLED=true java -jar "target/uberjar/$project_dir.jar"
    popd
}

run-test org.example/foo-bar1 +all +nakadi +credentials
run-test org.example/foo-bar2
run-test org.example/foo-bar3 +all
run-test org.example/foo-bar4 +nakadi
run-test org.example/foo-bar5 +credentials
run-test org.example/foo-bar6 +http
run-test org.example/foo-bar7 +db
run-test org.example/foo-bar8 +swagger1st
run-test org.example/foo-bar9 +ui

# Just in case we want to try it outside of target/
lein install
echo "Use:   CYRUS_TEST=1 lein new cyrus org.example.footeam/bar-project --snapshot -- +all"
