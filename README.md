# Zeus Studio — Phase 1.1A — CodeOnTheGo Edition

This milestone validates the runtime-management layer before adding Node.js.

## Build configuration

- Gradle 8.13.1
- Android Gradle Plugin 8.13.2
- JDK 17
- compileSdk 36
- targetSdk 36
- minSdk 26
- Java
- No Kotlin
- No Compose
- No NDK
- No CMake

## Phase 1.1A goals

This version adds:

- Private runtime directory management.
- `zeus-home/bin` executable directory.
- Persistent `projects`, `cache`, and `tmp` directories.
- Runtime environment variables.
- PATH injection.
- Executable permission handling.
- Android ABI detection.
- A real executable test command.
- Runtime diagnostics.
- Terminal execution of a PATH-installed command.

The test command is:

    zeus-runtime-test

It is a tiny `/system/bin/sh` script installed into the application's
private `zeus-home/bin` directory. It is intentionally not Node.js.

## Test procedure

After installing the APK:

1. Tap **Initialize**.
2. Confirm `Runtime Test` says `✓ installed/executable`.
3. Tap **Runtime Test**.

Expected terminal output:

    $ zeus-runtime-test
    ZEUS_RUNTIME_TEST_OK
    HOME=.../files/zeus-home
    ARCH=...
    PREFIX=.../files/zeus-home/bin

4. Tap **Shell**.

Expected output includes:

    .../files/zeus-home
    .../files/zeus-home/bin:/system/bin:/system/xbin
    ...

5. `node --version` and `zeus --version` should still say not installed.
   That is expected at this stage.

## Why we do this first

The IDE must be able to:

    extract -> install -> chmod -> PATH -> execute

a future Android-native runtime without depending on Termux.

Android supports native code by ABI, including `arm64-v8a`, but the actual
Node runtime must be built for Android rather than assuming a conventional
Linux/glibc executable will work.

## Next

Phase 1.1B will introduce the actual Android-compatible Node runtime and
connect it to this runtime manager.

The target is:

    node --version

Then:

    npm --version

Only after that works will we install:

    @zeppos/zeus-cli
