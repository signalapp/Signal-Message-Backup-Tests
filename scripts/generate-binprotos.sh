#!/usr/bin/env sh

if ! command -v cargo > /dev/null; then
  echo "Error: cargo not installed!"
  exit 1
fi

if [ -z "$TEST_CASE_DIR" ]; then
  TEST_CASE_DIR="$PWD/test-cases"
fi

for TEST_CASE_FILE in $(ls $TEST_CASE_DIR/*.jsonproto); do
  TEST_CASE_NAME=$(basename $TEST_CASE_FILE .jsonproto)

  cargo run \
    "$TEST_CASE_DIR/$TEST_CASE_NAME.jsonproto" \
    > "$TEST_CASE_DIR/$TEST_CASE_NAME.binproto"
done
