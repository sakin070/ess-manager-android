package com.ess.manager_ui_native

import com.ess.manager_ui_native.models.CardSecurity
import com.ess.manager_ui_native.models.CardStatus

data class LotteryCard(
    val a1: String,
    val a2: String,
    val a3: String,
    val a4: String,
    val a5: String,
    val serialNumber: String,
    val securityCode: String,
    val value: Int,
    val status: CardStatus
) {
    fun getCardSecurity() = CardSecurity(a1, a2, a3, a4, a5, serialNumber, securityCode)
}