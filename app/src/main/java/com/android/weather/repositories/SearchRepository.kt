package com.android.weather.repositories

import com.android.weather.apis.WeatherApi
import com.android.weather.apis.UIResult
import com.android.weather.models.SearchResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Exception
import javax.inject.Inject

/**
 *  MVVM Search Repository
 */
@ExperimentalCoroutinesApi
class SearchRepository @Inject constructor(private val weatherApi: WeatherApi) {

    private val _searchData = MutableStateFlow<UIResult<SearchResponse>>(UIResult.Loading(false))
    val searchData: StateFlow<UIResult<SearchResponse>> get() = _searchData

    // search repository method
    suspend fun getSearch(search: String?) {
        try {
            _searchData.value = UIResult.Loading(true)
            val response = if (search.isNullOrEmpty())
                weatherApi.getSearch(search = "lastSearch")
            else
                weatherApi.getSearch(search = search)
            _searchData.value = UIResult.Loading(false)
            _searchData.value = UIResult.Success(response)
        } catch (exception: Exception) {
            _searchData.value = exception.message?.let { UIResult.Error(it) }!!
        }
    }
}