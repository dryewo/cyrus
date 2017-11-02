#!/usr/bin/env bash
set -euo pipefail
IFS=$'\t\n'
set -x

# Build the template itself
lein do clean, test

# Generate a project based on the template and run tests in it
cd target
# We don't need to install it to ~/.m2, because it's already available on the classpath

export MILEY_CYRUS_TEST=1

DEBUG=1 lein new miley-cyrus com.example/foo-bar1
pushd foo-bar1
    lein test
    lein uberjar
    lein ancient
popd

DEBUG=1 lein new miley-cyrus com.example/foo-bar2 +http
pushd foo-bar2
    lein test
    lein uberjar
    lein ancient
popd

# Just in case we want to try it outside of target/
lein install
echo "Use:   MILEY_CYRUS_TEST=1 lein new miley-cyrus org.example.footeam/bar-project +http --snapshot"
