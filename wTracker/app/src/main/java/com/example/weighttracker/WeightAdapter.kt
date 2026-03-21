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

    private var displayUnit: String = "lbs"

    fun setUnit(unit: String) {
        displayUnit = unit
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight_entry, parent, false)
        return WeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val entry = getItem(position)
        val prevEntry = if (position + 1 < itemCount) getItem(position + 1) else null
        holder.bind(entry, prevEntry, displayUnit, onDelete)
    }

    class WeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val weightText: TextView = itemView.findViewById(R.id.tvWeight)
        private val unitText: TextView = itemView.findViewById(R.id.tvUnit)
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)
        private val deltaText: TextView = itemView.findViewById(R.id.tvDelta)
        private val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val indicator: View = itemView.findViewById(R.id.indicator)

        fun bind(
            entry: WeightEntry,
            prevEntry: WeightEntry?,
            displayUnit: String,
            onDelete: (WeightEntry) -> Unit
        ) {
            val displayWeight = convertWeight(entry.weight, entry.unit, displayUnit)
            weightText.text = String.format("%.1f", displayWeight)
            unitText.text = displayUnit

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
                val prevDisplay = convertWeight(prevEntry.weight, prevEntry.unit, displayUnit)
                val diff = displayWeight - prevDisplay
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

        private fun convertWeight(value: Double, from: String, to: String): Double {
            if (from == to) return value
            return if (from == "lbs" && to == "kg") value * 0.453592
            else value * 2.20462
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<WeightEntry>() {
        override fun areItemsTheSame(old: WeightEntry, new: WeightEntry) = old.id == new.id
        override fun areContentsTheSame(old: WeightEntry, new: WeightEntry) = old == new
    }
}
