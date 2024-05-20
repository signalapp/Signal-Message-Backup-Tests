# Signal Message Backup shared integration test cases

At a high level, the goal of Backup integration tests is to validate that each client can import a given Backup instance into local state; export that local state into a new Backup instance; and confirm that the imported and exported Backups are equivalent. This requires a representation of Backup instances that is both human- and computer-readable and -writable, and a set of shared Backup instances that all clients are consuming in tests.

## Key Terms

- “Backup binary” format: the `<varint><proto data>...` format used to serialize a Backup.
- `.binproto`: a file extension identifying a backup binary file that is neither gzipped nor encrypted.

## Representing test-case Backup instances

We will represent Backup test-case instances using JSON, relying on Proto3’s [JSON-mapping specification](https://protobuf.dev/programming-guides/proto3/#json) to convert types in `Backup.proto` to JSON objects. Specifically, a JSON representation of a Backup (a “`.jsonproto`” file) will contain a single top-level JSON array containing JSON representations of the ordered protos in a backup binary.

This format will allow developers to manually write test cases representing specific scenarios as well as to inspect and perform manual validation on existing test cases during development.

### Importing test cases in client tests

While `.jsonproto` is human-interpretable, client test frameworks are expecting to import `.binproto` files. Fortunately, a `.jsonproto` is straightforwardly convertible to `.binproto` by reading the JSON array, mapping each contained object back to its Proto3 representation, and re-serializing those protos.

To convert a `.jsonproto` test case into its corresponding `.binproto`, ensure you have Rust/`cargo` installed on your machine and run:

```sh
; cargo run test-cases/{test-case-name}.jsonproto > test-cases/{test-case-name}.binproto
```

Alternatively, having added a `.jsonproto` file to `test-cases/`, run `./scripts/generate-binprotos.sh`. CI will do this on every push/pull-request, to validate that all committed `.binproto` files match their corresponding `.jsonproto`s. A generated `.binproto` should be committed to the repo for each added `.jsonproto`.
