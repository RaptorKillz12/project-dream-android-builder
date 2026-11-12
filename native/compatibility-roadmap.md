# Project Dream compatibility runtime

This is a native compatibility-layer project, not a remake and not a source-to-source game conversion.

## Implemented foundation
- Android ARM64 JNI shared library target.
- Thin Mach-O header identification and CPU architecture reporting.
- Universal/FAT Mach-O signature detection.
- Android folder selection and source diagnostics.

## Engineering stages
1. Inspect all Mach-O slices, imported symbols, linked frameworks, Unity version, and binary dependencies.
2. Choose a legally usable Mach-O execution/translation strategy for the actual CPU architecture. A Mach-O dylib cannot be loaded by Android by renaming it to .so.
3. Implement only the required macOS/POSIX, libSystem, Objective-C runtime, Foundation, filesystem, threading, synchronization, and exception interfaces.
4. Integrate an IL2CPP-compatible runtime bridge and the matching Unity player interfaces.
5. Replace Metal with a Vulkan/OpenGL ES graphics backend, including shader/resource/synchronization translation.
6. Package runtime, player, game data, and ABI-correct Android APK/data delivery.

## Not implemented yet
This milestone does not execute GameAssembly.dylib, emulate macOS, provide Objective-C/Foundation, run IL2CPP, or render Unity. The Unity player itself can have additional platform-specific dependencies. The diagnostic is a first milestone, not a playable port.
