#!/usr/bin/env bash

set -euo pipefail

: "${CROWDIN_PROJECT_ID:?Need to set CROWDIN_PROJECT_ID}"
: "${CROWDIN_PERSONAL_TOKEN:?Need to set CROWDIN_PERSONAL_TOKEN}"

TARGET_FILE="src/main/java/net/kyrptonaught/inventorysorter/client/TranslationReminder.java"
PLACEHOLDER='"KNOWN_LANGUAGES_REPL"'

# Fetch and transform completed language locales
LANG_LIST=$(curl -s -H "Authorization: Bearer ${CROWDIN_PERSONAL_TOKEN}" \
  "https://api.crowdin.com/api/v2/projects/${CROWDIN_PROJECT_ID}/languages/progress" | jq -r '
  .data
  | map(.data)
  | map(select(.translationProgress == 100))
  | map(.language.locale | ascii_downcase | gsub("-"; "_") | "\"\(.)\"")
  | join(", ")
')

# Ensure placeholder is present
if ! grep -q "$PLACEHOLDER" "$TARGET_FILE"; then
  echo "❌ Placeholder $PLACEHOLDER not found in $TARGET_FILE"
  exit 1
fi

# Replace placeholder with actual list
sed -i.bak "s/$PLACEHOLDER/$LANG_LIST/" "$TARGET_FILE"

# Print updated file contents

cat "$TARGET_FILE"

echo ""
echo "----------------------------------------"
echo "✅ Injected completed locales into $TARGET_FILE"
