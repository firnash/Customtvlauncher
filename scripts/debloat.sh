#!/usr/bin/env bash
# Disable Xiaomi/Mi TV bloat for user 0 over ADB. Reversible: see restore.sh.
# Usage: scripts/debloat.sh <tv-ip>[:5555]   (or no arg if already connected)
#        scripts/debloat.sh --list            show candidate packages found on the TV
#
# Nothing is disabled unless it is BOTH in scripts/packages.txt AND installed on the TV.
# The stock home (com.mitv.tvhome*, com.google.android.tvlauncher) is NEVER touched
# by this script; disable it manually only after the custom launcher is set as default.
set -euo pipefail
cd "$(dirname "$0")"

LIST_ONLY=0
if [[ "${1:-}" == "--list" ]]; then LIST_ONLY=1; shift; fi
[[ -n "${1:-}" ]] && adb connect "$1" >/dev/null

installed=$(adb shell pm list packages | sed 's/^package://' | tr -d '\r')
LOG=disabled-$(date +%Y%m%d-%H%M%S).log

grep -v -e '^\s*#' -e '^\s*$' packages.txt | while read -r pkg; do
  if grep -qx "$pkg" <<<"$installed"; then
    if [[ $LIST_ONLY -eq 1 ]]; then
      echo "found: $pkg"
    else
      adb shell pm disable-user --user 0 "$pkg" && echo "$pkg" >> "$LOG"
    fi
  fi
done
[[ $LIST_ONLY -eq 0 && -f "$LOG" ]] && echo "Disabled packages logged to scripts/$LOG"
exit 0
