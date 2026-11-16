#!/usr/bin/env bash
# Renders all five Assessment 1 PDF deliverables from deliverables/*.html using Headless Chrome,
# verifies page counts against Table 1 + Section 1.2 Formatting Requirements (1 title page +
# content limit + 1 references page, where title page and references do not count towards limits),
# and copies them into website/docs/ so both the website and submission zip use identical PDFs.
set -euo pipefail
cd "$(dirname "$0")/.."

CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
if [ ! -x "$CHROME" ]; then
  echo "Error: Google Chrome not found at $CHROME" >&2
  exit 1
fi

mkdir -p website/docs

render_and_check() {
  local name="$1"
  local max_total_pages="$2"
  local label="$3"

  "$CHROME" --headless --disable-gpu --no-pdf-header-footer \
    --print-to-pdf="$PWD/deliverables/${name}.pdf" \
    "file://$PWD/deliverables/${name}.html" 2>/dev/null

  cp "deliverables/${name}.pdf" "website/docs/${name}.pdf"

  if command -v pdfinfo >/dev/null 2>&1; then
    local pages
    pages=$(pdfinfo "deliverables/${name}.pdf" | awk '/^Pages:/ {print $2}')
    if [ "$pages" -gt "$max_total_pages" ]; then
      echo "FAIL: ${name}.pdf has ${pages} pages (exceeds max ${max_total_pages}: ${label})" >&2
      exit 1
    fi
    echo "OK: ${name}.pdf -> ${pages} pages (limit: ${label})"
  else
    echo "OK: ${name}.pdf rendered"
  fi
}

# Section 1.2: 1 title page (excluded) + Table 1 content limit + 1 references page (excluded)
render_and_check "Req1"  6 "1 title + 4 content (1+3) + 1 references"
render_and_check "Arch1" 8 "1 title + 6 content + 1 references"
render_and_check "Plan1" 7 "1 title + 5 content (2+1+2) + 1 references"
render_and_check "Risk1" 6 "1 title + 4 content (1+3) + 1 references"
render_and_check "Impl1" 3 "1 title + 1 content + 1 references"

echo "All 5 deliverables rendered to deliverables/ and copied to website/docs/."
