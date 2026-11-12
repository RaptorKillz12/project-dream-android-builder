# Project Dream Android Setup (Godot)

Godot 4.7.2 project for an Android-only, PC-installer-style setup wizard.

Current milestone:
- Step-by-step installer UI.
- Source folder selection UI.
- Basic checks for GameAssembly.dylib, global-metadata.dat and Info.plist.
- Android ARM64 target configuration.
- Component and renderer selection.
- Writes a JSON build-plan file to the app user data directory.

Important limitation:
This is an installer front end and basic file inspector. It does not yet load or execute macOS Mach-O binaries, translate macOS APIs, implement IL2CPP compatibility, or produce a playable Sonic Dream Team APK. A macOS Unity IL2CPP binary cannot be made Android-compatible by renaming it to a .so file. Native runtime components must be implemented and tested separately.

Open the godot folder in Godot 4.7.2 and run Main.tscn.
