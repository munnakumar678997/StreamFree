package com.streamfree.app.ui.downloads

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamfree.app.databinding.FragmentDownloadsBinding
import com.streamfree.app.ui.adapter.DownloadAdapter
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadsFragment : Fragment() {
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DownloadsViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = DownloadAdapter { download ->
            viewModel.deleteDownload(download.videoId)
        }
        binding.rvDownloads.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDownloads.adapter = adapter

        viewModel.downloads.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) binding.tvEmpty.show() else binding.tvEmpty.gone()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
