package com.nooxe.file.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nooxe.file.R
import com.nooxe.file.data.FileSystem
import com.nooxe.file.data.StorageState
import com.nooxe.file.databinding.FragmentCleanBinding

class CleanFragment : Fragment() {

    private var _binding: FragmentCleanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnCleanOld.setOnClickListener {
            val freed = FileSystem.clearOldFiles("internal")
            android.widget.Toast.makeText(
                requireContext(),
                if (freed > 0) "Freed ${StorageState.formatSize(freed)}" else "Nothing to clean",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
