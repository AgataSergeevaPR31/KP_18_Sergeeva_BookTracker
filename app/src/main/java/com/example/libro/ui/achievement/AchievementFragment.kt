package com.example.libro.ui.achievement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libro.R
import com.example.libro.databinding.FragmentAchievementBinding
import com.example.libro.ui.achievement.adapter.AchievementAdapter
import com.example.libro.ui.achievement.dialog.AchievementDetailDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AchievementFragment : Fragment() {

    private var _binding: FragmentAchievementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AchievementViewModel by viewModels()
    private lateinit var adapter: AchievementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupRecyclerView()
        setupObservers()
        loadInitialData()
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            viewModel.loadAchievements()
        }
    }

    private fun setupSpinner() {
        val spinner: Spinner = binding.filterSpinner
        val filterOptions = arrayOf("Все", "Полученные", "Не полученные")

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinner.adapter = spinnerAdapter

        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> viewModel.setFilterType(AchievementViewModel.FilterType.ALL)
                    1 -> viewModel.setFilterType(AchievementViewModel.FilterType.ACHIEVED)
                    2 -> viewModel.setFilterType(AchievementViewModel.FilterType.NOT_ACHIEVED)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = AchievementAdapter(
            onItemClick = { achievement, userAchievement ->
                showAchievementDetails(achievement, userAchievement)
            }
        )
        val layoutManager = GridLayoutManager(context, 2)
        binding.recyclerViewAchievements.layoutManager = layoutManager
        binding.recyclerViewAchievements.adapter = adapter

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        binding.recyclerViewAchievements.addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
    }

    private fun setupObservers() {
        viewModel.filteredAchievements.observe(viewLifecycleOwner) { achievements ->
            adapter.submitList(achievements)
            updateEmptyState(achievements.isEmpty())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        binding.recyclerViewAchievements.visibility = View.GONE
                        binding.emptyState.visibility = View.GONE
                    } else {
                        binding.recyclerViewAchievements.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerViewAchievements.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewAchievements.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun showAchievementDetails(
        achievement: com.example.libro.Database.Achievement,
        userAchievement: com.example.libro.Database.UserAchievement?
    ) {
        val userAchievementForDialog = userAchievement ?: com.example.libro.Database.UserAchievement(
            achievementId = achievement.achievementId,
            achievementDate = null,
            currentProgress = 0.0,
            isAchieved = false
        )

        val dialog = AchievementDetailDialog.newInstance(achievement, userAchievementForDialog)
        dialog.show(childFragmentManager, "achievement_detail")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return

            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }
}