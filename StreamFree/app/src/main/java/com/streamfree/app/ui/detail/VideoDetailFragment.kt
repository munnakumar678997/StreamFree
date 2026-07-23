package com.streamfree.app.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.streamfree.app.databinding.FragmentVideoDetailBinding
import com.streamfree.app.player.PlayerActivity
import com.streamfree.app.player.PopupPlayerService
import com.streamfree.app.util.gone
import com.streamfree.app.util.show
import com.streamfree.app.util.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class VideoDetailFragment : Fragment() {

    private var _binding: FragmentVideoDetailBinding? = null
    private val binding get() = _binding!!
    private val args: VideoDetailFragmentArgs by navArgs()
    private val viewModel: VideoDetailViewModel by viewModel { parametersOf(args.videoUrl) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, saved: Bundle?): View {
        _binding = FragmentVideoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        binding.btnPlay.setOnClickListener {
            val url = args.videoUrl
            startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_URL, url)
                putExtra(PlayerActivity.EXTRA_TITLE, viewModel.title.value ?: "")
            })
        }

        binding.btnPopup.setOnClickListener {
            requireContext().startService(Intent(requireContext(), PopupPlayerService::class.java).apply {
                putExtra(PopupPlayerService.EXTRA_URL, args.videoUrl)
                putExtra(PopupPlayerService.EXTRA_TITLE, viewModel.title.value ?: "")
            })
        }

        binding.btnDownload.setOnClickListener { viewModel.showDownloadDialog(requireContext()) }
        binding.btnSubscribe.setOnClickListener { viewModel.toggleSubscription() }
        binding.btnBookmark.setOnClickListener { viewModel.toggleBookmark() }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { if (it) binding.progressBar.show() else binding.progressBar.gone() }
        viewModel.title.observe(viewLifecycleOwner)   { binding.tvTitle.text = it }
        viewModel.uploader.observe(viewLifecycleOwner) { binding.tvUploader.text = it }
        viewModel.viewCount.observe(viewLifecycleOwner) { binding.tvViews.text = it }
        viewModel.likes.observe(viewLifecycleOwner) { binding.tvLikes.text = it }
        viewModel.dislikes.observe(viewLifecycleOwner) { binding.tvDislikes.text = it }
        viewModel.description.observe(viewLifecycleOwner) { binding.tvDescription.text = it }
        viewModel.thumbnailUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this).load(url).into(binding.ivThumbnail)
        }
        viewModel.isSubscribed.observe(viewLifecycleOwner) { subbed ->
            binding.btnSubscribe.text = if (subbed) "Unsubscribe" else "Subscribe"
        }
        viewModel.isBookmarked.observe(viewLifecycleOwner) { bookmarked ->
            binding.btnBookmark.text = if (bookmarked) "Saved" else "Save"
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { toast(it) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
