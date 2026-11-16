package com.nooxe.file

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nooxe.file.adapter.PickerAdapter
import com.nooxe.file.data.FsNode
import com.nooxe.file.data.FileSystem
import com.nooxe.file.databinding.ActivityPickerBinding
import java.io.File

/**
 * Entry point shown in the system file picker (ACTION_GET_CONTENT / ACTION_OPEN_DOCUMENT).
 *
 * The user browses the Nooxe file tree, taps a file, and the picked file is returned
 * to the calling app (WhatsApp, Messenger, Gmail, ...) as a content:// URI.
 */
class PickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickerBinding
    private lateinit var adapter: PickerAdapter

    private val tab = "internal"
    private val path = mutableListOf("internal")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PickerAdapter(
            onFolder = { openFolder(it) },
            onFile = { returnFile(it) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        binding.btnBack.setOnClickListener { goBack() }

        render()
    }

    override fun onBackPressed() {
        goBack()
    }

    private fun goBack() {
        if (path.size > 1) {
            path.removeAt(path.size - 1)
            render()
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun openFolder(folder: FsNode.Folder) {
        path.add(folder.id)
        render()
    }

    private fun render() {
        val folder = FileSystem.resolve(tab, path)
        binding.tvTitle.text = getString(R.string.pick_file)

        val folders = folder.children
            .filterIsInstance<FsNode.Folder>()
            .sortedBy { it.name.lowercase() }
        val files = folder.children
            .filterIsInstance<FsNode.File>()
            .sortedBy { it.name.lowercase() }

        val list = mutableListOf<FsNode>()
        list.addAll(folders)
        list.addAll(files)
        adapter.submit(list)

        binding.tvMeta.text =
            "${folders.size + files.size} items  ·  tap a file to send it back"

        renderBreadcrumb()
    }

    private fun renderBreadcrumb() {
        binding.breadcrumb.removeAllViews()
        val names = mutableListOf<String>()
        var acc = listOf(path.first())
        names.add(FileSystem.resolve(tab, acc).name)
        for (i in 1 until path.size) {
            acc = acc + path[i]
            names.add(FileSystem.resolve(tab, acc).name)
        }
        names.forEachIndexed { i, name ->
            val tv = TextView(this).apply {
                text = name
                textSize = 10.5f
                setTextColor(
                    if (i == names.size - 1) resources.getColor(R.color.ink, null)
                    else resources.getColor(R.color.muted, null)
                )
                if (i == names.size - 1) paint.isFakeBoldText = true
            }
            binding.breadcrumb.addView(tv)
            if (i != names.size - 1) {
                val sep = TextView(this).apply {
                    text = "  ›  "
                    textSize = 10.5f
                    setTextColor(resources.getColor(R.color.muted, null))
                }
                binding.breadcrumb.addView(sep)
            }
        }
    }

    private fun returnFile(file: FsNode.File) {
        val dir = File(cacheDir, "shared")
        if (!dir.exists()) dir.mkdirs()

        val safeName = file.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val out = File(dir, safeName)
        out.writeText("Nooxe File — sample content for ${file.name}\n")

        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            out
        )

        val result = Intent().apply {
            data = uri
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri(file.name, uri)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}
