#!/usr/bin/env bash
set -euo pipefail

# ============================================================
#  🚀 Modern Java Release Helper
#  Supports:
#   - --patch / --minor / --major
#   - --dry-run
#   - Automatic preflight "verify" before tagging
# ============================================================

MODE="patch"
DRY_RUN=false
BRANCH="main"
PREFIX="v"
VERIFY_PROFILE="release-profile"

# --- Parse CLI arguments ---
for arg in "$@"; do
  case $arg in
    --patch) MODE="patch" ;;
    --minor) MODE="minor" ;;
    --major) MODE="major" ;;
    --dry-run) DRY_RUN=true ;;
    *) echo "Unknown option: $arg"; exit 1 ;;
  esac
done

echo "🧩 Release mode: $MODE"
$DRY_RUN && echo "🧪 Dry-run mode: ON (no tag/push will occur)"

# --- Ensure clean working directory ---
if [ -n "$(git status --porcelain)" ]; then
  echo "❌ Working directory not clean. Commit or stash changes first."
  exit 1
fi

# --- Sync with origin ---
git fetch origin "$BRANCH" --tags

# --- Read current version ---
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
BASE_VERSION="${CURRENT_VERSION%-SNAPSHOT}"
IFS='.' read -r MAJOR MINOR PATCH <<<"$BASE_VERSION"

echo "📦 Current version: $CURRENT_VERSION"

# --- Compute next version ---
case $MODE in
  patch)
    PATCH=$((PATCH + 1))
    ;;
  minor)
    MINOR=$((MINOR + 1)); PATCH=0 ;;
  major)
    MAJOR=$((MAJOR + 1)); MINOR=0; PATCH=0 ;;
esac

NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"
echo "🚀 Next release version: $NEW_VERSION"

# --- Preflight verification before tagging ---
echo "🔍 Running Maven build verification..."
if ! mvn -B clean verify -P "${VERIFY_PROFILE}"; then
  echo "❌ Build verification failed — aborting release."
  exit 1
fi
echo "✅ Build verification succeeded."

# --- Update version if not dry-run ---
if ! $DRY_RUN; then
  mvn versions:set -DnewVersion="${NEW_VERSION}"
  mvn versions:commit
else
  echo "💡 [Dry-run] Would run: mvn versions:set -DnewVersion=${NEW_VERSION}"
fi

# --- Commit, tag, push ---
if ! $DRY_RUN; then
  git add pom.xml
  git commit -m "chore(release): ${NEW_VERSION}"
  git tag "${PREFIX}${NEW_VERSION}"

  echo ""
  read -rp "⚠️  Confirm push of ${PREFIX}${NEW_VERSION} to origin? [y/N] " CONFIRM
  if [[ "${CONFIRM}" =~ ^[Yy]$ ]]; then
    git push origin "$BRANCH"
    git push origin "${PREFIX}${NEW_VERSION}"
    echo "✅ Pushed tag ${PREFIX}${NEW_VERSION}. CI/CD pipeline will publish it."
  else
    echo "🛑 Release canceled by user. No changes pushed."
    git tag -d "${PREFIX}${NEW_VERSION}" >/dev/null 2>&1 || true
    exit 0
  fi
else
  echo "💡 [Dry-run] Would commit, tag ${PREFIX}${NEW_VERSION}, and push to origin."
fi