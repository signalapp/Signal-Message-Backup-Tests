# Signal Message Backup shared integration test cases

This repository contains shared "integration test cases" for Signal message backups. Specifically, this repo contains paired `.jsonproto` and `.binproto` files, each representing a message backup instance.

A `.jsonproto` file contains a JSON representation of the protos serialized into a Backup file, thereby allowing developers to inspect existing test cases and create new test cases manually to test specific scenarios. A `.binproto` file contains a plaintext (unencrypted, not-gzipped) Backup, and is generated from a `.jsonproto` by serializing each contained JSON object as the Proto3 object from `Backup.proto` that it represents.

## Generating a `.binproto` from a `.jsonproto`

To convert a `.jsonproto` test case into its corresponding `.binproto`, simply run:

```sh
; cargo run test-cases/{test-case-name}.jsonproto > test-cases/{test-case-name}.binproto
```

Alternatively, having added a `.jsonproto` file to `test-cases/`, run `./generate-binprotos.sh`. CI will do this on every push/pull-request, to validate that all committed `.binproto` files match their corresponding `.jsonproto`s.
