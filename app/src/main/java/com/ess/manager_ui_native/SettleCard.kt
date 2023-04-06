package com.ess.manager_ui_native

import com.ess.manager_ui_native.models.BeneficiaryData
import com.ess.manager_ui_native.models.CardSecurity

data class SettleCard(
    val cardSecurity: CardSecurity,
    val beneficiaryData: BeneficiaryData,
)
