#!/usr/bin/env sh

if ! command -v cargo > /dev/null; then
  echo "Error: cargo not installed!"
  exit 1
fi

if [ "$#" -ne 1 ]; then
  echo "Error: expected the jsonproto file name as an argument!"
  exit 1
fi

TEST_CASE_FILE="$1"

if [ -z "$TEST_CASE_DIR" ]; then
  TEST_CASE_DIR="$PWD/test-cases"
fi

TEST_CASE_NAME=$(basename $TEST_CASE_FILE .jsonproto)

cargo run \
  "$TEST_CASE_DIR/$TEST_CASE_NAME.jsonproto" \
  > "$TEST_CASE_DIR/$TEST_CASE_NAME.binproto"
