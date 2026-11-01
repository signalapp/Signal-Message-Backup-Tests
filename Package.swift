// swift-tools-version:6.0

import PackageDescription

let package = Package(
    name: "SignalMessageBackupTests",
    platforms: [
        .iOS(.v15),
    ],
    products: [
        .library(
            name: "SignalMessageBackupTests",
            targets: ["SignalMessageBackupTests"]
        ),
    ],
    targets: [
        .target(
            name: "SignalMessageBackupTests",
            path: ".",
            sources: ["swift"],
            resources: [.copy("test-cases")]
        ),
    ]
)
