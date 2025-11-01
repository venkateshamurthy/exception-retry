#!/usr/bin/env bash
set -e

# 1. Get latest tag
TAG=$(git describe --tags --abbrev=0 || echo "v0.0.0")
echo "Current tag: $TAG"

VERSION=${TAG#v}
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"

# 2. Get last commit message
MSG=$(git log -1 --pretty=%B)
echo "Last commit message: $MSG"

# 3. Decide semantic bump
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

# 4. Update Maven version
mvn -B versions:set -DnewVersion=${NEXT_VERSION}
mvn -B versions:commit

# 5. Optionally tag and commit (dry-run)
echo "If this were a release, we'd now:"
echo "  git add pom.xml && git commit -m 'chore: release v$NEXT_VERSION'"
echo "  git tag v$NEXT_VERSION"