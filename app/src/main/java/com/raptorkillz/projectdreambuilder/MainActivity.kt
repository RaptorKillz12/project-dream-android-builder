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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            setBackgroundColor(0xFF101827.toInt())
        }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        content.addView(label("PROJECT DREAM", 27, 0xFFFFFFFF.toInt(), true))
        content.addView(label("ANDROID SETUP • PROTOTYPE 0.1.0", 13, 0xFF62D5FA.toInt(), true))
        content.addView(label(
            "Select the Contents folder inside your own SonicDreamTeam.app version 1.10.1.",
            16, 0xFFE5E7EB.toInt()
        ))
        val choose = Button(this).apply {
            text = "SELECT CONTENTS FOLDER"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
                startActivityForResult(intent, PICK_FOLDER)
            }
        }
        content.addView(choose)
        status = label("No folder selected.", 15, 0xFFFFD166.toInt())
        content.addView(status)
        content.addView(label(
            "THIS BUILD CAN\n• Select a folder using Android's file picker\n• Scan for key source files\n• Report what was found\n\n" +
            "THIS BUILD CANNOT YET\n• Convert the macOS game\n• Generate a playable game APK/OBB\n\n" +
            "The conversion runtime is not included. The original Project Dream Windows executable cannot simply be repackaged as Android code.",
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
        val checks = listOf("Info.plist", "GameAssembly.dylib", "global-metadata.dat")
        status.text = buildString {
            append("SOURCE FILE REPORT\n")
            checks.forEach { append(it).append(": ").append(if (it in found) "FOUND" else "NOT FOUND").append('\n') }
            append("\n")
            append(if (checks.all { it in found }) {
                "All expected marker files found. This is only a source check, not a conversion."
            } else {
                "Some marker files are missing. Verify that you selected the app's Contents folder and that this is the macOS build."
            })
        }
    }

    private fun scan(dir: DocumentFile, found: MutableSet<String>, depth: Int) {
        if (depth > 10 || found.size > 25000) return
        for (entry in dir.listFiles()) {
            if (entry.isDirectory) scan(entry, found, depth + 1)
            else entry.name?.let { found.add(it) }
        }
    }

    private fun label(value: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 12, 0, 12)
        }

    companion object { private const val PICK_FOLDER = 4102 }
}
