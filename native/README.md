# Runtime implementation notes

The goal is to host/adapt the original Unity IL2CPP game code through a compatibility runtime, not port gameplay source into another engine.

## Binary loader
The Android native module currently parses thin 64-bit Mach-O load commands, reports target platform where LC_BUILD_VERSION exists, and lists linked dylib/framework names. It does not map Mach-O segments, relocate symbols, implement dyld, or execute code. First gather full imports/dependencies and confirm architecture/runtime assumptions.

## macOS API compatibility
Implement only APIs proven required by binary inspection: POSIX/libSystem, file descriptors, mmap, pthreads, synchronization, Objective-C runtime, Foundation, dispatch queues, and C++ unwind/exception support. ABI, ownership, callbacks, error semantics, and structure layouts must match.

## Unity and IL2CPP
The bridge must match the exact Unity/IL2CPP version and native ABI. Do not assume GameAssembly.dylib can run independently of UnityPlayer or that a platform-native Unity player can consume macOS game code unchanged.

## Graphics
Metal replacement requires command/resource translation, shader conversion, texture formats, synchronization, render surfaces, and presentation. Candidate backends: Vulkan and OpenGL ES on Android; Vulkan and Direct3D 11 on Windows.

## Platform hosts
Windows and Android need separate OS adapters, native build configurations, player integration, asset/data delivery, input, audio, and lifecycle support.

## Current limitations
No Mach-O memory mapper, relocation/linker, dyld, Objective-C runtime, Foundation, POSIX compatibility implementation, IL2CPP runtime, Unity player, Metal translation, Windows host, MelonLoader/GamePatches runtime, or game package builder exists yet. The current APK is an installer/diagnostic shell.
