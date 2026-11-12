# Project Dream Android Compatibility Installer

This repository contains an Android-only Project Dream-style installer project. It is intended to preserve the original game's code and data rather than remake the game in Godot.

## Godot installer project

The current Godot 4.7.2 project is in [godot/](godot/).

- Multi-step PC-installer-style setup wizard.
- Select the original game Contents directory.
- Recursively locate `GameAssembly.dylib`, `global-metadata.dat` and `Info.plist`.
- Inspect supported thin 64-bit Mach-O headers, CPU architecture, file type, build platform and linked dylib names.
- Select compatibility components and Android ARM64 graphics preferences.
- Generate a JSON build-plan file.
- Export workflow: [Build Godot Project Dream Android APK](.github/workflows/godot-android.yml).

## Native compatibility foundation

The earlier native Android prototype remains in `app/` and includes JNI-based thin Mach-O inspection. The Godot GDScript inspector is a separate static-inspection implementation. See [native/ANDROID_RUNTIME_ARCHITECTURE.md](native/ANDROID_RUNTIME_ARCHITECTURE.md) and [native/compatibility-roadmap.md](native/compatibility-roadmap.md).

## Important status

The installer UI, basic source-file detection and limited static Mach-O inspection are implemented. **The actual compatibility runtime is not implemented.** This repository does not yet execute macOS Mach-O code on Android, provide macOS/POSIX or Objective-C/Foundation compatibility, bridge the original IL2CPP runtime, provide a compatible Unity player, translate Metal rendering, or produce a playable Sonic Dream Team APK.

A macOS Mach-O binary is not an Android ELF library. Renaming it to `.so` does not make it runnable. The original game files must be supplied by the user; proprietary game binaries and assets are not stored here.

## Build

Open `godot/project.godot` with Godot 4.7.2. For automated Android export, run the **Build Godot Project Dream Android APK** workflow and download its artifact only after the workflow succeeds.
