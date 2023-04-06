package com.ess.manager_ui_native

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ess.manager_ui_native.models.Bank
import com.ess.manager_ui_native.models.BeneficiaryData
import com.ess.manager_ui_native.models.CardSecurity

class RedeemViewModel : ViewModel() {
    private val _cardSecurity = MutableLiveData<CardSecurity>()
    val cardSecurityLiveData: LiveData<CardSecurity> get() = _cardSecurity

    private val _banks = MutableLiveData<List<Bank>>()
    val banks: LiveData<List<Bank>> get() = _banks

    private val _selectedBank = MutableLiveData<Bank>()
    val selectedBank: LiveData<Bank> get() = _selectedBank

    private val _cardValue = MutableLiveData<Int>()
    val cardValue: LiveData<Int> get() = _cardValue

    private val _feedbackText = MutableLiveData<String>()
    val feedbackText: LiveData<String> get() = _feedbackText

    private val _redeemScreen = MutableLiveData<RedeemScreen>()
    val redeemScreen: LiveData<RedeemScreen> get() = _redeemScreen

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    fun setCardSecurity(cardSecurity: CardSecurity) {
        _cardSecurity.postValue(cardSecurity)
    }

    fun setBanks(banks: List<Bank>) {
        _banks.postValue(banks)
    }

    fun setSelectedBank(bank: Bank) {
        _selectedBank.postValue(bank)
    }

    fun setCardValue(cardValue: Int) {
        _cardValue.postValue(cardValue)
    }

    fun setScreen(redeemScreen: RedeemScreen){
        _redeemScreen.postValue(redeemScreen)
    }

    fun showFeedback(feedbackText: String) {
        _feedbackText.postValue(feedbackText)
    }

    fun logout() {
        _isLoggedIn.postValue(false)
    }
}