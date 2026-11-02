#!/usr/bin/env bash
# Re-enable every package listed in packages.txt (or in a given disabled-*.log).
# Usage: scripts/restore.sh [<tv-ip>[:5555]] [disabled-*.log]
set -euo pipefail
cd "$(dirname "$0")"
[[ "${1:-}" == *.log ]] || { [[ -n "${1:-}" ]] && adb connect "$1" >/dev/null && shift; }
src="${1:-packages.txt}"
grep -v -e '^\s*#' -e '^\s*$' "$src" | while read -r pkg; do
  adb shell pm enable --user 0 "$pkg" >/dev/null 2>&1 && echo "enabled: $pkg" || true
done
