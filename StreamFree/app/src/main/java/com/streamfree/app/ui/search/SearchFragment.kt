package com.streamfree.app.ui.search

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamfree.app.databinding.FragmentSearchBinding
import com.streamfree.app.ui.adapter.VideoAdapter
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val args: SearchFragmentArgs by navArgs()
    private val viewModel: SearchViewModel by viewModel { parametersOf(args.query) }
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvSearchQuery.text = "Results for: \"${args.query}\""
        adapter = VideoAdapter { video ->
            findNavController().navigate(SearchFragmentDirections.actionSearchToDetail(video.url))
        }
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter

        viewModel.results.observe(viewLifecycleOwner) { adapter.submitList(it) }
        viewModel.loading.observe(viewLifecycleOwner) { if (it) binding.progressBar.show() else binding.progressBar.gone() }
        viewModel.error.observe(viewLifecycleOwner) { e ->
            binding.tvError.text = e
            if (e != null) binding.tvError.show() else binding.tvError.gone()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
