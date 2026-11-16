package com.nooxe.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nooxe.file.MainActivity
import com.nooxe.file.OnBackPressedListener
import com.nooxe.file.R
import com.nooxe.file.adapter.PickerAdapter
import com.nooxe.file.data.FsNode
import com.nooxe.file.data.FileSystem
import com.nooxe.file.databinding.FragmentBrowseBinding

class BrowseFragment : Fragment(), OnBackPressedListener {

    private var _binding: FragmentBrowseBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PickerAdapter
    private val path = mutableListOf("internal")
    private val tab = "internal"
    private var query = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PickerAdapter(
            onFolder = { openFolder(it) },
            onFile = { /* show details later */ }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

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

        render()
    }

    private fun openFolder(folder: FsNode.Folder) {
        path.add(folder.id)
        render()
    }

    override fun onBackPressed(): Boolean {
        return if (path.size > 1) {
            path.removeAt(path.size - 1)
            render()
            true
        } else false
    }

    private fun render() {
        val folder = FileSystem.resolve(tab, path)
        val folders = folder.children.filterIsInstance<FsNode.Folder>()
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
        val files = folder.children.filterIsInstance<FsNode.File>()
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }

        val list = mutableListOf<FsNode>()
        list.addAll(folders)
        list.addAll(files)
        adapter.submit(list)

        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.recycler.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE

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
            val tv = android.widget.TextView(requireContext()).apply {
                text = name
                textSize = 10.5f
                setTextColor(
                    if (i == names.size - 1) resources.getColor(R.color.ink, null)
                    else resources.getColor(R.color.on_surface_variant, null)
                )
                if (i == names.size - 1) paint.isFakeBoldText = true
            }
            binding.breadcrumb.addView(tv)
            if (i != names.size - 1) {
                val sep = android.widget.TextView(requireContext()).apply {
                    text = "  ›  "
                    textSize = 10.5f
                    setTextColor(resources.getColor(R.color.on_surface_variant, null))
                }
                binding.breadcrumb.addView(sep)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
