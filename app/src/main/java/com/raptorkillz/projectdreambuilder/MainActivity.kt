package com.raptorkillz.projectdreambuilder

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile

class MainActivity : Activity() {
    private lateinit var status: TextView
    private external fun nativeInspectMachO(header: ByteArray): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(0xFF101827.toInt())
        }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(label("PROJECT DREAM", 27, 0xFFFFFFFF.toInt(), true))
        content.addView(label("UNITY COMPATIBILITY LAYER • FOUNDATION 0.2.0", 13, 0xFF62D5FA.toInt(), true))
        content.addView(label(
            "Select the Contents folder inside your own SonicDreamTeam.app 1.10.1. This build starts a native ARM64 compatibility foundation; it does not recompile or remake the game.",
            16, 0xFFE5E7EB.toInt()
        ))
        val choose = Button(this).apply {
            text = "SELECT MAC GAME CONTENTS"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(intent, PICK_FOLDER)
            }
        }
        content.addView(choose)
        status = label("No source folder selected.", 15, 0xFFFFD166.toInt())
        content.addView(status)
        content.addView(label(
            "COMPATIBILITY ENGINE ROADMAP\n" +
            "1  Mach-O / ARM64 binary inspection — implemented\n" +
            "2  macOS API compatibility shims — scaffold\n" +
            "3  IL2CPP ABI and runtime bridge — not implemented\n" +
            "4  Unity player / asset integration — not implemented\n" +
            "5  Metal-to-Vulkan / OpenGL ES renderer — not implemented\n" +
            "6  Android APK + game data packaging — not implemented\n\n" +
            "IMPORTANT\nThe original GameAssembly.dylib is a macOS Mach-O binary. Android cannot load it as an ELF .so. A real compatibility runtime must translate or host its binary format and provide the missing macOS/Unity interfaces. This app currently diagnoses the input and builds the native ARM64 foundation only.",
            14, 0xFFE5E7EB.toInt()
        ))
        val scroll = ScrollView(this).apply { addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
    }

    @Deprecated("Platform folder picker retained for Android 9 compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        inspect(uri)
    }

    private fun inspect(uri: Uri) {
        val folder = DocumentFile.fromTreeUri(this, uri)
        if (folder == null || !folder.isDirectory) {
            status.text = "Could not read that folder. Select SonicDreamTeam.app/Contents."
            return
        }

        val found = mutableSetOf<String>()
        scan(folder, found, 0)
        val gameAssembly = findFile(folder, "GameAssembly.dylib", 0)
        val binaryReport = if (gameAssembly != null) {
            try {
                val header = contentResolver.openInputStream(gameAssembly.uri)?.use { input ->\n                    val buffer = ByteArray(64)\n                    val count = input.read(buffer)\n                    if (count <= 0) ByteArray(0) else buffer.copyOf(count)\n                }
                if (header == null) "Unable to read binary header."
                else nativeInspectMachO(header)
            } catch (error: Exception) {
                "Binary inspection failed: ${error.message ?: "unknown error"}"
            }
        } else {
            "GameAssembly.dylib not found."
        }

        val checks = listOf("Info.plist", "GameAssembly.dylib", "global-metadata.dat")
        status.text = buildString {
            append("MAC SOURCE ANALYSIS\n")
            checks.forEach { append(it).append(": ").append(if (it in found) "FOUND" else "NOT FOUND").append('\n') }
            append("\nNATIVE BINARY CHECK\n").append(binaryReport)
            append("\n\nENGINE STATUS\n")
            append("ARM64 Android native bridge: BUILT-IN\n")
            append("macOS API compatibility: NOT IMPLEMENTED\n")
            append("IL2CPP execution: NOT IMPLEMENTED\n")
            append("Unity rendering: NOT IMPLEMENTED\n")
            append("\nThis is a compatibility diagnostic, not a playable port.")
        }
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

    private fun label(value: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 12, 0, 12)
        }

    companion object {
        private const val PICK_FOLDER = 4102
        init { System.loadLibrary("dreamcompat") }
    }
}
