#!/usr/bin/env bash
# Builds, verifies and packages the complete ENG1 Assessment 1 submission archive into:
#   dist/Assessment1_Cohort2_Team8.zip
#
# Contents match Table 1 of the Assessment 1 specification:
#   url1.txt, Req1.pdf, Arch1.pdf, Plan1.pdf, Risk1.pdf, Impl1.pdf, GoosePatrol.jar, code/
set -euo pipefail
cd "$(dirname "$0")/.."

echo "=== 1. Rendering & verifying PDF deliverables ==="
bash scripts/build-deliverables.sh

echo "=== 2. Running automated test suite & building executable fat JAR ==="
./gradlew test lwjgl3:jar --console=plain

echo "=== 3. Assembling & verifying static website (_site/) ==="
bash scripts/build-website.sh

python3 - <<'PY'
import os, re, sys

site_dir = "_site"
missing = []
for root, _, files in os.walk(site_dir):
    # Skip generated Gradle/JaCoCo report subtrees; check all team-authored HTML pages
    rel_root = os.path.relpath(root, site_dir)
    if rel_root.startswith("testing"):
        continue
    for f in files:
        if not f.endswith(".html"):
            continue
        path = os.path.join(root, f)
        html = open(path, encoding="utf-8").read()
        for attr, target in re.findall(r'(href|src)="([^"]+)"', html):
            if target.startswith(("http://", "https://", "#", "mailto:")):
                continue
            clean = target.split("#")[0].split("?")[0]
            if not clean:
                continue
            resolved = os.path.normpath(os.path.join(root, clean))
            if not os.path.exists(resolved):
                missing.append((path, target, resolved))

if missing:
    for p, t, r in missing:
        print(f"BROKEN LINK in {p}: {t} -> {r}", file=sys.stderr)
    sys.exit(1)
print("OK: All local links and assets in _site/ verified.")
PY

echo "=== 4. Verifying JAR Java 17 bytecode & embedded licences ==="
JAR_SRC="lwjgl3/build/libs/GoosePatrol-1.0.0.jar"
python3 - <<'PY'
import struct, sys, zipfile

jar_path = "lwjgl3/build/libs/GoosePatrol-1.0.0.jar"
with zipfile.ZipFile(jar_path) as zf:
    names = set(zf.namelist())
    required = [
        "THIRD-PARTY-NOTICES.txt",
        "licenses/Apache-2.0.txt",
        "licenses/LGPL-2.1.txt",
        "licenses/LWJGL-BSD-3-Clause.txt",
        "licenses/OFL-1.1-LiberationSans.txt",
        "com/goosepatrol/lwjgl3/Lwjgl3Launcher.class",
        "com/goosepatrol/GoosePatrolGame.class",
    ]
    for req in required:
        if req not in names:
            print(f"MISSING in JAR: {req}", file=sys.stderr)
            sys.exit(1)
    cls_bytes = zf.read("com/goosepatrol/GoosePatrolGame.class")
    magic, minor, major = struct.unpack(">IHH", cls_bytes[:8])
    assert magic == 0xCAFEBABE
    if major != 61:
        print(f"FAIL: Expected Java 17 class major version 61, got {major}", file=sys.stderr)
        sys.exit(1)
print("OK: GoosePatrol JAR bytecode major=61 (Java 17) and all licence notices present.")
PY

echo "=== 5. Assembling clean submission zip ==="
STAGE="dist/staging"
OUT_ZIP="dist/Assessment1_Cohort2_Team8.zip"
rm -rf "$STAGE" "$OUT_ZIP"
mkdir -p "$STAGE/code"

cp url1.txt "$STAGE/url1.txt"
for pdf in Req1.pdf Arch1.pdf Plan1.pdf Risk1.pdf Impl1.pdf; do
  cp "deliverables/$pdf" "$STAGE/$pdf"
done
cp "$JAR_SRC" "$STAGE/GoosePatrol.jar"

rsync -a \
  --exclude='.git/' \
  --exclude='.gradle/' \
  --exclude='.kotlin/' \
  --exclude='build/' \
  --exclude='core/build/' \
  --exclude='lwjgl3/build/' \
  --exclude='_site/' \
  --exclude='dist/' \
  --exclude='course-material/' \
  --exclude='presentation/' \
  --exclude='/Req1.pdf' \
  --exclude='.DS_Store' \
  --exclude='assets/assets.txt' \
  ./ "$STAGE/code/"

(
  cd "$STAGE"
  zip -rq "../../$OUT_ZIP" \
    url1.txt Req1.pdf Arch1.pdf Plan1.pdf Risk1.pdf Impl1.pdf GoosePatrol.jar code
)
rm -rf "$STAGE"

echo "=== Submission archive ready: $OUT_ZIP ==="
ls -lh "$OUT_ZIP"
unzip -l "$OUT_ZIP" | sed -n '1,30p'
