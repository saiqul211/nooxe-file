package com.nooxe.file.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nooxe.file.R
import com.nooxe.file.data.FsNode
import com.nooxe.file.data.StorageState
import com.nooxe.file.databinding.ItemPickerFileBinding

class PickerAdapter(
    private val onFolder: (FsNode.Folder) -> Unit,
    private val onFile: (FsNode.File) -> Unit
) : RecyclerView.Adapter<PickerAdapter.VH>() {

    private val items = mutableListOf<FsNode>()

    fun submit(list: List<FsNode>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPickerFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val node = items[position]
        when (node) {
            is FsNode.Folder -> {
                holder.b.ivIcon.setImageResource(R.drawable.ic_folder)
                holder.b.tvName.text = node.name
                holder.b.tvMeta.text = "${node.children.size} items"
                holder.b.ivChevron.visibility = android.view.View.VISIBLE
                holder.itemView.setOnClickListener { onFolder(node) }
            }
            is FsNode.File -> {
                holder.b.ivIcon.setImageResource(iconFor(node.kind))
                holder.b.tvName.text = node.name
                holder.b.tvMeta.text =
                    "${StorageState.formatSize(node.sizeMB)}  ·  ${node.added}"
                holder.b.ivChevron.visibility = android.view.View.INVISIBLE
                holder.itemView.setOnClickListener { onFile(node) }
            }
        }
    }

    class VH(val b: ItemPickerFileBinding) : RecyclerView.ViewHolder(b.root)

    private fun iconFor(kind: String): Int = when (kind) {
        "pdf", "doc", "fig", "zip" -> R.drawable.ic_doc
        "png", "jpg" -> R.drawable.ic_pic
        "mp4" -> R.drawable.ic_video
        "mp3" -> R.drawable.ic_audio
        else -> R.drawable.ic_doc
    }
}
