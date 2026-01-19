package com.example.libro.ui.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.libro.R


class StatisticsAdapter(private var statistics: List<StatisticItem>) :
    RecyclerView.Adapter<StatisticsAdapter.StatisticViewHolder>() {

    fun updateStatistics(newStatistics: List<StatisticItem>) {
        statistics = newStatistics
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_statistic, parent, false)
        return StatisticViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticViewHolder, position: Int) {
        val stat = statistics[position]
        holder.bind(stat)
    }

    override fun getItemCount(): Int = statistics.size

    class StatisticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.statisticTitleTextView)
        private val valueTextView: TextView = itemView.findViewById(R.id.statisticValueTextView)
        private val subtitleTextView: TextView = itemView.findViewById(R.id.statisticSubtitleTextView)

        fun bind(statistic: StatisticItem) {
            titleTextView.text = statistic.title
            valueTextView.text = statistic.value
            subtitleTextView.text = statistic.subtitle
        }
    }
}