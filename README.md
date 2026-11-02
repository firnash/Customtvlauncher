# Custom TV Launcher

A minimal Android TV / Google TV home screen: clock, date, and a D-pad friendly
grid of your installed apps. Hold OK on an app to hide it; use the last tile to
show hidden apps. Layout ideas are borrowed from
[CleanFrame](https://github.com/nthnclrk/cleanframe) (a Samsung Tizen project;
no code is shared).

## Build
Requires JDK 17 and the Android SDK (platform 34).

    gradle wrapper --gradle-version 8.7   # first time only, generates ./gradlew
    ./gradlew assembleDebug               # app/build/outputs/apk/debug/app-debug.apk

## Install on the TV
1. Settings → Device Preferences → About → tap *Build* 7 times.
2. Developer options → enable USB debugging / network debugging. Note the TV IP.
3. `adb connect <tv-ip>:5555` and accept the prompt on the TV.
4. `adb install -r app/build/outputs/apk/debug/app-debug.apk`
5. Press Home; pick *Custom Launcher* and choose "Always" if asked. If the
   stock launcher keeps winning, disable it (step below) once you're sure this one works.

## Debloat (optional, reversible)
    scripts/debloat.sh --list <tv-ip>   # see which candidates exist on your TV
    scripts/debloat.sh <tv-ip>          # disable them (pm disable-user --user 0)
    scripts/restore.sh <tv-ip>          # undo

`packages.txt` is a starting point and is unverified for your model. Review it
against `adb shell pm list packages`. Disabling the stock home is deliberately
not scripted. Undo anything with `adb shell pm enable --user 0 <package>`;
a factory reset also restores everything.
Keep ADB on your LAN only and turn it off when finished.

## Limits
Does not remove ads inside apps (e.g. YouTube). Not tested on a real TV yet.
