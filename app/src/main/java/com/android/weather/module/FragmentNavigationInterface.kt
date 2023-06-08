package com.android.weather.module


interface FragmentNavigationInterface {
    var navigationListener: NavigationListener?

    fun setNavigationCallbackListener(listener: NavigationListener)
}