package com.example.weighttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WeightAdapter(
    private val onDelete: (WeightEntry) -> Unit
) : ListAdapter<WeightEntry, WeightAdapter.WeightViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight_entry, parent, false)
        return WeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val entry = getItem(position)
        val prevEntry = if (position + 1 < itemCount) getItem(position + 1) else null
        holder.bind(entry, prevEntry, onDelete)
    }

    class WeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weightText: TextView = itemView.findViewById(R.id.tvWeight)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)
        private val deltaText: TextView = itemView.findViewById(R.id.tvDelta)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val indicator: View = itemView.findViewById(R.id.indicator)

        fun bind(
            entry: WeightEntry,
            prevEntry: WeightEntry?,
            onDelete: (WeightEntry) -> Unit
        ) {
            val displayWeight = entry.weight
            weightText.text = String.format("%.1f", displayWeight)

            val sdfDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = Date(entry.timestamp)

            val today = Calendar.getInstance()
            val entryDay = Calendar.getInstance().apply { time = date }
            dateText.text = when {
                today.get(Calendar.DAY_OF_YEAR) == entryDay.get(Calendar.DAY_OF_YEAR) &&
                        today.get(Calendar.YEAR) == entryDay.get(Calendar.YEAR) -> "Today"
                today.get(Calendar.DAY_OF_YEAR) - 1 == entryDay.get(Calendar.DAY_OF_YEAR) &&
                        today.get(Calendar.YEAR) == entryDay.get(Calendar.YEAR) -> "Yesterday"
                else -> sdfDate.format(date)
            }
            timeText.text = sdfTime.format(date)

            if (prevEntry != null) {
                val diff = displayWeight - prevEntry.weight
                when {
                    diff > 0.05 -> {
                        deltaText.text = String.format("+%.1f", diff)
                        deltaText.setTextColor(ContextCompat.getColor(itemView.context, R.color.delta_up))
                        indicator.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.delta_up))
                    }
                    diff < -0.05 -> {
                        deltaText.text = String.format("%.1f", diff)
                        deltaText.setTextColor(ContextCompat.getColor(itemView.context, R.color.delta_down))
                        indicator.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.delta_down))
                    }
                    else -> {
                        deltaText.text = "±0"
                        deltaText.setTextColor(ContextCompat.getColor(itemView.context, R.color.delta_neutral))
                        indicator.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.delta_neutral))
                    }
                }
                deltaText.visibility = View.VISIBLE
            } else {
                deltaText.visibility = View.INVISIBLE
                indicator.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.delta_neutral))
            }

            deleteBtn.setOnClickListener { onDelete(entry) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WeightEntry>() {
        override fun areItemsTheSame(old: WeightEntry, new: WeightEntry) = old.id == new.id
        override fun areContentsTheSame(old: WeightEntry, new: WeightEntry) = old == new
    }
}
