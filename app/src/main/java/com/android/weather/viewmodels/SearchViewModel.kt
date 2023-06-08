package com.android.weather.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.weather.repositories.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *  MVVM Search ViewModel
 */
@ExperimentalCoroutinesApi
@HiltViewModel
class SearchViewModel @Inject constructor(private val searchRepository: SearchRepository) :
    ViewModel() {

    val searchData = searchRepository.searchData

    fun getSearch(search: String?) {
        viewModelScope.launch {
            searchRepository.getSearch(search)
        }
    }
}