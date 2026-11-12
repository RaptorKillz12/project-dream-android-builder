# Compatibility Runtime Roadmap

## Milestone 0 — Installer shell
- [x] Android installer prototype.
- [x] Godot Android setup wizard.
- [ ] Native Android Storage Access Framework folder picker.
- [ ] Godot Android APK export verified by GitHub Actions.

## Milestone 1 — Binary reconnaissance
- [x] Recognize thin Mach-O 32/64-bit headers in the earlier native prototype.
- [x] Identify CPU architecture in the earlier native prototype.
- [x] Traverse thin 64-bit Mach-O load commands in the earlier native prototype.
- [x] Report target platform and linked libraries/frameworks in the earlier native prototype.
- [ ] Parse universal/FAT slices.
- [ ] Parse symbol tables, exports, undefined imports and bind/rebase metadata.
- [ ] Inspect all native plugins and Unity player binaries.
- [ ] Identify exact Unity and IL2CPP versions.

## Milestone 2 — Binary execution foundation
- [ ] Select a validated architecture-compatible Mach-O execution/translation strategy.
- [ ] Implement segment mapping, relocations, symbol resolution and dependency loading.
- [ ] Implement loader initialization and safe teardown.
- [ ] Add non-proprietary Mach-O test fixtures.

## Milestone 3 — macOS API compatibility
- [ ] POSIX/libSystem adapter.
- [ ] Filesystem, mmap, environment and process APIs.
- [ ] pthreads, synchronization and dispatch behavior.
- [ ] Objective-C runtime and Foundation compatibility.
- [ ] C++ ABI and exception/unwind compatibility.

## Milestone 4 — Unity/IL2CPP
- [ ] Identify exact Unity and IL2CPP versions.
- [ ] Match IL2CPP native ABI and runtime services.
- [ ] Integrate a compatible Unity player and native plugin interfaces.
- [ ] Validate metadata and registration structures with test programs.

## Milestone 5 — Graphics and platform host
- [ ] Metal resource/command/shader translation.
- [ ] Vulkan backend.
- [ ] OpenGL ES backend for Android devices.
- [ ] Input, audio, lifecycle and controller integration.

## Milestone 6 — Mod layer and packaging
- [ ] Integrate MelonLoader only if compatible with this IL2CPP runtime/platform.
- [ ] GamePatches plugin interface.
- [ ] Graphics/display/accessibility options.
- [ ] Achievement and mod APIs.
- [ ] Android APK plus game-data delivery and install validation.

Unchecked items are not implemented. The current Godot app is an installer/diagnostic shell and does not produce a playable port.
