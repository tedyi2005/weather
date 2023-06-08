package com.android.weather.fragments

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.weather.module.FragmentNavigationInterface
import com.android.weather.module.NavigationListener

/**
 * base class that has common reusable codes.
 */
abstract class BaseFragment : Fragment(), FragmentNavigationInterface {
    override var navigationListener: NavigationListener? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnBackPressedCallback { findNavController().popBackStack() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (requireActivity() is NavigationListener) {
            setNavigationCallbackListener(requireActivity() as NavigationListener)
        }
    }

    override fun onDetach() {
        super.onDetach()

        navigationListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()

        onBackPressedCallback?.remove()
    }

    private fun setOnBackPressedCallback(lambda: () -> Unit) {
        onBackPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            lambda()
        }
    }

    override fun setNavigationCallbackListener(listener: NavigationListener) {
        this.navigationListener = listener
    }
}