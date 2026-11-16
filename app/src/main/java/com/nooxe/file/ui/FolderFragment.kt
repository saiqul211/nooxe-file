package com.nooxe.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nooxe.file.R
import com.nooxe.file.adapter.FileGridAdapter
import com.nooxe.file.adapter.FileListAdapter
import com.nooxe.file.data.FsNode
import com.nooxe.file.data.FileSystem
import com.nooxe.file.data.StorageState
import com.nooxe.file.databinding.FragmentFolderBinding

class FolderFragment : Fragment() {

    private var _binding: FragmentFolderBinding? = null
    private val binding get() = _binding!!

    private var tab: String = "internal"
    private var path: MutableList<String> = mutableListOf()
    private var folderName: String = "Folder"
    private var isGrid = false
    private val selected = mutableSetOf<String>()
    private var query = ""
    private var sortMode = 0

    private lateinit var listAdapter: FileListAdapter
    private lateinit var gridAdapter: FileGridAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tab = arguments?.getString(ARG_TAB) ?: "internal"
        path = (arguments?.getStringArrayList(ARG_PATH) ?: arrayListOf<String>()).toMutableList()
        folderName = arguments?.getString(ARG_NAME) ?: "Folder"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listAdapter = FileListAdapter({ onFileClick(it) }, { onFileLongClick(it) })
        gridAdapter = FileGridAdapter({ onFileClick(it) }, { onFileLongClick(it) })
        setupRecycler()
        setupHeader()
        setupBreadcrumb()
        render()
    }

    private fun setupRecycler() {
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = listAdapter
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener {
            if (path.size > 1) {
                path.removeAt(path.size - 1)
                selected.clear()
                render()
            } else {
                parentFragmentManager.popBackStack()
            }
        }
        binding.btnSearch.setOnClickListener {
            val v = binding.etSearch
            if (v.visibility == View.VISIBLE) {
                v.visibility = View.GONE
                query = ""
                render()
            } else {
                v.visibility = View.VISIBLE
                v.requestFocus()
            }
        }
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                query = s?.toString() ?: ""
                render()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        binding.btnViewToggle.setOnClickListener { toggleView() }
        binding.btnViewToggle.setOnLongClickListener { showSortMenu(); true }
        binding.btnCloseSelect.setOnClickListener { clearSelection() }
        binding.btnMore.setOnClickListener { showSortMenu() }
        binding.tbTag.setOnClickListener {
            toast(getString(R.string.tagged, selected.size))
            clearSelection()
        }
        binding.tbShare.setOnClickListener {
            toast(getString(R.string.link_copied))
            clearSelection()
        }
        binding.tbDelete.setOnClickListener { deleteSelected() }
        binding.tbMore.setOnClickListener { showSortMenu() }
        binding.fabNewFolder.setOnClickListener { showNewFolderDialog() }
    }

    private fun setupBreadcrumb() {
        val root = FileSystem.rootFor(tab)
        binding.breadcrumb.removeAllViews()
        val crumbs = mutableListOf<Pair<String, List<String>>>()
        crumbs.add(root.name to listOf(root.id))
        var acc = listOf(root.id)
        for (i in 1 until path.size) {
            acc = acc + path[i]
            val node = FileSystem.resolve(tab, acc)
            crumbs.add(node.name to acc)
        }
        crumbs.forEachIndexed { i, (name, _) ->
            val tv = TextView(requireContext()).apply {
                text = name
                textSize = 10.5f
                setTextColor(
                    if (i == crumbs.size - 1) resources.getColor(R.color.ink, null)
                    else resources.getColor(R.color.muted, null)
                )
                if (i == crumbs.size - 1) paint.isFakeBoldText = true
                setPadding(0, 0, 0, 0)
            }
            binding.breadcrumb.addView(tv)
            if (i != crumbs.size - 1) {
                val sep = TextView(requireContext()).apply {
                    text = "  ›  "
                    textSize = 10.5f
                    setTextColor(resources.getColor(R.color.muted, null))
                }
                binding.breadcrumb.addView(sep)
            }
        }
    }

    private fun currentFolder(): FsNode.Folder = FileSystem.resolve(tab, path)

    private fun render() {
        val folder = currentFolder()
        binding.tvTitle.text = folderName
        val files = folder.children.filterIsInstance<FsNode.File>()
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }

        val sorted = when (sortMode) {
            1 -> files.sortedBy { it.name.lowercase() }
            2 -> files.sortedByDescending { it.sizeMB }
            else -> files.sortedBy { it.addedDays }
        }

        binding.tvMeta.text =
            getString(R.string.items_count, files.size) + "  ·  " +
            StorageState.formatSize(folder.children.sumOf { if (it is FsNode.File) it.sizeMB else 0 })

        binding.tvEmpty.visibility = if (files.isEmpty()) View.VISIBLE else View.GONE
        binding.recycler.visibility = if (files.isEmpty()) View.GONE else View.VISIBLE

        if (isGrid) {
            binding.recycler.layoutManager = GridLayoutManager(requireContext(), 2)
            gridAdapter.selected = selected.toSet()
            gridAdapter.submit(sorted)
            binding.recycler.adapter = gridAdapter
        } else {
            binding.recycler.layoutManager = LinearLayoutManager(requireContext())
            listAdapter.selected = selected.toSet()
            listAdapter.submit(sorted)
            binding.recycler.adapter = listAdapter
        }
        updateSelectionMode()
    }

    private fun updateSelectionMode() {
        val active = selected.isNotEmpty()
        binding.headerNormal.visibility = if (active) View.GONE else View.VISIBLE
        binding.headerSelect.visibility = if (active) View.VISIBLE else View.GONE
        binding.toolbar.visibility = if (active) View.VISIBLE else View.GONE
        binding.fabNewFolder.visibility = if (active) View.GONE else View.VISIBLE
        if (active) {
            binding.tvSelectedCount.text =
                getString(R.string.selected_items, selected.size)
        }
    }

    private fun toggleView() {
        isGrid = !isGrid
        binding.btnViewToggle.setImageResource(if (isGrid) R.drawable.ic_list else R.drawable.ic_grid)
        render()
    }

    private fun onFileClick(file: FsNode.File) {
        if (selected.isNotEmpty()) {
            if (selected.contains(file.id)) selected.remove(file.id) else selected.add(file.id)
            render()
        } else {
            toast(file.name)
        }
    }

    private fun onFileLongClick(file: FsNode.File) {
        if (selected.contains(file.id)) selected.remove(file.id) else selected.add(file.id)
        render()
    }

    private fun clearSelection() {
        selected.clear()
        render()
    }

    private fun deleteSelected() {
        val count = FileSystem.removeFiles(tab, selected.toSet())
        selected.clear()
        render()
        toast(getString(R.string.files_deleted, count))
    }

    private fun showSortMenu() {
        val menu = PopupMenu(requireContext(), binding.btnMore)
        menu.menu.add(0, 0, 0, getString(R.string.sort_recent))
        menu.menu.add(0, 1, 1, getString(R.string.sort_name))
        menu.menu.add(0, 2, 2, getString(R.string.sort_size))
        menu.setOnMenuItemClickListener {
            sortMode = it.itemId
            render()
            true
        }
        menu.show()
    }

    private fun showNewFolderDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = getString(R.string.new_folder)
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.new_folder))
            .setView(input)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    FileSystem.addFolder(tab, path.last(), name)
                    render()
                    toast(getString(R.string.folder_created))
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun toast(msg: String) {
        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TAB = "tab"
        private const val ARG_PATH = "path"
        private const val ARG_NAME = "name"

        fun newInstance(tab: String, path: List<String>, name: String): FolderFragment {
            val f = FolderFragment()
            val args = Bundle()
            args.putString(ARG_TAB, tab)
            args.putStringArrayList(ARG_PATH, ArrayList(path))
            args.putString(ARG_NAME, name)
            f.arguments = args
            return f
        }
    }
}
