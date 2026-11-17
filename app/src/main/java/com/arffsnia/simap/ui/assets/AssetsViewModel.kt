package com.arffsnia.simap.ui.assets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arffsnia.simap.SimapApp
import com.arffsnia.simap.data.dto.Asset
import com.arffsnia.simap.data.dto.SimpleItem
import com.arffsnia.simap.data.repo.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AssetsViewModel : ViewModel() {
    private val repo = SimapApp.instance.assetRepo

    var search by mutableStateOf("")
    var items by mutableStateOf<List<Asset>>(emptyList())
    var total by mutableStateOf(0)
    var loading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var categories by mutableStateOf<List<SimpleItem>>(emptyList())
    var selectedCategory by mutableStateOf<Int?>(null)

    private var searchJob: Job? = null

    init {
        load(initial = true)
    }

    fun onSearchChange(v: String) {
        search = v
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            load()
        }
    }

    fun onCategorySelect(v: Int?) {
        selectedCategory = v
        load()
    }

    fun load(initial: Boolean = false) {
        viewModelScope.launch {
            loading = true
            when (val r = repo.assets(
                page = 1,
                search = search.ifBlank { null },
                categoryId = selectedCategory,
            )) {
                is AppResult.Success -> {
                    items = r.data.items
                    total = r.data.total
                    error = null
                }
                is AppResult.Error -> error = com.arffsnia.simap.ui.mapError(r.message)
            }
            loading = false
            if (initial) loadCategories()
        }
    }

    private suspend fun loadCategories() {
        categories = repo.categories()
    }
}