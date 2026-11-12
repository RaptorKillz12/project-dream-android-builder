# Project Dream Compatibility Installer

This project is being developed as a Project Dream-style Unity compatibility installer, not as a game remake or source-code conversion.

## Installer interface
- Select original macOS Unity game's Contents folder.
- Inspect GameAssembly.dylib's Mach-O header and metadata.
- Select Windows x64 and/or Android ARM64 targets.
- Select Windows DirectX 11/Vulkan and Android Vulkan/OpenGL ES 3 profiles.
- Select MelonLoader/GamePatches, graphics, display, accessibility, achievements, gameplay modifications, and mod-support options.
- Generate a local build profile summary.

## Native compatibility foundation implemented
- Android ARM64 JNI shared library.
- Thin Mach-O header and architecture recognition.
- Thin 64-bit Mach-O load-command traversal.
- Build target platform identification where LC_BUILD_VERSION is present.
- Linked dylib/framework dependency-name enumeration.
- Android installer copies the selected GameAssembly.dylib to app cache for static inspection.

See [native/compatibility-roadmap.md](native/compatibility-roadmap.md) for milestones and [native/README.md](native/README.md) for subsystem design notes.

## Not implemented yet
The repository does not yet contain a Mach-O memory mapper/relocator/dyld, macOS POSIX/libSystem or Objective-C/Foundation compatibility, an IL2CPP execution bridge, a Unity player, Metal-to-Vulkan/OpenGL ES translation, a Windows host, actual MelonLoader/GamePatches runtime integration, or playable game packaging. The installer currently creates a configuration summary only. A Mach-O dylib is not an Android ELF shared object and cannot be loaded by renaming it.

## Build
GitHub Actions builds the Android installer/diagnostic APK. Download the Project-Dream-Setup-debug artifact from a successful workflow run.

No proprietary game binaries or game assets are stored in this repository.
