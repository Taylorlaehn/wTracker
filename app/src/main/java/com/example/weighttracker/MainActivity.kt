package com.example.weighttracker

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class MainActivity : AppCompatActivity() {

    private val viewModel: WeightViewModel by viewModels()
    private lateinit var adapter: WeightAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val weightInput = findViewById<EditText>(R.id.etWeight)
        val logButton = findViewById<Button>(R.id.btnLog)
        val btnLbs = findViewById<Button>(R.id.btnLbs)
        val btnKg = findViewById<Button>(R.id.btnKg)
        val tvLatest = findViewById<TextView>(R.id.tvStatLatest)
        val tvLowest = findViewById<TextView>(R.id.tvStatLowest)
        val tvCount = findViewById<TextView>(R.id.tvStatCount)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyState = findViewById<TextView>(R.id.tvEmpty)
        val btnClearAll = findViewById<TextView>(R.id.btnClearAll)

        // Setup RecyclerView
        adapter = WeightAdapter { entry ->
            AlertDialog.Builder(this)
                .setMessage("Delete this entry?")
                .setPositiveButton("Delete") { _, _ -> viewModel.delete(entry) }
                .setNegativeButton("Cancel", null)
                .show()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Log button
        logButton.setOnClickListener { logWeight(weightInput) }

        // Submit on keyboard done
        weightInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                logWeight(weightInput)
                true
            } else false
        }

        // Unit toggle
        btnLbs.setOnClickListener { viewModel.setUnit("lbs") }
        btnKg.setOnClickListener { viewModel.setUnit("kg") }

        // Clear all
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear all entries?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Clear") { _, _ -> viewModel.deleteAll() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Observe unit changes
        viewModel.currentUnit.observe(this) { unit ->
            val isLbs = unit == "lbs"
            btnLbs.isSelected = isLbs
            btnKg.isSelected = !isLbs
            adapter.setUnit(unit)
        }

        // Observe entries
        viewModel.allEntries.observe(this) { entries ->
            adapter.submitList(entries)
            val isEmpty = entries.isEmpty()
            recyclerView.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
            emptyState.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE

            val unit = viewModel.currentUnit.value ?: "lbs"
            if (entries.isNotEmpty()) {
                val latest = entries.first()
                val displayWeight = if (latest.unit == unit) latest.weight
                    else if (unit == "kg") latest.weight * 0.453592
                    else latest.weight * 2.20462
                tvLatest.text = String.format("%.1f", displayWeight)
            } else {
                tvLatest.text = "—"
            }
        }

        // Observe stats
        viewModel.entryCount.observe(this) { count ->
            tvCount.text = count.toString()
        }

        viewModel.lowestWeight.observe(this) { lowest ->
            tvLowest.text = if (lowest != null) String.format("%.1f", lowest) else "—"
        }
    }

    private fun logWeight(input: EditText) {
        val value = input.text.toString().toDoubleOrNull()
        if (value == null || value <= 0 || value > 999) {
            Toast.makeText(this, "Enter a valid weight", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.insert(value)
        input.text.clear()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(input.windowToken, 0)
        Toast.makeText(this, "Logged ${String.format("%.1f", value)} ${viewModel.currentUnit.value}", Toast.LENGTH_SHORT).show()
    }
}
