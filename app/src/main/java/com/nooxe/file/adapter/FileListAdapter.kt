package com.nooxe.file.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nooxe.file.R
import com.nooxe.file.data.FsNode
import com.nooxe.file.databinding.ItemFileListBinding

class FileListAdapter(
    private val onClick: (FsNode.File) -> Unit,
    private val onLongClick: (FsNode.File) -> Unit
) : RecyclerView.Adapter<FileListAdapter.VH>() {

    private val items = mutableListOf<FsNode.File>()
    var selected: Set<String> = emptySet()
        set(value) { field = value; notifyDataSetChanged() }

    fun submit(list: List<FsNode.File>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemFileListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val f = items[position]
        holder.b.tvName.text = f.name
        holder.b.tvMeta.text = "${com.nooxe.file.data.StorageState.formatSize(f.sizeMB)}  ·  ${f.added}"
        holder.b.ivIcon.setImageResource(iconFor(f.kind))
        val isSel = selected.contains(f.id)
        if (isSel) {
            holder.b.ivSelect.setImageResource(com.nooxe.file.R.drawable.ic_check)
            holder.b.ivSelect.setBackgroundResource(com.nooxe.file.R.drawable.bg_circle_green)
        } else {
            holder.b.ivSelect.setImageResource(0)
            holder.b.ivSelect.setBackgroundResource(com.nooxe.file.R.drawable.ic_radio_unchecked)
        }
        holder.itemView.setOnClickListener { onClick(f) }
        holder.itemView.setOnLongClickListener { onLongClick(f); true }
    }

    class VH(val b: ItemFileListBinding) : RecyclerView.ViewHolder(b.root)

    private fun iconFor(kind: String): Int = when (kind) {
        "pdf", "doc", "fig", "zip" -> R.drawable.ic_doc
        "png", "jpg" -> R.drawable.ic_pic
        "mp4" -> R.drawable.ic_video
        "mp3" -> R.drawable.ic_audio
        else -> R.drawable.ic_doc
    }
}
