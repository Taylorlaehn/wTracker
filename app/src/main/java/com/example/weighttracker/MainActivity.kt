package com.example.weighttracker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import android.view.HapticFeedbackConstants
import android.view.View

class MainActivity : AppCompatActivity() {

    private val viewModel: WeightViewModel by viewModels()
    private lateinit var adapter: WeightAdapter
    private var isInitialLoad = true
    private var showFullHistory = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val weightInput = findViewById<EditText>(R.id.etWeight)
        val logButton = findViewById<Button>(R.id.btnLog)
        val tvLatest = findViewById<TextView>(R.id.tvStatLatest)
        val tvLowest = findViewById<TextView>(R.id.tvStatLowest)
        val tvCount = findViewById<TextView>(R.id.tvStatCount)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val emptyState = findViewById<TextView>(R.id.tvEmpty)
        val btnClearAll = findViewById<TextView>(R.id.btnClearAll)
        val btnViewMore = findViewById<TextView>(R.id.btnViewMore)

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

        // Swipe/Scroll gesture for weight input
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            private var lastScrollTime = 0L

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (abs(distanceX) > abs(distanceY)) {
                    val now = System.currentTimeMillis()
                    val minInterval = 100L // ms between changes — raise to slow it down

                    val currentVal = weightInput.text.toString().toDoubleOrNull() ?: 0.0
                    val dp = resources.displayMetrics.density
                    val rawChange = -distanceX / (10.0 * dp)
                    val change = (rawChange * (1.0 + abs(rawChange) * 3.0)).coerceIn(-0.2, 0.2)

                    val effectiveChange = when {
                        change == 0.0 -> 0.0
                        abs(change) < 0.1 -> 0.1 * Math.signum(change)
                        else -> change
                    }

                    if (effectiveChange != 0.0 && (now - lastScrollTime) >= minInterval) {
                        val newVal = (currentVal + effectiveChange).coerceIn(0.0, 999.9)
                        val formatted = String.format("%.1f", newVal)

                        if (formatted != weightInput.text.toString()) {
                            weightInput.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            lastScrollTime = now
                        }

                        weightInput.setText(formatted)
                        weightInput.setSelection(weightInput.text.length)
                    }

                    return true
                }
                return false
            }
        })

        weightInput.setOnTouchListener { v, event ->
            val gestureHandled = gestureDetector.onTouchEvent(event)
            if (gestureHandled && (event.action == MotionEvent.ACTION_MOVE)) {
                // Prevent vertical scroll while scrubbing
                v.parent.requestDisallowInterceptTouchEvent(true)
                true
            } else {
                false 
            }
        }

        // Clear all
        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear all entries?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Clear") { _, _ -> viewModel.deleteAll() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnViewMore.setOnClickListener {
            showFullHistory = !showFullHistory
            updateHistoryList(viewModel.allEntries.value ?: emptyList())
        }

        // Observe entries
        viewModel.allEntries.observe(this) { entries ->
            updateHistoryList(entries)
            val isEmpty = entries.isEmpty()
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE

            if (entries.isNotEmpty()) {
                val latest = entries.first()
                tvLatest.text = String.format("%.1f", latest.weight)
                
                if (isInitialLoad) {
                    weightInput.setText(String.format("%.1f", latest.weight))
                    isInitialLoad = false
                }
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

    private fun updateHistoryList(entries: List<WeightEntry>) {
        val btnViewMore = findViewById<TextView>(R.id.btnViewMore)
        if (entries.size <= 5) {
            adapter.submitList(entries)
            btnViewMore.visibility = View.GONE
        } else {
            btnViewMore.visibility = View.VISIBLE
            if (showFullHistory) {
                adapter.submitList(entries)
                btnViewMore.text = "View Less"
            } else {
                adapter.submitList(entries.take(5))
                btnViewMore.text = "View More"
            }
        }
    }

    private fun logWeight(input: EditText) {
        val value = input.text.toString().toDoubleOrNull()
        if (value == null || value <= 0 || value > 999) {
            Toast.makeText(this, "Enter a valid weight", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.insert(value)
        
        // Keep the value as the new default
        input.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(input.windowToken, 0)

        Toast.makeText(this, "Logged ${String.format("%.1f", value)}", Toast.LENGTH_SHORT).show()
    }
}
