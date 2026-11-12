# Project Dream Compatibility Installer

This project is being developed as a Project Dream-style Unity compatibility installer, not as a game remake or a source-code conversion.

## Installer interface added
- Select the original macOS Unity game's Contents folder.
- Inspect GameAssembly.dylib's Mach-O header and report its CPU architecture.
- Select Windows x64 and/or Android ARM64 as build targets.
- Select Windows DirectX 11/Vulkan and Android Vulkan/OpenGL ES 3 renderer profiles.
- Select installer modules: MelonLoader integration, GamePatches, graphics settings, display options, accessibility, achievements, gameplay modifications, and mod support.
- Generate a local build profile summary.

## Compatibility engine status
The interface and profile selection are scaffolding only. The selected runtime modules are not implemented yet, and profile generation does not create a playable game build.

Still required:
1. Full Mach-O/fat-binary parsing and imported-symbol/dependency inventory.
2. A viable binary execution or translation strategy for the macOS ARM64 game code.
3. macOS/POSIX, libSystem, Objective-C runtime, Foundation, threading, filesystem, and exception compatibility.
4. Unity/IL2CPP runtime and native plugin integration.
5. Metal-to-Vulkan/OpenGL ES graphics translation, including shader/resource handling.
6. Windows host and Android ARM64 host/player integration.
7. Real MelonLoader/GamePatches integration and mod APIs.
8. Platform packaging: Windows distribution and Android APK/split APK plus game data.

A Mach-O dylib is not an Android ELF shared library. Renaming GameAssembly.dylib to GameAssembly.so will not make it executable. The original player and its platform-specific dependencies must also be handled.

## Build
GitHub Actions builds the Android installer interface APK. Download the Project-Dream-Setup-debug artifact from a successful workflow run.

The repository does not include proprietary game binaries or game assets.
