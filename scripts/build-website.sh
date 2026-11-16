#!/usr/bin/env bash
# Assembles the team website into _site/: the pages in website/, the architecture diagrams and
# (if they exist) the latest JUnit and JaCoCo reports. Used by .github/workflows/website.yml and
# for previewing locally:
#
#   ./gradlew test               # optional: generates the test and coverage reports
#   scripts/build-website.sh
#   python3 -m http.server -d _site 8000    # then open http://localhost:8000
set -euo pipefail
cd "$(dirname "$0")/.."

rm -rf _site
mkdir -p _site/architecture _site/testing/tests _site/testing/coverage
cp -r website/. _site/
cp -r architecture/v1 architecture/v2 _site/architecture/
if [ -d core/build/reports/tests/test ]; then
  cp -r core/build/reports/tests/test/. _site/testing/tests/
else
  echo "Note: no test report found (run ./gradlew test first)"
fi
if [ -d core/build/reports/jacoco/test/html ]; then
  cp -r core/build/reports/jacoco/test/html/. _site/testing/coverage/
else
  echo "Note: no coverage report found (run ./gradlew test first)"
fi
touch _site/.nojekyll
echo "Website assembled in _site/"
