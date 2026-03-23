package com.example.weighttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeightRepository(application)

    val allEntries: LiveData<List<WeightEntry>> = repository.allEntries
    val entryCount: LiveData<Int> = repository.entryCount

    val lowestWeight: LiveData<Double?> = allEntries.map {
        repository.getLowestWeight("default")
    }

    fun insert(weight: Double) {
        val nextId = (allEntries.value?.maxOfOrNull { it.id } ?: 0) + 1
        repository.insert(WeightEntry(id = nextId, weight = weight))
    }

    fun delete(entry: WeightEntry) {
        repository.delete(entry)
    }

    fun deleteAll() {
        repository.deleteAll()
    }
}
