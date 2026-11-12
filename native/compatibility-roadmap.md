# Compatibility Runtime Roadmap

## Milestone 0 — Installer shell
- [x] Android ARM64 JNI library build.
- [x] Select original macOS app Contents folder.
- [x] Select Windows/Android target profiles and graphics preferences.
- [x] Select mod, patch, display, accessibility, and achievement options in installer UI.

## Milestone 1 — Binary reconnaissance
- [x] Recognize thin Mach-O 32/64-bit headers.
- [x] Identify CPU architecture.
- [x] Traverse thin 64-bit Mach-O load commands.
- [x] Report target platform and linked libraries/frameworks.
- [ ] Parse universal/FAT slices.
- [ ] Parse symbol tables, exports, undefined imports, and bind/rebase metadata.
- [ ] Inspect all native plugins and Unity player binaries.
- [ ] Identify exact Unity and IL2CPP versions.

## Milestone 2 — Binary execution foundation
- [ ] Select an architecture-compatible Mach-O execution/translation strategy.
- [ ] Implement segment mapping, relocations, symbol resolution, and dependency loading.
- [ ] Implement loader initialization and safe teardown.
- [ ] Add non-proprietary Mach-O test fixtures.

## Milestone 3 — macOS API compatibility
- [ ] POSIX/libSystem adapter.
- [ ] Filesystem, mmap, environment, and process APIs.
- [ ] pthreads, synchronization, and dispatch behavior.
- [ ] Objective-C runtime and Foundation compatibility.
- [ ] C++ ABI and exception/unwind compatibility.

## Milestone 4 — Unity/IL2CPP
- [ ] Identify exact Unity and IL2CPP versions.
- [ ] Match IL2CPP native ABI and runtime services.
- [ ] Integrate compatible Unity player and native plugin interfaces.
- [ ] Validate metadata and registration structures with test programs.

## Milestone 5 — Graphics and platform hosts
- [ ] Metal resource/command/shader translation.
- [ ] Vulkan backend.
- [ ] OpenGL ES backend for Android devices.
- [ ] Windows host with Direct3D 11/Vulkan.
- [ ] Input, audio, lifecycle, and controller integration.

## Milestone 6 — Mod layer and packaging
- [ ] Integrate MelonLoader only if compatible with this IL2CPP runtime/platform.
- [ ] GamePatches plugin interface.
- [ ] Graphics/display/accessibility options.
- [ ] Achievement and mod APIs.
- [ ] Windows installer/build output.
- [ ] Android APK plus game-data delivery and install validation.

Unchecked items are not implemented. The current APK is an installer/diagnostic shell and does not produce a playable port.
