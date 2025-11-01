#!/usr/bin/env bash
set -e

echo "🚀 Starting local release dry-run..."
echo "--------------------------------------"

# 1️⃣ Determine current tag
TAG=$(git describe --tags --abbrev=0 || echo "v0.0.0")
echo "Current tag: $TAG"

VERSION=${TAG#v}
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"

# 2️⃣ Get last commit message
MSG=$(git log -1 --pretty=%B)
echo "Last commit message: $MSG"

# 3️⃣ Semantic bump logic
if echo "$MSG" | grep -q "BREAKING CHANGE"; then
  MAJOR=$((MAJOR+1)); MINOR=0; PATCH=0
  echo "Detected major change"
elif echo "$MSG" | grep -iq "^feat:"; then
  MINOR=$((MINOR+1)); PATCH=0
  echo "Detected feature change"
elif echo "$MSG" | grep -iq "^fix:"; then
  PATCH=$((PATCH+1))
  echo "Detected fix change"
else
  PATCH=$((PATCH+1))
  echo "Defaulting to patch bump"
fi

NEXT_VERSION="$MAJOR.$MINOR.$PATCH"
echo "Next version: $NEXT_VERSION"
echo "--------------------------------------"

# 4️⃣ Update Maven version locally
mvn -B versions:set -DnewVersion=$NEXT_VERSION
mvn -B versions:commit

# 5️⃣ GPG setup check
echo "🔐 Checking GPG keys..."
gpg --list-secret-keys --keyid-format=long || {
  echo "❌ No GPG keys found! Please import your private key first."
  exit 1
}

# 6️⃣ Run Maven verify (with signing)
echo "🏗️ Building and signing artifacts..."
mvn -B clean verify -P release-profile -DskipTests=false

# 7️⃣ Check generated JARs and signatures
echo "🔍 Validating artifacts..."
ls -lh target/*.jar* || echo "No JARs found in target/"
echo "--------------------------------------"

# 8️⃣ Validate POM integrity (optional)
echo "🧩 Validating generated POM..."
mvn help:effective-pom | grep -E '<version>|<groupId>|<artifactId>' | head -n 10
echo "--------------------------------------"

# 9️⃣ Summary
echo "✅ Dry run completed successfully!"
echo "Version bumped to: $NEXT_VERSION"
echo "Artifacts signed and verified locally (not deployed)."
echo "--------------------------------------"
echo "🧭 Next steps:"
echo "  - Review 'target/' artifacts."
echo "  - If satisfied, commit and tag manually:"
echo "      git add pom.xml"
echo "      git commit -m 'chore: release v$NEXT_VERSION'"
echo "      git tag v$NEXT_VERSION"
echo "  - Push tag to trigger CI/CD:"
echo "      git push origin main --tags"
echo "--------------------------------------"
