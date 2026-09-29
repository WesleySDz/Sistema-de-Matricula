#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
./mvnw -q -DskipTests package
exec java -jar target/matricula-mais-0.0.1-SNAPSHOT.jar --spring.profiles.active=terminal "$@"
