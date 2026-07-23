package com.streamfree.app.ui.history

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamfree.app.databinding.FragmentHistoryBinding
import com.streamfree.app.ui.adapter.VideoAdapter
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModel()
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = VideoAdapter { video ->
            findNavController().navigate(HistoryFragmentDirections.actionHistoryToDetail(video.url))
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        viewModel.history.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) binding.tvEmpty.show() else binding.tvEmpty.gone()
        }

        binding.btnClearHistory.setOnClickListener { viewModel.clearHistory() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
