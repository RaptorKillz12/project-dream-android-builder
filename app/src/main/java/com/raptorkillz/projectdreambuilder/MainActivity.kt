package com.raptorkillz.projectdreambuilder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var sourceStatus: TextView
    private lateinit var windowsTarget: CheckBox
    private lateinit var androidTarget: CheckBox
    private lateinit var windowsGraphics: Spinner
    private lateinit var androidGraphics: Spinner
    private val components = linkedMapOf<String, CheckBox>()
    private var selectedSource: Uri? = null
    private var sourceFiles = emptySet<String>()
    private var gameAssemblyReport = "Not inspected"
    private external fun nativeInspectMachO(header: ByteArray): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF101827.toInt()
        window.navigationBarColor = 0xFF101827.toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 22)
            setBackgroundColor(0xFF101827.toInt())
        }
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        page.addView(label("PROJECT DREAM", 29, 0xFFFFFFFF.toInt(), true))
        page.addView(label("UNITY COMPATIBILITY INSTALLER", 14, 0xFF62D5FA.toInt(), true))
        page.addView(label(
            "Prepare a platform build profile from your original macOS Unity game files. The goal is to preserve the original IL2CPP game code and adapt the runtime—not remake the game.",
            15, 0xFFE5E7EB.toInt()
        ))

        page.addView(section("1. ORIGINAL GAME FILES"))
        page.addView(label("Choose the Contents folder inside SonicDreamTeam.app.", 14, 0xFFCBD5E1.toInt()))
        page.addView(Button(this).apply {
            text = "SELECT SONIC DREAM TEAM CONTENTS"
            setOnClickListener { chooseSource() }
        })
        sourceStatus = label("No source selected.", 14, 0xFFFFD166.toInt())
        page.addView(sourceStatus)

        page.addView(section("2. BUILD TARGETS"))
        windowsTarget = CheckBox(this).apply {
            text = "Windows 64-bit"
            setTextColor(0xFFFFFFFF.toInt())
            isChecked = false
        }
        androidTarget = CheckBox(this).apply {
            text = "Android ARM64"
            setTextColor(0xFFFFFFFF.toInt())
            isChecked = true
        }
        page.addView(windowsTarget)
        page.addView(androidTarget)

        page.addView(section("3. GRAPHICS BACKENDS"))
        page.addView(label("Windows renderer", 13, 0xFFCBD5E1.toInt()))
        windowsGraphics = spinner(listOf("DirectX 11", "Vulkan"))
        page.addView(windowsGraphics)
        page.addView(label("Android renderer", 13, 0xFFCBD5E1.toInt()))
        androidGraphics = spinner(listOf("Vulkan", "OpenGL ES 3"))
        page.addView(androidGraphics)

        page.addView(section("4. INSTALLER COMPONENTS"))
        page.addView(label("Select the compatibility and enhancement modules to include in the build profile.", 14, 0xFFCBD5E1.toInt()))
        addComponent(page, "MelonLoader integration", "MelonLoader")
        addComponent(page, "GamePatches compatibility layer", "GamePatches")
        addComponent(page, "Advanced graphics settings", "Graphics settings")
        addComponent(page, "Display and resolution options", "Display options")
        addComponent(page, "Accessibility options", "Accessibility")
        addComponent(page, "Achievement tracking", "Achievements")
        addComponent(page, "Gameplay modification support", "Gameplay mods")
        addComponent(page, "Mod loading support", "Mod support")

        page.addView(section("5. PREPARE BUILD PROFILE"))
        page.addView(Button(this).apply {
            text = "GENERATE BUILD CONFIGURATION"
            setOnClickListener { generateBuildProfile() }
        })
        status = label(
            "Waiting for a source folder.\n\nThis installer interface can prepare a profile, but it cannot yet execute the macOS binary or produce a playable Windows/Android game build.",
            14, 0xFFE5E7EB.toInt()
        )
        page.addView(status)

        page.addView(section("COMPATIBILITY ENGINE STATUS"))
        page.addView(label(
            "Mach-O binary inspection: implemented\n" +
            "Android ARM64 native bridge: implemented\n" +
            "Windows compatibility host: not implemented\n" +
            "macOS POSIX/Foundation/Objective-C shims: not implemented\n" +
            "IL2CPP execution bridge: not implemented\n" +
            "Unity player integration: not implemented\n" +
            "Metal-to-Vulkan/OpenGL ES translation: not implemented\n" +
            "MelonLoader/GamePatches runtime integration: not implemented\n" +
            "APK/OBB and Windows packaging: not implemented",
            13, 0xFFCBD5E1.toInt()
        ))
        page.addView(label(
            "Important: a macOS Mach-O library cannot be loaded by Android as an ELF .so. A real compatibility engine needs a binary execution strategy, macOS API implementations, Unity/IL2CPP integration, and a graphics translation layer. Selecting an option here does not claim that its runtime is already implemented.",
            13, 0xFFFFD166.toInt()
        ))

        val scroll = ScrollView(this).apply { addView(page) }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun chooseSource() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_FOLDER)
    }

    @Deprecated("Platform folder picker retained for Android 9 compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        selectedSource = uri
        inspect(uri)
    }

    private fun inspect(uri: Uri) {
        val folder = DocumentFile.fromTreeUri(this, uri)
        if (folder == null || !folder.isDirectory) {
            sourceStatus.text = "Could not read the selected folder."
            return
        }
        val names = mutableSetOf<String>()
        scan(folder, names, 0)
        sourceFiles = names
        val assembly = findFile(folder, "GameAssembly.dylib", 0)
        gameAssemblyReport = if (assembly == null) {
            "GameAssembly.dylib not found."
        } else {
            try {
                val header = contentResolver.openInputStream(assembly.uri)?.use { input ->
                    val buffer = ByteArray(64)
                    val count = input.read(buffer)
                    if (count <= 0) ByteArray(0) else buffer.copyOf(count)
                }
                if (header == null) "Could not read the Mach-O header." else nativeInspectMachO(header)
            } catch (e: Exception) {
                "Inspection error: ${e.message ?: "unknown"}"
            }
        }
        val required = listOf("Info.plist", "GameAssembly.dylib", "global-metadata.dat")
        sourceStatus.text = buildString {
            append("Source selected.\n")
            required.forEach { append(if (it in names) "FOUND  " else "MISSING  ").append(it).append('\n') }
            append(gameAssemblyReport)
        }
    }

    private fun generateBuildProfile() {
        if (selectedSource == null) {
            status.text = "Select the original game Contents folder first."
            return
        }
        if (!windowsTarget.isChecked && !androidTarget.isChecked) {
            status.text = "Select at least one build target."
            return
        }
        val targets = mutableListOf<String>()
        if (windowsTarget.isChecked) targets.add("Windows x64 — ${windowsGraphics.selectedItem}")
        if (androidTarget.isChecked) targets.add("Android ARM64 — ${androidGraphics.selectedItem}")
        val selectedModules = components.filterValues { it.isChecked }.keys
        status.text = buildString {
            append("BUILD PROFILE PREPARED\n\n")
            append("Targets:\n")
            targets.forEach { append("• ").append(it).append('\n') }
            append("\nInstaller modules:\n")
            if (selectedModules.isEmpty()) append("• None selected\n")
            else selectedModules.forEach { append("• ").append(it).append('\n') }
            append("\nSource files detected: ").append(sourceFiles.size)
            append("\nGameAssembly: ").append(gameAssemblyReport)
            append("\n\nSTATUS: configuration only. The selected compatibility modules and platform build pipelines are not yet implemented, so no game APK/OBB or Windows build has been generated.")
        }
    }

    private fun addComponent(parent: LinearLayout, title: String, key: String) {
        val check = CheckBox(this).apply {
            text = title
            setTextColor(0xFFFFFFFF.toInt())
        }
        components[key] = check
        parent.addView(check)
    }

    private fun spinner(options: List<String>) = Spinner(this).apply {
        adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, options)
    }

    private fun scan(dir: DocumentFile, found: MutableSet<String>, depth: Int) {
        if (depth > 12 || found.size > 25000) return
        for (entry in dir.listFiles()) {
            if (entry.isDirectory) scan(entry, found, depth + 1)
            else entry.name?.let { found.add(it) }
        }
    }

    private fun findFile(dir: DocumentFile, wanted: String, depth: Int): DocumentFile? {
        if (depth > 12) return null
        for (entry in dir.listFiles()) {
            if (entry.isDirectory) {
                val match = findFile(entry, wanted, depth + 1)
                if (match != null) return match
            } else if (entry.name == wanted) return entry
        }
        return null
    }

    private fun section(text: String) = label(text, 17, 0xFF62D5FA.toInt(), true)

    private fun label(value: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 10, 0, 10)
        }

    companion object {
        private const val PICK_FOLDER = 4102
        init { System.loadLibrary("dreamcompat") }
    }
}
