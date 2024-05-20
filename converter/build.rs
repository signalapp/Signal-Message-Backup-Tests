//
// Copyright 2022 Signal Messenger, LLC.
// SPDX-License-Identifier: AGPL-3.0-only
//

fn main() {
    const PROTOS_DIR: &str = "protos";

    let out_dir = format!(
        "{}/{PROTOS_DIR}",
        std::env::var("OUT_DIR").expect("OUT_DIR env var not set")
    );
    std::fs::create_dir_all(&out_dir).expect("failed to create output directory");

    const PROTOS: &[&str] = &["src/proto/backup.proto"];
    let mut codegen = protobuf_codegen::Codegen::new();

    codegen
        .protoc()
        .protoc_extra_arg(
            // Enable optional fields. This isn't needed in the most recent
            // protobuf compiler version, but adding it lets us support older
            // versions that might be installed in CI or on developer machines.
            "--experimental_allow_proto3_optional",
        )
        .include("src")
        .out_dir(&out_dir)
        .inputs(PROTOS)
        .run_from_script();

    for proto in PROTOS {
        println!("cargo:rerun-if-changed={}", proto);
    }
}
