#!/usr/bin/env sh

if ! command -v openssl > /dev/null; then
  echo "Error: OpenSSL not installed!"
  exit 1
fi

if [ "$#" -ne 1 ]; then
  length=32
else
  length="$1"
fi

openssl rand -base64 "$length"
