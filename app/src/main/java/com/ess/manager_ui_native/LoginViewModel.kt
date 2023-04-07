package com.ess.manager_ui_native

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ess.manager_ui_native.models.LoginStatus

class LoginViewModel : ViewModel() {
    private val _loginStatus = MutableLiveData<LoginStatus>()
    val loginStatus: LiveData<LoginStatus> get() = _loginStatus

    private val _loaded = MutableLiveData<Boolean>()
    val loaded: LiveData<Boolean> get() = _loaded

    fun setLoginStatus(status: LoginStatus) {
        _loginStatus.postValue(status)
    }

    fun setLoaded(loaded: Boolean) {
        _loaded.postValue(loaded)
    }
}