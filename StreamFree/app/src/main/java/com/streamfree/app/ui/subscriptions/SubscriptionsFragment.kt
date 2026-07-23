package com.streamfree.app.ui.subscriptions

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.streamfree.app.databinding.FragmentSubscriptionsBinding
import com.streamfree.app.ui.adapter.ChannelAdapter
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubscriptionsFragment : Fragment() {
    private var _binding: FragmentSubscriptionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubscriptionsViewModel by viewModel()
    private lateinit var adapter: ChannelAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentSubscriptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChannelAdapter { channel -> /* open channel */ }
        binding.rvSubscriptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubscriptions.adapter = adapter

        viewModel.subscriptions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) binding.tvEmpty.show() else binding.tvEmpty.gone()
        }

        binding.btnImport.setOnClickListener { viewModel.importSubscriptions(requireContext()) }
        binding.btnExport.setOnClickListener { viewModel.exportSubscriptions(requireContext()) }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
