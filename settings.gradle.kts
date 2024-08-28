// To build libsignal from source, set the libsignalClientPath property in gradle.properties.
val libsignalClientPath = if (extra.has("libsignalClientPath")) extra.get("libsignalClientPath") else null
if (libsignalClientPath is String) {
  includeBuild(rootDir.resolve(libsignalClientPath + "/java")) {
    name = "libsignal-client"
    dependencySubstitution {
      substitute(module("org.signal:libsignal-client")).using(project(":client"))
    }
  }
}
