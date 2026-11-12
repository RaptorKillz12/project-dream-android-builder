# Project Dream Android Compatibility Foundation

A native Android ARM64 foundation for investigating a Project Dream-style Unity compatibility runtime. The goal is to host/adapt the original macOS Unity game code where technically and legally possible, not remake Sonic Dream Team or translate its gameplay source into another engine.

## Current milestone: 0.2.0-compat
- Android folder picker for the user's own SonicDreamTeam.app contents.
- Native C++ JNI library built for arm64-v8a.
- Thin Mach-O header identification and CPU architecture report.
- Universal/FAT Mach-O signature detection.
- Compatibility architecture roadmap in native/compatibility-roadmap.md.

## Important status
This is not yet a playable port. It does not load or execute GameAssembly.dylib, implement macOS APIs or Objective-C/Foundation, run IL2CPP, integrate the matching Unity player, translate Metal shaders, or package a working game APK. Android uses ELF shared libraries; a macOS Mach-O dylib cannot be made loadable by renaming it. The actual binary architecture and imported dependencies must be inspected before choosing an execution/translation strategy.

## Build
GitHub Actions builds the Android setup/diagnostic APK. Open Actions, select the latest successful Build Android Setup APK run, and download the Project-Dream-Setup-debug artifact.

No proprietary game binaries, assets, or copyrighted game data are stored in this repository.