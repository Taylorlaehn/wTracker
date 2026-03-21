package com.example.weighttracker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class WeightRepository(context: Context) {

    private val storage = WeightStorage(context)
    private val _entries = MutableLiveData<List<WeightEntry>>(storage.loadEntries())

    val allEntries: LiveData<List<WeightEntry>> = _entries

    val entryCount: LiveData<Int>
        get() = MutableLiveData((_entries.value ?: emptyList()).size).also { liveCount ->
            _entries.observeForever { liveCount.postValue(it.size) }
        }

    fun getLowestWeight(unit: String): Double? {
        val entries = _entries.value ?: return null
        if (entries.isEmpty()) return null
        return entries.minOf { e ->
            if (e.unit == unit) e.weight
            else if (unit == "kg") e.weight * 0.453592
            else e.weight * 2.20462
        }
    }

    fun insert(entry: WeightEntry) {
        val current = _entries.value ?: emptyList()
        val updated = listOf(entry) + current
        _entries.postValue(updated)
        storage.saveEntries(updated)
    }

    fun delete(entry: WeightEntry) {
        val updated = (_entries.value ?: emptyList()).filter { it.id != entry.id }
        _entries.postValue(updated)
        storage.saveEntries(updated)
    }

    fun deleteAll() {
        _entries.postValue(emptyList())
        storage.saveEntries(emptyList())
    }
}
