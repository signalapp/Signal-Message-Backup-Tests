#!/usr/bin/env sh

uuid=$(uuidgen)

echo "UUID: $uuid"
echo "UUID Base64: $(echo $uuid | xxd -r -p | base64)"
