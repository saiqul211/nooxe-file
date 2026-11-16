# Nooxe File

[![Build Android APK](https://github.com/saiqul211/nooxe-file/actions/workflows/android.yml/badge.svg)](https://github.com/saiqul211/nooxe-file/actions/workflows/android.yml)
[![Download APK](https://img.shields.io/badge/Download-APK-7C5CE6)](https://github.com/saiqul211/nooxe-file/releases/latest)

A beautiful, complete **Android file manager app written in Kotlin** — inspired by a [Dribbble concept](https://dribbble.com). Lavender theme, soft shadows, rounded cards, storage donut, list/grid browsing, multi-select and a floating action bar.

## Download

Grab the latest APK from **[Releases](https://github.com/saiqul211/nooxe-file/releases/latest)** → `nooxe-file-v1.1.0.apk` (Android 7.0+).

## File picker integration

Nooxe File registers `ACTION_GET_CONTENT` and `ACTION_OPEN_DOCUMENT` (mime `*/*`), so it shows up in the Android **Open from / Open with** sheet when another app asks for a file (WhatsApp, Messenger, Gmail, …).

Tapping it opens `PickerActivity`, where you browse the tree and pick a file. The pick is returned to the calling app as a `content://` URI served by a `FileProvider`.

> No runtime permission is needed — this is done with intent filters, the standard Android way to expose a file source.

## Screens

- **Home** — storage card with animated donut (Total / Used), Internal & External tabs, category grid (Document / Pictures / Video / Audio), recent files, *Clear Up Space*
- **Folder** — breadcrumb trail, search, sort (recent / name / size), list ⇄ grid toggle
- **Selection mode** — tap or long-press to select, floating bar with Tag / Share / Delete / More
- **New folder** — FAB creates a folder in the current directory

## Tech

- Kotlin 1.9 · Android Gradle Plugin 8.5 · Gradle 8.9
- Material Components (Material 3) · ViewBinding · RecyclerView
- minSdk 24 · targetSdk 34 · compileSdk 34

## Project layout

```
app/src/main/java/com/nooxe/file/
  MainActivity.kt
  data/FileSystem.kt          # file tree model + mock storage
  ui/HomeFragment.kt
  ui/FolderFragment.kt
  adapter/FileListAdapter.kt
  adapter/FileGridAdapter.kt
  view/DonutView.kt           # custom Canvas donut chart
app/src/main/res/
  layout/                     # XML layouts
  drawable/                   # vector icons + shape backgrounds
  values/                     # colors, strings, themes
```

## Build

### On GitHub (automated)

`.github/workflows/android.yml` builds the APK on every push to `main`:

1. Push this repo to GitHub
2. Open **Actions → Build Android APK**
3. Download the artifact `nooxe-file-debug` (`app-debug.apk`)

### Locally

Requires JDK 17 + the Android SDK.

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```
