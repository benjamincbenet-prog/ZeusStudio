# Phase 1.1A Runtime Contract

The runtime manager owns:

    files/zeus-home/

with:

    bin/
    projects/
    cache/
    tmp/

Environment:

    HOME=<zeus-home>
    PATH=<zeus-home>/bin:/system/bin:/system/xbin
    TMPDIR=<zeus-home>/tmp

A future Node installation should place its launcher/executable in `bin`
or expose a launcher from `bin` that sets any required runtime variables.

Do not assume that an ordinary Linux ARM64 Node binary is Android-compatible.
The next milestone must use an Android-compatible runtime.
