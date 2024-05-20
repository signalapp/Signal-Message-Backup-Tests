# Signal Message Backup shared integration test cases

This repository contains shared "integration test cases" for Signal message backups. Specifically, this repo contains paired `.jsonproto` and `.binproto` files, each representing a message backup instance.

A `.jsonproto` file contains a JSON representation of the protos serialized into a Backup file, thereby allowing developers to inspect existing test cases and create new test cases manually to test specific scenarios. A `.binproto` file contains a plaintext (unencrypted, not-gzipped) Backup, and is generated from a `.jsonproto` by serializing each contained JSON object as the Proto3 object from `Backup.proto` that it represents.
