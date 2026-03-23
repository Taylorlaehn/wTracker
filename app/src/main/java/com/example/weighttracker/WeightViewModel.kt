package com.example.weighttracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeightRepository(application)

    private val _currentUnit = MutableLiveData("lbs")
    val currentUnit: LiveData<String> = _currentUnit

    val allEntries: LiveData<List<WeightEntry>> = repository.allEntries
    val entryCount: LiveData<Int> = repository.entryCount

    val lowestWeight: LiveData<Double?> = allEntries.map {
        repository.getLowestWeight(_currentUnit.value ?: "lbs")
    }

    fun setUnit(unit: String) {
        _currentUnit.value = unit
    }

    fun insert(weight: Double) {
        val unit = _currentUnit.value ?: "lbs"
        val nextId = (allEntries.value?.maxOfOrNull { it.id } ?: 0) + 1
        repository.insert(WeightEntry(id = nextId, weight = weight, unit = unit))
    }

    fun delete(entry: WeightEntry) {
        repository.delete(entry)
    }

    fun deleteAll() {
        repository.deleteAll()
    }
}
