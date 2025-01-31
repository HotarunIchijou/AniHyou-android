package com.axiel7.anihyou.ui.screens.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axiel7.anihyou.common.GlobalVariables
import com.axiel7.anihyou.data.repository.DefaultPreferencesRepository
import com.axiel7.anihyou.data.repository.LoginRepository
import com.axiel7.anihyou.utils.ANIHYOU_SCHEME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val globalVariables: GlobalVariables,
    private val loginRepository: LoginRepository,
    private val defaultPreferencesRepository: DefaultPreferencesRepository,
) : ViewModel() {

    fun onIntentDataReceived(data: Uri?) = viewModelScope.launch {
        if (data?.scheme == ANIHYOU_SCHEME) {
            loginRepository.parseRedirectUri(data)
        }
    }

    private val accessToken = defaultPreferencesRepository.accessToken

    val isLoggedIn = accessToken.map { it != null }

    val startTab = defaultPreferencesRepository.lastTab

    val homeTab = defaultPreferencesRepository.defaultHomeTab

    val theme = defaultPreferencesRepository.theme

    val appColor = defaultPreferencesRepository.appColor

    val appColorMode = defaultPreferencesRepository.appColorMode

    fun saveLastTab(index: Int) = viewModelScope.launch {
        defaultPreferencesRepository.setLastTab(index)
    }

    init {
        accessToken
            .onEach {
                globalVariables.accessToken = it
            }
            .launchIn(viewModelScope)
    }
}