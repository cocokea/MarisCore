#!/bin/sh
DIR="$(cd "$(dirname "$0")" && pwd)"
exec java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
