package com.nooxe.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import com.nooxe.file.R
import com.nooxe.file.data.FileSystem
import com.nooxe.file.data.StorageState
import com.nooxe.file.databinding.FragmentHomeBinding
import com.nooxe.file.databinding.ItemCategoryBinding
import com.nooxe.file.databinding.ItemRecentBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var tab = "internal"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnMenu.setOnClickListener { /* drawer placeholder */ }
        binding.btnSearchHome.setOnClickListener { }
        binding.btnInternal.setOnClickListener { switchTab("internal") }
        binding.btnExternal.setOnClickListener { switchTab("external") }
        binding.btnClearSpace.setOnClickListener { clearSpace() }
        render()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun switchTab(newTab: String) {
        tab = newTab
        render()
    }

    private fun render() {
        val isInternal = tab == "internal"
        binding.btnInternal.setBackgroundResource(
            if (isInternal) R.drawable.bg_chip_orange else R.drawable.bg_chip_outline
        )
        binding.btnInternal.alpha = if (isInternal) 1f else 0.7f
        binding.btnExternal.setBackgroundResource(
            if (!isInternal) R.drawable.bg_chip_orange else R.drawable.bg_chip_outline
        )
        binding.btnExternal.alpha = if (!isInternal) 1f else 0.7f

        binding.tvTotal.text = StorageState.formatSize(StorageState.TOTAL_MB)
        binding.tvUsed.text = StorageState.formatSize(StorageState.usedMB)
        binding.donut.setProgress(StorageState.usedMB, StorageState.TOTAL_MB, StorageState.formatSize(StorageState.usedMB))

        renderCategories()
        renderRecent()
    }

    private fun rootId(): String = if (tab == "internal") "internal" else "external"

    private fun renderCategories() {
        binding.gridCats.removeAllViews()
        val cats = listOf(
            Category("Document", R.drawable.ic_doc, "250 MB", "documents", R.color.chip_doc),
            Category("Pictures", R.drawable.ic_pic, "1.6 GB", "pictures", R.color.chip_pic),
            Category("Video", R.drawable.ic_video, "77 GB", "videos", R.color.chip_vid),
            Category("Audio", R.drawable.ic_audio, "450 MB", "audio", R.color.chip_aud)
        )
        for (c in cats) {
            val b = ItemCategoryBinding.inflate(layoutInflater)
            val params = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                setMargins(4, 4, 4, 4)
            }
            b.root.layoutParams = params
            b.ivIcon.setImageResource(c.iconRes)
            b.ivIcon.backgroundTintList =
                android.content.res.ColorStateList.valueOf(resources.getColor(c.chipColor, null))
            b.tvName.text = c.name
            b.tvSize.text = c.sizeLabel
            b.root.setOnClickListener { openFolder(listOf(rootId(), c.folderId), c.name) }
            binding.gridCats.addView(b.root)
        }
    }

    private fun renderRecent() {
        binding.listRecent.removeAllViews()
        val recent = FileSystem.recentFiles(tab)
        val b = ItemRecentBinding.inflate(layoutInflater)
        b.ivIcon.setImageResource(R.drawable.ic_folder)
        b.tvName.text = "Figma files"
        b.tvMeta.text = "24 items  ·  28 GB  ·  19 April 2022"
        b.root.setOnClickListener {
            if (tab == "internal") openFolder(listOf(rootId(), "my-storage", "figma"), "Figma files")
        }
        binding.listRecent.addView(b.root)
        for (f in recent.take(2)) {
            val rb = ItemRecentBinding.inflate(layoutInflater)
            rb.ivIcon.setImageResource(
                when (f.kind) {
                    "png", "jpg" -> R.drawable.ic_pic
                    "mp4" -> R.drawable.ic_video
                    "mp3" -> R.drawable.ic_audio
                    else -> R.drawable.ic_doc
                }
            )
            rb.tvName.text = f.name
            rb.tvMeta.text = "${StorageState.formatSize(f.sizeMB)}  ·  ${f.added}"
            rb.root.setOnClickListener { }
            binding.listRecent.addView(rb.root)
        }
    }

    private fun clearSpace() {
        val freed = FileSystem.clearOldFiles(tab)
        render()
        val msg = if (freed > 0) getString(R.string.freed_space, StorageState.formatSize(freed)) else "Nothing to clear"
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun openFolder(path: List<String>, name: String) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in, android.R.anim.fade_out,
                android.R.anim.fade_in, android.R.anim.fade_out
            )
            .replace(R.id.container, FolderFragment.newInstance(tab, path, name))
            .addToBackStack("folder")
            .commit()
    }
}

private data class Category(val name: String, val iconRes: Int, val sizeLabel: String, val folderId: String, val chipColor: Int)
