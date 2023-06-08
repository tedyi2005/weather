package com.android.weather.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.weather.R
import com.android.weather.apis.UIResult
import com.android.weather.databinding.ErrorLayoutBinding
import com.android.weather.databinding.FragmentHomeBinding
import com.android.weather.models.Coord
import com.android.weather.models.SearchResponse
import com.android.weather.utils.hide
import com.android.weather.utils.hideKeyboard
import com.android.weather.utils.loadImage
import com.android.weather.utils.setTextOrHide
import com.android.weather.utils.show
import com.android.weather.utils.toIconUrl
import com.android.weather.viewmodels.SearchViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * search class.
 */

@AndroidEntryPoint
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val permissionCode = 101
    private lateinit var searchResponse: SearchResponse
    private lateinit var googleMap: GoogleMap
    private var allMarkers = mutableListOf<Marker?>()
    private var latLngBoundBuilder = LatLngBounds.Builder()
    private val searchViewModel: SearchViewModel by viewModels()

    companion object {
        const val padding = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layout.hide()
        binding.apply {
            searchEditTextImageView.setOnClickListener {
                it.hideKeyboard()
                binding.layout.hide()
                val searchValue = searchEditText.text.toString()
                if (searchValue.isNotEmpty()) {
                    // search model
                    searchViewModel.getSearch(searchValue)
                }
            }
            lifecycleScope.launch {
                searchViewModel.searchData.collect { result ->
                    when (result) {
                        is UIResult.Success -> {
                            val response = result.data
                            if (response != null) {
                                binding.layout.show()
                                searchResponse = response
                                val mapFragment =
                                    childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
                                mapFragment.getMapAsync {
                                    googleMap = it
                                    addMarker(coord = searchResponse.coord)
                                }
                                setViews(response)
                            } else {
                                hide(spinner)
                                // empty data to show error
                                setUpErrorScreenBottomSheet(response)
                            }
                        }

                        is UIResult.Loading -> {
                            if (result.loading)
                                show(spinner)
                            else
                                hide(spinner)
                        }

                        is UIResult.Error -> {
                            hide(spinner)
                            setUpErrorScreenBottomSheet(result.error)
                        }
                    }
                }
            }
        }
    }

    private fun setViews(searchResponse: SearchResponse) {
        searchResponse.apply {
            binding.apply {
                weatherImage.loadImage(weather[0].icon.toIconUrl())
                placeName.setTextOrHide(name)
                weatherDetails.text = String.format(weather[0].main + ", " + weather[0].description)
            }
        }
    }

    // Error bottom sheet
    private fun setUpErrorScreenBottomSheet(message: String) {
        val errorLayoutBinding = ErrorLayoutBinding.inflate(layoutInflater)
        // custom style
        val bottomSheetDialog = BottomSheetDialog(
            ContextThemeWrapper(requireActivity(), R.style.BasicBottomSheetDialogTheme)
        )
        bottomSheetDialog.setContentView(errorLayoutBinding.root)
        // Canceling not allowed
        bottomSheetDialog.setCancelable(false)
        val standardBottomSheetBehavior =
            BottomSheetBehavior.from(errorLayoutBinding.root.parent as View)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Set Dragging off
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetDialog.show()

        // Apply click events
        errorLayoutBinding.apply {
            close.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            // set error text
            errorMessage.text = message
            retryButton.setOnClickListener {
                bottomSheetDialog.dismiss()
                binding.apply {
                    searchEditText.text?.clear()
                    searchEditText.requestFocus()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addMarker(coord: Coord) {
        for (locationMarker in allMarkers) {
            locationMarker?.remove()
        }
        allMarkers.clear()
        googleMap.clear()
        // Collect longitude and latitude from locations
        val latLng = LatLng(coord.lat, coord.lon)
        latLngBoundBuilder.include(latLng)
        // Put marker options
        val markerOptions = MarkerOptions().position(latLng)
        googleMap.addMarker(markerOptions)
        val locationMarker = googleMap.addMarker(markerOptions)
        allMarkers.add(locationMarker)

        googleMap.setMaxZoomPreference(16F)
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(latLngBoundBuilder.build(), padding)
        )
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                fetchLocation()
            }
        }
    }
}