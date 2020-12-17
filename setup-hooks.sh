#!/usr/bin/env bash

echo '#!/bin/sh

cd "$GIT_DIR/.."
FILES=$(git diff --cached --name-only --diff-filter=ACMR "*.kt" | sed "s| |\\ |g")
[ -z "$FILES" ] && echo "Nothing to run ktlint on." && exit 0
echo Running ktlint...
./gradlew :dna:ktlintFormat || exit 1
echo "$FILES" | xargs git add
exit 0
' > ./.git/hooks/pre-commit

chmod +x ./.git/hooks/pre-commit
