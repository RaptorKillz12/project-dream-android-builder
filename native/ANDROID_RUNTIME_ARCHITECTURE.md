# Android compatibility runtime — implementation plan

This document defines the native runtime work required behind the Godot installer. It is an engineering plan, not a claim that these systems are already implemented.

## Why the Mac executable cannot be used directly

The source game's Mach-O executable and dynamic libraries target Apple's platform ABI. Android loads ELF shared objects and uses the Android/Bionic ABI. Renaming a Mach-O file to `.so`, copying it into an APK, or changing its file extension cannot make it executable.

A usable port needs a proven execution strategy for the original machine code plus compatible implementations of every required operating-system and Unity service. A Unity game also depends on a matching Unity player; the IL2CPP game library alone is not a complete game.

## Runtime modules

1. **Source package reader** — safely enumerate the user-selected game bundle and validate expected files.
2. **Mach-O inspector** — parse thin and FAT headers, load commands, CPU slices, deployment targets, dylib dependencies, symbol tables, exports, imports and chained fixups.
3. **Execution strategy** — establish whether the source architecture can be translated or whether a compatible native rebuild is possible. Do not attempt arbitrary native code execution until this is proven.
4. **Loader** — segment mapping, relocations, dependency graph, symbol resolution, initialization and teardown, only for a supported executable format and ABI.
5. **System compatibility** — implement only APIs proven necessary by import analysis: filesystem, memory mapping, threading, synchronization, time, sockets, Objective-C/Foundation behavior and C++ ABI/unwinding.
6. **Unity/IL2CPP bridge** — identify exact Unity and IL2CPP versions, runtime structures, metadata format and native registration ABI.
7. **Graphics backend** — translate the actual graphics API and shader/resource model used by the game. Metal-to-Vulkan is not a simple function-name mapping.
8. **Android host** — activity lifecycle, storage SAF, input/touch/controller, audio, display, permissions and pause/resume.
9. **Packaging** — package only compatible Android ELF libraries and redistributable game data; preserve user-provided copyrighted game data outside the public repository.

## Current status

- Godot setup wizard: initial implementation.
- Android ARM64 installer APK export workflow: added; must pass CI before considered verified.
- Mach-O execution loader: not implemented.
- macOS API compatibility: not implemented.
- Unity player / IL2CPP compatibility: not implemented.
- Metal translation: not implemented.
- Playable game APK: not implemented.

## Test gates

Each module needs small, redistributable synthetic test fixtures and automated tests. Never test by executing untrusted game binaries inside the build runner. The first meaningful runtime milestone is complete static inspection of the actual bundle and a dependency report; only then can an execution strategy be selected.
