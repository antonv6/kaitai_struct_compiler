# Developers README

KS compiler is written in [Scala language] and thus uses [SBT] for
building. It can be compiled in one of 2 ways:

* generating Java `.class` files, to be run in JVM
* generating JavaScript `.js` file, to be run either inside a browser or
  in node.js environment

[Scala language]: http://www.scala-lang.org/
[SBT]: http://www.scala-sbt.org/

## Building for JVM

We use [sbt-native-packager] to build deployable formats.

[sbt-native-packager]: http://www.scala-sbt.org/sbt-native-packager/

### Building an [universal] (.zip) package

[universal]: http://www.scala-sbt.org/sbt-native-packager/formats/universal.html

`sbt compilerJVM/universal:packageBin` → generates `jvm/target/universal/kaitai-struct-compiler-*.zip`

### Building [Debian package]

[Debian package]: http://www.scala-sbt.org/sbt-native-packager/formats/debian.html

1. Install prerequisites: `sudo -i apt-get install dpkg-deb dpkg-sig dpkg-genchanges lintian fakeroot`
2. `sbt compilerJVM/debian:packageBin` -> generates `jvm/target/kaitai-struct-compiler_*_all.deb`

### Building [Windows package]

[Windows package]: http://www.scala-sbt.org/sbt-native-packager/formats/windows.html

1. Install WIX
2. `sbt compilerJVM/windows:packageBin` -> genereates `jvm/target/windows/kaitai-struct-compiler.msi`

### Dependencies for JVM target

TODO

## Building for JavaScript platform

Building to JavaScript platform is done using a Scala.js project. Note
that it uses a somewhat different set of dependencies, as they must
actually be JavaScript libraries, not Java jars.

1. Run `sbt fastOptJS` -> generates `js/target/scala-2.11/kaitai-struct-compiler-fastopt.js`
2. Use this JavaScript file on a website

### Dependencies for JavaScript target

TODO

## Publishing a new version

1. Choose a new version number (WIX imposes harsh requirements for
  version to look like `x.x.x.x`) and update it in `build.sbt`,
  `version := ...`, commit
2. Prepare an entry in RELEASE_NOTES.md, commit
3. Create version tag:
  * `git tag $VERSION`
  * `git push --tags`
4. Update [main repository](https://github.com/kaitai-io/kaitai_struct)
5. Create new version at:
  * https://bintray.com/kaitai-io/debian/kaitai-struct-compiler/new/version
  * https://bintray.com/kaitai-io/universal/kaitai-struct-compiler/new/version
6. Upload:
  * https://bintray.com/kaitai-io/debian/kaitai-struct-compiler/$VERSION/upload
    * Debian distribution: `jessie`
    * Debian component: `main`
    * Debian architecture: `all`
    * Attached file: `jvm/target/kaitai-struct-compiler_*_all.deb`
  * https://bintray.com/kaitai-io/universal/kaitai-struct-compiler/$VERSION/upload
    * Attached file: `jvm/target/universal/kaitai-struct-compiler-*.zip`
7. Publish them all

## Adding new language

Don't forget to update lists of languages:

* /build.sbt - supportedLanguages
* https://github.com/kaitai-io/kaitai_struct — project description
* https://github.com/kaitai-io/kaitai_struct_compiler — project description
* https://github.com/kaitai-io/kaitai_struct_compiler/blob/master/README.md — `-t` option documentation
* https://bintray.com/kaitai-io/debian/kaitai-struct-compiler/view — package description
