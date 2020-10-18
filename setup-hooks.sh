#!/usr/bin/env bash

echo '#!/bin/sh

cd "$GIT_DIR/.."
echo Running ktlint...
./gradlew :dna:ktlintFormat || exit 1
' > ./.git/hooks/pre-commit

chmod +x ./.git/hooks/pre-commit
