package com.streamfree.app.ui.home

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamfree.app.R
import com.streamfree.app.databinding.FragmentHomeBinding
import com.streamfree.app.ui.adapter.VideoAdapter
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModel()
    private lateinit var adapter: VideoAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMenu()
        observeViewModel()
        viewModel.loadTrending()
    }

    private fun setupRecyclerView() {
        adapter = VideoAdapter { video ->
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToDetail(video.url)
            )
        }
        binding.rvVideos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVideos.adapter = adapter
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_home, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = "Search YouTube..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        if (query.isNotEmpty()) {
                            findNavController().navigate(
                                HomeFragmentDirections.actionHomeToSearch(query)
                            )
                        }
                        return true
                    }
                    override fun onQueryTextChange(q: String) = false
                })
            }
            override fun onMenuItemSelected(item: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            adapter.submitList(videos)
            binding.progressBar.gone()
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) binding.progressBar.show() else binding.progressBar.gone()
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            binding.tvError.text = err
            if (err != null) binding.tvError.show() else binding.tvError.gone()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
